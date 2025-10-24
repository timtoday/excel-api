package com.excel.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理后台配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "admin")
public class AdminConfig {
    
    private List<AdminUser> users = new ArrayList<>();
    private TokenConfig tokens = new TokenConfig();
    
    @Data
    public static class AdminUser {
        private String username;
        private String password;
        private String role = "USER";
    }
    
    @Data
    public static class TokenConfig {
        private Integer defaultExpiryDays = 30;
        private Integer maxTokensPerUser = 10;
    }
}

