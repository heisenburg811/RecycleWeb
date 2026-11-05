package com.recycle.order.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单展示对象（列表与详情共用）。
 */
public record OrderVO(
        Long id,
        String orderNo,
        Long itemId,
        String itemTitle,
        String itemCover,
        Long buyerId,
        Long sellerId,
        BigDecimal itemPrice,
        BigDecimal deliveryFee,
        BigDecimal floorFee,
        BigDecimal assemblyFee,
        BigDecimal serviceFee,
        BigDecimal totalAmount,
        /** 卖家实收 = 商品价 − 平台服务费（运费等归运力方，不经过卖家） */
        BigDecimal sellerIncome,
        Integer deliveryType,
        String addressSnapshot,
        String status,
        String statusLabel,
        LocalDateTime payExpireAt,
        LocalDateTime paidAt,
        LocalDateTime shippedAt,
        LocalDateTime completedAt,
        String remark,
        LocalDateTime createdAt
) {
}
