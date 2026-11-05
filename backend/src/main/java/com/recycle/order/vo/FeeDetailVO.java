package com.recycle.order.vo;

import java.math.BigDecimal;

/**
 * 费用明细（PRD 7.1）。订单页必须逐项展示，禁止隐藏。
 */
public record FeeDetailVO(
        Long itemId,
        BigDecimal itemPrice,
        BigDecimal deliveryFee,
        BigDecimal floorFee,
        BigDecimal assemblyFee,
        BigDecimal serviceFee,
        BigDecimal totalAmount,
        String note
) {
}
