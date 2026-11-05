package com.recycle.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.recycle.common.exception.BizException;
import com.recycle.user.domain.RefreshToken;
import com.recycle.user.mapper.RefreshTokenMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

/**
 * 刷新令牌的签发、轮换与撤销。
 *
 * <p>用数据库表而不是 Redis：能撤销、能持久化、不引入新中间件。
 * 量级上来后把这张表换成 Redis 只是替换实现，调用方不用改。
 */
@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenMapper refreshTokenMapper;
    private final long refreshExpireSeconds;

    public RefreshTokenService(RefreshTokenMapper refreshTokenMapper,
                               @Value("${app.jwt.refresh-expire-seconds:604800}") long refreshExpireSeconds) {
        this.refreshTokenMapper = refreshTokenMapper;
        this.refreshExpireSeconds = refreshExpireSeconds;
    }

    public long getRefreshExpireSeconds() {
        return refreshExpireSeconds;
    }

    /**
     * 签发一个新的刷新令牌，返回明文（仅此一次）。
     */
    public String issue(Long userId) {
        String raw = UUID.randomUUID().toString().replace("-", "");
        RefreshToken record = new RefreshToken();
        record.setUserId(userId);
        record.setTokenHash(sha256(raw));
        record.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExpireSeconds));
        record.setRevoked(0);
        refreshTokenMapper.insert(record);
        return raw;
    }

    /**
     * 轮换：校验旧令牌 → 作废，返回归属用户，由调用方签发新的一对令牌。
     *
     * <p>旧令牌被重复使用时（可能是被盗后重放），撤销该用户全部刷新令牌，
     * 强制所有设备重新登录。
     *
     * <p><b>刻意不加 @Transactional</b>：检测到重放时要先撤销全部令牌再抛异常，
     * 如果包在事务里，抛异常会把撤销一起回滚，等于什么都没做。
     * 代价是「作废旧令牌」与「签发新令牌」不在同一事务，签发失败时用户需重新登录——
     * 这是安全侧可接受的方向。
     */
    public Long rotate(String rawToken) {
        RefreshToken record = refreshTokenMapper.selectOne(
                new LambdaQueryWrapper<RefreshToken>().eq(RefreshToken::getTokenHash, sha256(rawToken)));

        if (record == null) {
            throw new BizException("登录状态已失效，请重新登录");
        }
        if (record.getRevoked() != null && record.getRevoked() == 1) {
            // 已作废的令牌又被用了：判定为疑似重放，全部撤销
            log.warn("刷新令牌重复使用，撤销用户 {} 的全部令牌", record.getUserId());
            revokeAll(record.getUserId());
            throw new BizException("登录状态异常，请重新登录");
        }
        if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BizException("登录已过期，请重新登录");
        }

        record.setRevoked(1);
        refreshTokenMapper.updateById(record);
        return record.getUserId();
    }

    /**
     * 登出：撤销单个令牌，幂等。
     */
    public void revoke(String rawToken) {
        refreshTokenMapper.update(null, new LambdaUpdateWrapper<RefreshToken>()
                .eq(RefreshToken::getTokenHash, sha256(rawToken))
                .set(RefreshToken::getRevoked, 1));
    }

    /**
     * 撤销某用户的全部刷新令牌（改密码、封号、疑似盗用时调用）。
     */
    public void revokeAll(Long userId) {
        refreshTokenMapper.update(null, new LambdaUpdateWrapper<RefreshToken>()
                .eq(RefreshToken::getUserId, userId)
                .eq(RefreshToken::getRevoked, 0)
                .set(RefreshToken::getRevoked, 1));
    }

    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }
}
