package com.recycle.logistics.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/**
 * 配送时段容量。booked 等于 capacity 时该时段约满。
 */
@Data
@TableName("logistics_slot")
public class LogisticsSlot {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDate slotDate;

    /** 09-12 / 12-15 / 15-18 / 18-21 */
    private String slotCode;

    private Integer startHour;

    private Integer capacity;

    private Integer booked;
}
