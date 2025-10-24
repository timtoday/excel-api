package com.excel.api.service;

import com.excel.api.model.RequestLog;
import com.excel.api.repository.RequestLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 请求日志服务（使用JPA持久化）
 */
@Slf4j
@Service
@Transactional
public class RequestLogService {
    
    @Autowired
    private RequestLogRepository logRepository;
    
    /**
     * 记录请求日志
     */
    public void logRequest(RequestLog requestLog) {
        requestLog.setId(UUID.randomUUID().toString());
        requestLog.setTimestamp(LocalDateTime.now());
        
        logRepository.save(requestLog);
    }
    
    /**
     * 获取所有日志
     */
    public List<RequestLog> getAllLogs() {
        return logRepository.findAll();
    }
    
    /**
     * 获取最近的N条日志
     */
    public List<RequestLog> getRecentLogs(int count) {
        return logRepository.findTop100ByOrderByTimestampDesc();
    }
    
    /**
     * 按token筛选日志
     */
    public List<RequestLog> getLogsByToken(String token) {
        return logRepository.findByTokenOrderByTimestampDesc(token);
    }
    
    /**
     * 清空日志
     */
    public void clearLogs() {
        logRepository.deleteAll();
        log.info("清空所有请求日志（从数据库删除）");
    }
    
    /**
     * 获取统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<RequestLog> allLogs = logRepository.findAll();
        
        stats.put("totalRequests", allLogs.size());
        stats.put("successRequests", allLogs.stream().filter(l -> l.getStatusCode() == 200).count());
        stats.put("errorRequests", allLogs.stream().filter(l -> l.getStatusCode() != 200).count());
        
        // 按路径统计
        Map<String, Long> pathStats = allLogs.stream()
                .collect(Collectors.groupingBy(RequestLog::getPath, Collectors.counting()));
        stats.put("pathStats", pathStats);
        
        return stats;
    }
}

