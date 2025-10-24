package com.excel.api.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 本地Excel文件锁管理器（单机模式）
 * 使用ReadWriteLock实现读写锁，支持多读单写
 */
@Slf4j
@Component
public class LocalExcelLockManager implements ExcelLockManager {
    
    private final ConcurrentHashMap<String, ReadWriteLock> lockMap = new ConcurrentHashMap<>();
    
    private ReadWriteLock getLock(String fileName) {
        return lockMap.computeIfAbsent(fileName, k -> new ReentrantReadWriteLock(true));
    }
    
    @Override
    public boolean acquireReadLock(String fileName) {
        log.debug("尝试获取读锁: {}", fileName);
        getLock(fileName).readLock().lock();
        log.debug("成功获取读锁: {}", fileName);
        return true;
    }
    
    @Override
    public void releaseReadLock(String fileName) {
        log.debug("释放读锁: {}", fileName);
        getLock(fileName).readLock().unlock();
    }
    
    @Override
    public boolean acquireWriteLock(String fileName) {
        log.debug("尝试获取写锁: {}", fileName);
        getLock(fileName).writeLock().lock();
        log.debug("成功获取写锁: {}", fileName);
        return true;
    }
    
    @Override
    public void releaseWriteLock(String fileName) {
        log.debug("释放写锁: {}", fileName);
        getLock(fileName).writeLock().unlock();
    }
    
    @Override
    public boolean tryAcquireReadLock(String fileName, long timeout) {
        log.debug("尝试获取读锁（超时{}ms）: {}", timeout, fileName);
        try {
            boolean acquired = getLock(fileName).readLock().tryLock(timeout, TimeUnit.MILLISECONDS);
            if (acquired) {
                log.debug("成功获取读锁: {}", fileName);
            } else {
                log.warn("获取读锁超时: {}", fileName);
            }
            return acquired;
        } catch (InterruptedException e) {
            log.error("获取读锁被中断: {}", fileName, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    @Override
    public boolean tryAcquireWriteLock(String fileName, long timeout) {
        log.debug("尝试获取写锁（超时{}ms）: {}", timeout, fileName);
        try {
            boolean acquired = getLock(fileName).writeLock().tryLock(timeout, TimeUnit.MILLISECONDS);
            if (acquired) {
                log.debug("成功获取写锁: {}", fileName);
            } else {
                log.warn("获取写锁超时: {}", fileName);
            }
            return acquired;
        } catch (InterruptedException e) {
            log.error("获取写锁被中断: {}", fileName, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }
}

