package com.recycle.item.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * 发布商品请求（PRD 5.3 字段清单）。
 */
public class ItemPublishReq {

    @NotEmpty(message = "至少上传 1 张图片")
    @Size(max = 9, message = "最多上传 9 张图片")
    private List<String> images;

    @NotNull(message = "请选择品类")
    private Long categoryId;

    @NotBlank(message = "请填写标题")
    @Size(min = 5, max = 30, message = "标题需 5-30 个字")
    private String title;

    @NotNull(message = "请选择成色")
    @Min(value = 1, message = "成色取值 1-5")
    @Max(value = 5, message = "成色取值 1-5")
    private Integer itemCondition;

    @Size(max = 500, message = "描述最多 500 字")
    private String description;

    @NotNull(message = "请填写价格")
    @DecimalMin(value = "1", message = "价格不能低于 1 元")
    @DecimalMax(value = "99999", message = "价格不能高于 99999 元")
    private BigDecimal price;

    @Min(value = 1, message = "长度不合法")
    @Max(value = 500, message = "长度不合法")
    private Integer lengthCm;

    @Min(value = 1, message = "宽度不合法")
    @Max(value = 500, message = "宽度不合法")
    private Integer widthCm;

    @Min(value = 1, message = "高度不合法")
    @Max(value = 500, message = "高度不合法")
    private Integer heightCm;

    private String material;

    private String style;

    @Size(max = 20, message = "品牌名最多 20 字")
    private String brand;

    /** 1 仅自提 / 2 仅配送 / 3 皆可 */
    @NotNull(message = "请选择交易方式")
    @Min(1)
    @Max(3)
    private Integer tradeType;

    @NotBlank(message = "请选择所在区域")
    private String districtCode;

    @Size(max = 100, message = "详细地址最多 100 字")
    private String address;

    /** 是否同时申请回收（PRD D1 方案 B） */
    private Boolean applyRecycle;

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getItemCondition() {
        return itemCondition;
    }

    public void setItemCondition(Integer itemCondition) {
        this.itemCondition = itemCondition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getLengthCm() {
        return lengthCm;
    }

    public void setLengthCm(Integer lengthCm) {
        this.lengthCm = lengthCm;
    }

    public Integer getWidthCm() {
        return widthCm;
    }

    public void setWidthCm(Integer widthCm) {
        this.widthCm = widthCm;
    }

    public Integer getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(Integer heightCm) {
        this.heightCm = heightCm;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Integer getTradeType() {
        return tradeType;
    }

    public void setTradeType(Integer tradeType) {
        this.tradeType = tradeType;
    }

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getApplyRecycle() {
        return applyRecycle;
    }

    public void setApplyRecycle(Boolean applyRecycle) {
        this.applyRecycle = applyRecycle;
    }
}
