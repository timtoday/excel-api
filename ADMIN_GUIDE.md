## 📚 Web管理后台使用指南

本项目提供了一个简洁易用的Web管理后台，用于管理Excel文件、API Token和查看请求日志。

---

## 🚀 快速开始

### 1. 启动服务

```bash
# 使用Maven启动
mvn spring-boot:run

# 或使用启动脚本
./start.sh  # Linux/Mac
start.bat   # Windows
```

### 2. 访问管理后台

打开浏览器访问：**http://localhost:18081/admin/login**

### 3. 登录

默认账号（可在`application.yml`中配置）：

| 账号 | 密码 | 权限 |
|------|------|------|
| admin | admin123 | 管理员 |
| user | user123 | 普通用户 |

---

## 🎯 功能模块

### 📊 控制台 (Dashboard)

**访问路径**: `/admin/dashboard`

**功能说明**:
- 总览统计信息
  - Token总数和活跃数
  - 总请求数
- 最近10条请求记录快速查看

![控制台](docs/images/dashboard.png)

---

### 📁 Excel文件管理

**访问路径**: `/admin/files`

**功能列表**:

#### 1. 上传Excel文件
- 支持`.xlsx`和`.xls`格式
- 拖拽上传或选择文件
- 自动保存到配置的存储目录

#### 2. 文件列表
- 显示所有已上传的Excel文件
- 查看文件大小和最后修改时间
- 文件总数和总大小统计

#### 3. 文件操作
- **下载**: 直接下载Excel文件
- **删除**: 删除不需要的文件（需确认）

**使用示例**:
```bash
# 命令行上传（可选）
curl -X POST "http://localhost:18081/api/excel/upload" \
  -H "X-API-Token: YOUR_TOKEN" \
  -F "file=@template.xlsx"
```

---

### 🔑 Token管理

**访问路径**: `/admin/tokens`

**功能说明**: 管理API访问令牌，所有API请求都需要有效的Token

#### 1. 创建Token

**必填信息**:
- **Token名称**: 便于识别的名称（如："前端应用"）
- **描述**: Token的用途说明（可选）
- **有效期**: 默认30天，可自定义（1-365天）

**创建后**:
- 系统生成唯一的Token字符串（格式：`tk_xxxxx`）
- ⚠️ **重要**: Token只显示一次，请立即复制保存！
- 建议将Token保存到安全的地方

#### 2. Token列表

显示所有Token的详细信息：
- 名称和描述
- 创建者
- 创建时间和过期时间
- 使用次数统计
- 当前状态（活跃/已禁用）

#### 3. Token操作

- **启用/禁用**: 快速控制Token的可用状态
- **删除**: 永久删除Token（需确认）

**最佳实践**:
- 为不同的应用或环境创建不同的Token
- 定期检查和清理不用的Token
- Token即将过期时提前创建新Token

---

### 📝 请求日志

**访问路径**: `/admin/logs`

**功能说明**: 查看和分析API请求记录

#### 1. 统计概览

- **总请求数**: 所有API调用次数
- **成功请求**: HTTP 200状态码的请求
- **失败请求**: 非200状态码的请求

#### 2. 日志列表

显示最近100条请求记录：
- 请求时间
- HTTP方法（GET/POST等）
- 请求路径
- 使用的Token名称
- 响应状态码
- 处理耗时（毫秒）
- 客户端IP地址

#### 3. 日志详情

点击"查看"按钮可展开详细信息：
- 请求体内容
- 响应体内容
- 错误信息（如果有）

#### 4. 清空日志

- 点击"清空日志"按钮清除所有记录
- 需要确认操作
- 清空后统计数据重置

**使用场景**:
- 调试API调用问题
- 分析接口使用情况
- 审计API访问记录
- 排查性能问题

---

### 🧪 API测试

**访问路径**: `/admin/test`

**功能说明**: 在线测试API接口，无需编写代码

#### 1. 基础配置

- **选择Token**: 从下拉列表选择活跃的Token
- **选择接口**: 现在支持**所有8个API接口**，分为4大类：

**📝 Excel操作**:
- 写入Excel (`/api/excel/write`)
- 读取Excel (`/api/excel/read`)
- 写入并读取 (`/api/excel/operation`)

**📁 文件管理**:
- 上传文件 (`/api/excel/upload`)
- 下载文件 (`/api/excel/download/{fileName}`)

**📄 模板功能**:
- 模板生成 (`/api/excel/template/generate`)
- 模板填充并下载 (`/api/excel/template/fill-and-download`)

**🔧 系统**:
- 健康检查 (`/api/excel/health`)

#### 2. 测试用例

- **选择文件**: 从已上传的Excel文件中选择（自动填充到请求中）
- **文件上传**: 上传接口显示文件选择器
- **文件下载**: 下载接口显示文件名输入框
- **编辑请求**: JSON格式的请求体（自动适应不同接口）
- **加载示例**: 自动填充符合接口规范的示例数据

#### 3. 发送请求

- 点击"🚀 发送请求"按钮
- 实时显示响应结果
- 显示状态码和响应时间

#### 4. 响应查看

- 状态信息（成功/失败）
- 响应时间统计
- 格式化的JSON响应

**快速测试示例**:

1. **写入测试**:
```json
{
  "fileName": "test.xlsx",
  "sheetName": "Sheet1",
  "cells": [
    {"cellAddress": "A1", "value": "Hello", "valueType": "STRING"},
    {"cellAddress": "B1", "value": 100, "valueType": "NUMBER"}
  ]
}
```

2. **读取测试**:
```json
{
  "fileName": "test.xlsx",
  "sheetName": "Sheet1",
  "cells": [
    {"cellAddress": "A1"},
    {"cellAddress": "B1"}
  ]
}
```

3. **公式计算测试**:
```json
{
  "fileName": "test.xlsx",
  "writeRequest": {
    "sheetName": "Sheet1",
    "cells": [
      {"cellAddress": "A1", "value": 10, "valueType": "NUMBER"},
      {"cellAddress": "B1", "value": 20, "valueType": "NUMBER"},
      {"cellAddress": "C1", "value": "A1+B1", "valueType": "FORMULA"}
    ]
  },
  "readRequest": {
    "sheetName": "Sheet1",
    "cells": [{"cellAddress": "C1"}]
  }
}
```

---

## 🔒 API Token使用

### 在代码中使用Token

#### 方式1: HTTP Header（推荐）

```bash
curl -X POST "http://localhost:18081/api/excel/write" \
  -H "X-API-Token: tk_your_token_here" \
  -H "Content-Type: application/json" \
  -d '{...}'
```

```python
# Python
import requests

headers = {
    'X-API-Token': 'tk_your_token_here',
    'Content-Type': 'application/json'
}

response = requests.post('http://localhost:18081/api/excel/write', 
                        headers=headers, 
                        json=data)
```

```javascript
// JavaScript
fetch('http://localhost:18081/api/excel/write', {
  method: 'POST',
  headers: {
    'X-API-Token': 'tk_your_token_here',
    'Content-Type': 'application/json'
  },
  body: JSON.stringify(data)
});
```

#### 方式2: Authorization Header

```bash
curl -X POST "http://localhost:18081/api/excel/write" \
  -H "Authorization: Bearer tk_your_token_here" \
  -H "Content-Type: application/json" \
  -d '{...}'
```

#### 方式3: URL参数（不推荐，仅用于测试）

```bash
curl -X POST "http://localhost:18081/api/excel/write?token=tk_your_token_here" \
  -H "Content-Type: application/json" \
  -d '{...}'
```

---

## ⚙️ 配置说明

### application.yml配置

```yaml
# 管理后台用户配置
admin:
  users:
    - username: admin
      password: admin123
      role: ADMIN
    - username: developer
      password: dev123
      role: USER
  
  # Token配置
  tokens:
    default-expiry-days: 30    # 默认有效期
    max-tokens-per-user: 10    # 每个用户最大Token数
```

### 修改管理员账号

编辑`src/main/resources/application.yml`:

```yaml
admin:
  users:
    - username: myusername      # 自定义用户名
      password: mypassword      # 自定义密码
      role: ADMIN
```

重启服务后生效。

---

## 🔧 常见问题

### Q1: 忘记了管理员密码怎么办？

**A:** 修改`application.yml`中的密码配置，重启服务即可。

### Q2: API返回401错误

**A:** 检查：
1. Token是否已创建
2. Token是否处于活跃状态
3. Token是否已过期
4. 请求头中Token格式是否正确

### Q3: Token过期了怎么办？

**A:** Token过期后需要：
1. 登录管理后台
2. 创建新的Token
3. 更新应用中的Token配置

### Q4: 如何查看API调用是否成功？

**A:** 
1. 访问"请求日志"页面
2. 查找对应的请求记录
3. 查看状态码和详细信息

### Q5: 支持多个用户同时登录吗？

**A:** 支持。每个用户可以独立登录和操作。

---

## 🛡️ 安全建议

### 1. 修改默认密码

生产环境务必修改默认的admin密码：

```yaml
admin:
  users:
    - username: admin
      password: your_strong_password_here  # 使用强密码
      role: ADMIN
```

### 2. Token管理

- ✅ 为不同环境使用不同Token
- ✅ 定期轮换Token
- ✅ 及时删除不用的Token
- ❌ 不要在URL中传递Token
- ❌ 不要将Token提交到代码仓库

### 3. HTTPS部署

生产环境建议启用HTTPS：

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: your_password
    key-store-type: PKCS12
```

### 4. 访问控制

- 限制管理后台的访问IP
- 使用防火墙规则
- 启用请求频率限制

---

## 📊 监控和运维

### 查看运行状态

```bash
# 健康检查
curl http://localhost:18081/api/excel/health

# 查看应用信息
curl http://localhost:18081/actuator/health
```

### 日志位置

- **应用日志**: `./logs/excel-api.log`
- **请求日志**: 管理后台在线查看
- **Excel文件**: `./excel-files/`
- **临时文件**: `./excel-temp/`

### 性能优化

1. **定期清理日志**: 避免日志文件过大
2. **清理临时文件**: 定期清理`excel-temp`目录
3. **Token清理**: 删除过期的Token

---

## 🎯 最佳实践

### 开发环境

1. 使用默认账号快速测试
2. 创建测试专用Token
3. 使用API测试工具调试接口

### 生产环境

1. 修改所有默认密码
2. 为每个应用创建独立Token
3. 启用HTTPS
4. 配置日志持久化
5. 定期备份Excel文件
6. 监控API使用情况

---

## 📚 相关文档

- [快速开始](QUICK_START.md)
- [API使用示例](API_EXAMPLES.md)
- [API测试指南](API_TEST_GUIDE.md) 🆕
- [模板功能指南](TEMPLATE_GUIDE.md)
- [完整文档](README.md)

---

**开始使用Web管理后台，让Excel API管理更简单！** 🎉

