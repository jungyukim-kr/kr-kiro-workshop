package com.tableorder.auth.security;

import com.tableorder.auth.dto.AuthInfo;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;
    private static final String SECRET = "my-super-secret-key-for-jwt-signing-at-least-256-bits-long!!";
    private static final long EXPIRATION_MS = 57600000L; // 16h
    private static final long REFRESH_THRESHOLD_MS = 28800000L; // 8h

    @BeforeEach
    void setUp() throws Exception {
        provider = new JwtTokenProvider();
        setField(provider, "secret", SECRET);
        setField(provider, "expirationMs", EXPIRATION_MS);
        setField(provider, "refreshThresholdMs", REFRESH_THRESHOLD_MS);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // TC-AUTH-001: 관리자 AuthInfo로 JWT 토큰 생성 시 올바른 클레임 포함
    @Test
    void generateToken_admin_containsCorrectClaims() {
        AuthInfo authInfo = new AuthInfo(1L, 1L, "ADMIN", null, null);

        String token = provider.generateToken(authInfo);

        assertNotNull(token);
        AuthInfo parsed = provider.validateToken(token);
        assertEquals("ADMIN", parsed.getRole());
        assertEquals(1L, parsed.getStoreId());
        assertEquals(1L, parsed.getUserId());
        assertNull(parsed.getTableId());
        assertNull(parsed.getSessionId());
    }

    // TC-AUTH-002: 테이블 AuthInfo로 JWT 토큰 생성 시 tableId, sessionId 포함
    @Test
    void generateToken_table_containsTableIdAndSessionId() {
        AuthInfo authInfo = new AuthInfo(5L, 1L, "TABLE", 5L, 10L);

        String token = provider.generateToken(authInfo);

        assertNotNull(token);
        AuthInfo parsed = provider.validateToken(token);
        assertEquals("TABLE", parsed.getRole());
        assertEquals(1L, parsed.getStoreId());
        assertEquals(5L, parsed.getUserId());
        assertEquals(5L, parsed.getTableId());
        assertEquals(10L, parsed.getSessionId());
    }

    // TC-AUTH-003: 유효한 토큰 검증 시 AuthInfo 반환
    @Test
    void validateToken_validToken_returnsAuthInfo() {
        AuthInfo original = new AuthInfo(1L, 1L, "ADMIN", null, null);
        String token = provider.generateToken(original);

        AuthInfo result = provider.validateToken(token);

        assertNotNull(result);
        assertEquals(original.getUserId(), result.getUserId());
        assertEquals(original.getStoreId(), result.getStoreId());
        assertEquals(original.getRole(), result.getRole());
    }

    // TC-AUTH-004: 만료된 토큰 검증 시 ExpiredJwtException 발생
    @Test
    void validateToken_expiredToken_throwsExpiredJwtException() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("admin:1")
                .claim("storeId", 1L)
                .claim("role", "ADMIN")
                .claim("userId", 1L)
                .issuedAt(new Date(System.currentTimeMillis() - 200000))
                .expiration(new Date(System.currentTimeMillis() - 100000))
                .signWith(key)
                .compact();

        assertThrows(ExpiredJwtException.class, () -> provider.validateToken(expiredToken));
    }

    // TC-AUTH-005: 잘못된 서명의 토큰 검증 시 예외 발생
    @Test
    void validateToken_invalidSignature_throwsException() {
        SecretKey wrongKey = Keys.hmacShaKeyFor("another-secret-key-that-is-at-least-256-bits-long!!!!!".getBytes(StandardCharsets.UTF_8));
        String badToken = Jwts.builder()
                .subject("admin:1")
                .claim("storeId", 1L)
                .claim("role", "ADMIN")
                .claim("userId", 1L)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(wrongKey)
                .compact();

        assertThrows(Exception.class, () -> provider.validateToken(badToken));
    }

    // TC-AUTH-006: 잔여시간 < refresh-threshold이면 shouldRefresh() true
    @Test
    void shouldRefresh_remainingTimeLessThanThreshold_returnsTrue() throws Exception {
        // 만료까지 1시간 남은 토큰 생성
        JwtTokenProvider shortProvider = new JwtTokenProvider();
        setField(shortProvider, "secret", SECRET);
        setField(shortProvider, "expirationMs", 3600000L); // 1h expiration
        setField(shortProvider, "refreshThresholdMs", REFRESH_THRESHOLD_MS); // 8h threshold

        AuthInfo authInfo = new AuthInfo(1L, 1L, "ADMIN", null, null);
        String token = shortProvider.generateToken(authInfo);

        // 1시간 < 8시간이므로 true
        assertTrue(provider.shouldRefresh(token));
    }

    // TC-AUTH-007: 잔여시간 >= refresh-threshold이면 shouldRefresh() false
    @Test
    void shouldRefresh_remainingTimeGreaterThanThreshold_returnsFalse() {
        AuthInfo authInfo = new AuthInfo(1L, 1L, "ADMIN", null, null);
        String token = provider.generateToken(authInfo);

        // 방금 발급 → 16시간 남음 > 8시간
        assertFalse(provider.shouldRefresh(token));
    }

    // TC-AUTH-008: refreshToken()으로 새 토큰 생성 시 동일 페이로드, 새 exp
    @Test
    void refreshToken_returnsNewTokenWithSamePayload() throws InterruptedException {
        AuthInfo authInfo = new AuthInfo(1L, 1L, "ADMIN", null, null);
        String originalToken = provider.generateToken(authInfo);

        // 최소 1초 대기하여 iat/exp가 달라지도록 함
        Thread.sleep(1100);

        String newToken = provider.refreshToken(originalToken);

        assertNotNull(newToken);
        assertNotEquals(originalToken, newToken);
        AuthInfo parsed = provider.validateToken(newToken);
        assertEquals(1L, parsed.getUserId());
        assertEquals(1L, parsed.getStoreId());
        assertEquals("ADMIN", parsed.getRole());
    }
}
