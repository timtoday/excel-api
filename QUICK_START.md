# 快速开始指南

## 📌 核心概念

### 单元格地址格式

本API使用Excel原生的单元格地址格式，无需计算行列号：

| Excel地址 | 说明 | 示例用途 |
|----------|------|---------|
| `A1` | A列第1行 | 表头 |
| `B2` | B列第2行 | 数据 |
| `AZ27` | AZ列第27行 | 大表格 |
| `AA1` | AA列第1行 | 宽表格 |

**无需转换行列号！** 直接使用Excel中看到的地址即可。

---

## 🚀 5分钟上手

### 1️⃣ 启动服务

```bash
mvn spring-boot:run
```

访问 http://localhost:18081/swagger-ui.html 查看API文档

### 2️⃣ 创建第一个Excel

```bash
curl -X POST "http://localhost:18081/api/excel/write" \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "demo.xlsx",
    "sheetName": "演示",
    "cells": [
      {"cellAddress": "A1", "value": "产品", "valueType": "STRING"},
      {"cellAddress": "B1", "value": "价格", "valueType": "STRING"},
      {"cellAddress": "A2", "value": "苹果", "valueType": "STRING"},
      {"cellAddress": "B2", "value": 5.5, "valueType": "NUMBER"}
    ]
  }'
```

### 3️⃣ 读取数据

```bash
curl -X POST "http://localhost:18081/api/excel/read" \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "demo.xlsx",
    "sheetName": "演示",
    "cells": [
      {"cellAddress": "A2"},
      {"cellAddress": "B2"}
    ]
  }'
```

### 4️⃣ 使用公式

```bash
curl -X POST "http://localhost:18081/api/excel/operation" \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "demo.xlsx",
    "writeRequest": {
      "sheetName": "演示",
      "cells": [
        {"cellAddress": "C1", "value": "数量", "valueType": "STRING"},
        {"cellAddress": "D1", "value": "总价", "valueType": "STRING"},
        {"cellAddress": "C2", "value": 10, "valueType": "NUMBER"},
        {"cellAddress": "D2", "value": "B2*C2", "valueType": "FORMULA"}
      ]
    },
    "readRequest": {
      "sheetName": "演示",
      "cells": [{"cellAddress": "D2"}]
    }
  }'
```

**响应：**
```json
{
  "success": true,
  "data": [
    {
      "cellAddress": "D2",
      "value": 55.0,
      "formula": "B2*C2",
      "valueType": "NUMERIC"
    }
  ]
}
```

### 5️⃣ 模板功能（批量生成）⭐

```bash
# 基于模板批量生成Excel
curl -X POST "http://localhost:18081/api/excel/template/fill-and-download" \
  -H "Content-Type: application/json" \
  -d '{
    "templateFileName": "invoice_template.xlsx",
    "downloadFileName": "invoice_001.xlsx",
    "sheetName": "Sheet1",
    "cells": [
      {"cellAddress": "B1", "value": "客户名称", "valueType": "STRING"},
      {"cellAddress": "B2", "value": 10000, "valueType": "NUMBER"}
    ]
  }' \
  --output invoice_001.xlsx
```

> 💡 模板功能可用于批量生成工资条、发票、报表等！详见 [TEMPLATE_GUIDE.md](TEMPLATE_GUIDE.md)

---

## 📝 参数说明

### 写入请求 (ExcelWriteRequest)

```json
{
  "fileName": "文件名.xlsx",
  "sheetName": "Sheet名称",
  "cells": [
    {
      "cellAddress": "A1",      // ✅ Excel地址格式：A1, B2, AZ27
      "value": "值",            // 单元格值
      "valueType": "STRING"     // STRING|NUMBER|BOOLEAN|FORMULA
    }
  ]
}
```

### 读取请求 (ExcelReadRequest)

```json
{
  "fileName": "文件名.xlsx",
  "sheetName": "Sheet名称",
  "cells": [
    {
      "cellAddress": "A1"       // ✅ Excel地址格式
    }
  ],
  "readFormula": false          // false=读计算结果, true=读公式本身
}
```

### 响应格式 (ExcelResponse)

```json
{
  "success": true,
  "message": "操作成功",
  "data": [
    {
      "cellAddress": "A1",      // 单元格地址
      "value": "实际值",        // 单元格值（公式则为计算结果）
      "formula": "SUM(A1:A10)", // 如果是公式单元格
      "valueType": "NUMERIC"    // 值类型
    }
  ]
}
```

---

## 🎯 常用场景

### 场景1：简单计算器

```bash
# 输入两个数，计算和、差、积、商
curl -X POST "http://localhost:18081/api/excel/operation" \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "calculator.xlsx",
    "writeRequest": {
      "sheetName": "计算",
      "cells": [
        {"cellAddress": "A1", "value": "第一个数", "valueType": "STRING"},
        {"cellAddress": "B1", "value": 100, "valueType": "NUMBER"},
        {"cellAddress": "A2", "value": "第二个数", "valueType": "STRING"},
        {"cellAddress": "B2", "value": 25, "valueType": "NUMBER"},
        {"cellAddress": "A3", "value": "和", "valueType": "STRING"},
        {"cellAddress": "B3", "value": "B1+B2", "valueType": "FORMULA"},
        {"cellAddress": "A4", "value": "差", "valueType": "STRING"},
        {"cellAddress": "B4", "value": "B1-B2", "valueType": "FORMULA"},
        {"cellAddress": "A5", "value": "积", "valueType": "STRING"},
        {"cellAddress": "B5", "value": "B1*B2", "valueType": "FORMULA"},
        {"cellAddress": "A6", "value": "商", "valueType": "STRING"},
        {"cellAddress": "B6", "value": "B1/B2", "valueType": "FORMULA"}
      ]
    },
    "readRequest": {
      "sheetName": "计算",
      "cells": [
        {"cellAddress": "B3"},
        {"cellAddress": "B4"},
        {"cellAddress": "B5"},
        {"cellAddress": "B6"}
      ]
    }
  }'
```

### 场景2：购物车总价

```bash
curl -X POST "http://localhost:18081/api/excel/operation" \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "cart.xlsx",
    "writeRequest": {
      "sheetName": "购物车",
      "cells": [
        {"cellAddress": "A1", "value": "商品", "valueType": "STRING"},
        {"cellAddress": "B1", "value": "单价", "valueType": "STRING"},
        {"cellAddress": "C1", "value": "数量", "valueType": "STRING"},
        {"cellAddress": "D1", "value": "小计", "valueType": "STRING"},
        
        {"cellAddress": "A2", "value": "商品1", "valueType": "STRING"},
        {"cellAddress": "B2", "value": 99.9, "valueType": "NUMBER"},
        {"cellAddress": "C2", "value": 2, "valueType": "NUMBER"},
        {"cellAddress": "D2", "value": "B2*C2", "valueType": "FORMULA"},
        
        {"cellAddress": "A3", "value": "商品2", "valueType": "STRING"},
        {"cellAddress": "B3", "value": 199.9, "valueType": "NUMBER"},
        {"cellAddress": "C3", "value": 1, "valueType": "NUMBER"},
        {"cellAddress": "D3", "value": "B3*C3", "valueType": "FORMULA"},
        
        {"cellAddress": "A5", "value": "总计", "valueType": "STRING"},
        {"cellAddress": "D5", "value": "SUM(D2:D3)", "valueType": "FORMULA"}
      ]
    },
    "readRequest": {
      "sheetName": "购物车",
      "cells": [{"cellAddress": "D5"}]
    }
  }'
```

### 场景3：成绩统计

```bash
curl -X POST "http://localhost:18081/api/excel/operation" \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "scores.xlsx",
    "writeRequest": {
      "sheetName": "成绩",
      "cells": [
        {"cellAddress": "A1", "value": "姓名", "valueType": "STRING"},
        {"cellAddress": "B1", "value": "语文", "valueType": "STRING"},
        {"cellAddress": "C1", "value": "数学", "valueType": "STRING"},
        {"cellAddress": "D1", "value": "英语", "valueType": "STRING"},
        {"cellAddress": "E1", "value": "总分", "valueType": "STRING"},
        {"cellAddress": "F1", "value": "平均分", "valueType": "STRING"},
        
        {"cellAddress": "A2", "value": "张三", "valueType": "STRING"},
        {"cellAddress": "B2", "value": 85, "valueType": "NUMBER"},
        {"cellAddress": "C2", "value": 90, "valueType": "NUMBER"},
        {"cellAddress": "D2", "value": 88, "valueType": "NUMBER"},
        {"cellAddress": "E2", "value": "SUM(B2:D2)", "valueType": "FORMULA"},
        {"cellAddress": "F2", "value": "AVERAGE(B2:D2)", "valueType": "FORMULA"}
      ]
    },
    "readRequest": {
      "sheetName": "成绩",
      "cells": [
        {"cellAddress": "E2"},
        {"cellAddress": "F2"}
      ]
    }
  }'
```

---

## 💡 实用技巧

### 1. 批量写入

一次性写入多个单元格，性能更好：

```json
{
  "cells": [
    {"cellAddress": "A1", "value": "数据1"},
    {"cellAddress": "A2", "value": "数据2"},
    {"cellAddress": "A3", "value": "数据3"},
    // ... 最多建议一次100-1000个单元格
  ]
}
```

### 2. 使用范围公式

```json
{
  "cellAddress": "B10",
  "value": "SUM(B1:B9)",
  "valueType": "FORMULA"
}
```

### 3. 读取公式本身

```json
{
  "readFormula": true  // 返回公式文本而不是计算结果
}
```

### 4. 处理大数据

对于大量数据，建议：
- 分批写入（每批100-500个单元格）
- 使用异步处理
- 启用Redis分布式锁（集群环境）

---

## ❌ 常见错误

### 错误1：单元格地址格式错误

❌ **错误：**
```json
{"row": 0, "col": 0}  // 旧版本格式
```

✅ **正确：**
```json
{"cellAddress": "A1"}  // 使用Excel地址
```

### 错误2：公式语法错误

❌ **错误：**
```json
{"cellAddress": "A1", "value": "=SUM(B1:B10)", "valueType": "FORMULA"}
```

✅ **正确：**
```json
{"cellAddress": "A1", "value": "SUM(B1:B10)", "valueType": "FORMULA"}
// 注意：不需要等号！
```

### 错误3：文件不存在

确保文件已上传或已创建：

```bash
# 先上传文件
curl -X POST "http://localhost:18081/api/excel/upload" \
  -F "file=@template.xlsx"
  
# 再读写
curl -X POST "http://localhost:18081/api/excel/write" ...
```

---

## 📊 公式支持说明

POI支持大部分常用Excel函数（约85%），但某些特殊函数不被支持：

### ✅ 推荐使用（完全支持）
```
SUM, AVERAGE, MAX, MIN, IF, VLOOKUP, COUNTIF, SUMIF
```

### ❌ 不支持（需避免）
```
XLOOKUP, FILTER, SORT, UNIQUE, WEBSERVICE, GETPIVOTDATA
```

> 💡 查看 [FORMULA_SUPPORT.md](FORMULA_SUPPORT.md) 了解详细的函数支持列表

---

## 🔗 更多资源

- **API文档**: http://localhost:18081/swagger-ui.html
- **完整示例**: [API_EXAMPLES.md](API_EXAMPLES.md)
- **模板功能**: [TEMPLATE_GUIDE.md](TEMPLATE_GUIDE.md) ⭐
- **公式支持**: [FORMULA_SUPPORT.md](FORMULA_SUPPORT.md)
- **详细说明**: [README.md](README.md)
- **健康检查**: http://localhost:18081/api/excel/health

---

## 🆘 获取帮助

遇到问题？
1. 查看日志：`logs/excel-api.log`
2. 检查Swagger文档测试API
3. 提交Issue

**祝使用愉快！** 🎉

