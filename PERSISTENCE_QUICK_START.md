# Token持久化快速上手 ⚡

## 30秒了解

**问题**: 重启服务后Token丢失  
**解决**: 使用H2数据库持久化  
**结果**: 重启后Token和日志都还在 ✅

## 快速验证

### 步骤1: 重新编译启动

```bash
# 重新编译（自动下载JPA和H2依赖）
mvn clean package -DskipTests

# 启动服务
java -jar target/excel-api-1.0.0.jar
```

### 步骤2: 创建一个Token

1. 访问：`http://localhost:18081/admin/login`
2. 登录：`admin` / `admin123`
3. 进入"Token管理"
4. 创建一个新Token，名称如：`测试Token`
5. 复制Token值（如：`tk_abc123...`）

### 步骤3: 验证持久化

**重启服务**:
```bash
# 停止服务 (Ctrl+C)
# 再次启动
java -jar target/excel-api-1.0.0.jar
```

**检查Token**:
1. 重新访问：`http://localhost:18081/admin/login`
2. 进入"Token管理"
3. ✅ **你会看到之前创建的Token还在！**

### 步骤4: 查看数据库

访问H2控制台：
```
URL: http://localhost:18081/h2-console

连接信息：
- JDBC URL: jdbc:h2:file:./data/excel-api
- User Name: sa
- Password: (留空)
```

执行查询：
```sql
SELECT * FROM api_tokens;
```

✅ **你会看到Token数据！**

## 数据库文件位置

```
E:\codes\excel-api\
└── data\                    ← 新增目录
    └── excel-api.mv.db      ← 数据库文件（自动创建）
```

## 主要变化

### 之前（v1.2.2）
- Token存储在内存（`ConcurrentHashMap`）
- 日志存储在内存（`ConcurrentLinkedQueue`）
- ❌ 重启后数据丢失

### 现在（v1.3.0）
- Token存储在H2数据库（`api_tokens`表）
- 日志存储在H2数据库（`request_logs`表）
- ✅ 重启后数据保留

## 代码无需修改

API接口完全兼容，客户端代码无需任何修改！

```bash
# 之前的请求
curl -H "X-API-Token: tk_abc123..." http://localhost:18081/api/excel/...

# 现在仍然有效
curl -H "X-API-Token: tk_abc123..." http://localhost:18081/api/excel/...
```

## 数据迁移注意

**⚠️ 从旧版本升级**:

如果你从 v1.2.2 或更早版本升级到 v1.3.0:

1. **之前的Token会丢失**（因为存在内存中）
2. **之前的日志会丢失**（因为存在内存中）
3. **重新创建Token即可**

**首次启动v1.3.0后**:
- 所有新创建的Token都会持久化
- 所有新的请求日志都会持久化
- 重启后数据不再丢失 ✅

## 备份数据

### 简单备份

```bash
# 停止服务
# 复制data目录
xcopy /E /I data data_backup_20241024
```

### 恢复备份

```bash
# 停止服务
# 删除data目录
# 复制备份
xcopy /E /I data_backup_20241024 data
# 启动服务
```

## 查看日志

所有请求日志也会持久化：

**方式1: Web界面**
```
http://localhost:18081/admin/logs
```

**方式2: H2控制台**
```sql
SELECT * FROM request_logs ORDER BY timestamp DESC LIMIT 100;
```

## 常见问题

### Q: 数据库文件在哪里？
```
A: ./data/excel-api.mv.db
```

### Q: 如何清空所有Token？
```
A: Web界面 → Token管理 → 逐个删除
   或 H2控制台 → DELETE FROM api_tokens
```

### Q: 数据库会占用多少空间？
```
A: - 100个Token ≈ 10KB
   - 1万条日志 ≈ 5MB
   - 定期清理日志即可
```

### Q: 如何禁用H2控制台？
```
A: application.yml:
   spring:
     h2:
       console:
         enabled: false
```

### Q: 可以换成MySQL吗？
```
A: 可以！参考 DATABASE_PERSISTENCE.md
```

## 性能影响

### 写入性能
- 之前：纯内存写入，极快
- 现在：数据库写入，略慢（毫秒级）
- 影响：可忽略不计

### 查询性能
- 之前：内存查询，极快
- 现在：数据库查询 + 索引优化，仍然很快
- 影响：可忽略不计

## 监控建议

### 定期检查数据库大小

```bash
# Windows
dir data\

# 应该看到类似：
# excel-api.mv.db (几MB)
```

### 定期清理日志

```
http://localhost:18081/admin/logs
→ 点击"清空日志"
```

或SQL：
```sql
DELETE FROM request_logs WHERE timestamp < DATEADD('DAY', -30, CURRENT_TIMESTAMP);
```

## 下一步

1. **详细了解**: 阅读 [DATABASE_PERSISTENCE.md](DATABASE_PERSISTENCE.md)
2. **生产部署**: 考虑切换到MySQL/PostgreSQL
3. **定期备份**: 设置自动备份 `data/` 目录

## 相关文档

- [数据库持久化完整说明](DATABASE_PERSISTENCE.md)
- [Web管理后台指南](ADMIN_GUIDE.md)
- [更新日志](CHANGELOG.md)

---

**现在你的Token和日志都安全地存储在数据库中了！** 💾✅

