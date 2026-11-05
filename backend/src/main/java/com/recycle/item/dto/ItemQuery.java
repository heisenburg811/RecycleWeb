package com.recycle.item.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品列表查询条件（PRD 6.4 筛选维度）。
 */
public class ItemQuery {

    private String keyword;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    /** 成色多选，如 conditions=3&conditions=4 */
    private List<Integer> conditions;
    /** 1 自提 / 2 配送 */
    private Integer tradeType;
    private String material;
    private String style;
    private String districtCode;
    /** composite | latest | price_asc | price_desc */
    private String sort = "composite";
    private Integer page = 1;
    private Integer size = 20;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public List<Integer> getConditions() {
        return conditions;
    }

    public void setConditions(List<Integer> conditions) {
        this.conditions = conditions;
    }

    public Integer getTradeType() {
        return tradeType;
    }

    public void setTradeType(Integer tradeType) {
        this.tradeType = tradeType;
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

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort == null ? "composite" : sort;
    }

    public Integer getPage() {
        return page == null || page < 1 ? 1 : page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        if (size == null || size < 1) {
            return 20;
        }
        return Math.min(size, 50);
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
