package com.recycle.logistics.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单的物流预约记录，一单一物流。
 */
@Data
@TableName("order_logistics")
public class OrderLogistics {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    /** 运力方：mock 表示模拟派单 */
    private String provider;

    private String providerOrderNo;

    private LocalDate scheduleDate;

    private String scheduleSlot;

    private String driverName;

    private String driverPhone;

    /** ACCEPTED 已派单 / DELIVERING 配送中 / ARRIVED 已送达 / CANCELLED */
    private String status;

    private String signPhoto;

    private Integer rescheduleCount;

    private LocalDateTime createdAt;
}
