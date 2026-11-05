package com.recycle.user.vo;

/**
 * 登录 / 注册 / 刷新 成功后的返回。
 *
 * <p>accessToken 短（30 分钟）随每个请求带；refreshToken 长（7 天）
 * 只用于换新 accessToken，且一次性使用（轮换）。
 */
public record LoginVO(
        String accessToken,
        String refreshToken,
        long accessExpiresIn,
        long refreshExpiresIn,
        UserVO user
) {
}
