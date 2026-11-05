package com.recycle.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recycle.common.api.PageResult;
import com.recycle.common.exception.BizException;
import com.recycle.item.domain.Item;
import com.recycle.item.enums.ItemStatus;
import com.recycle.item.service.ItemService;
import com.recycle.order.domain.Order;
import com.recycle.order.dto.CreateOrderReq;
import com.recycle.order.dto.OrderQuoteReq;
import com.recycle.order.enums.OrderStatus;
import com.recycle.order.mapper.OrderMapper;
import com.recycle.order.vo.FeeDetailVO;
import com.recycle.order.vo.OrderVO;
import com.recycle.payment.PaymentGateway;
import com.recycle.payment.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单领域服务。
 *
 * <p>核心不变式：
 * <ul>
 *   <li>一件商品同时只能有一个有效订单——靠 {@link ItemService#reserve} 的条件更新保证；</li>
 *   <li>所有状态流转都用「带状态条件的 UPDATE」，靠受影响行数判断是否抢到，避免并发重复推进；</li>
 *   <li>订单里的金额与商品信息是下单时快照，之后商品改价不影响已成交订单。</li>
 * </ul>
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderMapper orderMapper;
    private final ItemService itemService;
    private final FeeCalculator feeCalculator;
    private final PaymentGateway paymentGateway;

    /** 待支付超时时间（PRD 7.4），可配置便于联调时调小 */
    private final int payTimeoutMinutes;

    public OrderService(OrderMapper orderMapper,
                        ItemService itemService,
                        FeeCalculator feeCalculator,
                        PaymentGateway paymentGateway,
                        @Value("${app.order.pay-timeout-minutes:30}") int payTimeoutMinutes) {
        this.orderMapper = orderMapper;
        this.itemService = itemService;
        this.feeCalculator = feeCalculator;
        this.paymentGateway = paymentGateway;
        this.payTimeoutMinutes = payTimeoutMinutes;
    }

    // ------------------------------------------------------------------ 下单

    @Transactional(rollbackFor = Exception.class)
    public OrderVO create(CreateOrderReq req, Long buyerId) {
        Item item = itemService.getById(req.itemId());
        if (item == null) {
            throw new BizException("商品不存在或已下架");
        }
        if (!ItemStatus.ON_SALE.name().equals(item.getStatus())) {
            throw new BizException("该商品已被预订或已售出");
        }
        if (item.getSellerId().equals(buyerId)) {
            throw new BizException("不能购买自己发布的商品");
        }
        checkTradeType(item, req.deliveryType());

        // 条件更新抢库存：返回 false 说明被人抢先
        if (!itemService.reserve(item.getId())) {
            throw new BizException("手慢了，商品刚被别人买走");
        }

        FeeDetailVO fee = calcFee(item, req);

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setItemId(item.getId());
        order.setBuyerId(buyerId);
        order.setSellerId(item.getSellerId());
        order.setItemTitle(item.getTitle());
        order.setItemCover(item.getCoverUrl());
        order.setItemPrice(fee.itemPrice());
        order.setDeliveryFee(fee.deliveryFee());
        order.setFloorFee(fee.floorFee());
        order.setAssemblyFee(fee.assemblyFee());
        order.setServiceFee(fee.serviceFee());
        order.setTotalAmount(fee.totalAmount());
        order.setDeliveryType(req.deliveryType());
        order.setAddressSnapshot(req.address());
        order.setStatus(OrderStatus.PENDING_PAYMENT.name());
        order.setPayExpireAt(LocalDateTime.now().plusMinutes(payTimeoutMinutes));
        order.setRemark(req.remark());
        orderMapper.insert(order);

        log.info("创建订单 {} 商品 {} 买家 {} 金额 {}", order.getOrderNo(), item.getId(), buyerId, order.getTotalAmount());
        return toVO(order);
    }

    // ------------------------------------------------------------------ 支付

    public OrderVO pay(String orderNo, Long buyerId, String channel) {
        Order order = requireOrder(orderNo);
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BizException("无权操作该订单");
        }
        if (!OrderStatus.PENDING_PAYMENT.name().equals(order.getStatus())) {
            throw new BizException("订单当前状态不可支付");
        }
        if (order.getPayExpireAt() != null && order.getPayExpireAt().isBefore(LocalDateTime.now())) {
            closeExpiredOrder(order);
            throw new BizException("订单已超时关闭，请重新下单");
        }

        PaymentResult result = paymentGateway.pay(order, StringUtils.hasText(channel) ? channel : "mock");
        if (!result.success()) {
            throw new BizException(result.message());
        }

        LocalDateTime now = LocalDateTime.now();
        int rows = orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, order.getId())
                .eq(Order::getStatus, OrderStatus.PENDING_PAYMENT.name())
                .set(Order::getStatus, OrderStatus.PAID.name())
                .set(Order::getPaidAt, now));
        if (rows == 0) {
            throw new BizException("订单状态已变化，请刷新后重试");
        }
        order.setStatus(OrderStatus.PAID.name());
        order.setPaidAt(now);
        return toVO(order);
    }

    // ------------------------------------------------------------------ 履约

    /** 卖家发货。接入物流后改由运力接单回调驱动。 */
    public OrderVO ship(String orderNo, Long sellerId) {
        Order order = requireOrder(orderNo);
        if (!order.getSellerId().equals(sellerId)) {
            throw new BizException("无权操作该订单");
        }
        LocalDateTime now = LocalDateTime.now();
        int rows = orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, order.getId())
                .eq(Order::getStatus, OrderStatus.PAID.name())
                .set(Order::getStatus, OrderStatus.SHIPPED.name())
                .set(Order::getShippedAt, now));
        if (rows == 0) {
            throw new BizException("订单当前状态不可发货");
        }
        order.setStatus(OrderStatus.SHIPPED.name());
        order.setShippedAt(now);
        return toVO(order);
    }

    /** 买家确认收货 → 订单完成、商品标记已售出。 */
    @Transactional(rollbackFor = Exception.class)
    public OrderVO confirm(String orderNo, Long buyerId) {
        Order order = requireOrder(orderNo);
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BizException("无权操作该订单");
        }
        LocalDateTime now = LocalDateTime.now();
        int rows = orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, order.getId())
                .eq(Order::getStatus, OrderStatus.SHIPPED.name())
                .set(Order::getStatus, OrderStatus.COMPLETED.name())
                .set(Order::getCompletedAt, now));
        if (rows == 0) {
            throw new BizException("订单当前状态不可确认收货");
        }
        itemService.markSold(order.getItemId());
        order.setStatus(OrderStatus.COMPLETED.name());
        order.setCompletedAt(now);
        return toVO(order);
    }

    /** 买家取消未支付订单，释放商品。 */
    @Transactional(rollbackFor = Exception.class)
    public OrderVO cancel(String orderNo, Long buyerId) {
        Order order = requireOrder(orderNo);
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BizException("无权操作该订单");
        }
        if (OrderStatus.PAID.name().equals(order.getStatus())) {
            throw new BizException("已支付订单暂不支持在线取消，请联系客服");
        }
        int rows = orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, order.getId())
                .eq(Order::getStatus, OrderStatus.PENDING_PAYMENT.name())
                .set(Order::getStatus, OrderStatus.CANCELLED.name()));
        if (rows == 0) {
            throw new BizException("订单当前状态不可取消");
        }
        itemService.release(order.getItemId());
        order.setStatus(OrderStatus.CANCELLED.name());
        return toVO(order);
    }

    // ------------------------------------------------------------------ 查询

    public PageResult<OrderVO> page(String role, String status, int page, int size, Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if ("seller".equals(role)) {
            wrapper.eq(Order::getSellerId, userId);
        } else {
            wrapper.eq(Order::getBuyerId, userId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreatedAt);

        Page<Order> result = orderMapper.selectPage(new Page<>(page, size), wrapper);
        List<OrderVO> list = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(list, result.getTotal(), result.getCurrent(), result.getSize());
    }

    public OrderVO detail(String orderNo, Long userId) {
        Order order = requireOrder(orderNo);
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BizException("无权查看该订单");
        }
        return toVO(order);
    }

    // ------------------------------------------------------------------ 到期关单

    /**
     * 关闭超时未支付的订单并释放商品，由定时任务调用。
     *
     * @return 本次关闭数量
     */
    public int closeExpiredOrders() {
        List<Order> expired = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, OrderStatus.PENDING_PAYMENT.name())
                .lt(Order::getPayExpireAt, LocalDateTime.now())
                .last("limit 200"));
        int closed = 0;
        for (Order order : expired) {
            if (closeExpiredOrder(order)) {
                closed++;
            }
        }
        return closed;
    }

    private boolean closeExpiredOrder(Order order) {
        int rows = orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, order.getId())
                .eq(Order::getStatus, OrderStatus.PENDING_PAYMENT.name())
                .set(Order::getStatus, OrderStatus.CLOSED.name()));
        if (rows == 0) {
            return false;
        }
        itemService.release(order.getItemId());
        log.info("订单 {} 超时关闭，商品 {} 已释放", order.getOrderNo(), order.getItemId());
        return true;
    }

    // ------------------------------------------------------------------ 内部

    private void checkTradeType(Item item, Integer deliveryType) {
        Integer tradeType = item.getTradeType();
        if (tradeType == null) {
            return;
        }
        if (deliveryType == 2 && tradeType == 1) {
            throw new BizException("该商品仅支持同城自提");
        }
        if (deliveryType == 1 && tradeType == 2) {
            throw new BizException("该商品仅支持平台配送");
        }
    }

    private FeeDetailVO calcFee(Item item, CreateOrderReq req) {
        OrderQuoteReq quote = new OrderQuoteReq();
        quote.setItemId(item.getId());
        quote.setDeliveryType(req.deliveryType());
        quote.setDistanceKm(req.distanceKm());
        quote.setFloor(req.floor());
        quote.setHasElevator(req.hasElevator());
        quote.setAssemblyType(req.assemblyType());
        if (item.getLengthCm() != null) {
            quote.setVolumeM3(estimateVolume(item));
        }
        return feeCalculator.compute(item.getPrice(), quote);
    }

    private BigDecimal estimateVolume(Item item) {
        BigDecimal l = BigDecimal.valueOf(item.getLengthCm());
        BigDecimal w = BigDecimal.valueOf(item.getWidthCm() == null ? 60 : item.getWidthCm());
        BigDecimal h = BigDecimal.valueOf(item.getHeightCm() == null ? 60 : item.getHeightCm());
        return l.multiply(w).multiply(h)
                .divide(new BigDecimal("1000000"), 3, RoundingMode.HALF_UP);
    }

    private Order requireOrder(String orderNo) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) {
            throw new BizException("订单不存在");
        }
        return order;
    }

    private String generateOrderNo() {
        return LocalDateTime.now().format(ORDER_NO_FMT)
                + String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }

    private OrderVO toVO(Order o) {
        return new OrderVO(o.getId(), o.getOrderNo(), o.getItemId(), o.getItemTitle(), o.getItemCover(),
                o.getBuyerId(), o.getSellerId(),
                o.getItemPrice(), o.getDeliveryFee(), o.getFloorFee(), o.getAssemblyFee(), o.getServiceFee(),
                o.getTotalAmount(), o.getDeliveryType(), o.getAddressSnapshot(),
                o.getStatus(), OrderStatus.labelOf(o.getStatus()),
                o.getPayExpireAt(), o.getPaidAt(), o.getShippedAt(), o.getCompletedAt(),
                o.getRemark(), o.getCreatedAt());
    }

    /**
     * 下单前费用试算（无副作用）。
     */
    public FeeDetailVO quote(OrderQuoteReq req) {
        Item item = itemService.getById(req.getItemId());
        if (item == null) {
            throw new BizException("商品不存在或已下架");
        }
        if (req.getVolumeM3() == null && item.getLengthCm() != null) {
            req.setVolumeM3(estimateVolume(item));
        }
        return feeCalculator.compute(item.getPrice(), req);
    }
}
