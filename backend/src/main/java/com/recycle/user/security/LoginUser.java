package com.recycle.user.security;

/**
 * 认证主体：放进 SecurityContext 的当前登录用户。
 *
 * <p>只带 token 里已有的字段，不做每请求查库——需要完整信息时调 AuthService。
 */
public record LoginUser(Long id, String phone, String nickname) {
}
