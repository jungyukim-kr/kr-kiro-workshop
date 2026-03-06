package com.tableorder.auth.dto;

public class TokenResponse {
    private String token;
    private Long storeId;
    private String role;
    private Long userId;
    private Long tableId;
    private Long sessionId;

    public TokenResponse() {}
    public TokenResponse(String token, Long storeId, String role, Long userId, Long tableId, Long sessionId) {
        this.token = token;
        this.storeId = storeId;
        this.role = role;
        this.userId = userId;
        this.tableId = tableId;
        this.sessionId = sessionId;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
}
