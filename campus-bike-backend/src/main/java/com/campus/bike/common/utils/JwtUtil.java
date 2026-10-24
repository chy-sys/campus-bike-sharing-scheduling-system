package com.campus.bike.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret:gxu_bike_secret_key_2026}")
    private String secret;

    @Value("${jwt.expiration:86400000}")  // 默认 24 小时
    private Long expiration;

    /**
     * 生成 JWT Token
     */
    public String generateToken(Long userId, String phone) {
        return generateToken(userId, phone, 0);
    }

    /**
     * 生成 JWT Token（携带角色，供运维小程序与 Web 后台鉴权）
     */
    public String generateToken(Long userId, String phone, Integer role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("phone", phone);
        claims.put("role", role == null ? 0 : role);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(phone)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * 从 Token 中获取 Claims
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        return getClaimsFromToken(token).get("userId", Long.class);
    }

    /**
     * 获取手机号
     */
    public String getPhoneFromToken(String token) {
        return getClaimsFromToken(token).get("phone", String.class);
    }

    /**
     * 获取角色：0普通用户 1运维人员 2管理员
     */
    public Integer getRoleFromToken(String token) {
        Object role = getClaimsFromToken(token).get("role");
        if (role instanceof Number) {
            return ((Number) role).intValue();
        }
        if (role == null) {
            return 0;
        }
        return Integer.parseInt(String.valueOf(role));
    }

    /**
     * 验证 Token 是否有效
     */
    public Boolean validateToken(String token) {
        try {
            return !getClaimsFromToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
