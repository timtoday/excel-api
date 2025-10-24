package com.excel.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API Token实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "api_tokens")
public class ApiToken {
    
    @Id
    @Column(length = 50)
    private String id;
    
    @Column(unique = true, nullable = false, length = 100)
    private String token;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "created_by", length = 50)
    private String createdBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    private boolean active;
    
    @Column(name = "usage_count")
    private int usageCount;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
}

