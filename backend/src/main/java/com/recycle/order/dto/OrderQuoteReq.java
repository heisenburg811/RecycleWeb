package com.recycle.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 运费试算请求（PRD 7.1 / 7.3）。
 */
public class OrderQuoteReq {

    @NotNull(message = "商品不能为空")
    private Long itemId;

    /** 1 同城自提 / 2 平台配送 */
    @NotNull(message = "请选择配送方式")
    @Min(1)
    private Integer deliveryType;

    /** 配送距离（公里），自提可为空 */
    @DecimalMin(value = "0", message = "距离不合法")
    private BigDecimal distanceKm;

    /** 楼层，1 表示一楼 */
    @Min(value = 1, message = "楼层不合法")
    private Integer floor;

    private Boolean hasElevator;

    /** 体积（m³），为空时按商品尺寸估算 */
    @DecimalMin(value = "0", message = "体积不合法")
    private BigDecimal volumeM3;

    /** bed / wardrobe / sofa / none */
    private String assemblyType;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Integer getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(Integer deliveryType) {
        this.deliveryType = deliveryType;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(BigDecimal distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public Boolean getHasElevator() {
        return hasElevator;
    }

    public void setHasElevator(Boolean hasElevator) {
        this.hasElevator = hasElevator;
    }

    public BigDecimal getVolumeM3() {
        return volumeM3;
    }

    public void setVolumeM3(BigDecimal volumeM3) {
        this.volumeM3 = volumeM3;
    }

    public String getAssemblyType() {
        return assemblyType;
    }

    public void setAssemblyType(String assemblyType) {
        this.assemblyType = assemblyType;
    }
}
