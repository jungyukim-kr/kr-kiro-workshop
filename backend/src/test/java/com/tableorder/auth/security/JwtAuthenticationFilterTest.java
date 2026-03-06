package com.tableorder.auth.security;

import com.tableorder.auth.dto.AuthInfo;
import com.tableorder.auth.repository.AdminRepository;
import com.tableorder.common.entity.Admin;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private AdminRepository adminRepository;
    @Mock private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenProvider, adminRepository);
        SecurityContextHolder.clearContext();
    }

    // TC-AUTH-028: 유효한 토큰 시 SecurityContext 설정
    @Test
    void doFilter_validToken_setsSecurityContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AuthInfo authInfo = new AuthInfo(5L, 1L, "TABLE", 5L, 10L);
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(authInfo);
        when(jwtTokenProvider.shouldRefresh("valid-token")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(authInfo, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    // TC-AUTH-029: 관리자 단일 세션 검증 실패 시 401
    @Test
    void doFilter_adminSingleSessionViolation_returns401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer admin-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AuthInfo authInfo = new AuthInfo(1L, 1L, "ADMIN", null, null);
        when(jwtTokenProvider.validateToken("admin-token")).thenReturn(authInfo);

        // 토큰의 iat와 DB의 lastTokenIssuedAt이 다름
        Date tokenIat = new Date(1000000000L * 1000); // epoch seconds
        when(jwtTokenProvider.getIssuedAt("admin-token")).thenReturn(tokenIat);

        Admin admin = new Admin();
        admin.setId(1L);
        admin.setLastTokenIssuedAt(LocalDateTime.of(2026, 1, 1, 0, 0, 0)); // 다른 시각
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("TOKEN_INVALID"));
    }

    // TC-AUTH-030: 토큰 갱신 필요 시 X-New-Token 헤더 추가
    @Test
    void doFilter_tokenNeedsRefresh_addsXNewTokenHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer old-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AuthInfo authInfo = new AuthInfo(5L, 1L, "TABLE", 5L, null);
        when(jwtTokenProvider.validateToken("old-token")).thenReturn(authInfo);
        when(jwtTokenProvider.shouldRefresh("old-token")).thenReturn(true);
        when(jwtTokenProvider.refreshToken("old-token")).thenReturn("new-token");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals("new-token", response.getHeader("X-New-Token"));
        verify(filterChain).doFilter(request, response);
    }
}
