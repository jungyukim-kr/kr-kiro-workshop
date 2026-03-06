package com.tableorder.auth.dto;

public class ValidateResponse {
    private boolean valid;
    private String role;
    private Long storeId;
    private Long userId;
    private Long tableId;
    private Long sessionId;

    public ValidateResponse() {}
    public ValidateResponse(boolean valid, String role, Long storeId, Long userId, Long tableId, Long sessionId) {
        this.valid = valid;
        this.role = role;
        this.storeId = storeId;
        this.userId = userId;
        this.tableId = tableId;
        this.sessionId = sessionId;
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
}
