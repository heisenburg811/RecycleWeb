package com.recycle.logistics.vo;

import java.time.LocalDate;

/**
 * 订单的物流信息。
 */
public record LogisticsVO(
        String orderNo,
        LocalDate scheduleDate,
        String scheduleSlot,
        String provider,
        String driverName,
        String driverPhone,
        String status,
        Integer rescheduleCount,
        boolean canReschedule
) {
}
