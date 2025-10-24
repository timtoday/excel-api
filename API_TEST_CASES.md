# 📋 Excel API 完整接口列表与测试用例

## 目录
- [REST API 接口](#rest-api-接口)
  - [Excel 操作接口](#excel-操作接口)
  - [文件管理接口](#文件管理接口)
  - [模板功能接口](#模板功能接口)
  - [系统接口](#系统接口)
- [Web 管理后台接口](#web-管理后台接口)
- [测试环境准备](#测试环境准备)
- [Postman 测试集合](#postman-测试集合)

---

## REST API 接口

### 认证说明
所有 REST API 请求都需要在 Header 中携带 Token：
```
X-API-Token: tk_your_token_here
或
Authorization: Bearer tk_your_token_here
```

### Excel 操作接口

#### 1. 写入 Excel
**接口地址**: `POST /api/excel/write`

**请求头**:
```
Content-Type: application/json
X-API-Token: tk_your_token_here
```

**请求体**:
```json
{
  "fileName": "test.xlsx",
  "sheetName": "Sheet1",
  "cells": [
    {
      "cellAddress": "A1",
      "value": "姓名",
      "type": "STRING"
    },
    {
      "cellAddress": "B1",
      "value": "年龄",
      "type": "STRING"
    },
    {
      "cellAddress": "A2",
      "value": "张三",
      "type": "STRING"
    },
    {
      "cellAddress": "B2",
      "value": 25,
      "type": "NUMBER"
    },
    {
      "cellAddress": "C2",
      "value": "=B2*2",
      "type": "FORMULA"
    }
  ]
}
```

**测试用例**:
```bash
# 基本写入测试
curl -X POST http://localhost:8080/api/excel/write \
  -H "Content-Type: application/json" \
  -H "X-API-Token: tk_your_token_here" \
  -d '{
    "fileName": "test.xlsx",
    "sheetName": "Sheet1",
    "cells": [
      {"cellAddress": "A1", "value": "测试", "type": "STRING"},
      {"cellAddress": "B1", "value": 100, "type": "NUMBER"},
      {"cellAddress": "C1", "value": "=B1*2", "type": "FORMULA"}
    ]
  }'

# 布尔值测试
curl -X POST http://localhost:8080/api/excel/write \
  -H "Content-Type: application/json" \
  -H "X-API-Token: tk_your_token_here" \
  -d '{
    "fileName": "test.xlsx",
    "sheetName": "Sheet1",
    "cells": [
      {"cellAddress": "A1", "value": true, "type": "BOOLEAN"}
    ]
  }'

# 复杂公式测试
curl -X POST http://localhost:8080/api/excel/write \
  -H "Content-Type: application/json" \
  -H "X-API-Token: tk_your_token_here" \
  -d '{
    "fileName": "test.xlsx",
    "sheetName": "Sheet1",
    "cells": [
      {"cellAddress": "A1", "value": 10, "type": "NUMBER"},
      {"cellAddress": "A2", "value": 20, "type": "NUMBER"},
      {"cellAddress": "A3", "value": "=SUM(A1:A2)", "type": "FORMULA"}
    ]
  }'
```

**期望响应**:
```json
{
  "success": true,
  "message": "写入成功",
  "data": null,
  "timestamp": "2024-10-24T12:00:00"
}
```

---

#### 2. 读取 Excel
**接口地址**: `POST /api/excel/read`

**请求头**:
```
Content-Type: application/json
X-API-Token: tk_your_token_here
```

**请求体**:
```json
{
  "fileName": "test.xlsx",
  "sheetName": "Sheet1",
  "positions": [
    {"cellAddress": "A1"},
    {"cellAddress": "B1"},
    {"cellAddress": "C1"}
  ]
}
```

**测试用例**:
```bash
# 基本读取测试
curl -X POST http://localhost:8080/api/excel/read \
  -H "Content-Type: application/json" \
  -H "X-API-Token: tk_your_token_here" \
  -d '{
    "fileName": "test.xlsx",
    "sheetName": "Sheet1",
    "positions": [
      {"cellAddress": "A1"},
      {"cellAddress": "B1"},
      {"cellAddress": "C1"}
    ]
  }'

# 读取公式计算结果
curl -X POST http://localhost:8080/api/excel/read \
  -H "Content-Type: application/json" \
  -H "X-API-Token: tk_your_token_here" \
  -d '{
    "fileName": "test.xlsx",
    "sheetName": "Sheet1",
    "positions": [
      {"cellAddress": "A3"}
    ]
  }'

# 读取大范围单元格
curl -X POST http://localhost:8080/api/excel/read \
  -H "Content-Type: application/json" \
  -H "X-API-Token: tk_your_token_here" \
  -d '{
    "fileName": "test.xlsx",
    "sheetName": "Sheet1",
    "positions": [
      {"cellAddress": "A1"},
      {"cellAddress": "A2"},
      {"cellAddress": "A3"},
      {"cellAddress": "A4"},
      {"cellAddress": "A5"}
    ]
  }'
```

**期望响应**:
```json
{
  "success": true,
  "message": "读取成功",
  "data": {
    "A1": "测试",
    "B1": 100.0,
    "C1": 200.0
  },
  "timestamp": "2024-10-24T12:00:00"
}
```

---

#### 3. 同时写入和读取
**接口地址**: `POST /api/excel/operation`

**请求头**:
```
Content-Type: application/json
X-API-Token: tk_your_token_here
```

**请求体**:
```json
{
  "fileName": "test.xlsx",
  "sheetName": "Sheet1",
  "writeCells": [
    {
      "cellAddress": "A1",
      "value": 10,
      "type": "NUMBER"
    },
    {
      "cellAddress": "A2",
      "value": 20,
      "type": "NUMBER"
    },
    {
      "cellAddress": "A3",
      "value": "=A1+A2",
      "type": "FORMULA"
    }
  ],
  "readPositions": [
    {"cellAddress": "A3"}
  ]
}
```

**测试用例**:
```bash
# 写入数据并立即读取计算结果
curl -X POST http://localhost:8080/api/excel/operation \
  -H "Content-Type: application/json" \
  -H "X-API-Token: tk_your_token_here" \
  -d '{
    "fileName": "calc.xlsx",
    "sheetName": "Sheet1",
    "writeCells": [
      {"cellAddress": "A1", "value": 100, "type": "NUMBER"},
      {"cellAddress": "A2", "value": 200, "type": "NUMBER"},
      {"cellAddress": "A3", "value": "=A1+A2", "type": "FORMULA"}
    ],
    "readPositions": [
      {"cellAddress": "A3"}
    ]
  }'

# 复杂计算场景
curl -X POST http://localhost:8080/api/excel/operation \
  -H "Content-Type: application/json" \
  -H "X-API-Token: tk_your_token_here" \
  -d '{
    "fileName": "calc.xlsx",
    "sheetName": "计算",
    "writeCells": [
      {"cellAddress": "A1", "value": "价格", "type": "STRING"},
      {"cellAddress": "B1", "value": "数量", "type": "STRING"},
      {"cellAddress": "C1", "value": "总计", "type": "STRING"},
      {"cellAddress": "A2", "value": 50, "type": "NUMBER"},
      {"cellAddress": "B2", "value": 10, "type": "NUMBER"},
      {"cellAddress": "C2", "value": "=A2*B2", "type": "FORMULA"}
    ],
    "readPositions": [
      {"cellAddress": "C2"}
    ]
  }'
```

**期望响应**:
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "A3": 300.0
  },
  "timestamp": "2024-10-24T12:00:00"
}
```

---

### 文件管理接口

#### 4. 上传 Excel 文件
**接口地址**: `POST /api/excel/upload`

**请求头**:
```
Content-Type: multipart/form-data
X-API-Token: tk_your_token_here
```

**表单参数**:
- `file`: Excel 文件（必填）
- `fileName`: 自定义文件名（可选）

**测试用例**:
```bash
# 基本上传
curl -X POST http://localhost:8080/api/excel/upload \
  -H "X-API-Token: tk_your_token_here" \
  -F "file=@/path/to/your/file.xlsx"

# 上传并指定文件名
curl -X POST http://localhost:8080/api/excel/upload \
  -H "X-API-Token: tk_your_token_here" \
  -F "file=@/path/to/your/file.xlsx" \
  -F "fileName=custom_name.xlsx"

# Windows PowerShell 示例
Invoke-RestMethod -Uri "http://localhost:8080/api/excel/upload" `
  -Method Post `
  -Headers @{"X-API-Token"="tk_your_token_here"} `
  -Form @{file=Get-Item "C:\path\to\file.xlsx"}
```

**期望响应**:
```json
{
  "success": true,
  "message": "上传成功: test.xlsx",
  "data": null,
  "timestamp": "2024-10-24T12:00:00"
}
```

---

#### 5. 下载 Excel 文件
**接口地址**: `GET /api/excel/download/{fileName}`

**请求头**:
```
X-API-Token: tk_your_token_here
```

**路径参数**:
- `fileName`: 文件名（必填）

**测试用例**:
```bash
# 下载文件
curl -X GET http://localhost:8080/api/excel/download/test.xlsx \
  -H "X-API-Token: tk_your_token_here" \
  -o downloaded_file.xlsx

# 使用浏览器访问（需要先登录并获取Token）
http://localhost:8080/api/excel/download/test.xlsx?token=tk_your_token_here

# Windows PowerShell 下载
Invoke-WebRequest -Uri "http://localhost:8080/api/excel/download/test.xlsx" `
  -Headers @{"X-API-Token"="tk_your_token_here"} `
  -OutFile "downloaded.xlsx"
```

**期望响应**:
- 成功: 返回 Excel 文件（二进制流）
- 失败: 返回 404 或错误信息

---

### 模板功能接口

#### 6. 基于模板生成 Excel（返回文件名）
**接口地址**: `POST /api/excel/template/generate`

**请求头**:
```
Content-Type: application/json
X-API-Token: tk_your_token_here
```

**请求体**:
```json
{
  "templateFileName": "template.xlsx",
  "outputFileName": "output_2024.xlsx",
  "sheetName": "Sheet1",
  "data": [
    {
      "cellAddress": "A1",
      "value": "2024年度报告",
      "type": "STRING"
    },
    {
      "cellAddress": "B5",
      "value": 12345.67,
      "type": "NUMBER"
    }
  ]
}
```

**测试用例**:
```bash
# 基于模板生成报告
curl -X POST http://localhost:8080/api/excel/template/generate \
  -H "Content-Type: application/json" \
  -H "X-API-Token: tk_your_token_here" \
  -d '{
    "templateFileName": "report_template.xlsx",
    "outputFileName": "monthly_report_2024_10.xlsx",
    "sheetName": "报表",
    "data": [
      {"cellAddress": "B2", "value": "2024年10月", "type": "STRING"},
      {"cellAddress": "C5", "value": 50000, "type": "NUMBER"},
      {"cellAddress": "C6", "value": 30000, "type": "NUMBER"}
    ]
  }'

# 不指定输出文件名（自动生成）
curl -X POST http://localhost:8080/api/excel/template/generate \
  -H "Content-Type: application/json" \
  -H "X-API-Token: tk_your_token_here" \
  -d '{
    "templateFileName": "template.xlsx",
    "sheetName": "Sheet1",
    "data": [
      {"cellAddress": "A1", "value": "测试数据", "type": "STRING"}
    ]
  }'
```

**期望响应**:
```json
{
  "success": true,
  "message": "模板生成成功",
  "data": {
    "fileName": "output_2024.xlsx",
    "downloadUrl": "/api/excel/download/output_2024.xlsx"
  },
  "timestamp": "2024-10-24T12:00:00"
}
```

---

#### 7. 填充模板并直接下载
**接口地址**: `POST /api/excel/template/fill-and-download`

**请求头**:
```
Content-Type: application/json
X-API-Token: tk_your_token_here
```

**请求体**:
```json
{
  "templateFileName": "template.xlsx",
  "sheetName": "Sheet1",
  "data": [
    {
      "cellAddress": "A1",
      "value": "即时报告",
      "type": "STRING"
    }
  ]
}
```

**测试用例**:
```bash
# 填充模板并下载
curl -X POST http://localhost:8080/api/excel/template/fill-and-download \
  -H "Content-Type: application/json" \
  -H "X-API-Token: tk_your_token_here" \
  -d '{
    "templateFileName": "invoice_template.xlsx",
    "sheetName": "发票",
    "data": [
      {"cellAddress": "B3", "value": "张三", "type": "STRING"},
      {"cellAddress": "B4", "value": "2024-10-24", "type": "STRING"},
      {"cellAddress": "D10", "value": 1000, "type": "NUMBER"}
    ]
  }' \
  -o invoice_filled.xlsx
```

**期望响应**:
- 直接返回填充后的 Excel 文件（二进制流）

---

### 系统接口

#### 8. 健康检查
**接口地址**: `GET /api/excel/health`

**请求头**: 无需 Token

**测试用例**:
```bash
# 健康检查
curl -X GET http://localhost:8080/api/excel/health

# 使用浏览器访问
http://localhost:8080/api/excel/health
```

**期望响应**:
```json
{
  "success": true,
  "message": "服务运行正常",
  "data": {
    "status": "UP",
    "version": "1.0.0",
    "timestamp": "2024-10-24T12:00:00"
  },
  "timestamp": "2024-10-24T12:00:00"
}
```

---

## Web 管理后台接口

### 页面访问接口

#### 1. 登录页面
**接口地址**: `GET /admin/login`

**访问方式**:
```
浏览器访问: http://localhost:8080/admin/login
```

**默认账号**:
- 管理员: `admin` / `admin123`
- 普通用户: `user` / `user123`

---

#### 2. 控制台首页
**接口地址**: `GET /admin/dashboard` 或 `GET /admin/`

**访问方式**:
```
浏览器访问: http://localhost:8080/admin/dashboard
```

**需要登录**: 是

**功能**:
- 查看 Token 统计
- 查看请求统计
- 查看最近请求日志

---

#### 3. Excel 文件管理
**接口地址**: `GET /admin/files`

**访问方式**:
```
浏览器访问: http://localhost:8080/admin/files
```

**需要登录**: 是

**功能**:
- 查看所有 Excel 文件列表
- 上传 Excel 文件
- 下载 Excel 文件
- 删除 Excel 文件

---

#### 4. Token 管理
**接口地址**: `GET /admin/tokens`

**访问方式**:
```
浏览器访问: http://localhost:8080/admin/tokens
```

**需要登录**: 是

**功能**:
- 查看所有 Token
- 创建新 Token
- 启用/禁用 Token
- 删除 Token

---

#### 5. 请求日志
**接口地址**: `GET /admin/logs`

**访问方式**:
```
浏览器访问: http://localhost:8080/admin/logs
```

**需要登录**: 是

**功能**:
- 查看最近 100 条请求日志
- 查看请求统计信息
- 清空日志

---

#### 6. API 测试
**接口地址**: `GET /admin/test`

**访问方式**:
```
浏览器访问: http://localhost:8080/admin/test
```

**需要登录**: 是

**功能**:
- 在线测试所有 API 接口
- 选择 Token
- 选择文件
- 发送请求并查看响应

---

### 管理后台操作接口

#### 7. 文件上传（Web 表单）
**接口地址**: `POST /admin/files/upload`

**表单参数**:
- `file`: Excel 文件

**测试用例**:
```bash
# 通过 Web 表单上传（需要登录后的 Cookie）
curl -X POST http://localhost:8080/admin/files/upload \
  -H "Cookie: JSESSIONID=your_session_id" \
  -F "file=@/path/to/file.xlsx"
```

---

#### 8. 文件删除
**接口地址**: `POST /admin/files/delete`

**表单参数**:
- `filename`: 文件名

**测试用例**:
```bash
# 通过 Web 表单删除（需要登录后的 Cookie）
curl -X POST http://localhost:8080/admin/files/delete \
  -H "Cookie: JSESSIONID=your_session_id" \
  -d "filename=test.xlsx"
```

---

#### 9. 创建 Token
**接口地址**: `POST /admin/tokens/create`

**表单参数**:
- `name`: Token 名称
- `description`: Token 描述
- `expiryDays`: 过期天数（可选）

**测试用例**:
```bash
# 创建 Token（需要登录后的 Cookie）
curl -X POST http://localhost:8080/admin/tokens/create \
  -H "Cookie: JSESSIONID=your_session_id" \
  -d "name=测试Token&description=用于API测试&expiryDays=30"
```

---

#### 10. 启用/禁用 Token
**接口地址**: `POST /admin/tokens/toggle`

**表单参数**:
- `token`: Token 值

---

#### 11. 删除 Token
**接口地址**: `POST /admin/tokens/delete`

**表单参数**:
- `token`: Token 值

---

#### 12. 清空日志
**接口地址**: `POST /admin/logs/clear`

**测试用例**:
```bash
# 清空日志（需要登录后的 Cookie）
curl -X POST http://localhost:8080/admin/logs/clear \
  -H "Cookie: JSESSIONID=your_session_id"
```

---

## 测试环境准备

### 1. 获取 API Token

**方法一：通过 Web 管理后台**
1. 访问 http://localhost:8080/admin/login
2. 登录：`admin` / `admin123`
3. 进入"Token 管理"页面
4. 点击"创建新 Token"
5. 填写信息并提交
6. 复制生成的 Token

**方法二：使用默认 Token（如果有）**
```bash
# 查看配置文件中是否有预设的 Token
cat src/main/resources/application.yml
```

### 2. 准备测试文件

**创建测试 Excel 文件**:
```bash
# 上传一个基础的 Excel 文件作为测试
# 或者通过 API 创建新文件
```

**创建模板文件**:
1. 在 Excel 中创建一个模板文件
2. 在需要填充的位置留空或使用占位符
3. 上传到系统中

### 3. 环境变量设置

**设置 Token 环境变量** (方便测试):
```bash
# Linux/Mac
export API_TOKEN="tk_your_token_here"

# Windows PowerShell
$env:API_TOKEN="tk_your_token_here"

# 使用环境变量
curl -X GET http://localhost:8080/api/excel/health \
  -H "X-API-Token: $API_TOKEN"
```

---

## Postman 测试集合

### 创建 Postman Collection

1. **创建 Environment**
```json
{
  "name": "Excel API - Local",
  "values": [
    {
      "key": "base_url",
      "value": "http://localhost:8080",
      "enabled": true
    },
    {
      "key": "api_token",
      "value": "tk_your_token_here",
      "enabled": true
    }
  ]
}
```

2. **Collection 结构**
```
Excel API Tests/
├── 01. 系统/
│   └── Health Check
├── 02. Excel 操作/
│   ├── Write Excel
│   ├── Read Excel
│   └── Write and Read
├── 03. 文件管理/
│   ├── Upload File
│   └── Download File
└── 04. 模板功能/
    ├── Generate from Template
    └── Fill and Download
```

3. **Pre-request Script** (添加到 Collection 级别):
```javascript
// 自动添加 Token 到 Header
pm.request.headers.add({
    key: 'X-API-Token',
    value: pm.environment.get('api_token')
});
```

4. **Tests Script** (添加到 Collection 级别):
```javascript
// 检查响应状态
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

// 检查响应格式
pm.test("Response has success field", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('success');
});

// 检查响应时间
pm.test("Response time is less than 5000ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(5000);
});
```

---

## 完整测试脚本

### Bash 测试脚本

```bash
#!/bin/bash

# 配置
BASE_URL="http://localhost:8080"
API_TOKEN="tk_your_token_here"

echo "======================================"
echo "Excel API 接口测试"
echo "======================================"

# 1. 健康检查
echo "\n[1/8] 测试健康检查接口..."
curl -s -X GET "$BASE_URL/api/excel/health" | jq

# 2. 写入 Excel
echo "\n[2/8] 测试写入 Excel..."
curl -s -X POST "$BASE_URL/api/excel/write" \
  -H "Content-Type: application/json" \
  -H "X-API-Token: $API_TOKEN" \
  -d '{
    "fileName": "test.xlsx",
    "sheetName": "Sheet1",
    "cells": [
      {"cellAddress": "A1", "value": "测试", "type": "STRING"},
      {"cellAddress": "B1", "value": 100, "type": "NUMBER"}
    ]
  }' | jq

# 3. 读取 Excel
echo "\n[3/8] 测试读取 Excel..."
curl -s -X POST "$BASE_URL/api/excel/read" \
  -H "Content-Type: application/json" \
  -H "X-API-Token: $API_TOKEN" \
  -d '{
    "fileName": "test.xlsx",
    "sheetName": "Sheet1",
    "positions": [
      {"cellAddress": "A1"},
      {"cellAddress": "B1"}
    ]
  }' | jq

# 4. 同时写入和读取
echo "\n[4/8] 测试同时写入和读取..."
curl -s -X POST "$BASE_URL/api/excel/operation" \
  -H "Content-Type: application/json" \
  -H "X-API-Token: $API_TOKEN" \
  -d '{
    "fileName": "calc.xlsx",
    "sheetName": "Sheet1",
    "writeCells": [
      {"cellAddress": "A1", "value": 10, "type": "NUMBER"},
      {"cellAddress": "A2", "value": 20, "type": "NUMBER"},
      {"cellAddress": "A3", "value": "=A1+A2", "type": "FORMULA"}
    ],
    "readPositions": [
      {"cellAddress": "A3"}
    ]
  }' | jq

# 5. 上传文件
echo "\n[5/8] 测试上传文件..."
# 需要有实际文件
# curl -s -X POST "$BASE_URL/api/excel/upload" \
#   -H "X-API-Token: $API_TOKEN" \
#   -F "file=@test.xlsx" | jq

# 6. 下载文件
echo "\n[6/8] 测试下载文件..."
curl -s -X GET "$BASE_URL/api/excel/download/test.xlsx" \
  -H "X-API-Token: $API_TOKEN" \
  -o downloaded_test.xlsx
echo "文件已下载到: downloaded_test.xlsx"

# 7. 模板生成
echo "\n[7/8] 测试模板生成..."
# 需要有模板文件
# curl -s -X POST "$BASE_URL/api/excel/template/generate" \
#   -H "Content-Type: application/json" \
#   -H "X-API-Token: $API_TOKEN" \
#   -d '{
#     "templateFileName": "template.xlsx",
#     "sheetName": "Sheet1",
#     "data": [
#       {"cellAddress": "A1", "value": "报告", "type": "STRING"}
#     ]
#   }' | jq

echo "\n======================================"
echo "测试完成！"
echo "======================================"
```

### PowerShell 测试脚本

```powershell
# Excel API 接口测试脚本

$baseUrl = "http://localhost:8080"
$apiToken = "tk_your_token_here"
$headers = @{"X-API-Token"=$apiToken}

Write-Host "======================================"
Write-Host "Excel API 接口测试"
Write-Host "======================================"

# 1. 健康检查
Write-Host "`n[1/8] 测试健康检查接口..."
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/excel/health" -Method Get
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "错误: $_"
}

# 2. 写入 Excel
Write-Host "`n[2/8] 测试写入 Excel..."
$writeBody = @{
    fileName = "test.xlsx"
    sheetName = "Sheet1"
    cells = @(
        @{cellAddress="A1"; value="测试"; type="STRING"},
        @{cellAddress="B1"; value=100; type="NUMBER"}
    )
} | ConvertTo-Json -Depth 10

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/excel/write" `
        -Method Post `
        -Headers $headers `
        -ContentType "application/json" `
        -Body $writeBody
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "错误: $_"
}

# 3. 读取 Excel
Write-Host "`n[3/8] 测试读取 Excel..."
$readBody = @{
    fileName = "test.xlsx"
    sheetName = "Sheet1"
    positions = @(
        @{cellAddress="A1"},
        @{cellAddress="B1"}
    )
} | ConvertTo-Json -Depth 10

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/excel/read" `
        -Method Post `
        -Headers $headers `
        -ContentType "application/json" `
        -Body $readBody
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "错误: $_"
}

Write-Host "`n======================================"
Write-Host "测试完成！"
Write-Host "======================================"
```

---

## 错误码说明

### 常见错误响应

**401 Unauthorized**
```json
{
  "success": false,
  "message": "Token无效或已过期",
  "timestamp": "2024-10-24T12:00:00"
}
```

**404 Not Found**
```json
{
  "success": false,
  "message": "文件不存在: test.xlsx",
  "timestamp": "2024-10-24T12:00:00"
}
```

**500 Internal Server Error**
```json
{
  "success": false,
  "message": "操作失败: 具体错误信息",
  "timestamp": "2024-10-24T12:00:00"
}
```

---

## 性能测试

### 并发测试

**使用 Apache Bench**:
```bash
# 测试健康检查接口
ab -n 1000 -c 10 http://localhost:8080/api/excel/health

# 测试读取接口（需要创建测试脚本）
ab -n 100 -c 5 -p read_request.json -T application/json \
  -H "X-API-Token: tk_your_token_here" \
  http://localhost:8080/api/excel/read
```

**使用 JMeter**:
1. 创建 Thread Group
2. 添加 HTTP Request Sampler
3. 配置并发用户数和循环次数
4. 添加 Listeners 查看结果

---

## 附录

### 快速测试命令

```bash
# 1. 创建并测试一个完整流程
TOKEN="tk_your_token_here"

# 写入数据
curl -X POST http://localhost:8080/api/excel/write \
  -H "Content-Type: application/json" \
  -H "X-API-Token: $TOKEN" \
  -d '{"fileName":"quick_test.xlsx","sheetName":"Sheet1","cells":[{"cellAddress":"A1","value":"Hello","type":"STRING"}]}'

# 读取数据
curl -X POST http://localhost:8080/api/excel/read \
  -H "Content-Type: application/json" \
  -H "X-API-Token: $TOKEN" \
  -d '{"fileName":"quick_test.xlsx","sheetName":"Sheet1","positions":[{"cellAddress":"A1"}]}'

# 下载文件
curl -X GET "http://localhost:8080/api/excel/download/quick_test.xlsx" \
  -H "X-API-Token: $TOKEN" \
  -o quick_test_downloaded.xlsx
```

### 常见问题

**Q: Token 在哪里获取？**
A: 登录 Web 管理后台 → Token 管理 → 创建新 Token

**Q: 文件保存在哪里？**
A: 默认保存在 `./excel-files/` 目录

**Q: 如何测试大文件？**
A: 调整配置文件中的 `max-file-size` 和 `max-request-size`

**Q: API 响应慢怎么办？**
A: 检查日志，可能是并发锁等待或公式计算复杂

---

## 文档更新日志

- 2024-10-24: 创建完整的 API 测试文档
- 包含所有 REST API 接口
- 包含 Web 管理后台接口
- 提供完整测试用例和脚本

**相关文档**:
- [API 使用示例](API_EXAMPLES.md)
- [快速开始](QUICK_START.md)
- [Web 管理后台指南](ADMIN_GUIDE.md)

