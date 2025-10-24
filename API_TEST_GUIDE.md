# API 测试指南

## 概述

Web管理后台的 `/admin/test` 页面现在支持测试所有API接口，包括Excel操作、文件管理、模板功能和系统接口。

## 支持的接口

### 📝 Excel 操作

#### 1. 写入Excel (`/api/excel/write`)
- **方法**: POST
- **需要Token**: 是
- **示例请求**:
```json
{
  "fileName": "test.xlsx",
  "sheetName": "Sheet1",
  "cells": [
    {"cellAddress": "A1", "value": "姓名", "valueType": "STRING"},
    {"cellAddress": "B1", "value": "分数", "valueType": "STRING"},
    {"cellAddress": "A2", "value": "张三", "valueType": "STRING"},
    {"cellAddress": "B2", "value": 85, "valueType": "NUMBER"},
    {"cellAddress": "B4", "value": "=AVERAGE(B2:B3)", "valueType": "FORMULA"}
  ]
}
```

#### 2. 读取Excel (`/api/excel/read`)
- **方法**: POST
- **需要Token**: 是
- **示例请求**:
```json
{
  "fileName": "test.xlsx",
  "sheetName": "Sheet1",
  "cells": [
    {"cellAddress": "A1"},
    {"cellAddress": "B2"},
    {"cellAddress": "B4"}
  ],
  "readFormula": false
}
```

#### 3. 写入并读取 (`/api/excel/operation`)
- **方法**: POST
- **需要Token**: 是
- **说明**: 组合操作，先写入数据再读取，适合需要立即获取计算结果的场景
- **特性**: 
  - 只需在外层指定 `fileName`，会自动传递给 `writeRequest` 和 `readRequest`
  - 支持多Sheet操作和跨Sheet公式计算 🆕
- **示例请求（单Sheet）**:
```json
{
  "fileName": "test.xlsx",
  "writeRequest": {
    "sheetName": "Sheet1",
    "cells": [
      {"cellAddress": "A1", "value": 10, "valueType": "NUMBER"},
      {"cellAddress": "A2", "value": 20, "valueType": "NUMBER"},
      {"cellAddress": "A3", "value": "=A1+A2", "valueType": "FORMULA"}
    ]
  },
  "readRequest": {
    "sheetName": "Sheet1",
    "cells": [{"cellAddress": "A3"}],
    "readFormula": false
  }
}
```

- **示例请求（多Sheet跨表计算）** 🆕:
```json
{
  "fileName": "report.xlsx",
  "writeRequest": {
    "cells": [
      {"sheetName": "销售数据", "cellAddress": "B1", "value": 50000, "valueType": "NUMBER"},
      {"sheetName": "成本数据", "cellAddress": "B1", "value": 30000, "valueType": "NUMBER"},
      {"sheetName": "利润统计", "cellAddress": "A1", "value": "净利润", "valueType": "STRING"},
      {"sheetName": "利润统计", "cellAddress": "B1", "value": "=销售数据!B1-成本数据!B1", "valueType": "FORMULA"}
    ]
  },
  "readRequest": {
    "cells": [
      {"sheetName": "销售数据", "cellAddress": "B1"},
      {"sheetName": "成本数据", "cellAddress": "B1"},
      {"sheetName": "利润统计", "cellAddress": "B1"}
    ],
    "readFormula": false
  }
}
```
> 💡 **注意**: 无需在 `writeRequest` 和 `readRequest` 内部重复指定 `fileName`，系统会自动处理  
> 🆕 **多Sheet**: 可以在 `cells` 中为每个单元格单独指定 `sheetName`，实现跨Sheet操作

### 📁 文件管理

#### 4. 上传文件 (`/api/excel/upload`)
- **方法**: POST (multipart/form-data)
- **需要Token**: 是
- **操作**: 使用页面上的文件选择器选择Excel文件上传
- **支持格式**: .xlsx, .xls

#### 5. 下载文件 (`/api/excel/download/{fileName}`)
- **方法**: GET
- **需要Token**: 是
- **操作**: 输入要下载的文件名（如 `test.xlsx`），点击发送请求
- **响应**: 浏览器自动下载文件

### 📄 模板功能

#### 6. 模板生成 (`/api/excel/template/generate`)
- **方法**: POST
- **需要Token**: 是
- **说明**: 基于模板生成新文件并保存在服务器，返回生成的文件名
- **示例请求**:
```json
{
  "templateFileName": "template.xlsx",
  "outputFileName": "report_2024_10.xlsx",
  "sheetName": "Sheet1",
  "cells": [
    {"cellAddress": "A1", "value": "2024年10月报告", "valueType": "STRING"},
    {"cellAddress": "B5", "value": 12345.67, "valueType": "NUMBER"},
    {"cellAddress": "C10", "value": "=B5*1.1", "valueType": "FORMULA"}
  ]
}
```

#### 7. 模板填充并下载 (`/api/excel/template/fill-and-download`)
- **方法**: POST
- **需要Token**: 是
- **说明**: 基于模板生成新文件并直接下载
- **示例请求**:
```json
{
  "templateFileName": "template.xlsx",
  "sheetName": "Sheet1",
  "cells": [
    {"cellAddress": "A1", "value": "即时报告", "valueType": "STRING"},
    {"cellAddress": "B5", "value": 999, "valueType": "NUMBER"}
  ]
}
```

### 🔧 系统

#### 8. 健康检查 (`/api/excel/health`)
- **方法**: GET
- **需要Token**: 否
- **说明**: 检查服务是否正常运行
- **响应示例**:
```json
{
  "status": "UP",
  "timestamp": "2024-10-24T12:00:00",
  "storageAvailable": true,
  "fileCount": 10
}
```

## 使用步骤

### 1. 选择Token
1. 在页面顶部"基础配置"区域选择一个API Token
2. 如果没有Token，先到"Token管理"页面创建一个

### 2. 选择API接口
1. 从"API接口"下拉列表中选择要测试的接口
2. 系统会自动加载该接口的示例数据

### 3. 准备测试数据

#### 方法A：使用示例数据
- 点击"📝 加载示例"按钮，加载预设的示例请求

#### 方法B：选择已上传的文件
- 在"选择Excel文件"下拉列表中选择一个已上传的文件
- 系统会自动将文件名填入请求体的 `fileName` 字段

#### 方法C：手动编辑JSON
- 直接在"请求体（JSON）"文本框中编辑请求数据

### 4. 特殊接口操作

#### 文件上传接口
- 使用"选择文件上传"按钮选择本地Excel文件
- JSON请求体会被隐藏（不需要）

#### 文件下载接口
- 在"要下载的文件名"输入框中输入文件名
- JSON请求体会被隐藏（不需要）

### 5. 发送请求
1. 点击"🚀 发送请求"按钮
2. 等待响应（下方会显示响应结果）

### 6. 查看响应
响应区域会显示：
- **状态码**: HTTP状态码和状态文本
- **耗时**: 请求处理时间（毫秒）
- **方法和URL**: 实际调用的接口路径
- **响应体**: JSON格式的响应内容或错误信息

## 页面功能说明

### 自动适应UI
根据选择的接口类型，页面会自动显示/隐藏相应的输入区域：
- **JSON请求**: 显示JSON文本框（大多数接口）
- **文件上传**: 显示文件选择器（上传接口）
- **文件下载**: 显示文件名输入框（下载接口）

### 文件下载处理
- 对于返回文件的接口（下载、模板下载），浏览器会自动触发文件下载
- 响应区域会显示文件大小和下载成功信息

### 错误处理
- 如果请求失败，响应区域会显示详细的错误信息
- 包括错误消息和堆栈跟踪（如果有）

## 常见场景

### 场景1：测试新公式
1. 选择"写入Excel"接口
2. 修改示例中的 `cellAddress` 和 `value`
3. 发送请求写入数据
4. 切换到"读取Excel"接口
5. 读取公式计算结果

### 场景2：快速验证计算
1. 选择"写入并读取（组合操作）"接口
2. 在 `writeRequest.cells` 中设置输入值和公式
3. 在 `readRequest.cells` 中指定要读取的单元格
4. 一次请求完成写入和读取

### 场景3：生成月度报告
1. 选择"模板生成"接口
2. 指定模板文件名（如 `template.xlsx`）
3. 设置输出文件名（如 `report_2024_10.xlsx`）
4. 填充报告数据
5. 发送请求，服务器生成文件
6. 可以在"Excel文件"页面查看生成的文件

### 场景4：即时生成并下载
1. 选择"模板填充并下载"接口
2. 指定模板文件和数据
3. 发送请求，浏览器自动下载生成的文件

## 提示和最佳实践

### 1. Token管理
- 为测试环境创建专用Token，方便追踪测试请求
- 在Token描述中注明用途（如"测试专用"）

### 2. 文件准备
- 测试前先在"Excel文件"页面上传测试用的Excel文件
- 为模板功能准备好模板文件

### 3. 数据验证
- 发送请求后，可以到"请求日志"页面查看完整的请求和响应
- 使用"Excel文件"页面的下载功能验证生成的文件

### 4. 错误排查
- 如果请求失败，检查：
  - Token是否有效且未过期
  - 文件名是否存在
  - JSON格式是否正确
  - 单元格地址是否有效（如A1, B2）
- 查看"请求日志"获取详细错误信息

### 5. 性能测试
- 响应状态会显示请求耗时
- 可以测试不同大小的数据集，观察性能变化

## 技术细节

### valueType 枚举值
- `STRING`: 字符串
- `NUMBER`: 数字
- `BOOLEAN`: 布尔值
- `FORMULA`: 公式（值应以 `=` 开头）

### 单元格地址格式
- 标准Excel格式：`列字母 + 行号`
- 示例：`A1`, `B2`, `AZ27`, `AA100`
- 不区分大小写

### 公式格式
- 必须以 `=` 开头
- 使用Excel标准函数语法
- 示例：`=SUM(A1:A10)`, `=AVERAGE(B1:B5)`, `=A1+B1`

### Token传递
- 所有需要认证的接口都通过HTTP Header传递Token
- Header名称：`X-API-Token`
- Header值：完整的Token字符串

## 更新日志

### v1.2.1 (2024-10-24)
- ✅ 新增所有API接口的测试支持
- ✅ 分类显示接口（Excel操作、文件管理、模板功能、系统）
- ✅ 自适应UI，根据接口类型显示相应的输入区域
- ✅ 支持文件上传和下载测试
- ✅ 支持模板功能测试
- ✅ 优化示例数据，确保与后端模型匹配
- ✅ 改进文件下载处理，自动触发浏览器下载
- ✅ 增强错误提示和响应显示

### v1.2.0
- 初始版本，支持基本的Excel读写和组合操作测试

## 相关文档
- [API示例文档](API_EXAMPLES.md)
- [管理后台使用指南](ADMIN_GUIDE.md)
- [模板功能指南](TEMPLATE_GUIDE.md)
- [公式支持说明](FORMULA_SUPPORT.md)

