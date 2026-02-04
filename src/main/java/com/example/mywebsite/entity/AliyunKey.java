package com.example.mywebsite.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "aliyun_keys")
public class AliyunKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String alias;

    @Column(name = "access_key_id", nullable = false, length = 100)
    private String accessKeyId;

    @Column(name = "access_key_secret", nullable = false, length = 500)
    private String accessKeySecret;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public AliyunKey() {
        this.createdAt = LocalDateTime.now();
    }

    public AliyunKey(Long userId, String alias, String accessKeyId, String accessKeySecret) {
        this.userId = userId;
        this.alias = alias;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMaskedAccessKeyId() {
        if (accessKeyId == null || accessKeyId.length() < 12) {
            return accessKeyId;
        }
        return accessKeyId.substring(0, 8) + "****" + accessKeyId.substring(accessKeyId.length() - 4);
    }
}
