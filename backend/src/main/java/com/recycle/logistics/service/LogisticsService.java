package com.recycle.logistics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.recycle.common.exception.BizException;
import com.recycle.logistics.domain.LogisticsSlot;
import com.recycle.logistics.domain.OrderLogistics;
import com.recycle.logistics.mapper.OrderLogisticsMapper;
import com.recycle.logistics.vo.LogisticsVO;
import com.recycle.order.domain.Order;
import com.recycle.order.enums.OrderStatus;
import com.recycle.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 配送预约与履约推进。
 *
 * <p>状态：ACCEPTED（已派单）→ DELIVERING（到点自动出发）→ 买家确认收货。
 * 运力目前是模拟派单（provider = mock），真实接入时替换为第三方下单 + 回调。
 */
@Service
public class LogisticsService {

    private static final Logger log = LoggerFactory.getLogger(LogisticsService.class);
    private static final String[] DRIVERS = {"王师傅", "李师傅", "张师傅", "赵师傅"};
    /** 免费改约次数 */
    private static final int FREE_RESCHEDULE = 1;

    private final OrderLogisticsMapper logisticsMapper;
    private final SlotService slotService;
    private final OrderService orderService;

    public LogisticsService(OrderLogisticsMapper logisticsMapper,
                            SlotService slotService,
                            OrderService orderService) {
        this.logisticsMapper = logisticsMapper;
        this.slotService = slotService;
        this.orderService = orderService;
    }

    /**
     * 预约配送：占时段名额 → 建物流单 → 订单进入已预约。
     */
    @Transactional(rollbackFor = Exception.class)
    public LogisticsVO book(String orderNo, Long userId, LocalDate date, String slotCode) {
        Order order = orderService.requireBuyerOrder(orderNo, userId);

        if (!Integer.valueOf(2).equals(order.getDeliveryType())) {
            throw new BizException("自提订单无需预约配送");
        }
        if (!OrderStatus.PAID.name().equals(order.getStatus())) {
            throw new BizException("当前订单状态不可预约配送");
        }
        if (getByOrderNo(orderNo) != null) {
            throw new BizException("该订单已预约过配送");
        }

        LogisticsSlot slot = requireAvailableSlot(date, slotCode);
        if (!slotService.lock(slot.getId())) {
            throw new BizException("该时段已约满，请换一个时段");
        }

        OrderLogistics record = new OrderLogistics();
        record.setOrderNo(orderNo);
        record.setProvider("mock");
        record.setProviderOrderNo("MOCK" + System.currentTimeMillis());
        record.setScheduleDate(date);
        record.setScheduleSlot(slotCode);
        record.setDriverName(DRIVERS[ThreadLocalRandom.current().nextInt(DRIVERS.length)]);
        record.setDriverPhone("138****" + String.format("%04d", ThreadLocalRandom.current().nextInt(10000)));
        record.setStatus("ACCEPTED");
        record.setRescheduleCount(0);
        logisticsMapper.insert(record);

        if (!orderService.markScheduled(orderNo)) {
            throw new BizException("订单状态已变化，请刷新后重试");
        }

        log.info("订单 {} 预约配送 {} {}", orderNo, date, slotCode);
        return toVO(record, true);
    }

    /**
     * 改约：释放旧名额 → 占新名额。
     *
     * <p>配送当日不可改；免费改约 1 次，超出后提示联系客服（二次收费待接支付后实现）。
     */
    @Transactional(rollbackFor = Exception.class)
    public LogisticsVO reschedule(String orderNo, Long userId, LocalDate date, String slotCode) {
        Order order = orderService.requireBuyerOrder(orderNo, userId);
        if (!OrderStatus.SCHEDULED.name().equals(order.getStatus())) {
            throw new BizException("当前订单状态不可改约");
        }

        OrderLogistics record = getByOrderNo(orderNo);
        if (record == null) {
            throw new BizException("该订单还没有预约配送");
        }
        if (record.getScheduleDate().equals(LocalDate.now())) {
            throw new BizException("配送当日不可改约");
        }
        int used = record.getRescheduleCount() == null ? 0 : record.getRescheduleCount();
        if (used >= FREE_RESCHEDULE) {
            throw new BizException("已用完免费改约次数，请联系客服");
        }
        if (record.getScheduleDate().equals(date) && record.getScheduleSlot().equals(slotCode)) {
            throw new BizException("新时段与当前预约相同");
        }

        LogisticsSlot target = requireAvailableSlot(date, slotCode);
        if (!slotService.lock(target.getId())) {
            throw new BizException("该时段已约满，请换一个时段");
        }
        LogisticsSlot old = slotService.get(record.getScheduleDate(), record.getScheduleSlot());
        if (old != null) {
            slotService.release(old.getId());
        }

        record.setScheduleDate(date);
        record.setScheduleSlot(slotCode);
        record.setRescheduleCount(used + 1);
        logisticsMapper.updateById(record);

        log.info("订单 {} 改约到 {} {}", orderNo, date, slotCode);
        return toVO(record, used + 1 < FREE_RESCHEDULE);
    }

    /**
     * 到点自动出发：预约日期当天、时段已开始且仍为 ACCEPTED 的物流单推成配送中。
     *
     * @return 推进数量
     */
    public int advanceDue() {
        List<OrderLogistics> list = logisticsMapper.selectList(
                new LambdaQueryWrapper<OrderLogistics>().eq(OrderLogistics::getStatus, "ACCEPTED"));
        LocalDateTime now = LocalDateTime.now();
        int advanced = 0;

        for (OrderLogistics record : list) {
            if (!hasStarted(record, now)) {
                continue;
            }
            if (!orderService.markDelivering(record.getOrderNo())) {
                continue;
            }
            logisticsMapper.update(null, new LambdaUpdateWrapper<OrderLogistics>()
                    .eq(OrderLogistics::getId, record.getId())
                    .eq(OrderLogistics::getStatus, "ACCEPTED")
                    .set(OrderLogistics::getStatus, "DELIVERING"));
            advanced++;
            log.info("订单 {} 进入配送中", record.getOrderNo());
        }
        return advanced;
    }

    public LogisticsVO info(String orderNo, Long userId) {
        orderService.requireBuyerOrder(orderNo, userId);
        OrderLogistics record = getByOrderNo(orderNo);
        if (record == null) {
            return null;
        }
        int used = record.getRescheduleCount() == null ? 0 : record.getRescheduleCount();
        boolean canReschedule = used < FREE_RESCHEDULE
                && OrderStatus.SCHEDULED.name().equals(
                        orderService.requireBuyerOrder(orderNo, userId).getStatus())
                && !record.getScheduleDate().equals(LocalDate.now());
        return toVO(record, canReschedule);
    }

    // ------------------------------------------------------------------ 内部

    private OrderLogistics getByOrderNo(String orderNo) {
        return logisticsMapper.selectOne(
                new LambdaQueryWrapper<OrderLogistics>().eq(OrderLogistics::getOrderNo, orderNo));
    }

    private LogisticsSlot requireAvailableSlot(LocalDate date, String slotCode) {
        slotService.checkDateRange(date);
        int startHour = slotService.startHourOf(slotCode);

        LogisticsSlot slot = slotService.get(date, slotCode);
        if (slot == null) {
            // 该日尚未生成时段，查询一次触发初始化
            slotService.list(date);
            slot = slotService.get(date, slotCode);
        }
        if (slot == null) {
            throw new BizException("时段不存在");
        }
        if (date.equals(LocalDate.now()) && LocalDateTime.now().getHour() >= startHour + 3) {
            throw new BizException("该时段已结束，请选择更晚的时段");
        }
        if (slot.getBooked() >= slot.getCapacity()) {
            throw new BizException("该时段已约满，请换一个时段");
        }
        return slot;
    }

    private boolean hasStarted(OrderLogistics record, LocalDateTime now) {
        if (record.getScheduleDate().isBefore(now.toLocalDate())) {
            return true;
        }
        if (record.getScheduleDate().isAfter(now.toLocalDate())) {
            return false;
        }
        int startHour = slotService.startHourOf(record.getScheduleSlot());
        return now.getHour() >= startHour;
    }

    private LogisticsVO toVO(OrderLogistics record, boolean canReschedule) {
        return new LogisticsVO(record.getOrderNo(), record.getScheduleDate(), record.getScheduleSlot(),
                record.getProvider(), record.getDriverName(), record.getDriverPhone(),
                record.getStatus(), record.getRescheduleCount(), canReschedule);
    }
}
