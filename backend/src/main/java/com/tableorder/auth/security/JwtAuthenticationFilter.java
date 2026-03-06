package com.tableorder.auth.security;

import com.tableorder.auth.dto.AuthInfo;
import com.tableorder.auth.repository.AdminRepository;
import com.tableorder.common.entity.Admin;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final AdminRepository adminRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, AdminRepository adminRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.adminRepository = adminRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (token != null) {
            try {
                AuthInfo authInfo = jwtTokenProvider.validateToken(token);

                // 관리자 단일 세션 검증
                if ("ADMIN".equals(authInfo.getRole())) {
                    Admin admin = adminRepository.findById(authInfo.getUserId()).orElse(null);
                    if (admin != null && admin.getLastTokenIssuedAt() != null) {
                        Date tokenIat = jwtTokenProvider.getIssuedAt(token);
                        long tokenIatEpoch = tokenIat.getTime() / 1000;
                        long dbIatEpoch = admin.getLastTokenIssuedAt()
                                .atZone(ZoneId.systemDefault()).toEpochSecond();
                        if (Math.abs(tokenIatEpoch - dbIatEpoch) > 1) {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"error\":\"TOKEN_INVALID\",\"message\":\"유효하지 않은 토큰입니다\"}");
                            return;
                        }
                    }
                }

                var authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + authInfo.getRole()));
                var authentication = new UsernamePasswordAuthenticationToken(authInfo, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 토큰 자동 갱신
                if (jwtTokenProvider.shouldRefresh(token)) {
                    String newToken = jwtTokenProvider.refreshToken(token);
                    response.setHeader("X-New-Token", newToken);

                    // 관리자면 DB도 갱신
                    if ("ADMIN".equals(authInfo.getRole())) {
                        Admin admin = adminRepository.findById(authInfo.getUserId()).orElse(null);
                        if (admin != null) {
                            Date newIat = jwtTokenProvider.getIssuedAt(newToken);
                            admin.setLastTokenIssuedAt(
                                    newIat.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                            adminRepository.save(admin);
                        }
                    }
                }
            } catch (Exception e) {
                // 토큰 검증 실패 시 인증 없이 진행 (Spring Security가 401 처리)
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
