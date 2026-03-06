package com.tableorder.auth.dto;

public class AuthInfo {
    private Long userId;
    private Long storeId;
    private String role;
    private Long tableId;
    private Long sessionId;

    public AuthInfo() {}
    public AuthInfo(Long userId, Long storeId, String role, Long tableId, Long sessionId) {
        this.userId = userId;
        this.storeId = storeId;
        this.role = role;
        this.tableId = tableId;
        this.sessionId = sessionId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
}
