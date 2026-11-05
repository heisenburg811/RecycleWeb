package com.recycle.logistics.vo;

/**
 * 可预约时段。
 */
public record SlotVO(
        String code,
        String label,
        int capacity,
        int booked,
        int remaining,
        boolean available
) {
}
