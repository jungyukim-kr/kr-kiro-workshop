package com.tableorder.auth.controller;

import com.tableorder.auth.dto.*;
import com.tableorder.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/admin/login")
    public ResponseEntity<TokenResponse> adminLogin(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.adminLogin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/table/login")
    public ResponseEntity<TokenResponse> tableLogin(@Valid @RequestBody TableLoginRequest request) {
        TokenResponse response = authService.tableLogin(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/password")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }
        AuthInfo authInfo = (AuthInfo) auth.getPrincipal();
        authService.changePassword(authInfo.getUserId(), request);
        return ResponseEntity.ok(new MessageResponse("비밀번호가 변경되었습니다"));
    }

    @GetMapping("/validate")
    public ResponseEntity<ValidateResponse> validate() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }
        AuthInfo authInfo = (AuthInfo) auth.getPrincipal();
        ValidateResponse response = new ValidateResponse(
                true, authInfo.getRole(), authInfo.getStoreId(),
                authInfo.getUserId(), authInfo.getTableId(), authInfo.getSessionId());
        return ResponseEntity.ok(response);
    }
}
