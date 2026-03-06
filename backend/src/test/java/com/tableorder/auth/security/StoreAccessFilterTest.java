package com.tableorder.auth.security;

import com.tableorder.auth.dto.AuthInfo;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreAccessFilterTest {

    @Mock private FilterChain filterChain;

    private StoreAccessFilter filter;

    @BeforeEach
    void setUp() {
        filter = new StoreAccessFilter();
        SecurityContextHolder.clearContext();
    }

    // TC-AUTH-031: URL storeId와 토큰 storeId 불일치 시 403
    @Test
    void doFilter_storeIdMismatch_returns403() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/stores/2/admin/menus");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AuthInfo authInfo = new AuthInfo(1L, 1L, "ADMIN", null, null); // storeId=1
        var auth = new UsernamePasswordAuthenticationToken(authInfo, null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("STORE_ACCESS_DENIED"));
        verify(filterChain, never()).doFilter(request, response);
    }

    // TC-AUTH-032: /api/auth/** 경로는 검증 생략
    @Test
    void doFilter_authPath_skipsValidation() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/validate");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }
}
