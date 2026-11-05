package com.recycle.order.service;

import com.recycle.common.exception.BizException;
import com.recycle.item.domain.Item;
import com.recycle.item.service.ItemService;
import com.recycle.order.dto.OrderQuoteReq;
import com.recycle.order.vo.FeeDetailVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 订单领域服务。
 *
 * <p>当前只实现「下单前费用试算」这条纯逻辑链路（无副作用、无支付）。
 * 完整下单（锁库存 → 建单 → 支付 → 预约物流）在下一迭代补齐，
 * 预留入口见 {@link ItemService#reserve(Long)}。
 */
@Service
public class OrderService {

    private final ItemService itemService;
    private final FeeCalculator feeCalculator;

    public OrderService(ItemService itemService, FeeCalculator feeCalculator) {
        this.itemService = itemService;
        this.feeCalculator = feeCalculator;
    }

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

    private BigDecimal estimateVolume(Item item) {
        BigDecimal l = BigDecimal.valueOf(item.getLengthCm());
        BigDecimal w = BigDecimal.valueOf(item.getWidthCm() == null ? 60 : item.getWidthCm());
        BigDecimal h = BigDecimal.valueOf(item.getHeightCm() == null ? 60 : item.getHeightCm());
        // cm³ → m³
        return l.multiply(w).multiply(h)
                .divide(new BigDecimal("1000000"), 3, RoundingMode.HALF_UP);
    }
}
