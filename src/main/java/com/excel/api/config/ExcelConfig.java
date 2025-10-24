package com.excel.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * Excel配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "excel")
public class ExcelConfig {
    
    private Storage storage = new Storage();
    private Lock lock = new Lock();
    private Calculation calculation = new Calculation();
    
    @Data
    public static class Storage {
        private String path = "./excel-files";
        private String tempPath = "./excel-temp";
        private Boolean versionControl = true;
        private Integer maxVersions = 10;
        
        public File getStorageDir() {
            // 返回绝对路径，避免相对路径在Tomcat环境下的问题
            File dir = new File(path).getAbsoluteFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return dir;
        }
        
        public File getTempDir() {
            // 返回绝对路径，避免相对路径在Tomcat环境下的问题
            File dir = new File(tempPath).getAbsoluteFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return dir;
        }
    }
    
    @Data
    public static class Lock {
        private String type = "local";
        private Long timeout = 30000L;
        private Long waitTime = 10000L;
    }
    
    @Data
    public static class Calculation {
        private Boolean cacheEnabled = true;
        private Integer maxConcurrentReads = 50;
        private Integer maxConcurrentWrites = 1;
    }
}

