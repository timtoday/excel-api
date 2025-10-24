# 端口更新说明 🔄

## 更新概述

从 **v1.3.1** 开始，API服务端口从 `8080` 变更为 `18081`。

## 变更内容

### 1. 应用端口

**之前**: `8080`  
**现在**: `18081`

### 2. 访问地址变更

| 服务 | 旧地址 | 新地址 |
|------|--------|--------|
| **Web管理后台** | http://localhost:8080/admin/login | http://localhost:18081/admin/login |
| **H2数据库控制台** | http://localhost:8080/h2-console | http://localhost:18081/h2-console |
| **API文档** | http://localhost:8080/swagger-ui.html | http://localhost:18081/swagger-ui.html |
| **健康检查** | http://localhost:8080/api/excel/health | http://localhost:18081/api/excel/health |
| **API测试工具** | http://localhost:8080/admin/test | http://localhost:18081/admin/test |

### 3. API调用示例

**之前**:
```bash
curl -X POST "http://localhost:8080/api/excel/write" \
  -H "X-API-Token: your_token" \
  -H "Content-Type: application/json" \
  -d '{...}'
```

**现在**:
```bash
curl -X POST "http://localhost:18081/api/excel/write" \
  -H "X-API-Token: your_token" \
  -H "Content-Type: application/json" \
  -d '{...}'
```

## Docker配置

### docker-compose.yml

端口映射已更新：

```yaml
services:
  excel-api:
    ports:
      - "18081:18081"  # 宿主机:容器
    environment:
      - REDIS_HOST=redis  # 使用Docker内部链接
      - REDIS_PORT=6379
```

**Redis配置说明**:
- `REDIS_HOST=redis` - 使用Docker内部服务名（不需要localhost）
- Docker Compose会自动创建内部网络，服务间可以通过服务名互相访问
- Redis服务在同一网络中，通过服务名`redis`访问

### Dockerfile

```dockerfile
EXPOSE 18081
HEALTHCHECK CMD wget --spider http://localhost:18081/api/excel/health
```

## 配置文件

### application.yml (本地开发)

```yaml
server:
  port: 18081

spring:
  redis:
    host: localhost  # 本地开发使用localhost
    port: 6379
```

### application-prod.yml (生产环境)

```yaml
server:
  port: ${SERVER_PORT:18081}

spring:
  redis:
    host: ${REDIS_HOST:localhost}  # Docker中通过环境变量设置为'redis'
    port: ${REDIS_PORT:6379}
```

## 升级指南

### 本地开发

1. **拉取最新代码**
   ```bash
   git pull
   ```

2. **重新编译**
   ```bash
   mvn clean package -DskipTests
   ```

3. **启动应用**
   ```bash
   java -jar target/excel-api-1.0.0.jar
   ```

4. **访问新地址**
   ```
   http://localhost:18081/admin/login
   ```

### Docker部署

1. **停止旧容器**
   ```bash
   docker-compose down
   ```

2. **重新构建并启动**
   ```bash
   docker-compose up -d --build
   ```

3. **验证服务**
   ```bash
   curl http://localhost:18081/api/excel/health
   ```

4. **查看日志**
   ```bash
   docker-compose logs -f excel-api
   ```

## 浏览器书签更新

如果你收藏了以下页面，请更新书签：

- ~~http://localhost:8080/admin/login~~ → http://localhost:18081/admin/login
- ~~http://localhost:8080/h2-console~~ → http://localhost:18081/h2-console
- ~~http://localhost:8080/swagger-ui.html~~ → http://localhost:18081/swagger-ui.html
- ~~http://localhost:8080/admin/test~~ → http://localhost:18081/admin/test

## API客户端更新

如果你的应用调用了Excel API，请更新基础URL：

### JavaScript示例

```javascript
// 之前
const BASE_URL = 'http://localhost:8080/api/excel';

// 现在
const BASE_URL = 'http://localhost:18081/api/excel';
```

### Python示例

```python
# 之前
base_url = 'http://localhost:8080/api/excel'

# 现在
base_url = 'http://localhost:18081/api/excel'
```

### Java示例

```java
// 之前
private static final String BASE_URL = "http://localhost:8080/api/excel";

// 现在
private static final String BASE_URL = "http://localhost:18081/api/excel";
```

## Nginx配置更新

如果使用Nginx反向代理，请更新配置：

```nginx
upstream excel_api {
    server 192.168.1.101:18081;  # 更新端口
    server 192.168.1.102:18081;
    server 192.168.1.103:18081;
}

server {
    listen 80;
    server_name api.example.com;
    
    location /api/excel/ {
        proxy_pass http://excel_api;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    location /admin/ {
        proxy_pass http://excel_api;
        proxy_set_header Host $host;
    }
}
```

重载Nginx配置：
```bash
nginx -t
nginx -s reload
```

## 防火墙规则

如果配置了防火墙，需要更新规则：

### Linux (iptables)

```bash
# 删除旧规则
sudo iptables -D INPUT -p tcp --dport 8080 -j ACCEPT

# 添加新规则
sudo iptables -A INPUT -p tcp --dport 18081 -j ACCEPT
sudo iptables-save > /etc/iptables/rules.v4
```

### Linux (firewalld)

```bash
# 删除旧端口
sudo firewall-cmd --permanent --remove-port=8080/tcp

# 添加新端口
sudo firewall-cmd --permanent --add-port=18081/tcp
sudo firewall-cmd --reload
```

### Windows防火墙

```powershell
# 删除旧规则
Remove-NetFirewallRule -DisplayName "Excel API 8080"

# 添加新规则
New-NetFirewallRule -DisplayName "Excel API 18081" -Direction Inbound -Protocol TCP -LocalPort 18081 -Action Allow
```

## 验证步骤

### 1. 检查端口监听

```bash
# Linux/Mac
netstat -an | grep 18081

# Windows
netstat -an | findstr 18081
```

### 2. 测试健康检查

```bash
curl http://localhost:18081/api/excel/health
```

预期响应：
```json
{
  "status": "UP",
  "timestamp": "2024-10-24T12:00:00"
}
```

### 3. 访问Web界面

```
http://localhost:18081/admin/login
```

### 4. 检查Docker容器

```bash
docker ps | grep excel-api
docker exec excel-api netstat -tuln | grep 18081
```

## 常见问题

### Q: 为什么改变端口？
**A**: 18081是更标准的自定义应用端口，避免与其他服务的8080端口冲突。

### Q: 旧端口还能用吗？
**A**: 不能。应用已经完全切换到18081端口。

### Q: Docker中如何访问Redis？
**A**: 
- Docker Compose会创建内部网络
- 通过服务名`redis`访问（环境变量`REDIS_HOST=redis`）
- 不需要使用`localhost`或IP地址

### Q: 本地开发时Redis怎么配置？
**A**: 
- 本地开发使用`application.yml`，Redis host为`localhost`
- Docker部署使用`application-prod.yml`，Redis host通过环境变量设置为`redis`

### Q: 如何临时使用8080端口？
**A**: 可以通过环境变量覆盖：
```bash
SERVER_PORT=8080 java -jar excel-api.jar
```

或修改`application.yml`:
```yaml
server:
  port: 8080
```

### Q: 集群部署需要注意什么？
**A**: 确保所有节点都使用18081端口，更新负载均衡器配置。

## 更新日志

### v1.3.1 - 2024-10-24

#### 端口变更
- 应用端口: 8080 → 18081
- Docker端口映射: 8080:8080 → 18081:18081
- 健康检查URL: /8080/ → /18081/

#### Docker配置优化
- Redis使用Docker内部服务名`redis`
- 通过环境变量`REDIS_HOST=redis`配置
- 利用Docker Compose网络自动解析

#### 文档更新
- 所有文档中的端口引用已更新
- API示例全部更新为18081
- Docker配置示例已更新

## 相关文档

- [README.md](README.md) - 主文档（已更新端口）
- [QUICK_START.md](QUICK_START.md) - 快速开始（已更新）
- [ADMIN_GUIDE.md](ADMIN_GUIDE.md) - 管理后台指南（已更新）
- [DATABASE_PERSISTENCE.md](DATABASE_PERSISTENCE.md) - 数据库说明（已更新）

---

**所有端口已更新为18081，Docker中使用Redis内部链接！** 🔄✅

