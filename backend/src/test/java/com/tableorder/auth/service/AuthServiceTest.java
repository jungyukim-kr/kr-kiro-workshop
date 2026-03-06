package com.tableorder.auth.service;

import com.tableorder.auth.dto.*;
import com.tableorder.auth.exception.AuthException;
import com.tableorder.auth.repository.*;
import com.tableorder.auth.security.JwtTokenProvider;
import com.tableorder.common.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private StoreRepository storeRepository;
    @Mock private AdminRepository adminRepository;
    @Mock private StoreTableRepository storeTableRepository;
    @Mock private TableSessionRepository tableSessionRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AuthService authService;

    private Store store;
    private Admin admin;
    private StoreTable storeTable;
    private TableSession tableSession;

    @BeforeEach
    void setUp() {
        store = new Store();
        store.setId(1L);
        store.setStoreCode("STORE001");

        admin = new Admin();
        admin.setId(1L);
        admin.setStoreId(1L);
        admin.setUsername("admin");
        admin.setPasswordHash("$2a$10$hashedpassword");

        storeTable = new StoreTable();
        storeTable.setId(5L);
        storeTable.setStoreId(1L);
        storeTable.setTableNumber(5);
        storeTable.setPasswordHash("$2a$10$hashedpin");

        tableSession = new TableSession();
        tableSession.setId(10L);
        tableSession.setTableId(5L);
        tableSession.setActive(true);
    }

    // TC-AUTH-009: 관리자 로그인 성공
    @Test
    void adminLogin_success_returnsTokenResponse() {
        when(storeRepository.findByStoreCode("STORE001")).thenReturn(Optional.of(store));
        when(adminRepository.findByStoreIdAndUsername(1L, "admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("password123", "$2a$10$hashedpassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(AuthInfo.class))).thenReturn("jwt-token");

        TokenResponse response = authService.adminLogin(new LoginRequest("STORE001", "admin", "password123"));

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(1L, response.getStoreId());
        assertEquals("ADMIN", response.getRole());
        assertEquals(1L, response.getUserId());
    }

    // TC-AUTH-010: 매장 미존재
    @Test
    void adminLogin_storeNotFound_throwsAuthException() {
        when(storeRepository.findByStoreCode("INVALID")).thenReturn(Optional.empty());

        AuthException ex = assertThrows(AuthException.class,
                () -> authService.adminLogin(new LoginRequest("INVALID", "admin", "pass")));
        assertEquals("AUTHENTICATION_FAILED", ex.getErrorCode());
    }

    // TC-AUTH-011: 관리자 미존재
    @Test
    void adminLogin_adminNotFound_throwsAuthException() {
        when(storeRepository.findByStoreCode("STORE001")).thenReturn(Optional.of(store));
        when(adminRepository.findByStoreIdAndUsername(1L, "unknown")).thenReturn(Optional.empty());

        AuthException ex = assertThrows(AuthException.class,
                () -> authService.adminLogin(new LoginRequest("STORE001", "unknown", "pass")));
        assertEquals("AUTHENTICATION_FAILED", ex.getErrorCode());
    }

    // TC-AUTH-012: 비밀번호 불일치
    @Test
    void adminLogin_wrongPassword_throwsAuthException() {
        when(storeRepository.findByStoreCode("STORE001")).thenReturn(Optional.of(store));
        when(adminRepository.findByStoreIdAndUsername(1L, "admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("wrong", "$2a$10$hashedpassword")).thenReturn(false);

        AuthException ex = assertThrows(AuthException.class,
                () -> authService.adminLogin(new LoginRequest("STORE001", "admin", "wrong")));
        assertEquals("AUTHENTICATION_FAILED", ex.getErrorCode());
    }

    // TC-AUTH-013: lastTokenIssuedAt 갱신
    @Test
    void adminLogin_success_updatesLastTokenIssuedAt() {
        when(storeRepository.findByStoreCode("STORE001")).thenReturn(Optional.of(store));
        when(adminRepository.findByStoreIdAndUsername(1L, "admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("password123", "$2a$10$hashedpassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(AuthInfo.class))).thenReturn("jwt-token");

        authService.adminLogin(new LoginRequest("STORE001", "admin", "password123"));

        assertNotNull(admin.getLastTokenIssuedAt());
        verify(adminRepository).save(admin);
    }

    // TC-AUTH-014: 테이블 로그인 성공 (활성 세션 있음)
    @Test
    void tableLogin_successWithSession_returnsTokenResponse() {
        when(storeRepository.findByStoreCode("STORE001")).thenReturn(Optional.of(store));
        when(storeTableRepository.findByStoreIdAndTableNumber(1L, 5)).thenReturn(Optional.of(storeTable));
        when(passwordEncoder.matches("1234", "$2a$10$hashedpin")).thenReturn(true);
        when(tableSessionRepository.findByTableIdAndActiveTrue(5L)).thenReturn(Optional.of(tableSession));
        when(jwtTokenProvider.generateToken(any(AuthInfo.class))).thenReturn("jwt-token");

        TokenResponse response = authService.tableLogin(new TableLoginRequest("STORE001", 5, "1234"));

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("TABLE", response.getRole());
        assertEquals(5L, response.getTableId());
        assertEquals(10L, response.getSessionId());
    }

    // TC-AUTH-015: 테이블 로그인 성공 (활성 세션 없음)
    @Test
    void tableLogin_successNoSession_sessionIdNull() {
        when(storeRepository.findByStoreCode("STORE001")).thenReturn(Optional.of(store));
        when(storeTableRepository.findByStoreIdAndTableNumber(1L, 5)).thenReturn(Optional.of(storeTable));
        when(passwordEncoder.matches("1234", "$2a$10$hashedpin")).thenReturn(true);
        when(tableSessionRepository.findByTableIdAndActiveTrue(5L)).thenReturn(Optional.empty());
        when(jwtTokenProvider.generateToken(any(AuthInfo.class))).thenReturn("jwt-token");

        TokenResponse response = authService.tableLogin(new TableLoginRequest("STORE001", 5, "1234"));

        assertNull(response.getSessionId());
    }

    // TC-AUTH-016: 테이블 로그인 매장 미존재
    @Test
    void tableLogin_storeNotFound_throwsAuthException() {
        when(storeRepository.findByStoreCode("INVALID")).thenReturn(Optional.empty());

        AuthException ex = assertThrows(AuthException.class,
                () -> authService.tableLogin(new TableLoginRequest("INVALID", 5, "1234")));
        assertEquals("AUTHENTICATION_FAILED", ex.getErrorCode());
    }

    // TC-AUTH-017: 테이블 로그인 PIN 불일치
    @Test
    void tableLogin_wrongPin_throwsAuthException() {
        when(storeRepository.findByStoreCode("STORE001")).thenReturn(Optional.of(store));
        when(storeTableRepository.findByStoreIdAndTableNumber(1L, 5)).thenReturn(Optional.of(storeTable));
        when(passwordEncoder.matches("9999", "$2a$10$hashedpin")).thenReturn(false);

        AuthException ex = assertThrows(AuthException.class,
                () -> authService.tableLogin(new TableLoginRequest("STORE001", 5, "9999")));
        assertEquals("AUTHENTICATION_FAILED", ex.getErrorCode());
    }

    // TC-AUTH-018: 비밀번호 변경 성공
    @Test
    void changePassword_success_updatesPasswordAndInvalidatesToken() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("oldPass", "$2a$10$hashedpassword")).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("$2a$10$newhash");

        authService.changePassword(1L, new ChangePasswordRequest("oldPass", "newPass123"));

        assertEquals("$2a$10$newhash", admin.getPasswordHash());
        assertNull(admin.getLastTokenIssuedAt());
        verify(adminRepository).save(admin);
    }

    // TC-AUTH-019: 비밀번호 변경 현재 비밀번호 불일치
    @Test
    void changePassword_wrongCurrentPassword_throwsAuthException() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("wrong", "$2a$10$hashedpassword")).thenReturn(false);

        AuthException ex = assertThrows(AuthException.class,
                () -> authService.changePassword(1L, new ChangePasswordRequest("wrong", "newPass123")));
        assertEquals("AUTHENTICATION_FAILED", ex.getErrorCode());
    }
}
