package com.maning.expensecontrol.security;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码哈希（PBKDF2-HMAC-SHA256 + 随机盐）。
 * <p>
 * 为什么不直接 MD5：
 * 1. MD5 太快，显卡每秒能算几十亿次，弱口令字典一撞就出；
 * 2. 不加盐的话，相同的密码得到相同的摘要，彩虹表直接查。
 * PBKDF2 靠「迭代 12 万次」把单次校验成本抬高到毫秒级，攻击成本随之放大。
 * <p>
 * 存储格式：pbkdf2$迭代次数$盐Base64$摘要Base64 —— 盐跟着密码走，每人不重复。
 */
@Component
public class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "pbkdf2";
    private static final int ITERATIONS = 120_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;

    private final SecureRandom random = new SecureRandom();

    public String hash(String rawPassword) {
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        byte[] digest = pbkdf2(rawPassword, salt, ITERATIONS);
        Base64.Encoder encoder = Base64.getEncoder();
        return PREFIX + "$" + ITERATIONS + "$"
                + encoder.encodeToString(salt) + "$"
                + encoder.encodeToString(digest);
    }

    public boolean matches(String rawPassword, String stored) {
        if (rawPassword == null || stored == null) {
            return false;
        }
        String[] parts = stored.split("\\$");
        if (parts.length != 4 || !PREFIX.equals(parts[0])) {
            return false;
        }
        int iterations = Integer.parseInt(parts[1]);
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] salt = decoder.decode(parts[2]);
        byte[] expected = decoder.decode(parts[3]);
        byte[] actual = pbkdf2(rawPassword, salt, iterations);
        // 常量时间比较，避免「逐字节比较 + 提前返回」被计时攻击猜出前缀
        return MessageDigest.isEqual(expected, actual);
    }

    private byte[] pbkdf2(String rawPassword, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, iterations, HASH_BITS);
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("密码哈希计算失败", e);
        }
    }
}
