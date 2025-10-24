package com.excel.api.lock;

/**
 * Excel文件锁管理器接口
 */
public interface ExcelLockManager {
    
    /**
     * 获取读锁
     * @param fileName 文件名
     * @return 是否成功获取
     */
    boolean acquireReadLock(String fileName);
    
    /**
     * 释放读锁
     * @param fileName 文件名
     */
    void releaseReadLock(String fileName);
    
    /**
     * 获取写锁
     * @param fileName 文件名
     * @return 是否成功获取
     */
    boolean acquireWriteLock(String fileName);
    
    /**
     * 释放写锁
     * @param fileName 文件名
     */
    void releaseWriteLock(String fileName);
    
    /**
     * 尝试获取读锁（带超时）
     * @param fileName 文件名
     * @param timeout 超时时间（毫秒）
     * @return 是否成功获取
     */
    boolean tryAcquireReadLock(String fileName, long timeout);
    
    /**
     * 尝试获取写锁（带超时）
     * @param fileName 文件名
     * @param timeout 超时时间（毫秒）
     * @return 是否成功获取
     */
    boolean tryAcquireWriteLock(String fileName, long timeout);
}

