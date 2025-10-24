package com.excel.api.service;

import com.excel.api.config.AdminConfig;
import com.excel.api.model.ApiToken;
import com.excel.api.repository.ApiTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Token管理服务（使用JPA持久化）
 */
@Slf4j
@Service
@Transactional
public class TokenService {
    
    @Autowired
    private AdminConfig adminConfig;
    
    @Autowired
    private ApiTokenRepository tokenRepository;
    
    /**
     * 创建新token
     */
    public ApiToken createToken(String name, String description, String createdBy, Integer expiryDays) {
        if (expiryDays == null) {
            expiryDays = adminConfig.getTokens().getDefaultExpiryDays();
        }
        
        String tokenValue = generateToken();
        String tokenId = UUID.randomUUID().toString();
        
        ApiToken token = ApiToken.builder()
                .id(tokenId)
                .token(tokenValue)
                .name(name)
                .description(description)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(expiryDays))
                .active(true)
                .usageCount(0)
                .build();
        
        tokenRepository.save(token);
        
        log.info("创建新Token: {} by {} (持久化到数据库)", name, createdBy);
        
        return token;
    }
    
    /**
     * 验证token
     */
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        Optional<ApiToken> apiTokenOpt = tokenRepository.findByToken(token);
        
        if (apiTokenOpt.isEmpty()) {
            return false;
        }
        
        ApiToken apiToken = apiTokenOpt.get();
        
        if (!apiToken.isActive()) {
            return false;
        }
        
        if (apiToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        // 更新使用统计（持久化）
        apiToken.setUsageCount(apiToken.getUsageCount() + 1);
        apiToken.setLastUsedAt(LocalDateTime.now());
        tokenRepository.save(apiToken);
        
        return true;
    }
    
    /**
     * 获取token信息
     */
    public ApiToken getToken(String token) {
        return tokenRepository.findByToken(token).orElse(null);
    }
    
    /**
     * 获取所有token
     */
    public List<ApiToken> getAllTokens() {
        return tokenRepository.findAll();
    }
    
    /**
     * 获取按创建者筛选的token
     */
    public List<ApiToken> getTokensByCreator(String creator) {
        return tokenRepository.findByCreatedBy(creator);
    }
    
    /**
     * 禁用token
     */
    public void disableToken(String token) {
        tokenRepository.findByToken(token).ifPresent(apiToken -> {
            apiToken.setActive(false);
            tokenRepository.save(apiToken);
            log.info("禁用Token: {} (持久化到数据库)", apiToken.getName());
        });
    }
    
    /**
     * 启用token
     */
    public void enableToken(String token) {
        tokenRepository.findByToken(token).ifPresent(apiToken -> {
            apiToken.setActive(true);
            tokenRepository.save(apiToken);
            log.info("启用Token: {} (持久化到数据库)", apiToken.getName());
        });
    }
    
    /**
     * 删除token
     */
    public void deleteToken(String token) {
        tokenRepository.findByToken(token).ifPresent(apiToken -> {
            tokenRepository.delete(apiToken);
            log.info("删除Token: {} (从数据库删除)", apiToken.getName());
        });
    }
    
    /**
     * 生成随机token
     */
    private String generateToken() {
        return "tk_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 清理过期token
     */
    public void cleanExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        List<ApiToken> expiredTokens = tokenRepository.findByExpiresAtBefore(now);
        tokenRepository.deleteAll(expiredTokens);
        log.info("清理过期Token: {} 个", expiredTokens.size());
    }
}

