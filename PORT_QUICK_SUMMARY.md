# 端口更新快速总结 ⚡

## 一句话总结

**端口从 8080 改为 18081，Docker中Redis使用服务名 `redis` 连接**

## 快速验证

### 本地开发

```bash
# 1. 重新编译
mvn clean package -DskipTests

# 2. 启动服务
java -jar target/excel-api-1.0.0.jar

# 3. 访问新地址
http://localhost:18081/admin/login
```

### Docker部署

```bash
# 1. 重新构建
docker-compose down
docker-compose up -d --build

# 2. 验证服务
curl http://localhost:18081/api/excel/health

# 3. 验证Redis连接
docker exec excel-api env | grep REDIS
# 应该看到: REDIS_HOST=redis
```

## 主要变更

| 项目 | 之前 | 现在 |
|------|------|------|
| **应用端口** | 8080 | 18081 |
| **Web管理** | :8080/admin | :18081/admin |
| **H2控制台** | :8080/h2-console | :18081/h2-console |
| **API文档** | :8080/swagger-ui | :18081/swagger-ui |
| **Docker Redis** | localhost | redis (服务名) |

## 配置文件变更

### application.yml
```yaml
server:
  port: 18081  # 改为18081
```

### application-prod.yml
```yaml
server:
  port: ${SERVER_PORT:18081}  # 改为18081

spring:
  redis:
    host: ${REDIS_HOST:localhost}  # Docker中通过环境变量设为'redis'
```

### docker-compose.yml
```yaml
services:
  excel-api:
    ports:
      - "18081:18081"  # 改为18081
    environment:
      - REDIS_HOST=redis  # 使用Docker服务名
      - REDIS_PORT=6379
```

### Dockerfile
```dockerfile
EXPOSE 18081  # 改为18081
HEALTHCHECK CMD wget --spider http://localhost:18081/api/excel/health
```

## Docker Redis 说明

### 为什么使用服务名？

在 `docker-compose.yml` 中定义的服务可以通过服务名互相访问：

```yaml
services:
  excel-api:
    environment:
      - REDIS_HOST=redis  # ← 使用服务名，不是localhost
  
  redis:  # ← 服务名
    image: redis:7-alpine
```

**Docker Compose 会自动**:
1. 创建内部网络
2. 配置DNS解析，让 `redis` 解析到Redis容器IP
3. 服务间可以直接通过服务名通信

### 本地开发 vs Docker

| 环境 | Redis Host | 原因 |
|------|-----------|------|
| **本地开发** | `localhost` | Redis运行在本机 |
| **Docker** | `redis` | Redis运行在另一个容器 |

## 需要更新的地方

### ✅ 已自动更新
- [x] application.yml
- [x] application-prod.yml
- [x] docker-compose.yml
- [x] Dockerfile
- [x] README.md（所有示例）
- [x] 所有文档中的URL

### ⚠️ 需要手动更新

如果你有以下内容，需要手动更新：

1. **浏览器书签**
   - 更新管理后台URL为 :18081

2. **客户端代码**
   ```javascript
   // 更新基础URL
   const BASE_URL = 'http://localhost:18081/api/excel';
   ```

3. **Nginx配置**（如果有）
   ```nginx
   upstream excel_api {
       server 192.168.1.101:18081;
   }
   ```

4. **防火墙规则**（如果配置了）
   ```bash
   # 开放18081端口
   sudo firewall-cmd --add-port=18081/tcp
   ```

## 验证清单

- [ ] 本地可以访问 http://localhost:18081/admin/login
- [ ] Docker可以访问 http://localhost:18081/api/excel/health
- [ ] Docker日志中显示 "Started on port(s): 18081"
- [ ] Docker中Redis连接正常（检查环境变量 REDIS_HOST=redis）
- [ ] H2数据库可以访问 http://localhost:18081/h2-console
- [ ] API调用使用新端口正常

## 常见问题

### Q: 端口修改后原来的API还能用吗？
**A**: 不能。必须更新为18081端口。

### Q: Docker中为什么Redis用服务名？
**A**: Docker Compose创建了内部网络，服务间通过服务名通信，这是Docker的最佳实践。

### Q: 本地开发Redis配置要改吗？
**A**: 不用。本地开发仍然使用 localhost。

### Q: 如何快速测试？
**A**: 
```bash
# 本地
curl http://localhost:18081/api/excel/health

# Docker
docker exec excel-api curl http://localhost:18081/api/excel/health
```

## 回滚方法

如果需要回退到8080端口：

```bash
# 1. 修改 application.yml
server:
  port: 8080

# 2. 修改 docker-compose.yml
ports:
  - "8080:8080"

# 3. 修改 Dockerfile
EXPOSE 8080
```

## 文档链接

- [PORT_UPDATE.md](PORT_UPDATE.md) - 完整的端口更新指南
- [README.md](README.md) - 主文档（已更新）
- [CHANGELOG.md](CHANGELOG.md) - v1.3.1版本说明

---

**端口已更新为18081，Docker使用Redis内部链接！** 🚀✅
