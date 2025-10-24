package com.excel.api.repository;

import com.excel.api.model.RequestLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 请求日志 Repository
 */
@Repository
public interface RequestLogRepository extends JpaRepository<RequestLog, String> {
    
    /**
     * 分页查询所有日志（按时间倒序）
     */
    Page<RequestLog> findAllByOrderByTimestampDesc(Pageable pageable);
    
    /**
     * 查询最近的N条日志
     */
    List<RequestLog> findTop100ByOrderByTimestampDesc();
    
    /**
     * 根据token查询日志
     */
    List<RequestLog> findByTokenOrderByTimestampDesc(String token);
    
    /**
     * 查询时间范围内的日志
     */
    List<RequestLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * 统计成功请求数
     */
    long countByStatusCode(int statusCode);
}


