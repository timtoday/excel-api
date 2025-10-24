package com.excel.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 目录初始化器
 * 在应用启动时自动创建必需的目录
 */
@Slf4j
@Component
public class DirectoryInitializer implements CommandLineRunner {
    
    @Autowired
    private ExcelConfig excelConfig;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化应用目录...");
        
        // 创建 Excel 文件存储目录
        createDirectory(excelConfig.getStorage().getPath(), "Excel文件存储");
        
        // 创建临时文件目录
        createDirectory(excelConfig.getStorage().getTempPath(), "临时文件");
        
        // 创建日志目录
        createDirectory("./logs", "日志");
        
        log.info("应用目录初始化完成");
    }
    
    /**
     * 创建目录
     * @param dirPath 目录路径
     * @param dirName 目录描述名称
     */
    private void createDirectory(String dirPath, String dirName) {
        try {
            Path path = Paths.get(dirPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("✓ 创建{}目录: {}", dirName, path.toAbsolutePath());
            } else {
                log.info("✓ {}目录已存在: {}", dirName, path.toAbsolutePath());
            }
        } catch (Exception e) {
            log.error("✗ 创建{}目录失败: {} - {}", dirName, dirPath, e.getMessage());
            throw new RuntimeException("Failed to create directory: " + dirPath, e);
        }
    }
}

