package com.recycle.order.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单。
 *
 * <p>金额与商品信息都是下单时的<b>快照</b>：商品后续改价、编辑都不影响已成交订单。
 */
@Data
@TableName("orders")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long itemId;

    private Long buyerId;

    private Long sellerId;

    /** 商品标题快照 */
    private String itemTitle;

    /** 商品首图快照 */
    private String itemCover;

    private BigDecimal itemPrice;

    private BigDecimal deliveryFee;

    private BigDecimal floorFee;

    private BigDecimal assemblyFee;

    private BigDecimal serviceFee;

    private BigDecimal totalAmount;

    /** 1 自提 / 2 配送 */
    private Integer deliveryType;

    private String addressSnapshot;

    /** 见 OrderStatus */
    private String status;

    private LocalDateTime payExpireAt;

    private LocalDateTime paidAt;

    private LocalDateTime shippedAt;

    private LocalDateTime completedAt;

    private String remark;

    private LocalDateTime createdAt;
}
