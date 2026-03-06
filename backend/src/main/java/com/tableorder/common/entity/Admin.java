package com.tableorder.common.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin", uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "username"}))
public class Admin {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "last_token_issued_at")
    private LocalDateTime lastTokenIssuedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public LocalDateTime getLastTokenIssuedAt() { return lastTokenIssuedAt; }
    public void setLastTokenIssuedAt(LocalDateTime lastTokenIssuedAt) { this.lastTokenIssuedAt = lastTokenIssuedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastTokenIssuedAt() { return lastTokenIssuedAt; }
    public void setLastTokenIssuedAt(LocalDateTime lastTokenIssuedAt) { this.lastTokenIssuedAt = lastTokenIssuedAt; }
}
