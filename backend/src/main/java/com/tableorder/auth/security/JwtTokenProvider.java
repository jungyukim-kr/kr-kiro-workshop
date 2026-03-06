package com.tableorder.auth.security;

import com.tableorder.auth.dto.AuthInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    @Value("${app.jwt.refresh-threshold-ms}")
    private long refreshThresholdMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(AuthInfo authInfo) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        String subject = authInfo.getRole().equals("ADMIN")
                ? "admin:" + authInfo.getUserId()
                : "table:" + authInfo.getUserId();

        var builder = Jwts.builder()
                .subject(subject)
                .claim("storeId", authInfo.getStoreId())
                .claim("role", authInfo.getRole())
                .claim("userId", authInfo.getUserId())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey());

        if (authInfo.getTableId() != null) {
            builder.claim("tableId", authInfo.getTableId());
        }
        if (authInfo.getSessionId() != null) {
            builder.claim("sessionId", authInfo.getSessionId());
        }

        return builder.compact();
    }

    public AuthInfo validateToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new AuthInfo(
                claims.get("userId", Long.class),
                claims.get("storeId", Long.class),
                claims.get("role", String.class),
                claims.get("tableId", Long.class),
                claims.get("sessionId", Long.class)
        );
    }

    public boolean shouldRefresh(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        long remaining = claims.getExpiration().getTime() - System.currentTimeMillis();
        return remaining < refreshThresholdMs;
    }

    public String refreshToken(String token) {
        AuthInfo authInfo = validateToken(token);
        return generateToken(authInfo);
    }

    public Date getIssuedAt(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getIssuedAt();
    }
}
