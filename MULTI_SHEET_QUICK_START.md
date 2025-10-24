# 多Sheet功能快速上手 ⚡

## 30秒理解

**以前**: 一个请求只能操作一个Sheet  
**现在**: 一个请求可以操作多个Sheet

## 快速示例

### 你的实际需求（立即可用）

```json
{
  "fileName": "公式.xlsx",
  "writeRequest": {
    "sheetName": "伤寒法要",
    "cells": [
      {
        "cellAddress": "D8",
        "value": "女",
        "valueType": "STRING"
      },
      {
        "cellAddress": "D9",
        "value": "戌",
        "valueType": "STRING"
      },
      {
        "cellAddress": "F8",
        "value": "庚",
        "valueType": "STRING"
      }
    ]
  },
  "readRequest": {
    "sheetName": "伤寒法要",
    "cells": [
      {
        "cellAddress": "I8"
      },
      {
        "cellAddress": "I9"
      },
      {
        "cellAddress": "I10"
      },
      {
        "cellAddress": "I11"
      },
      {
        "cellAddress": "I12"
      }
    ],
    "readFormula": true
  }
}
```

✅ 这个请求现在可以正常工作了！不需要在内部重复指定 `fileName`。

## 如果需要同时操作其他Sheet

只需为特定单元格添加 `sheetName`：

```json
{
  "fileName": "公式.xlsx",
  "writeRequest": {
    "cells": [
      {
        "sheetName": "伤寒法要",
        "cellAddress": "D8",
        "value": "女",
        "valueType": "STRING"
      },
      {
        "sheetName": "伤寒法要",
        "cellAddress": "D9",
        "value": "戌",
        "valueType": "STRING"
      },
      {
        "sheetName": "参考数据",
        "cellAddress": "A1",
        "value": "辅助值",
        "valueType": "STRING"
      }
    ]
  },
  "readRequest": {
    "cells": [
      {
        "sheetName": "伤寒法要",
        "cellAddress": "I8"
      },
      {
        "sheetName": "参考数据",
        "cellAddress": "B1"
      }
    ],
    "readFormula": true
  }
}
```

## 两种使用方式

### 方式1: 默认Sheet + 特定Sheet（推荐）

```json
{
  "fileName": "data.xlsx",
  "sheetName": "主表",
  "cells": [
    {
      "cellAddress": "A1",
      "value": "默认写到主表"
    },
    {
      "sheetName": "明细表",
      "cellAddress": "A1",
      "value": "写到明细表"
    }
  ]
}
```

### 方式2: 全部指定Sheet

```json
{
  "fileName": "data.xlsx",
  "cells": [
    {
      "sheetName": "Sheet1",
      "cellAddress": "A1",
      "value": "数据1"
    },
    {
      "sheetName": "Sheet2",
      "cellAddress": "A1",
      "value": "数据2"
    }
  ]
}
```

## 跨Sheet公式

可以在公式中引用其他Sheet：

```json
{
  "fileName": "report.xlsx",
  "cells": [
    {
      "sheetName": "销售",
      "cellAddress": "B10",
      "value": 10000,
      "valueType": "NUMBER"
    },
    {
      "sheetName": "成本",
      "cellAddress": "B10",
      "value": 6000,
      "valueType": "NUMBER"
    },
    {
      "sheetName": "利润",
      "cellAddress": "B10",
      "value": "=销售!B10-成本!B10",
      "valueType": "FORMULA"
    }
  ]
}
```

## 响应格式

返回的数据会包含 `sheetName`，告诉你每个值来自哪个Sheet：

```json
{
  "success": true,
  "data": [
    {
      "sheetName": "伤寒法要",
      "cellAddress": "I8",
      "value": "..."
    },
    {
      "sheetName": "伤寒法要",
      "cellAddress": "I9",
      "value": "..."
    }
  ]
}
```

## 常见问题

### Q: 旧代码还能用吗？
**A**: 能！完全向后兼容，不需要修改任何现有代码。

### Q: 不指定sheetName会怎样？
**A**: 如果外层和单元格都没指定，会报错：  
`"单元格 A1 未指定sheet名称"`

### Q: Sheet不存在怎么办？
**A**: 
- 写入操作：自动创建
- 读取操作：报错，需要先创建Sheet

### Q: 性能怎么样？
**A**: 
- ✅ 一次请求比多次请求快很多
- ✅ 同一Sheet的操作会被优化处理
- ⚠️ 跨Sheet公式可能需要更多计算时间

## 快速测试

### 使用Web测试工具

1. 访问：`http://localhost:18081/admin/test`
2. 选择接口：`写入并读取（组合操作）`
3. 粘贴你的JSON请求
4. 点击"🚀 发送请求"

### 使用curl

```bash
curl -X POST "http://localhost:18081/api/excel/operation" \
  -H "X-API-Token: your_token" \
  -H "Content-Type: application/json" \
  -d @your_request.json
```

## 完整文档

- 📖 [多Sheet操作指南](MULTI_SHEET_GUIDE.md) - 详细教程和案例
- 📖 [API测试指南](API_TEST_GUIDE.md) - 测试工具使用
- 📖 [README](README.md) - 完整项目文档

## 版本要求

✅ v1.2.2 或更高版本

---

**立即试用多Sheet功能！** 🚀

