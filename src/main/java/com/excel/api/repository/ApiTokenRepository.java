package com.excel.api.repository;

import com.excel.api.model.ApiToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * API Token Repository
 */
@Repository
public interface ApiTokenRepository extends JpaRepository<ApiToken, String> {
    
    /**
     * 根据token查找
     */
    Optional<ApiToken> findByToken(String token);
    
    /**
     * 查找所有活跃的token
     */
    List<ApiToken> findByActiveTrue();
    
    /**
     * 根据创建者查找
     */
    List<ApiToken> findByCreatedBy(String createdBy);
    
    /**
     * 查找过期的token
     */
    List<ApiToken> findByExpiresAtBefore(LocalDateTime dateTime);
    
    /**
     * 统计用户的token数量
     */
    long countByCreatedBy(String createdBy);
}


