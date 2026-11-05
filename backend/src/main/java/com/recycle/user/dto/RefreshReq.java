package com.recycle.user.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshReq(
        @NotBlank(message = "缺少刷新令牌") String refreshToken
) {
}
