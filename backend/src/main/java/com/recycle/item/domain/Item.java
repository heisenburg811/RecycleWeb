package com.recycle.item.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品（二手家具）。
 *
 * <p>本期一物一价，不做库存与数量（PRD 5.4 R1）。
 * <p>{@code weightKg} / {@code carbonSavedKg} 为二期碳核算预留字段，先落库不算。
 */
@Data
@TableName("item")
public class Item {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sellerId;

    /** 叶子类目 ID（三级或二级） */
    private Long categoryId;

    private String title;

    private String description;

    /** 成色 1-5，见 ItemCondition */
    private Integer itemCondition;

    private BigDecimal price;

    private Integer lengthCm;

    private Integer widthCm;

    private Integer heightCm;

    /** 实木 / 板式人造板 / 金属 / 皮质 / 布艺 ... */
    private String material;

    private String style;

    private String brand;

    /** 1 仅自提 / 2 仅配送 / 3 皆可 */
    private Integer tradeType;

    private String districtCode;

    /** 详细地址：不在商品页公开，下单后对买家可见 */
    private String address;

    /** 首图（列表封面），冗余字段，来源 item_image 的第一张 */
    private String coverUrl;

    /** 见 ItemStatus */
    private String status;

    private BigDecimal weightKg;

    private BigDecimal carbonSavedKg;

    /** 是否来自回收（二期 C2B2C 链路） */
    private Integer recycleFlag;

    private Integer viewCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
