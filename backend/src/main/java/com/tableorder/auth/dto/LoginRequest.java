package com.tableorder.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {
    @NotBlank(message = "매장 식별자를 입력해주세요")
    @Size(max = 50)
    private String storeCode;

    @NotBlank(message = "사용자명을 입력해주세요")
    @Size(max = 50)
    private String username;

    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;

    public LoginRequest() {}
    public LoginRequest(String storeCode, String username, String password) {
        this.storeCode = storeCode;
        this.username = username;
        this.password = password;
    }

    public String getStoreCode() { return storeCode; }
    public void setStoreCode(String storeCode) { this.storeCode = storeCode; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
