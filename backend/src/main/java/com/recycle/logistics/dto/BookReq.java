package com.recycle.logistics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * 预约 / 改约配送时段。
 */
public record BookReq(
        @NotNull(message = "请选择配送日期") LocalDate date,
        @NotBlank(message = "请选择配送时段") String slotCode
) {
}
