package com.tableorder.auth.service;

import com.tableorder.auth.dto.*;
import com.tableorder.auth.exception.AuthException;
import com.tableorder.auth.repository.*;
import com.tableorder.auth.security.JwtTokenProvider;
import com.tableorder.common.entity.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final StoreRepository storeRepository;
    private final AdminRepository adminRepository;
    private final StoreTableRepository storeTableRepository;
    private final TableSessionRepository tableSessionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(StoreRepository storeRepository, AdminRepository adminRepository,
                       StoreTableRepository storeTableRepository, TableSessionRepository tableSessionRepository,
                       JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.storeRepository = storeRepository;
        this.adminRepository = adminRepository;
        this.storeTableRepository = storeTableRepository;
        this.tableSessionRepository = tableSessionRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public TokenResponse adminLogin(LoginRequest request) {
        Store store = storeRepository.findByStoreCode(request.getStoreCode())
                .orElseThrow(() -> new AuthException("AUTHENTICATION_FAILED", "로그인 정보가 올바르지 않습니다"));

        Admin admin = adminRepository.findByStoreIdAndUsername(store.getId(), request.getUsername())
                .orElseThrow(() -> new AuthException("AUTHENTICATION_FAILED", "로그인 정보가 올바르지 않습니다"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new AuthException("AUTHENTICATION_FAILED", "로그인 정보가 올바르지 않습니다");
        }

        admin.setLastTokenIssuedAt(LocalDateTime.now());
        adminRepository.save(admin);

        AuthInfo authInfo = new AuthInfo(admin.getId(), store.getId(), "ADMIN", null, null);
        String token = jwtTokenProvider.generateToken(authInfo);

        return new TokenResponse(token, store.getId(), "ADMIN", admin.getId(), null, null);
    }

    @Transactional(readOnly = true)
    public TokenResponse tableLogin(TableLoginRequest request) {
        Store store = storeRepository.findByStoreCode(request.getStoreCode())
                .orElseThrow(() -> new AuthException("AUTHENTICATION_FAILED", "로그인 정보가 올바르지 않습니다"));

        StoreTable table = storeTableRepository.findByStoreIdAndTableNumber(store.getId(), request.getTableNumber())
                .orElseThrow(() -> new AuthException("AUTHENTICATION_FAILED", "로그인 정보가 올바르지 않습니다"));

        if (!passwordEncoder.matches(request.getPassword(), table.getPasswordHash())) {
            throw new AuthException("AUTHENTICATION_FAILED", "로그인 정보가 올바르지 않습니다");
        }

        Long sessionId = tableSessionRepository.findByTableIdAndActiveTrue(table.getId())
                .map(TableSession::getId)
                .orElse(null);

        AuthInfo authInfo = new AuthInfo(table.getId(), store.getId(), "TABLE", table.getId(), sessionId);
        String token = jwtTokenProvider.generateToken(authInfo);

        return new TokenResponse(token, store.getId(), "TABLE", table.getId(), table.getId(), sessionId);
    }

    @Transactional
    public void changePassword(Long adminId, ChangePasswordRequest request) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), admin.getPasswordHash())) {
            throw new AuthException("AUTHENTICATION_FAILED", "현재 비밀번호가 올바르지 않습니다");
        }

        admin.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        admin.setLastTokenIssuedAt(null);
        adminRepository.save(admin);
    }
}
