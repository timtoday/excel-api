# 📁 目录结构说明

本文档说明应用程序的目录结构和自动初始化机制。

## 核心目录

### 1. excel-files/
**用途**: Excel 文件存储目录
- 存储所有上传和使用的 Excel 文件
- 支持版本控制功能
- 可通过 API 进行文件操作

**配置**:
```yaml
excel:
  storage:
    path: ./excel-files  # 开发环境
    # path: /data/excel-files  # 生产环境
```

### 2. excel-temp/
**用途**: 临时文件目录
- 存储模板处理过程中的临时文件
- 存储 Excel 操作的中间文件
- 系统会自动清理过期的临时文件

**配置**:
```yaml
excel:
  storage:
    temp-path: ./excel-temp  # 开发环境
    # temp-path: /data/excel-temp  # 生产环境
```

### 3. logs/
**用途**: 应用日志目录
- 存储应用运行日志
- 日志文件自动轮转
- 支持日志级别配置

**配置**:
```yaml
logging:
  file:
    name: ./logs/excel-api.log
    max-size: 10MB      # 单个日志文件最大大小
    max-history: 30     # 保留最近30天的日志
    total-size-cap: 1GB # 总日志大小上限
```

## 自动初始化

### 启动时自动创建
应用程序使用 `DirectoryInitializer` 组件在启动时自动创建所有必需的目录：

```java
@Component
public class DirectoryInitializer implements CommandLineRunner {
    @Override
    public void run(String... args) {
        // 自动创建 excel-files、excel-temp 和 logs 目录
    }
}
```

### 启动日志示例
```
2024-10-24 12:00:00 [main] INFO  DirectoryInitializer - 开始初始化应用目录...
2024-10-24 12:00:00 [main] INFO  DirectoryInitializer - ✓ 创建Excel文件存储目录: E:\codes\excel-api\excel-files
2024-10-24 12:00:00 [main] INFO  DirectoryInitializer - ✓ 创建临时文件目录: E:\codes\excel-api\excel-temp
2024-10-24 12:00:00 [main] INFO  DirectoryInitializer - ✓ 创建日志目录: E:\codes\excel-api\logs
2024-10-24 12:00:00 [main] INFO  DirectoryInitializer - 应用目录初始化完成
```

## Git 版本控制

### 目录保留策略
为了在 Git 中保留目录结构但不提交目录内容，使用以下配置：

**.gitignore**:
```gitignore
# 忽略目录内容
excel-files/*
excel-temp/*
logs/*

# 但保留 .gitkeep 文件
!excel-files/.gitkeep
!excel-temp/.gitkeep
!logs/.gitkeep
```

每个目录中都包含一个 `.gitkeep` 文件以确保目录被 Git 跟踪。

## 权限要求

### 文件系统权限
确保应用程序有权限在以下位置创建目录和文件：

- **开发环境**: 当前工作目录 (`./`)
- **生产环境**: 配置的路径（通常是 `/data/`）

### Linux/Unix 权限设置
```bash
# 创建目录
mkdir -p /data/excel-files /data/excel-temp /data/logs

# 设置权限（根据实际运行用户调整）
chown -R appuser:appuser /data/excel-files /data/excel-temp /data/logs
chmod -R 755 /data/excel-files /data/excel-temp /data/logs
```

## 目录维护

### 磁盘空间监控
建议定期监控目录大小：

```bash
# 查看目录大小
du -sh excel-files excel-temp logs

# 查看详细文件列表
ls -lh excel-files/
ls -lh excel-temp/
ls -lh logs/
```

### 清理策略

#### 临时文件清理
临时文件会在以下情况被清理：
- 操作完成后自动清理
- 系统重启后可手动清理

```bash
# 手动清理临时文件
rm -rf excel-temp/*
```

#### 日志清理
日志文件根据配置自动轮转和清理：
- 单个文件超过 `max-size` 时自动轮转
- 超过 `max-history` 天的日志自动删除
- 总大小超过 `total-size-cap` 时删除最旧的日志

## 故障排查

### 目录创建失败
如果启动时出现目录创建失败的错误：

1. **检查权限**:
   ```bash
   ls -ld .
   ```

2. **手动创建目录**:
   ```bash
   mkdir excel-files excel-temp logs
   ```

3. **查看错误日志**:
   ```bash
   tail -f logs/excel-api.log
   ```

### 磁盘空间不足
如果磁盘空间不足：

1. **清理临时文件**:
   ```bash
   rm -rf excel-temp/*
   ```

2. **归档旧的 Excel 文件**:
   ```bash
   tar -czf excel-backup-$(date +%Y%m%d).tar.gz excel-files/
   ```

3. **清理旧日志**:
   ```bash
   find logs/ -name "*.log.*" -mtime +30 -delete
   ```

## 环境变量配置

可以通过环境变量自定义目录路径：

```bash
# 设置自定义路径
export EXCEL_STORAGE_PATH=/custom/path/excel-files
export EXCEL_TEMP_PATH=/custom/path/excel-temp
export LOG_FILE_PATH=/custom/path/logs

# 启动应用
java -jar excel-api.jar
```

或在启动时指定：

```bash
java -jar excel-api.jar \
  --excel.storage.path=/custom/path/excel-files \
  --excel.storage.temp-path=/custom/path/excel-temp \
  --logging.file.name=/custom/path/logs/excel-api.log
```

## 最佳实践

1. **开发环境**: 使用相对路径 (`./excel-files`)
2. **生产环境**: 使用绝对路径 (`/data/excel-files`)
3. **定期备份**: 定期备份 `excel-files` 目录
4. **监控磁盘**: 设置磁盘空间告警
5. **日志归档**: 定期归档历史日志文件

## 相关配置文件

- `src/main/resources/application.yml` - 开发环境配置
- `src/main/resources/application-prod.yml` - 生产环境配置
- `src/main/java/com/excel/api/config/DirectoryInitializer.java` - 目录初始化逻辑
- `src/main/java/com/excel/api/config/ExcelConfig.java` - Excel 配置类

