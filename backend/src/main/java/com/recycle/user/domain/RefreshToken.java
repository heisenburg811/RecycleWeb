package com.recycle.user.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 刷新令牌。库里只存 SHA-256 摘要，明文只在签发那一刻返回给客户端。
 */
@Data
@TableName("refresh_token")
public class RefreshToken {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 明文 token 的 SHA-256 十六进制摘要 */
    private String tokenHash;

    private LocalDateTime expiresAt;

    /** 1 已撤销（登出、轮换、疑似盗用） */
    private Integer revoked;

    private LocalDateTime createdAt;
}
