package com.tableorder.auth.security;

import com.tableorder.auth.dto.AuthInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StoreAccessFilter extends OncePerRequestFilter {

    private static final Pattern STORE_PATH_PATTERN = Pattern.compile("/api/stores/(\\d+)/");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // /api/auth/** 경로는 검증 생략
        if (path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        Matcher matcher = STORE_PATH_PATTERN.matcher(path);
        if (matcher.find()) {
            Long urlStoreId = Long.parseLong(matcher.group(1));

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof AuthInfo) {
                AuthInfo authInfo = (AuthInfo) auth.getPrincipal();
                if (!urlStoreId.equals(authInfo.getStoreId())) {
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                            "{\"error\":\"STORE_ACCESS_DENIED\",\"message\":\"매장 접근 권한이 없습니다\"}");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
