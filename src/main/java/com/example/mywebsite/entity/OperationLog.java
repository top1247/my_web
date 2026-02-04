package com.example.mywebsite.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "operation_logs")
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(name = "operation_type", nullable = false, length = 50)
    private String operationType;

    @Column(name = "operation_desc", length = 500)
    private String operationDesc;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(length = 20)
    private String result = "success";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public OperationLog() {
        this.createdAt = LocalDateTime.now();
    }

    public OperationLog(Long userId, String username, String operationType, String operationDesc) {
        this.userId = userId;
        this.username = username;
        this.operationType = operationType;
        this.operationDesc = operationDesc;
        this.createdAt = LocalDateTime.now();
    }

    public OperationLog(Long userId, String username, String operationType, String operationDesc, String result) {
        this.userId = userId;
        this.username = username;
        this.operationType = operationType;
        this.operationDesc = operationDesc;
        this.result = result;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getOperationDesc() {
        return operationDesc;
    }

    public void setOperationDesc(String operationDesc) {
        this.operationDesc = operationDesc;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isSuccess() {
        return "success".equals(result);
    }
}
