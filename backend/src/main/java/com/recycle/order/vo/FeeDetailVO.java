package com.recycle.order.vo;

import java.math.BigDecimal;

/**
 * 费用明细（PRD 7.1）。
 *
 * <p>资金流向：
 * <ul>
 *   <li>买家实付 {@code totalAmount} = 商品价 + 运费 + 楼层费 + 拆装费</li>
 *   <li>运力方拿 运费 + 楼层费 + 拆装费</li>
 *   <li>卖家实收 {@code sellerIncome} = 商品价 − 平台服务费</li>
 *   <li>平台收 {@code serviceFee}（商品价 × 5%，从卖家所得中扣除）</li>
 * </ul>
 * 服务费只收一次，由卖家承担；买家实付里不含服务费。
 */
public record FeeDetailVO(
        Long itemId,
        BigDecimal itemPrice,
        BigDecimal deliveryFee,
        BigDecimal floorFee,
        BigDecimal assemblyFee,
        BigDecimal serviceFee,
        BigDecimal totalAmount,
        BigDecimal sellerIncome,
        String note
) {
}
