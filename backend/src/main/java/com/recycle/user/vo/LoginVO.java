package com.recycle.user.vo;

/**
 * 登录 / 注册成功后的返回。
 */
public record LoginVO(String token, long expiresIn, UserVO user) {
}
