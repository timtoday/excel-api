# Docker快速开始 🚀

## 5分钟部署 Excel API

### 前提条件

✅ 已安装 Java 17+  
✅ 已安装 Maven 3.6+  
✅ 已安装 Docker & Docker Compose

### 一键部署

#### Windows用户

```cmd
docker-build.bat
```

#### Linux/Mac用户

```bash
chmod +x docker-build.sh
./docker-build.sh
```

### 脚本做了什么？

```
📦 步骤 1/4: Maven构建jar包
  ↓
🐳 步骤 2/4: 构建Docker镜像 (~200MB)
  ↓
📊 步骤 3/4: 显示镜像信息
  ↓
🚀 步骤 4/4: 启动Docker Compose
  ↓
🏥 健康检查: 验证服务运行正常
  ↓
🎉 部署成功！显示访问地址
```

### 立即访问

部署成功后，可访问：

| 服务 | 地址 | 用途 |
|------|------|------|
| **Web管理后台** | http://localhost:18081/admin/login | 文件管理、Token管理、API测试 |
| **H2数据库控制台** | http://localhost:18081/h2-console | 查看数据库 |
| **API文档** | http://localhost:18081/swagger-ui.html | 接口文档 |
| **健康检查** | http://localhost:18081/api/excel/health | 服务状态 |

**默认管理员账号**:
- 用户名: `admin`
- 密码: `admin123`

### 手动部署（可选）

如果不想用脚本，可以手动执行：

```bash
# 1. Maven构建
mvn clean package -DskipTests

# 2. 构建Docker镜像
docker build -t excel-api:latest .

# 3. 启动服务
docker-compose up -d

# 4. 查看日志
docker-compose logs -f excel-api

# 5. 验证服务
curl http://localhost:18081/api/excel/health
```

### 常用命令

```bash
# 查看运行状态
docker-compose ps

# 查看实时日志
docker-compose logs -f

# 重启服务
docker-compose restart

# 停止服务
docker-compose down

# 完全清理（包括数据卷）
docker-compose down -v
```

### 服务架构

```
┌─────────────────────────────────┐
│  宿主机 localhost:18081          │
│                                 │
│  ┌──────────────────────────┐  │
│  │  Docker Compose          │  │
│  │                          │  │
│  │  ┌──────────────────┐    │  │
│  │  │  excel-api       │    │  │
│  │  │  (Alpine ~200MB) │    │  │
│  │  │  - Web UI        │    │  │
│  │  │  - REST API      │    │  │
│  │  │  - H2 Database   │    │  │
│  │  └────────┬─────────┘    │  │
│  │           │              │  │
│  │           │ redis:6379   │  │
│  │           ↓              │  │
│  │  ┌──────────────────┐    │  │
│  │  │  redis           │    │  │
│  │  │  (分布式锁)       │    │  │
│  │  └──────────────────┘    │  │
│  │                          │  │
│  └──────────────────────────┘  │
└─────────────────────────────────┘
```

### 数据持久化

Docker Compose会自动创建以下数据卷：

| 数据卷 | 路径 | 说明 |
|--------|------|------|
| **excel-files** | `/data/excel-files` | Excel文件存储 |
| **excel-backups** | `/data/excel-backups` | 文件版本备份 |
| **h2-database** | `/data` | H2数据库文件 |
| **logs** | `/app/logs` | 应用日志 |

**重要**: 执行 `docker-compose down` 不会删除数据卷。如需完全清理，使用 `docker-compose down -v`。

### 性能指标

| 指标 | 数值 |
|------|------|
| **镜像大小** | ~200MB |
| **启动时间** | 15-30秒 |
| **内存占用** | ~256-512MB |
| **构建时间** | 2-3分钟（含Maven） |
| **Docker构建** | 10-30秒 |

### 故障排查

#### 端口被占用

```bash
# Windows查看端口占用
netstat -ano | findstr :18081

# 修改端口（编辑 docker-compose.yml）
ports:
  - "18082:18081"  # 改用18082
```

#### 服务启动失败

```bash
# 查看详细日志
docker-compose logs excel-api

# 常见原因：
# 1. jar文件不存在 -> 先执行 mvn package
# 2. 端口被占用 -> 修改端口映射
# 3. Redis连接失败 -> 检查redis容器状态
```

#### Maven构建失败

```bash
# 清理后重新构建
mvn clean
mvn package -DskipTests -U
```

#### Redis连接问题

```bash
# 检查Redis状态
docker-compose ps redis

# 重启Redis
docker-compose restart redis

# 查看Redis日志
docker-compose logs redis
```

### 环境变量配置

可以创建 `.env` 文件自定义配置：

```properties
# 端口配置
SERVER_PORT=18081

# Redis配置
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=

# 管理员密码
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin123

# 日志级别
LOGGING_LEVEL=INFO
```

### 下一步

1. **登录管理后台**: http://localhost:18081/admin/login
2. **创建API Token**: 进入"Token管理"创建访问令牌
3. **上传Excel文件**: 进入"文件管理"上传测试文件
4. **在线测试API**: 进入"API测试"测试接口

### 详细文档

- [README.md](README.md) - 完整项目文档
- [DOCKERFILE_OPTIMIZATION.md](DOCKERFILE_OPTIMIZATION.md) - Docker优化详情
- [API_TEST_GUIDE.md](API_TEST_GUIDE.md) - API使用指南
- [ADMIN_GUIDE.md](ADMIN_GUIDE.md) - 管理后台使用指南

---

**5分钟完成部署，立即开始使用！** 🎉

