package com.excel.api.lock;

import com.excel.api.config.ExcelConfig;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis分布式Excel文件锁管理器（集群模式）
 * 使用Redisson实现分布式读写锁
 */
@Slf4j
@Component
@Primary
@ConditionalOnProperty(name = "excel.lock.type", havingValue = "redis")
@ConditionalOnBean(RedissonClient.class)
public class RedisExcelLockManager implements ExcelLockManager {
    
    private final RedissonClient redissonClient;
    private final ExcelConfig excelConfig;
    
    public RedisExcelLockManager(RedissonClient redissonClient, ExcelConfig excelConfig) {
        this.redissonClient = redissonClient;
        this.excelConfig = excelConfig;
        log.info("使用Redis分布式锁管理器");
    }
    
    private RReadWriteLock getLock(String fileName) {
        return redissonClient.getReadWriteLock("excel:lock:" + fileName);
    }
    
    @Override
    public boolean acquireReadLock(String fileName) {
        log.debug("尝试获取Redis读锁: {}", fileName);
        getLock(fileName).readLock().lock();
        log.debug("成功获取Redis读锁: {}", fileName);
        return true;
    }
    
    @Override
    public void releaseReadLock(String fileName) {
        log.debug("释放Redis读锁: {}", fileName);
        try {
            getLock(fileName).readLock().unlock();
        } catch (Exception e) {
            log.error("释放Redis读锁失败: {}", fileName, e);
        }
    }
    
    @Override
    public boolean acquireWriteLock(String fileName) {
        log.debug("尝试获取Redis写锁: {}", fileName);
        getLock(fileName).writeLock().lock();
        log.debug("成功获取Redis写锁: {}", fileName);
        return true;
    }
    
    @Override
    public void releaseWriteLock(String fileName) {
        log.debug("释放Redis写锁: {}", fileName);
        try {
            getLock(fileName).writeLock().unlock();
        } catch (Exception e) {
            log.error("释放Redis写锁失败: {}", fileName, e);
        }
    }
    
    @Override
    public boolean tryAcquireReadLock(String fileName, long timeout) {
        log.debug("尝试获取Redis读锁（超时{}ms）: {}", timeout, fileName);
        try {
            boolean acquired = getLock(fileName).readLock()
                    .tryLock(timeout, excelConfig.getLock().getTimeout(), TimeUnit.MILLISECONDS);
            if (acquired) {
                log.debug("成功获取Redis读锁: {}", fileName);
            } else {
                log.warn("获取Redis读锁超时: {}", fileName);
            }
            return acquired;
        } catch (InterruptedException e) {
            log.error("获取Redis读锁被中断: {}", fileName, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    @Override
    public boolean tryAcquireWriteLock(String fileName, long timeout) {
        log.debug("尝试获取Redis写锁（超时{}ms）: {}", timeout, fileName);
        try {
            boolean acquired = getLock(fileName).writeLock()
                    .tryLock(timeout, excelConfig.getLock().getTimeout(), TimeUnit.MILLISECONDS);
            if (acquired) {
                log.debug("成功获取Redis写锁: {}", fileName);
            } else {
                log.warn("获取Redis写锁超时: {}", fileName);
            }
            return acquired;
        } catch (InterruptedException e) {
            log.error("获取Redis写锁被中断: {}", fileName, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }
}

