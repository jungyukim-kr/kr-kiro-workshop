package com.tableorder.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tableorder.auth.dto.*;
import com.tableorder.auth.exception.AuthException;
import com.tableorder.auth.exception.GlobalExceptionHandler;
import com.tableorder.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        SecurityContextHolder.clearContext();
    }

    // ===== TC-AUTH-020: POST /api/auth/admin/login 성공 → 200 =====
    @Test
    @DisplayName("TC-AUTH-020: 관리자 로그인 성공 시 200 + TokenResponse 반환")
    void adminLogin_success_returns200() throws Exception {
        LoginRequest request = new LoginRequest("STORE001", "admin", "password123");
        TokenResponse response = new TokenResponse("jwt-token", 1L, "ADMIN", 1L, null, null);
        when(authService.adminLogin(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.storeId").value(1))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    // ===== TC-AUTH-021: POST /api/auth/admin/login 인증 실패 → 401 =====
    @Test
    @DisplayName("TC-AUTH-021: 관리자 로그인 인증 실패 시 401 + ErrorResponse 반환")
    void adminLogin_authFailed_returns401() throws Exception {
        LoginRequest request = new LoginRequest("STORE001", "admin", "wrongpass");
        when(authService.adminLogin(any(LoginRequest.class)))
                .thenThrow(new AuthException("AUTHENTICATION_FAILED", "로그인 정보가 올바르지 않습니다"));

        mockMvc.perform(post("/api/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTHENTICATION_FAILED"))
                .andExpect(jsonPath("$.message").value("로그인 정보가 올바르지 않습니다"));
    }

    // ===== TC-AUTH-022: POST /api/auth/admin/login 입력 검증 실패 → 400 =====
    @Test
    @DisplayName("TC-AUTH-022: 관리자 로그인 입력 검증 실패 시 400 + ErrorResponse 반환")
    void adminLogin_validationFailed_returns400() throws Exception {
        LoginRequest request = new LoginRequest("", "admin", "password123"); // storeCode blank

        mockMvc.perform(post("/api/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    // ===== TC-AUTH-023: POST /api/auth/table/login 성공 → 200 =====
    @Test
    @DisplayName("TC-AUTH-023: 테이블 로그인 성공 시 200 + TokenResponse 반환")
    void tableLogin_success_returns200() throws Exception {
        TableLoginRequest request = new TableLoginRequest("STORE001", 1, "1234");
        TokenResponse response = new TokenResponse("jwt-token", 1L, "TABLE", 5L, 5L, 10L);
        when(authService.tableLogin(any(TableLoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/table/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.role").value("TABLE"))
                .andExpect(jsonPath("$.tableId").value(5))
                .andExpect(jsonPath("$.sessionId").value(10));
    }

    // ===== TC-AUTH-024: POST /api/auth/table/login PIN 형식 오류 → 400 =====
    @Test
    @DisplayName("TC-AUTH-024: 테이블 로그인 PIN 형식 오류 시 400 반환")
    void tableLogin_invalidPin_returns400() throws Exception {
        TableLoginRequest request = new TableLoginRequest("STORE001", 1, "abc"); // not 4-digit

        mockMvc.perform(post("/api/auth/table/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    // ===== TC-AUTH-025: PUT /api/auth/admin/password 성공 → 200 =====
    @Test
    @DisplayName("TC-AUTH-025: 비밀번호 변경 성공 시 200 + MessageResponse 반환")
    void changePassword_success_returns200() throws Exception {
        // Set up SecurityContext with ADMIN auth
        AuthInfo authInfo = new AuthInfo(1L, 1L, "ADMIN", null, null);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                authInfo, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        ChangePasswordRequest request = new ChangePasswordRequest("oldPass123", "newPass123");
        doNothing().when(authService).changePassword(eq(1L), any(ChangePasswordRequest.class));

        mockMvc.perform(put("/api/auth/admin/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("비밀번호가 변경되었습니다"));
    }

    // ===== TC-AUTH-026: PUT /api/auth/admin/password 인증 없이 → 401 =====
    @Test
    @DisplayName("TC-AUTH-026: 비밀번호 변경 인증 없이 요청 시 401 반환")
    void changePassword_noAuth_returns401() throws Exception {
        // No SecurityContext set
        ChangePasswordRequest request = new ChangePasswordRequest("oldPass123", "newPass123");

        mockMvc.perform(put("/api/auth/admin/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ===== TC-AUTH-027: GET /api/auth/validate 성공 → 200 =====
    @Test
    @DisplayName("TC-AUTH-027: 토큰 유효성 확인 성공 시 200 + ValidateResponse 반환")
    void validate_success_returns200() throws Exception {
        AuthInfo authInfo = new AuthInfo(1L, 1L, "ADMIN", null, null);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                authInfo, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(get("/api/auth/validate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.storeId").value(1))
                .andExpect(jsonPath("$.userId").value(1));
    }
}
