package com.excel.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 请求日志实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "request_logs", indexes = {
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_token", columnList = "token")
})
public class RequestLog {
    
    @Id
    @Column(length = 50)
    private String id;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(length = 10)
    private String method;
    
    @Column(length = 500)
    private String path;
    
    @Column(length = 100)
    private String token;
    
    @Column(name = "token_name", length = 100)
    private String tokenName;
    
    @Column(name = "status_code")
    private int statusCode;
    
    @Column
    private long duration;
    
    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;
    
    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @Column(name = "client_ip", length = 50)
    private String clientIp;
}

