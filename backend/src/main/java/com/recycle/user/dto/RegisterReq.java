package com.recycle.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterReq(
        @NotBlank(message = "请输入手机号")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone,

        @NotBlank(message = "请输入密码")
        @Size(min = 6, max = 32, message = "密码长度需 6-32 位")
        String password,

        @Size(max = 20, message = "昵称最多 20 字")
        String nickname
) {
}
