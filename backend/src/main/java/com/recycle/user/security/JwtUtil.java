package com.recycle.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 签发与解析（HS256）。
 *
 * <p>secret 至少 32 字节；生产环境通过环境变量覆盖，不要提交到仓库。
 */
@Component
public class JwtUtil {

    private static final String CLAIM_PHONE = "phone";
    private static final String CLAIM_NICKNAME = "nickname";

    private final SecretKey key;
    private final long expireMillis;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.access-expire-seconds:1800}") long expireSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMillis = expireSeconds * 1000L;
    }

    public String generate(Long userId, String phone, String nickname) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_PHONE, phone)
                .claim(CLAIM_NICKNAME, nickname)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMillis))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 解析并校验 token，返回登录用户；签名错误或过期抛 {@link JwtException}。
     */
    public LoginUser parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return new LoginUser(
                Long.valueOf(claims.getSubject()),
                claims.get(CLAIM_PHONE, String.class),
                claims.get(CLAIM_NICKNAME, String.class));
    }

    public long getExpireSeconds() {
        return expireMillis / 1000L;
    }
}
