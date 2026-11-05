package com.recycle.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 下单请求：商品 + 配送方式 + 配送参数。
 */
public record CreateOrderReq(
        @NotNull(message = "商品不能为空") Long itemId,

        /** 1 同城自提 / 2 平台配送 */
        @NotNull(message = "请选择配送方式") @Min(1) Integer deliveryType,

        @DecimalMin(value = "0", message = "距离不合法") BigDecimal distanceKm,

        @Min(value = 1, message = "楼层不合法") Integer floor,

        Boolean hasElevator,

        /** bed / wardrobe / sofa / none */
        String assemblyType,

        @Size(max = 200, message = "收货地址最多 200 字") String address,

        @Size(max = 200, message = "备注最多 200 字") String remark
) {
}
