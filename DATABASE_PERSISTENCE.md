# Token和日志持久化说明 💾

## 概述

从 **v1.3.0** 开始，API Token和请求日志已实现**数据库持久化**，重启服务后数据不再丢失！

## 技术方案

### 数据库选择

使用 **H2嵌入式数据库**：
- ✅ 无需额外安装和配置
- ✅ 支持SQL标准
- ✅ 自动创建表结构
- ✅ 文件持久化（存储在 `./data/` 目录）
- ✅ 轻松切换到MySQL/PostgreSQL

### 持久化内容

1. **API Tokens** (`api_tokens` 表)
   - Token信息
   - 创建者和创建时间
   - 过期时间
   - 使用次数和最后使用时间
   - 启用/禁用状态

2. **请求日志** (`request_logs` 表)
   - 所有API请求记录
   - 请求方法、路径、状态码
   - 请求和响应内容
   - Token使用情况
   - 耗时统计

## 数据库配置

### application.yml

```yaml
spring:
  # H2数据库配置
  datasource:
    url: jdbc:h2:file:./data/excel-api;AUTO_SERVER=TRUE
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  h2:
    console:
      enabled: true
      path: /h2-console
      settings:
        web-allow-others: false
  
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update  # 自动更新表结构
    show-sql: false
    properties:
      hibernate:
        format_sql: true
```

### 配置说明

| 配置项 | 说明 |
|--------|------|
| `url: jdbc:h2:file:./data/excel-api` | 数据库文件位置 |
| `AUTO_SERVER=TRUE` | 允许多个进程访问同一数据库 |
| `username: sa` | 默认用户名 |
| `password:` | 默认无密码 |
| `/h2-console` | Web控制台路径 |
| `ddl-auto: update` | 自动创建/更新表结构 |

## 数据库表结构

### api_tokens 表

```sql
CREATE TABLE api_tokens (
    id VARCHAR(50) PRIMARY KEY,
    token VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP,
    active BOOLEAN NOT NULL,
    usage_count INTEGER,
    last_used_at TIMESTAMP
);
```

### request_logs 表

```sql
CREATE TABLE request_logs (
    id VARCHAR(50) PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL,
    method VARCHAR(10),
    path VARCHAR(500),
    token VARCHAR(100),
    token_name VARCHAR(100),
    status_code INTEGER,
    duration BIGINT,
    request_body TEXT,
    response_body TEXT,
    error_message VARCHAR(1000),
    client_ip VARCHAR(50),
    INDEX idx_timestamp (timestamp),
    INDEX idx_token (token)
);
```

## 使用H2控制台

### 访问控制台

1. **启动应用后访问**:
   ```
   http://localhost:18081/h2-console
   ```

2. **登录信息**:
   - JDBC URL: `jdbc:h2:file:./data/excel-api`
   - User Name: `sa`
   - Password: (留空)

3. **点击 "Connect"**

### 常用SQL查询

#### 查看所有Token
```sql
SELECT * FROM api_tokens ORDER BY created_at DESC;
```

#### 查看活跃Token
```sql
SELECT * FROM api_tokens WHERE active = TRUE;
```

#### 查看Token使用统计
```sql
SELECT 
    name,
    token,
    usage_count,
    last_used_at,
    active
FROM api_tokens 
ORDER BY usage_count DESC;
```

#### 查看最近的请求日志
```sql
SELECT * FROM request_logs 
ORDER BY timestamp DESC 
LIMIT 100;
```

#### 统计请求成功率
```sql
SELECT 
    COUNT(*) as total_requests,
    SUM(CASE WHEN status_code = 200 THEN 1 ELSE 0 END) as success_count,
    SUM(CASE WHEN status_code != 200 THEN 1 ELSE 0 END) as error_count,
    ROUND(100.0 * SUM(CASE WHEN status_code = 200 THEN 1 ELSE 0 END) / COUNT(*), 2) as success_rate
FROM request_logs;
```

#### 按接口统计请求量
```sql
SELECT 
    path,
    COUNT(*) as request_count,
    AVG(duration) as avg_duration
FROM request_logs 
GROUP BY path 
ORDER BY request_count DESC;
```

## 数据存储位置

### 文件结构

```
E:\codes\excel-api\
├── data/                        ← H2数据库文件（新增）
│   ├── excel-api.mv.db         ← 主数据文件
│   └── excel-api.trace.db      ← 跟踪文件（如果有）
├── excel-files/                 ← Excel文件
├── excel-temp/                  ← 临时文件
└── logs/                        ← 日志文件
```

### 数据文件说明

| 文件 | 说明 | 大小 |
|------|------|------|
| `excel-api.mv.db` | H2数据库主文件 | 动态增长 |
| `excel-api.trace.db` | 错误跟踪文件 | 很小 |

## 备份和恢复

### 备份数据

**方式1: 文件备份（推荐）**

```bash
# Windows
xcopy /E /I data data_backup_20241024

# Linux/Mac
cp -r data data_backup_20241024
```

**方式2: SQL导出**

在H2控制台执行：
```sql
SCRIPT TO 'backup.sql';
```

### 恢复数据

**方式1: 文件恢复**

```bash
# 停止应用
# 删除 data 目录
# 恢复备份文件
xcopy /E /I data_backup_20241024 data
# 重启应用
```

**方式2: SQL导入**

```sql
RUNSCRIPT FROM 'backup.sql';
```

## 数据迁移

### 切换到MySQL

1. **添加MySQL依赖** (`pom.xml`):
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

2. **修改配置** (`application.yml`):
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/excel_api?useSSL=false&serverTimezone=UTC
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: your_password
  jpa:
    database-platform: org.hibernate.dialect.MySQLDialect
    hibernate:
      ddl-auto: update
```

3. **导出H2数据** → **导入MySQL**

### 切换到PostgreSQL

1. **添加PostgreSQL依赖**:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

2. **修改配置**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/excel_api
    driver-class-name: org.postgresql.Driver
    username: postgres
    password: your_password
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
```

## 性能优化

### 索引已创建

- `request_logs.timestamp` - 提升时间范围查询
- `request_logs.token` - 提升按Token查询

### 日志管理建议

由于所有请求都会记录到数据库，建议定期清理：

**方式1: 通过Web界面**
```
http://localhost:18081/admin/logs
→ 点击"清空日志"
```

**方式2: 通过SQL**
```sql
-- 删除30天前的日志
DELETE FROM request_logs 
WHERE timestamp < DATEADD('DAY', -30, CURRENT_TIMESTAMP);
```

**方式3: 定时任务（TODO）**
```java
@Scheduled(cron = "0 0 2 * * *")  // 每天凌晨2点
public void cleanOldLogs() {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
    logRepository.deleteByTimestampBefore(cutoff);
}
```

## 常见问题

### Q1: 数据库文件在哪里？
**A**: `./data/excel-api.mv.db`，相对于应用启动目录。

### Q2: 如何备份数据？
**A**: 停止应用，复制整个 `data/` 目录。

### Q3: 数据库会占用多少空间？
**A**: 
- 1000个Token ≈ 100KB
- 10万条日志 ≈ 50MB
- 建议定期清理旧日志

### Q4: 可以查看数据库内容吗？
**A**: 可以！访问 `http://localhost:18081/h2-console`

### Q5: 如何清空所有数据？
**A**: 
- 方式1: 停止应用，删除 `data/` 目录
- 方式2: H2控制台执行 `DROP ALL OBJECTS`

### Q6: 重启后Token还在吗？
**A**: ✅ 是的！所有Token和日志都已持久化。

### Q7: 如何迁移到生产数据库？
**A**: 参考上面的"数据迁移"部分，修改配置即可。

### Q8: H2控制台安全吗？
**A**: 默认只允许本地访问。生产环境建议禁用：
```yaml
spring:
  h2:
    console:
      enabled: false  # 生产环境禁用
```

## 监控和维护

### 检查数据库状态

```bash
# 查看数据库文件大小
ls -lh data/

# 或在Windows
dir data\
```

### 监控SQL性能

临时启用SQL日志：
```yaml
spring:
  jpa:
    show-sql: true  # 显示SQL语句
```

### 数据库健康检查

```sql
-- 检查Token数量
SELECT COUNT(*) as token_count FROM api_tokens;

-- 检查日志数量
SELECT COUNT(*) as log_count FROM request_logs;

-- 检查最新记录
SELECT MAX(created_at) as latest_token FROM api_tokens;
SELECT MAX(timestamp) as latest_log FROM request_logs;
```

## 更新日志

### v1.3.0 - 2024-10-24

#### ✨ 新增
- Token数据库持久化
- 请求日志数据库持久化
- H2嵌入式数据库集成
- H2 Web控制台

#### 🔧 技术变更
- `TokenService`: 使用 `ApiTokenRepository`
- `RequestLogService`: 使用 `RequestLogRepository`
- `ApiToken`: 添加JPA注解
- `RequestLog`: 添加JPA注解

#### 📝 新增依赖
- `spring-boot-starter-data-jpa`
- `h2database`

## 相关文档

- [快速开始](QUICK_START.md)
- [Web管理后台指南](ADMIN_GUIDE.md)
- [更新日志](CHANGELOG.md)

---

**现在Token和日志都会持久化到数据库，重启无忧！** 💾✅

