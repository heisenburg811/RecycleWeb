package com.recycle.user.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginReq(
        @NotBlank(message = "请输入手机号") String phone,
        @NotBlank(message = "请输入密码") String password
) {
}
