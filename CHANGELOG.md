# 更新日志

## [1.3.1] - 2024-10-24

### 🔧 配置变更

#### 端口调整
- **应用端口**: `8080` → `18081`
- **原因**: 避免与其他服务端口冲突，使用更标准的自定义应用端口

**影响范围**:
- 所有Web访问地址
- API调用端点
- Docker端口映射
- 健康检查URL

**访问地址更新**:
- Web管理后台: http://localhost:18081/admin/login
- H2数据库控制台: http://localhost:18081/h2-console
- API文档: http://localhost:18081/swagger-ui.html
- 健康检查: http://localhost:18081/api/excel/health

#### Docker Redis配置优化
- **Redis连接方式**: 使用Docker内部服务名 `redis`
- **配置方式**: 通过环境变量 `REDIS_HOST=redis`
- **优势**: 
  - 利用Docker Compose内部网络
  - 自动服务发现
  - 无需配置IP地址

**docker-compose.yml配置**:
```yaml
environment:
  - REDIS_HOST=redis  # Docker内部服务名
  - REDIS_PORT=6379
```

### 🐳 Dockerfile优化

#### 镜像优化
- **去除多阶段构建**: 使用本地Maven构建，Docker只负责打包运行
- **基础镜像**: `eclipse-temurin:17-jre-alpine`
- **镜像大小**: ~500MB+ → **~200MB** (减少60%+)
- **构建速度**: 5-10分钟 → **10-30秒** (提升20倍+)

#### JVM参数优化
```dockerfile
-XX:+UseContainerSupport      # 容器内存感知
-XX:MaxRAMPercentage=75.0     # 限制堆内存为容器的75%
-XX:+UseG1GC                   # G1垃圾回收器
-XX:+UseStringDeduplication   # 字符串去重
```

#### 新增构建脚本
- **docker-build.sh** (Linux/Mac): 一键构建和部署
- **docker-build.bat** (Windows): 一键构建和部署
- **.dockerignore**: 优化构建上下文

**脚本功能**:
1. Maven构建jar包
2. Docker镜像构建
3. 启动Docker Compose
4. 健康检查验证
5. 显示访问地址

**使用方法**:
```bash
# Linux/Mac
./docker-build.sh

# Windows
docker-build.bat
```

### 📝 文档更新
- 所有文档中的端口引用已更新为18081
- API示例全部更新
- Docker配置示例已更新
- 新增 [PORT_UPDATE.md](PORT_UPDATE.md) - 端口更新完整指南
- 新增 [PORT_QUICK_SUMMARY.md](PORT_QUICK_SUMMARY.md) - 端口更新快速总结
- 新增 [DOCKERFILE_OPTIMIZATION.md](DOCKERFILE_OPTIMIZATION.md) - Dockerfile优化详细说明
- 更新 [README.md](README.md) - Docker部署章节全面改写

### ⚠️ 升级注意事项

**本地开发**:
1. 拉取最新代码
2. 重新编译: `mvn clean package -DskipTests`
3. 使用新端口访问: `http://localhost:18081`

**Docker部署**:
1. 停止容器: `docker-compose down`
2. 重新构建: `docker-compose up -d --build`
3. Redis自动使用内部网络连接

**客户端更新**:
- 更新API调用的基础URL端口为18081
- 更新浏览器书签
- 更新Nginx反向代理配置（如有）

---

## [1.3.0] - 2024-10-24

### ✨ 新增功能

#### 💾 数据库持久化
重大功能升级：**Token和请求日志实现数据库持久化**，重启后数据不再丢失！

**核心特性**:
- ✅ **Token持久化**: 所有API Token存储到数据库
- ✅ **日志持久化**: 所有请求日志存储到数据库
- ✅ **H2嵌入式数据库**: 无需额外安装配置
- ✅ **H2 Web控制台**: 可视化查看数据库内容
- ✅ **自动表结构管理**: JPA自动创建和更新表
- ✅ **数据迁移支持**: 轻松切换到MySQL/PostgreSQL

**技术实现**:
- 使用Spring Data JPA进行数据持久化
- H2数据库文件存储在 `./data/` 目录
- 所有CRUD操作自动持久化
- Token使用统计实时更新

**数据库表**:
- `api_tokens` - 存储所有Token信息
- `request_logs` - 存储所有请求日志

**H2控制台**:
- 访问地址: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/excel-api`
- 用户名: `sa`
- 密码: (留空)

**新增文档**:
- 📖 [数据库持久化说明](DATABASE_PERSISTENCE.md) - 完整的持久化使用指南

### 🔧 技术变更

#### 修改的类
- `TokenService`: 使用 `ApiTokenRepository` 替代 `ConcurrentHashMap`
- `RequestLogService`: 使用 `RequestLogRepository` 替代 `ConcurrentLinkedQueue`
- `ApiToken`: 添加JPA实体注解 `@Entity`、`@Table`、`@Column`
- `RequestLog`: 添加JPA实体注解和索引

#### 新增的类
- `ApiTokenRepository`: Token的JPA Repository接口
- `RequestLogRepository`: 请求日志的JPA Repository接口

#### 新增依赖
- `spring-boot-starter-data-jpa`
- `com.h2database:h2`

### 📝 配置变更

#### application.yml
```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/excel-api;AUTO_SERVER=TRUE
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: update
```

#### .gitignore
```
# H2 Database files
data/
*.db
*.trace.db
```

### ⚠️ 重要说明

1. **首次启动**: 会自动创建 `./data/` 目录和数据库文件
2. **数据迁移**: 如果从旧版本升级，之前内存中的Token和日志会丢失
3. **备份建议**: 定期备份 `./data/` 目录
4. **日志清理**: 建议定期清理旧日志，避免数据库过大

### ✅ 向后兼容
- API接口完全兼容，无需修改客户端代码
- Web管理后台功能保持不变
- 只是数据存储方式从内存改为数据库

---

## [1.2.2] - 2024-10-24

### 🐛 Bug修复

#### 修复组合操作接口的参数验证问题
- **问题**: 组合操作接口提示"文件名不能为空"，即使已经传了 `fileName`
- **原因**: Spring的 `@Valid` 在方法执行前进行校验，而自动传递逻辑在方法内部
- **修复**:
  - 移除了 `ExcelWriteRequest` 和 `ExcelReadRequest` 中 `fileName` 的 `@NotBlank` 注解
  - 在 `/write` 和 `/read` 接口中添加了手动验证
  - `/operation` 接口自动传递外层 `fileName`，无需内部重复指定
- **影响**: 组合操作接口现在可以正常工作，只需在外层指定 `fileName`

### ✨ 新增功能

#### 📚 多Sheet支持
重大功能升级：支持在单次请求中同时操作**多个Sheet**！

**核心特性**:
- ✅ **单次写入多个Sheet**: 一个请求可以向多个Sheet写入数据
- ✅ **单次读取多个Sheet**: 一个请求可以从多个Sheet读取数据
- ✅ **跨Sheet组合操作**: 写入和读取可以分别操作不同的Sheet
- ✅ **灵活指定方式**: 
  - 外层指定默认 `sheetName`
  - 单元格级别可单独指定 `sheetName`
  - 单元格级别优先于外层默认值

**API变更**:
- `ExcelWriteRequest.CellData` 新增可选字段 `sheetName`
- `ExcelReadRequest.CellPosition` 新增可选字段 `sheetName`
- `ExcelResponse.CellResult` 新增字段 `sheetName`（响应中标识来源Sheet）
- 外层的 `sheetName` 变为可选，作为默认值使用

**使用示例**:
```json
{
  "fileName": "report.xlsx",
  "sheetName": "销售数据",
  "cells": [
    {
      "cellAddress": "A1",
      "value": "产品",
      "valueType": "STRING"
    },
    {
      "sheetName": "统计汇总",
      "cellAddress": "B1",
      "value": "=销售数据!A1",
      "valueType": "FORMULA"
    }
  ]
}
```

**应用场景**:
- 跨Sheet公式计算
- 多Sheet数据汇总
- 同时填充多个Sheet的模板
- 批量更新整个工作簿

**新增文档**:
- 📖 [多Sheet操作指南](MULTI_SHEET_GUIDE.md) - 详细的多Sheet使用教程和案例

### 🔄 改进优化
- 日志输出增强：显示操作涉及的Sheet数量
- 错误提示优化：明确指出哪个单元格缺少Sheet信息
- Web测试工具示例更新：所有示例都展示多Sheet功能

### ✅ 向后兼容
- 完全兼容旧版本的单Sheet操作
- 现有代码无需修改即可继续使用

---

## [1.2.1] - 2024-10-24

### ✨ 新增功能

#### 🧪 完整API测试工具
扩展了Web管理后台的API测试功能，现在支持**所有8个API接口**：

**新增支持的接口**:
- ✅ **文件上传** (`/api/excel/upload`)
  - 支持multipart文件上传
  - 文件类型验证（.xlsx, .xls）
  
- ✅ **文件下载** (`/api/excel/download/{fileName}`)
  - 指定文件名下载
  - 自动触发浏览器下载

- ✅ **模板生成** (`/api/excel/template/generate`)
  - 基于模板生成新文件
  - 保存到服务器并返回文件名

- ✅ **模板填充并下载** (`/api/excel/template/fill-and-download`)
  - 基于模板生成文件
  - 直接下载到浏览器

- ✅ **健康检查** (`/api/excel/health`)
  - 无需Token
  - 检查服务状态

**UI增强**:
- 📝 接口按类型分组（Excel操作、文件管理、模板功能、系统）
- 🎯 自适应UI：根据接口类型显示/隐藏相应输入区域
- 📁 文件上传接口显示文件选择器
- 💾 文件下载接口显示文件名输入框
- 🔄 选择已上传文件自动填充到请求中
- ⏱️ 显示请求方法、URL和耗时

**新增文档**:
- 📖 [API测试指南](API_TEST_GUIDE.md) - 详细的测试工具使用教程

### 🐛 Bug修复
- 修正示例数据中的字段名称（`type` → `valueType`）
- 修正读取接口示例（`positions` → `cells`）
- 修正组合操作示例结构（添加完整的`writeRequest`和`readRequest`）
- 修正模板接口示例（`data` → `cells`）
- 修复组合操作接口的参数验证问题（自动传递外层`fileName`到内部请求）

### 📝 文档更新
- 更新 `README.md` 中关于API测试工具的说明
- 更新 `ADMIN_GUIDE.md` 添加8个接口的详细说明
- 新增 `API_TEST_GUIDE.md` 完整测试指南

---

## [1.2.0] - 2024-10-24

### ✨ 新增功能

#### 🌐 Web管理后台
新增完整的Web管理后台系统：

**核心功能**:
- **用户认证系统**
  - 基于YAML配置的用户管理
  - Spring Security安全认证
  - 支持多用户登录
  - 默认账号：admin/admin123, user/user123

- **Excel文件管理**
  - 在线上传Excel文件（支持.xlsx和.xls）
  - 文件列表展示（显示大小、修改时间）
  - 文件下载和删除操作
  - 文件统计信息

- **API Token管理**
  - 创建Token（自定义名称、描述、有效期）
  - Token列表（显示状态、使用次数、创建者）
  - 启用/禁用Token
  - 删除Token
  - Token自动过期

- **请求日志查看**
  - 实时记录所有API请求
  - 显示请求方法、路径、状态码、耗时
  - 查看请求/响应详情
  - 统计信息（总请求、成功/失败数）
  - 清空日志功能

- **在线API测试工具**
  - 选择Token和接口类型
  - 选择已上传的Excel文件
  - 编辑JSON请求体
  - 自动加载示例数据
  - 实时查看响应结果

#### 🔒 Token认证系统
- API请求必须携带有效Token
- 支持3种Token传递方式：
  - `X-API-Token` Header（推荐）
  - `Authorization: Bearer` Header
  - URL参数（仅测试）
- Token自动验证和统计
- Token过期检查

#### 📝 请求日志系统
- 自动记录所有API调用
- 记录内容：
  - 请求时间、方法、路径
  - Token信息
  - 状态码和响应时间
  - 请求体和响应体
  - 客户端IP
  - 错误信息
- 最多保留1000条日志
- 支持清空操作

### 🎨 用户界面
- 现代化的响应式设计
- 清晰的导航菜单
- 实时数据统计卡片
- 友好的操作提示
- 移动端适配

### 📚 新增文档
- **ADMIN_GUIDE.md** - Web管理后台完整使用指南
  - 功能模块详细说明
  - 操作步骤截图指引
  - Token使用示例
  - 常见问题解答
  - 安全建议和最佳实践

### 🔧 技术实现
- Spring Security用户认证
- Thymeleaf服务端模板渲染
- Token过滤器实现API认证
- 请求日志拦截器
- 内存存储（Token和日志）

### 📝 配置更新
- 新增管理后台用户配置
- 新增Token配置项
- Thymeleaf模板配置
- Spring Security配置

---

## [1.1.0] - 2024-10-24

### ✨ 新增功能

#### 🎯 Excel模板输出功能
新增基于模板批量生成Excel文件的强大功能：

- **下载接口** (`GET /api/excel/download/{fileName}`)
- **模板生成接口** (`POST /api/excel/template/generate`)
- **模板填充下载接口** (`POST /api/excel/template/fill-and-download`)

#### 📚 新增文档
- **TEMPLATE_GUIDE.md** - Excel模板功能完整使用指南
- **templates/README_TEMPLATES.md** - 模板示例说明

---

## [1.0.0] - 2024-10-24

### 🚀 首次发布

#### 核心功能
- Excel读写操作
- 公式自动计算
- 并发控制（读写锁）
- 分布式锁支持（Redis）
- 版本控制和自动备份

---

## 🎯 使用说明

### Web管理后台

1. **启动服务**
```bash
mvn spring-boot:run
```

2. **访问管理后台**
```
http://localhost:8080/admin/login
账号：admin / admin123
```

3. **创建Token**
- 登录后台 → Token管理 → 创建新Token
- 复制生成的Token

4. **调用API**
```bash
curl -X POST "http://localhost:8080/api/excel/write" \
  -H "X-API-Token: YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{...}'
```

### API Token使用

```python
# Python示例
import requests

headers = {
    'X-API-Token': 'tk_your_token_here',
    'Content-Type': 'application/json'
}

response = requests.post(
    'http://localhost:8080/api/excel/write',
    headers=headers,
    json=data
)
```

---

## ⚠️ 重要变更

### 从1.1.0升级到1.2.0

**必须的操作**:
1. 所有API调用需要添加Token认证
2. 登录Web管理后台创建Token
3. 更新客户端代码添加Token Header

**配置更新**:
```yaml
# 需要在application.yml添加
admin:
  users:
    - username: admin
      password: admin123
      role: ADMIN
```

**兼容性**:
- ⚠️ API接口现在需要Token认证
- ✅ 其他功能向后兼容
- ✅ 数据结构无变化

---

## 🔒 安全建议

1. **修改默认密码**（必须！）
```yaml
admin:
  users:
    - username: admin
      password: your_strong_password  # 修改这里
      role: ADMIN
```

2. **Token管理**
- 为不同环境使用不同Token
- 定期轮换Token
- 及时删除不用的Token

3. **生产部署**
- 启用HTTPS
- 配置防火墙
- 限制管理后台访问IP

---

## 📝 升级指南

### 步骤1: 更新依赖

```xml
<!-- pom.xml 已自动更新 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

### 步骤2: 配置用户

编辑`application.yml`:
```yaml
admin:
  users:
    - username: yourusername
      password: yourpassword
      role: ADMIN
```

### 步骤3: 创建Token

1. 启动服务
2. 访问 http://localhost:8080/admin/login
3. 登录并创建Token

### 步骤4: 更新客户端

```python
# 旧版本（不需要Token）
response = requests.post(url, json=data)

# 新版本（需要Token）
headers = {'X-API-Token': 'tk_xxx'}
response = requests.post(url, headers=headers, json=data)
```

---

## 🐛 已知问题

- 日志和Token存储在内存中，服务重启后会丢失
  - 计划：v1.3.0支持数据库持久化

---

## 🔮 未来计划

### v1.3.0 (计划中)
- [ ] Token和日志数据库持久化
- [ ] Token权限细分（读/写权限）
- [ ] API调用频率限制
- [ ] 更详细的统计图表

### v1.4.0 (计划中)
- [ ] 用户角色权限管理
- [ ] 操作审计日志
- [ ] 邮件通知功能
- [ ] Webhook集成

---

## 📚 相关文档

- **[Web管理后台使用指南](ADMIN_GUIDE.md)** 🆕
- [模板功能指南](TEMPLATE_GUIDE.md)
- [快速开始](QUICK_START.md)
- [API示例](API_EXAMPLES.md)
- [完整文档](README.md)

---

**感谢使用 Excel API Service！** 🎉
