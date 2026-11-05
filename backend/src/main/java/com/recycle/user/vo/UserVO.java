package com.recycle.user.vo;

import com.recycle.user.domain.User;

/**
 * 对外的用户信息，不含密码。
 */
public record UserVO(
        Long id,
        String phone,
        String nickname,
        String avatar,
        String role
) {
    public static UserVO of(User u) {
        return new UserVO(u.getId(), u.getPhone(), u.getNickname(), u.getAvatar(), u.getRole());
    }
}
