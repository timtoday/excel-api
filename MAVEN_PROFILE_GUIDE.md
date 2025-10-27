# Maven Profile 使用指南 📦

## 快速回答

**问：需要使用 `mvn -P prod` 吗？**  
**答：不需要！直接用 `mvn clean package -DskipTests` 即可。**

---

## Maven Profile vs Spring Profile

### 核心区别

```
┌─────────────────────────────────────────────────┐
│                                                 │
│  Maven Profile                                  │
│  ├─ 构建时决定                                   │
│  ├─ 影响：依赖、插件、资源文件                     │
│  └─ 用法：mvn -P prod clean package             │
│                                                 │
│  Spring Profile                                 │
│  ├─ 运行时决定                                   │
│  ├─ 影响：配置文件、Bean加载                      │
│  └─ 用法：--spring.profiles.active=prod        │
│                                                 │
└─────────────────────────────────────────────────┘
```

### 对比表

| 特性 | Maven Profile | Spring Profile |
|------|--------------|---------------|
| **何时生效** | 构建时 (mvn) | 运行时 (java) |
| **配置位置** | pom.xml | application.yml |
| **主要用途** | 控制依赖、插件 | 控制配置、Bean |
| **是否必需** | ❌ 可选 | ✅ 推荐使用 |
| **本项目** | 已添加但非必须 | 已配置并使用 |

---

## 本项目的配置

### 1. Spring Profile（已配置，推荐使用）✅

**配置文件**:
- `application.yml` - 默认/开发环境
- `application-prod.yml` - 生产环境

**运行时指定**:
```bash
# 开发环境（默认）
java -jar excel-api-1.0.0.jar

# 生产环境
java -jar excel-api-1.0.0.jar --spring.profiles.active=prod

# Docker环境（通过环境变量）
docker run -e SPRING_PROFILES_ACTIVE=prod excel-api
```

### 2. Maven Profile（已添加，但非必须）

**pom.xml 配置**:
```xml
<profiles>
    <!-- 开发环境（默认激活） -->
    <profile>
        <id>dev</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
    </profile>

    <!-- 生产环境 -->
    <profile>
        <id>prod</id>
    </profile>
</profiles>
```

**使用方式**:
```bash
# 方式1：不指定profile（推荐，默认dev）
mvn clean package -DskipTests

# 方式2：显式指定profile
mvn -P dev clean package -DskipTests
mvn -P prod clean package -DskipTests
```

**注意**：两种方式构建出的jar文件是**完全相同**的，因为我们的Maven Profile只是一个标记，实际配置在Spring Profile中。

---

## 使用场景

### 场景1: 标准开发（推荐）✅

```bash
# 1. 构建（不指定Maven Profile）
mvn clean package -DskipTests

# 2. 本地运行开发环境
java -jar target/excel-api-1.0.0.jar

# 3. 或运行生产环境
java -jar target/excel-api-1.0.0.jar --spring.profiles.active=prod
```

### 场景2: 使用Maven Profile

```bash
# 1. 构建生产版本
mvn -P prod clean package -DskipTests

# 2. 运行（效果与场景1完全相同）
java -jar target/excel-api-1.0.0.jar --spring.profiles.active=prod
```

### 场景3: Docker部署（推荐）✅

```bash
# 1. 构建jar（不需要-P参数）
mvn clean package -DskipTests

# 2. 构建Docker镜像
docker build -t excel-api:latest .

# 3. 启动（docker-compose.yml中已配置SPRING_PROFILES_ACTIVE=prod）
docker-compose up -d
```

---

## 何时需要Maven Profile？

### ✅ 需要使用的情况

1. **不同环境需要不同的依赖**
   ```xml
   <profile>
       <id>prod</id>
       <dependencies>
           <!-- 生产环境专用依赖 -->
       </dependencies>
   </profile>
   ```

2. **不同环境使用不同的插件配置**
   ```xml
   <profile>
       <id>prod</id>
       <build>
           <plugins>
               <!-- 生产环境打包时压缩资源 -->
           </plugins>
       </build>
   </profile>
   ```

3. **不同环境需要不同的资源过滤**
   ```xml
   <profile>
       <id>prod</id>
       <build>
           <resources>
               <!-- 过滤特定文件 -->
           </resources>
       </build>
   </profile>
   ```

### ❌ 不需要使用的情况（本项目）

1. **只是配置不同** → 使用Spring Profile（application-prod.yml）
2. **数据库地址不同** → 使用Spring Profile + 环境变量
3. **Redis地址不同** → 使用Spring Profile + 环境变量
4. **日志级别不同** → 使用Spring Profile

---

## 最佳实践

### ✅ 推荐方式

```bash
# 构建：简单直接
mvn clean package -DskipTests

# 运行：根据需要指定Spring Profile
java -jar app.jar                              # 开发
java -jar app.jar --spring.profiles.active=prod  # 生产
```

**优点**:
- ✅ 简单明了
- ✅ 一次构建，到处运行
- ✅ 符合12-Factor App原则
- ✅ 易于Docker化

### ⚠️ 不推荐方式

```bash
# 不推荐：构建时就决定环境
mvn -P prod clean package -DskipTests

# 问题：构建的jar只能用于生产环境
# 如果想在开发环境测试，需要重新构建
```

---

## 常见问题

### Q1: 为什么pom.xml中加了Maven Profile但不使用？

**A**: 为了兼容性和灵活性。虽然添加了Maven Profile，但实际上：
- Spring Boot应用主要通过Spring Profile控制配置
- 同一个jar可以在不同环境运行（通过Spring Profile切换）
- Maven Profile只在需要构建差异时才必须使用

### Q2: `mvn -P prod` 和不加 `-P prod` 有什么区别？

**A**: 在本项目中，**没有实质区别**：
```bash
# 方式1（默认）
mvn clean package -DskipTests
# 结果：excel-api-1.0.0.jar

# 方式2（显式指定）
mvn -P prod clean package -DskipTests
# 结果：excel-api-1.0.0.jar（完全相同）
```

两者构建的jar文件完全相同，因为Maven Profile中只定义了属性，没有改变构建行为。

### Q3: Docker构建应该用哪个命令？

**A**: 使用标准命令即可：
```bash
# Dockerfile 或 docker-build.bat 中
mvn clean package -DskipTests

# 不需要：
# mvn -P prod clean package -DskipTests
```

Spring Profile在`docker-compose.yml`中通过环境变量指定：
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=prod
```

### Q4: 如何查看当前激活的Maven Profile？

```bash
mvn help:active-profiles

# 输出示例：
# Active Profiles for Project 'com.excel:excel-api':
#   dev (source: pom.xml)
```

### Q5: 能否同时使用多个Profile？

```bash
# Maven Profile: 可以
mvn -P prod,docker clean package

# Spring Profile: 可以
java -jar app.jar --spring.profiles.active=prod,docker
```

---

## 项目文件说明

### 构建相关

| 文件 | 命令 | Maven Profile | Spring Profile |
|------|------|--------------|---------------|
| **docker-build.bat** | `mvn clean package -DskipTests` | ❌ 不使用 | ✅ 通过环境变量 |
| **start.bat** | `mvn spring-boot:run` | ❌ 不使用 | ✅ 可选参数 |
| **pom.xml** | 已添加但非必须 | ✅ dev/prod | - |

### 配置文件

| 文件 | 用途 | 何时生效 |
|------|------|----------|
| **application.yml** | 默认配置 | 始终加载 |
| **application-prod.yml** | 生产配置 | profile=prod时 |

---

## 命令速查表

### 构建

```bash
# 标准构建（推荐）
mvn clean package -DskipTests

# 使用Maven Profile构建（效果相同）
mvn -P dev clean package -DskipTests
mvn -P prod clean package -DskipTests

# 跳过测试
mvn clean package -DskipTests

# 强制更新依赖
mvn clean package -U -DskipTests
```

### 运行

```bash
# 开发环境（默认）
java -jar target/excel-api-1.0.0.jar

# 生产环境
java -jar target/excel-api-1.0.0.jar --spring.profiles.active=prod

# 指定端口
java -jar target/excel-api-1.0.0.jar --server.port=8080

# 组合使用
java -jar target/excel-api-1.0.0.jar \
  --spring.profiles.active=prod \
  --server.port=18081
```

### Docker

```bash
# 本地构建jar
mvn clean package -DskipTests

# 构建Docker镜像
docker build -t excel-api:latest .

# 运行（已配置SPRING_PROFILES_ACTIVE=prod）
docker-compose up -d

# 查看配置
docker exec excel-api env | findstr SPRING
```

---

## 总结

| 场景 | 推荐命令 | 说明 |
|------|---------|------|
| **本地开发** | `mvn clean package -DskipTests` | 简单直接 ✅ |
| **Docker构建** | `mvn clean package -DskipTests` | 与本地一致 ✅ |
| **生产部署** | `java -jar app.jar --spring.profiles.active=prod` | 运行时指定 ✅ |
| **使用Maven Profile** | `mvn -P prod clean package -DskipTests` | 可用但非必须 ⚠️ |

**核心理念**: 一次构建（Build Once），到处运行（Run Anywhere），通过配置适配环境。

---

**记住**：对于Spring Boot项目，Maven Profile是**可选的**，Spring Profile才是**必需的**！🎯

