package com.recycle.item.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 列表卡片（PRD 6.5）。「我的发布」复用同一结构，多带一个 status。
 */
public record ItemCardVO(
        Long id,
        String title,
        BigDecimal price,
        String coverUrl,
        Integer itemCondition,
        String conditionLabel,
        String districtCode,
        Integer tradeType,
        Integer viewCount,
        LocalDateTime createdAt,
        String status
) {
}
