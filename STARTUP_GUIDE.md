# 应用启动指南 🚀

## 快速启动清单

### ✅ 启动前检查

- [ ] Docker Desktop 已启动
- [ ] Redis 服务已运行
- [ ] 端口 18081 和 6379 未被占用
- [ ] 已执行 `mvn clean package -DskipTests`

---

## 方式1: 本地开发运行（推荐用于开发）

### 步骤1: 启动Redis

```bash
# 启动Docker Redis（推荐）
docker-compose up -d redis

# 验证Redis运行
docker exec excel-redis redis-cli ping
# 应该返回: PONG
```

### 步骤2: 构建应用

```bash
mvn clean package -DskipTests
```

### 步骤3: 运行应用

```bash
# 开发环境
java -jar target/excel-api-1.0.0.jar

# 或生产环境配置
java -jar target/excel-api-1.0.0.jar --spring.profiles.active=prod
```

### 步骤4: 验证

访问 http://localhost:18081/admin/login

---

## 方式2: 完整Docker部署（推荐用于生产）

### 步骤1: 一键构建和启动

```bash
# Windows
docker-build.bat

# Linux/Mac
./docker-build.sh
```

### 步骤2: 验证

```bash
# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

访问 http://localhost:18081/admin/login

---

## 方式3: 手动Docker部署

### 步骤1: 构建

```bash
# 1. Maven构建
mvn clean package -DskipTests

# 2. Docker镜像构建
docker build -t excel-api:latest .
```

### 步骤2: 启动所有服务

```bash
docker-compose up -d
```

### 步骤3: 查看日志

```bash
# 查看所有日志
docker-compose logs -f

# 仅查看应用日志
docker-compose logs -f excel-api

# 仅查看Redis日志
docker-compose logs -f redis
```

---

## 常见启动错误及解决方案

### 错误1: Redis连接失败 ❌

**错误信息**:
```
Unable to connect to Redis server: localhost/127.0.0.1:6379
RedisConnectionException
```

**原因**: Redis服务未启动

**解决方案**:
```bash
# 启动Redis
docker-compose up -d redis

# 验证Redis
docker exec excel-redis redis-cli ping

# 查看Redis日志
docker-compose logs redis
```

**如果仍失败**:
```bash
# 重启Redis
docker-compose restart redis

# 或完全重建
docker-compose down
docker-compose up -d redis
```

---

### 错误2: 端口被占用 ❌

**错误信息**:
```
Web server failed to start. Port 18081 was already in use.
```

**解决方案**:

**检查端口占用**:
```bash
# Windows
netstat -ano | findstr :18081
netstat -ano | findstr :6379

# 查看进程
tasklist | findstr <PID>
```

**停止占用端口的进程**:
```bash
# 停止Docker服务
docker-compose down

# 或强制结束进程
taskkill /PID <进程ID> /F
```

**修改端口**:
```yaml
# application.yml 或通过参数
java -jar excel-api-1.0.0.jar --server.port=8080
```

---

### 错误3: Maven构建失败 ❌

**错误信息**:
```
Failed to execute goal ... compilation failure
```

**解决方案**:
```bash
# 清理后重新构建
mvn clean

# 强制更新依赖
mvn clean package -U -DskipTests

# 如果还是失败，删除本地仓库缓存
# Windows: C:\Users\你的用户名\.m2\repository
# Linux/Mac: ~/.m2/repository
```

---

### 错误4: Docker构建失败 ❌

**错误信息**:
```
ERROR: failed to solve
failed to fetch anonymous token
```

**解决方案**: 配置Docker镜像加速器

参考 [START_HERE.md](START_HERE.md) 或 [DOCKER_NETWORK_FIX.md](DOCKER_NETWORK_FIX.md)

---

### 错误5: 数据库初始化失败 ❌

**错误信息**:
```
Failed to configure a DataSource
H2 database initialization failed
```

**解决方案**:
```bash
# 删除旧的数据库文件
rm -rf data/

# 重新启动
docker-compose down -v
docker-compose up -d
```

---

## 启动脚本使用

### start.bat（本地开发）

自动启动Redis和应用：

```bash
# 运行
start.bat

# 功能：
# 1. 检查Redis运行状态
# 2. 如果未运行，自动启动Redis
# 3. 询问是否重新编译
# 4. 启动Spring Boot应用
```

### docker-build.bat（Docker部署）

一键Docker部署：

```bash
# 运行
docker-build.bat

# 功能：
# 1. Maven构建jar包
# 2. 构建Docker镜像
# 3. 启动Docker Compose
# 4. 健康检查验证
# 5. 显示访问地址
```

---

## 环境验证

### 验证Docker环境

```bash
# Docker版本
docker --version
docker-compose --version

# Docker运行状态
docker ps

# Docker网络
docker network ls

# Docker卷
docker volume ls
```

### 验证Java环境

```bash
# Java版本（需要17+）
java -version

# Maven版本
mvn --version
```

### 验证Redis

```bash
# 检查Redis容器
docker ps | findstr redis

# 测试Redis连接
docker exec excel-redis redis-cli ping

# 查看Redis信息
docker exec excel-redis redis-cli INFO server

# 查看Redis数据
docker exec excel-redis redis-cli KEYS "*"
```

### 验证应用

```bash
# 健康检查
curl http://localhost:18081/api/excel/health

# 或浏览器访问
# http://localhost:18081/admin/login
```

---

## 完整的启动流程

### 首次启动（全新安装）

```bash
# 1. 克隆/下载代码
cd excel-api

# 2. 配置Docker镜像加速器（中国大陆用户）
# 参考 START_HERE.md

# 3. Maven构建
mvn clean package -DskipTests

# 4. 启动Redis
docker-compose up -d redis

# 5. 等待Redis就绪
timeout /t 5

# 6. 启动应用
java -jar target/excel-api-1.0.0.jar

# 或使用Docker
docker-compose up -d
```

### 日常开发启动

```bash
# 方式1: 使用启动脚本（推荐）
start.bat

# 方式2: 手动启动
docker-compose up -d redis
java -jar target/excel-api-1.0.0.jar

# 方式3: 使用Maven插件
mvn spring-boot:run
```

### 生产部署启动

```bash
# 方式1: 使用部署脚本（推荐）
docker-build.bat

# 方式2: 手动部署
mvn clean package -DskipTests
docker build -t excel-api:latest .
docker-compose up -d

# 方式3: 使用已构建的jar
java -jar excel-api-1.0.0.jar \
  --spring.profiles.active=prod \
  --server.port=18081
```

---

## 停止应用

### 停止本地运行

```bash
# Ctrl + C 停止应用

# 停止Redis（可选）
docker-compose stop redis
```

### 停止Docker服务

```bash
# 停止所有服务
docker-compose down

# 停止但保留数据
docker-compose stop

# 停止并删除数据卷（⚠️ 会清除所有数据）
docker-compose down -v
```

---

## 日志查看

### 本地运行日志

```bash
# 应用日志（控制台输出）
# 直接在运行窗口查看

# 日志文件
tail -f logs/excel-api.log

# Windows
type logs\excel-api.log
```

### Docker日志

```bash
# 查看所有日志
docker-compose logs

# 实时查看
docker-compose logs -f

# 查看特定服务
docker-compose logs -f excel-api
docker-compose logs -f redis

# 查看最近50行
docker-compose logs --tail=50 excel-api
```

---

## 性能优化建议

### JVM参数优化

```bash
java -jar excel-api-1.0.0.jar \
  -Xms512m \              # 初始堆内存
  -Xmx1024m \             # 最大堆内存
  -XX:+UseG1GC \          # 使用G1垃圾回收器
  -XX:MaxGCPauseMillis=200 \  # GC最大暂停时间
  --spring.profiles.active=prod
```

### Docker资源限制

```yaml
# docker-compose.yml
services:
  excel-api:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 1G
        reservations:
          cpus: '0.5'
          memory: 512M
```

---

## 故障恢复

### 完全重置（清除所有数据）

```bash
# ⚠️ 警告：会删除所有数据

# 1. 停止所有服务
docker-compose down -v

# 2. 删除数据目录
rm -rf data/
rm -rf logs/
rm -rf excel-files/
rm -rf excel-temp/

# 3. 清理Docker
docker system prune -f

# 4. 重新构建
mvn clean package -DskipTests

# 5. 重新启动
docker-compose up -d --build
```

### 保留数据的重启

```bash
# 1. 停止服务
docker-compose down

# 2. 重新启动
docker-compose up -d

# 数据卷会自动挂载
```

---

## 快速命令参考

```bash
# === 构建相关 ===
mvn clean package -DskipTests           # Maven构建
docker build -t excel-api:latest .     # Docker镜像构建

# === 启动相关 ===
docker-compose up -d                    # 启动所有服务
docker-compose up -d redis              # 仅启动Redis
docker-compose up -d excel-api          # 仅启动应用
java -jar target/excel-api-1.0.0.jar   # 本地运行

# === 状态查看 ===
docker-compose ps                       # 服务状态
docker-compose logs -f                  # 查看日志
docker ps                               # 容器列表
netstat -ano | findstr :18081          # 端口占用

# === 测试相关 ===
curl http://localhost:18081/api/excel/health     # 健康检查
docker exec excel-redis redis-cli ping           # 测试Redis

# === 停止相关 ===
docker-compose down                     # 停止所有服务
docker-compose stop                     # 暂停服务
docker-compose restart                  # 重启服务

# === 清理相关 ===
docker-compose down -v                  # 停止并删除数据
docker system prune -f                  # 清理Docker缓存
mvn clean                               # 清理Maven构建
```

---

## 诊断工具

```bash
# Redis连接诊断
diagnose-redis.bat

# 查看完整配置
docker exec excel-api env

# 查看JVM信息
docker exec excel-api java -version

# 查看应用进程
docker exec excel-api ps aux
```

---

## 相关文档

- [START_HERE.md](START_HERE.md) - Docker快速开始（中国大陆用户）
- [REDIS_CONNECTION_FIX.md](REDIS_CONNECTION_FIX.md) - Redis连接问题详解
- [MAVEN_PROFILE_GUIDE.md](MAVEN_PROFILE_GUIDE.md) - Maven使用指南
- [DOCKER_NETWORK_FIX.md](DOCKER_NETWORK_FIX.md) - Docker网络问题
- [README.md](README.md) - 完整项目文档

---

**记住这个启动顺序：Redis → 构建 → 运行 → 验证**  🚀

启动问题？运行 `diagnose-redis.bat` 进行自动诊断！

