package com.recycle.item.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品详情（PRD 6.6）。
 */
public record ItemDetailVO(
        Long id,
        Long sellerId,
        Long categoryId,
        String title,
        String description,
        BigDecimal price,
        Integer itemCondition,
        String conditionLabel,
        Integer lengthCm,
        Integer widthCm,
        Integer heightCm,
        String material,
        String style,
        String brand,
        Integer tradeType,
        String districtCode,
        String status,
        List<String> images,
        Integer viewCount,
        LocalDateTime createdAt
) {
}
