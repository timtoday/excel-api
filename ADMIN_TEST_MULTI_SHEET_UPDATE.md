# Web测试工具多Sheet示例更新 🎯

## 更新说明

Web管理后台测试工具（`/admin/test`）的所有示例JSON已更新，现在完整展示**多Sheet功能**！

## 更新的示例

### 1. 写入Excel 示例

**更新前**（仅单Sheet）:
```json
{
  "fileName": "test.xlsx",
  "sheetName": "Sheet1",
  "cells": [
    {"cellAddress": "A1", "value": "姓名", "valueType": "STRING"},
    {"cellAddress": "B1", "value": "分数", "valueType": "STRING"}
  ]
}
```

**更新后**（展示多Sheet）:
```json
{
  "fileName": "test.xlsx",
  "sheetName": "Sheet1",
  "cells": [
    {"cellAddress": "A1", "value": "姓名", "valueType": "STRING"},
    {"cellAddress": "B1", "value": "分数", "valueType": "STRING"},
    {"cellAddress": "A2", "value": "张三", "valueType": "STRING"},
    {"cellAddress": "B2", "value": 85, "valueType": "NUMBER"},
    {"cellAddress": "A3", "value": "李四", "valueType": "STRING"},
    {"cellAddress": "B3", "value": 92, "valueType": "NUMBER"},
    {"cellAddress": "B4", "value": "=AVERAGE(B2:B3)", "valueType": "FORMULA"},
    {"sheetName": "Sheet2", "cellAddress": "A1", "value": "统计", "valueType": "STRING"},
    {"sheetName": "Sheet2", "cellAddress": "B1", "value": "=Sheet1!B4", "valueType": "FORMULA"}
  ]
}
```

**新增特性**:
- ✅ 最后两个单元格指定了 `sheetName: "Sheet2"`
- ✅ 展示跨Sheet公式：`=Sheet1!B4`

### 2. 读取Excel 示例

**更新后**（展示多Sheet读取）:
```json
{
  "fileName": "test.xlsx",
  "sheetName": "Sheet1",
  "cells": [
    {"cellAddress": "A1"},
    {"cellAddress": "A2"},
    {"cellAddress": "B2"},
    {"cellAddress": "B4"},
    {"sheetName": "Sheet2", "cellAddress": "A1"},
    {"sheetName": "Sheet2", "cellAddress": "B1"}
  ],
  "readFormula": false
}
```

**新增特性**:
- ✅ 同时从 Sheet1 和 Sheet2 读取数据
- ✅ 展示如何在读取时指定不同的Sheet

### 3. 组合操作 示例 ⭐（最重要）

**更新后**（展示跨Sheet计算）:
```json
{
  "fileName": "test.xlsx",
  "writeRequest": {
    "cells": [
      {"sheetName": "销售数据", "cellAddress": "A1", "value": "产品", "valueType": "STRING"},
      {"sheetName": "销售数据", "cellAddress": "B1", "value": 50000, "valueType": "NUMBER"},
      {"sheetName": "成本数据", "cellAddress": "A1", "value": "成本", "valueType": "STRING"},
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

**新增特性**:
- ✅ 同时操作3个Sheet：销售数据、成本数据、利润统计
- ✅ 展示跨Sheet公式：`=销售数据!B1-成本数据!B1`
- ✅ 展示完整的业务场景：写入原始数据 → 计算 → 读取结果
- ✅ 移除了外层的 `sheetName`，所有单元格都明确指定Sheet

**预期响应**:
```json
{
  "success": true,
  "message": "读取成功",
  "data": [
    {
      "sheetName": "销售数据",
      "cellAddress": "B1",
      "value": 50000.0,
      "valueType": "NUMERIC"
    },
    {
      "sheetName": "成本数据",
      "cellAddress": "B1",
      "value": 30000.0,
      "valueType": "NUMERIC"
    },
    {
      "sheetName": "利润统计",
      "cellAddress": "B1",
      "value": 20000.0,
      "valueType": "NUMERIC"
    }
  ]
}
```

### 4. 模板生成 示例

**更新后**（展示多Sheet模板）:
```json
{
  "templateFileName": "template.xlsx",
  "outputFileName": "report_2024_10.xlsx",
  "cells": [
    {"sheetName": "报告首页", "cellAddress": "A1", "value": "2024年10月报告", "valueType": "STRING"},
    {"sheetName": "数据详情", "cellAddress": "B5", "value": 12345.67, "valueType": "NUMBER"},
    {"sheetName": "统计汇总", "cellAddress": "C10", "value": "=数据详情!B5*1.1", "valueType": "FORMULA"}
  ]
}
```

**新增特性**:
- ✅ 同时填充3个Sheet：报告首页、数据详情、统计汇总
- ✅ 展示模板中的跨Sheet公式

### 5. 模板填充并下载 示例

**更新后**（展示多Sheet模板下载）:
```json
{
  "templateFileName": "template.xlsx",
  "cells": [
    {"sheetName": "报告首页", "cellAddress": "A1", "value": "即时报告", "valueType": "STRING"},
    {"sheetName": "数据详情", "cellAddress": "B5", "value": 999, "valueType": "NUMBER"},
    {"sheetName": "统计汇总", "cellAddress": "A1", "value": "=数据详情!B5*2", "valueType": "FORMULA"}
  ]
}
```

**新增特性**:
- ✅ 展示如何在模板下载时填充多个Sheet

## 如何使用

### 步骤1: 访问测试页面

```
http://localhost:18081/admin/test
```

### 步骤2: 选择接口

选择任一接口，如：`写入并读取（组合操作）`

### 步骤3: 加载示例

点击 **"📝 加载示例"** 按钮，自动填充多Sheet示例JSON

### 步骤4: 发送请求

点击 **"🚀 发送请求"** 按钮，测试多Sheet功能

### 步骤5: 查看响应

响应中的每个单元格都会包含 `sheetName` 字段：

```json
{
  "success": true,
  "data": [
    {
      "sheetName": "销售数据",
      "cellAddress": "B1",
      "value": 50000.0
    }
  ]
}
```

## 学习要点

### 1. 理解优先级

```json
{
  "sheetName": "默认Sheet",
  "cells": [
    {
      "cellAddress": "A1",
      "value": "写入默认Sheet"
    },
    {
      "sheetName": "特定Sheet",
      "cellAddress": "A1",
      "value": "写入特定Sheet"
    }
  ]
}
```

- 单元格级别的 `sheetName` > 外层的 `sheetName`

### 2. 跨Sheet公式格式

Excel标准格式：`SheetName!CellAddress`

示例：
- `=Sheet1!A1`
- `=销售数据!B10`
- `=Sheet1!A1+Sheet2!B1`
- `=SUM(数据!A1:A10)`

### 3. 中文Sheet名称

完全支持中文Sheet名称：

```json
{
  "sheetName": "伤寒法要",
  "cellAddress": "D8",
  "value": "女",
  "valueType": "STRING"
}
```

### 4. 响应中的Sheet信息

所有读取操作的响应都包含 `sheetName`，方便识别数据来源：

```json
{
  "sheetName": "伤寒法要",
  "cellAddress": "I8",
  "value": "..."
}
```

## 实战示例

### 你的实际需求（中医辨证）

现在可以这样测试：

```json
{
  "fileName": "公式.xlsx",
  "writeRequest": {
    "sheetName": "伤寒法要",
    "cells": [
      {"cellAddress": "D8", "value": "女", "valueType": "STRING"},
      {"cellAddress": "D9", "value": "戌", "valueType": "STRING"},
      {"cellAddress": "F8", "value": "庚", "valueType": "STRING"}
    ]
  },
  "readRequest": {
    "sheetName": "伤寒法要",
    "cells": [
      {"cellAddress": "I8"},
      {"cellAddress": "I9"},
      {"cellAddress": "I10"},
      {"cellAddress": "I11"},
      {"cellAddress": "I12"}
    ],
    "readFormula": true
  }
}
```

如果需要同时读取其他辅助Sheet：

```json
{
  "fileName": "公式.xlsx",
  "writeRequest": {
    "sheetName": "伤寒法要",
    "cells": [
      {"cellAddress": "D8", "value": "女", "valueType": "STRING"},
      {"cellAddress": "D9", "value": "戌", "valueType": "STRING"},
      {"cellAddress": "F8", "value": "庚", "valueType": "STRING"}
    ]
  },
  "readRequest": {
    "cells": [
      {"sheetName": "伤寒法要", "cellAddress": "I8"},
      {"sheetName": "伤寒法要", "cellAddress": "I9"},
      {"sheetName": "伤寒法要", "cellAddress": "I10"},
      {"sheetName": "伤寒法要", "cellAddress": "I11"},
      {"sheetName": "伤寒法要", "cellAddress": "I12"},
      {"sheetName": "参考数据", "cellAddress": "A1"},
      {"sheetName": "参考数据", "cellAddress": "B1"}
    ],
    "readFormula": true
  }
}
```

## 对比总结

| 特性 | 更新前 | 更新后 |
|------|--------|--------|
| 示例类型 | 单Sheet | 多Sheet + 跨Sheet公式 |
| 学习难度 | 简单但不完整 | 完整展示所有功能 |
| 实际应用 | 无法看到多Sheet用法 | 直接看到业务场景 |
| 跨Sheet公式 | 未展示 | ✅ 展示 |
| 响应格式 | 未说明sheetName | ✅ 包含sheetName |

## 相关文档

- 📖 [多Sheet操作指南](MULTI_SHEET_GUIDE.md) - 完整教程
- 📖 [多Sheet快速上手](MULTI_SHEET_QUICK_START.md) - 30秒入门
- 📖 [API测试指南](API_TEST_GUIDE.md) - 所有接口测试方法
- 📖 [更新日志](CHANGELOG.md) - v1.2.2版本说明

## 下一步

1. **立即测试**: 访问 `/admin/test` 页面，加载新示例
2. **理解示例**: 仔细阅读组合操作示例，理解跨Sheet计算流程
3. **自定义测试**: 修改示例JSON，测试你自己的业务场景
4. **查看响应**: 注意响应中的 `sheetName` 字段

---

**现在Web测试工具完整支持多Sheet功能！** 🎉

