package com.maning.expensecontrol.security;

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
 * JWT 签发与校验（HS256 对称签名）。
 * <p>
 * 为什么用 JWT 而不用 Session：
 * 服务端不存会话，横向扩容时不需要同步 session；缺点是签发后无法立刻作废，
 * 所以过期时间要短（这里 2 小时），必要时再配 Redis 黑名单。
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expireMillis;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expire-minutes:120}") long expireMinutes) {
        // HS256 要求密钥至少 256 位（32 字节），太短会直接抛 WeakKeyException
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMillis = expireMinutes * 60_000L;
    }

    public String generate(Long userId, String empNo) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))     // sub 存用户 id，比存工号更稳（工号可能改）
                .claim("empNo", empNo)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMillis))
                .signWith(key)
                .compact();
    }

    /**
     * 解析失败（过期 / 签名不对 / 格式不对 / 被篡改）统一返回 null，
     * 由调用方转成「未登录」，不要把 JwtException 抛到业务层。
     */
    public Long parseUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.valueOf(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public long getExpireSeconds() {
        return expireMillis / 1000;
    }
}
