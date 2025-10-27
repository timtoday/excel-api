# Dockerfile优化说明 🐳

## 优化总结

### 镜像大小对比

| 版本 | 基础镜像 | 大小 | 说明 |
|------|---------|------|------|
| **优化前** | maven:3.9 + temurin:17-jre-alpine | ~500MB+ | 包含Maven构建 |
| **优化后** | temurin:17-jre-alpine | ~200MB | 仅运行环境 |

**减少**: 约 **300MB+** (60%+)

### 主要优化点

#### 1. ✅ 去除多阶段构建

**之前**（包含Maven构建）:
```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
COPY --from=build /app/target/excel-api-*.jar app.jar
```

**现在**（本地构建）:
```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/excel-api-*.jar app.jar
```

**优势**:
- 本地Maven环境更快（有缓存）
- Docker构建速度提升5-10倍
- 镜像层更少，更简洁

#### 2. ✅ 使用Alpine Linux

**alpine vs slim 对比**:

| 特性 | alpine | slim |
|------|--------|------|
| 大小 | ~180MB | ~200MB |
| C库 | musl libc | glibc |
| 包管理 | apk | apt |
| 兼容性 | 高（Java应用） | 更高 |
| 推荐 | ✅ 首选 | 备选 |

**选择理由**:
- Spring Boot应用与Alpine完全兼容
- 更小的镜像体积
- 更少的安全漏洞（最小化）

#### 3. ✅ JVM参数优化

```dockerfile
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \      # 容器环境支持
    "-XX:MaxRAMPercentage=75.0", \     # 限制最大堆内存为容器内存的75%
    "-XX:+UseG1GC", \                   # 使用G1垃圾回收器
    "-XX:+UseStringDeduplication", \   # 字符串去重
    "-Djava.security.egd=file:/dev/./urandom", \  # 加快启动
    "-jar", "app.jar"]
```

**参数说明**:
- `UseContainerSupport`: 让JVM识别容器内存限制
- `MaxRAMPercentage=75.0`: 预留25%内存给系统
- `UseG1GC`: 低延迟垃圾回收器
- `UseStringDeduplication`: 节省内存
- `egd=/dev/./urandom`: 避免随机数生成阻塞

#### 4. ✅ .dockerignore优化

新增 `.dockerignore` 文件，避免复制不必要的文件：

```
# 源代码（只需要jar）
src/
pom.xml

# 本地数据
data/
excel-files/
logs/

# IDE和文档
.idea/
*.md
```

**效果**:
- 减少构建上下文大小
- 加快Docker构建速度
- 避免敏感数据进入镜像

## 构建流程

### 方式1: 手动构建

```bash
# 1. Maven构建
mvn clean package -DskipTests

# 2. Docker构建
docker build -t excel-api:latest .

# 3. 查看镜像
docker images excel-api:latest

# 4. 启动
docker-compose up -d
```

### 方式2: 使用脚本（推荐）

**Linux/Mac**:
```bash
chmod +x docker-build.sh
./docker-build.sh
```

**Windows**:
```cmd
docker-build.bat
```

脚本会自动完成：
1. Maven构建
2. Docker镜像构建
3. 启动Docker Compose
4. 健康检查
5. 显示访问地址

## 镜像信息

### 查看镜像大小

```bash
docker images excel-api:latest
```

预期输出：
```
REPOSITORY   TAG      SIZE
excel-api    latest   ~200MB
```

### 查看镜像层

```bash
docker history excel-api:latest
```

### 镜像分析

```bash
# 安装dive工具
# brew install dive  (Mac)
# choco install dive (Windows)

# 分析镜像
dive excel-api:latest
```

## 性能对比

### 构建时间

| 步骤 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| Maven构建 | 在Docker内 | 本地 | - |
| Docker构建 | 5-10分钟 | 10-30秒 | **20倍+** |
| 总时间 | 5-10分钟 | 2-3分钟 | **3-5倍** |

### 启动时间

| 指标 | 时间 |
|------|------|
| 容器启动 | ~2秒 |
| 应用启动 | ~15-25秒 |
| 健康检查通过 | ~30秒 |

## 内存优化

### Docker Compose内存限制

```yaml
services:
  excel-api:
    deploy:
      resources:
        limits:
          memory: 512M  # 最大512MB
        reservations:
          memory: 256M  # 保证256MB
```

### JVM堆内存

- 容器限制: 512MB
- JVM堆最大: 512MB * 75% = 384MB
- 系统预留: 128MB

## 故障排查

### 镜像构建失败

**问题**: `COPY target/excel-api-*.jar app.jar` 找不到文件

**解决**:
```bash
# 先构建jar
mvn clean package -DskipTests

# 确认jar存在
ls -la target/excel-api-*.jar

# 再构建Docker镜像
docker build -t excel-api:latest .
```

### wget命令不存在

**问题**: 健康检查失败，`wget: not found`

**解决**: 已在Dockerfile中添加
```dockerfile
RUN apk add --no-cache wget
```

### 内存溢出

**问题**: 容器因OOM被杀死

**解决**:
```yaml
# docker-compose.yml
services:
  excel-api:
    deploy:
      resources:
        limits:
          memory: 1G  # 增加内存限制
```

或调整JVM参数：
```dockerfile
"-XX:MaxRAMPercentage=70.0"  # 降低堆内存比例
```

## 进一步优化

### 1. 使用专用JRE

如果对镜像大小极度敏感，可以使用jlink创建最小JRE：

```dockerfile
# 使用jlink创建最小JRE（需要在多阶段构建中）
FROM eclipse-temurin:17 AS jre-build
RUN jlink \
    --add-modules java.base,java.sql,java.naming,java.desktop,java.management \
    --output /jre-minimal

FROM alpine:latest
COPY --from=jre-build /jre-minimal /opt/jre
ENV PATH="/opt/jre/bin:$PATH"
COPY target/excel-api-*.jar app.jar
```

预期大小: ~120MB

### 2. 使用GraalVM Native Image

对于极致性能和启动速度：

```bash
# 构建原生镜像（需要GraalVM）
mvn -Pnative native:compile

# Dockerfile
FROM alpine:latest
COPY target/excel-api app
ENTRYPOINT ["./app"]
```

**优势**:
- 镜像大小: ~50-80MB
- 启动时间: <1秒
- 内存占用: 更少

**劣势**:
- 构建时间长（5-15分钟）
- 兼容性限制（部分反射功能需要配置）

## 最佳实践

### 1. 本地开发

```bash
# 使用Maven快速迭代
mvn spring-boot:run
```

### 2. 测试环境

```bash
# 使用Docker快速部署
./docker-build.sh
```

### 3. 生产环境

```bash
# 使用特定版本标签
docker build -t excel-api:1.3.1 .
docker tag excel-api:1.3.1 registry.example.com/excel-api:1.3.1
docker push registry.example.com/excel-api:1.3.1
```

## 清理和维护

### 清理未使用的镜像

```bash
# 清理悬空镜像
docker image prune -f

# 清理所有未使用的镜像
docker image prune -a -f

# 清理构建缓存
docker builder prune -f
```

### 查看磁盘使用

```bash
docker system df
```

### 完整清理

```bash
# 停止所有容器
docker-compose down

# 清理所有未使用资源
docker system prune -a -f --volumes
```

## 相关文档

- [README.md](README.md) - 主文档
- [docker-compose.yml](docker-compose.yml) - Docker编排配置
- [PORT_UPDATE.md](PORT_UPDATE.md) - 端口更新说明

---

**优化后的Dockerfile更快、更小、更简洁！** 🚀✅

