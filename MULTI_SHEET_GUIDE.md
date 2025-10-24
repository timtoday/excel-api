# 多Sheet操作指南 📚

## 功能概述

从 **v1.2.2** 开始，Excel API 支持在单次请求中同时读写**多个Sheet**，大大简化了跨Sheet操作的复杂度。

## 核心特性

### ✅ 支持的操作
- 📝 **单次写入多个Sheet**: 一个请求可以向多个Sheet写入数据
- 📖 **单次读取多个Sheet**: 一个请求可以从多个Sheet读取数据
- 🔄 **组合操作跨Sheet**: 写入和读取可以分别操作不同的Sheet
- 🎯 **灵活指定**: 支持全局默认Sheet + 单元格级别Sheet

### 📊 使用场景
1. **跨Sheet计算**: Sheet1的数据参与Sheet2的公式计算
2. **数据汇总**: 从多个Sheet读取数据汇总到主Sheet
3. **模板填充**: 同时填充多个Sheet的模板
4. **批量更新**: 一次性更新工作簿中多个Sheet的数据

## API使用方法

### 方式1: 全局Sheet + 单元格级Sheet（推荐）

在外层指定默认的 `sheetName`，对于需要操作其他Sheet的单元格，单独指定 `sheetName`。

#### 写入示例

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
      "cellAddress": "B1",
      "value": 1000,
      "valueType": "NUMBER"
    },
    {
      "sheetName": "统计汇总",
      "cellAddress": "A1",
      "value": "总销售额",
      "valueType": "STRING"
    },
    {
      "sheetName": "统计汇总",
      "cellAddress": "B1",
      "value": "=销售数据!B1",
      "valueType": "FORMULA"
    }
  ]
}
```

**说明**:
- `A1` 和 `B1` 没有指定 `sheetName`，使用外层的默认值 `"销售数据"`
- 后两个单元格明确指定了 `sheetName: "统计汇总"`，会写入到该Sheet

#### 读取示例

```json
{
  "fileName": "report.xlsx",
  "sheetName": "销售数据",
  "cells": [
    {
      "cellAddress": "B1"
    },
    {
      "sheetName": "统计汇总",
      "cellAddress": "B1"
    },
    {
      "sheetName": "成本数据",
      "cellAddress": "C5"
    }
  ],
  "readFormula": false
}
```

**说明**:
- 第一个单元格从 `"销售数据"` Sheet读取
- 第二个单元格从 `"统计汇总"` Sheet读取
- 第三个单元格从 `"成本数据"` Sheet读取

### 方式2: 仅使用单元格级Sheet

如果不需要默认Sheet，可以为每个单元格都指定 `sheetName`，外层的 `sheetName` 可以省略或设为空。

```json
{
  "fileName": "data.xlsx",
  "cells": [
    {
      "sheetName": "Sheet1",
      "cellAddress": "A1",
      "value": "数据1",
      "valueType": "STRING"
    },
    {
      "sheetName": "Sheet2",
      "cellAddress": "A1",
      "value": "数据2",
      "valueType": "STRING"
    },
    {
      "sheetName": "Sheet3",
      "cellAddress": "A1",
      "value": "数据3",
      "valueType": "STRING"
    }
  ]
}
```

## 实际应用案例

### 案例1: 跨Sheet引用公式

**需求**: 在"销售报表"中汇总"北京"、"上海"、"广州"三个分公司的销售数据

```json
{
  "fileName": "公司销售.xlsx",
  "writeRequest": {
    "cells": [
      {
        "sheetName": "北京",
        "cellAddress": "B10",
        "value": 50000,
        "valueType": "NUMBER"
      },
      {
        "sheetName": "上海",
        "cellAddress": "B10",
        "value": 60000,
        "valueType": "NUMBER"
      },
      {
        "sheetName": "广州",
        "cellAddress": "B10",
        "value": 45000,
        "valueType": "NUMBER"
      },
      {
        "sheetName": "销售报表",
        "cellAddress": "A1",
        "value": "总销售额",
        "valueType": "STRING"
      },
      {
        "sheetName": "销售报表",
        "cellAddress": "B1",
        "value": "=北京!B10+上海!B10+广州!B10",
        "valueType": "FORMULA"
      }
    ]
  },
  "readRequest": {
    "sheetName": "销售报表",
    "cells": [
      {
        "cellAddress": "B1"
      }
    ],
    "readFormula": false
  }
}
```

**响应**:
```json
{
  "success": true,
  "message": "读取成功",
  "data": [
    {
      "sheetName": "销售报表",
      "cellAddress": "B1",
      "value": 155000.0,
      "valueType": "NUMERIC"
    }
  ]
}
```

### 案例2: 中医辨证（你的实际需求）

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

如果需要同时读取其他Sheet的辅助数据：

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
    "cells": [
      {
        "sheetName": "伤寒法要",
        "cellAddress": "I8"
      },
      {
        "sheetName": "伤寒法要",
        "cellAddress": "I9"
      },
      {
        "sheetName": "伤寒法要",
        "cellAddress": "I10"
      },
      {
        "sheetName": "伤寒法要",
        "cellAddress": "I11"
      },
      {
        "sheetName": "伤寒法要",
        "cellAddress": "I12"
      },
      {
        "sheetName": "参考数据",
        "cellAddress": "A1"
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

### 案例3: 模板批量填充

**需求**: 同时填充"订单信息"、"客户信息"、"发货清单"三个Sheet

```json
{
  "templateFileName": "订单模板.xlsx",
  "outputFileName": "订单_20241024_001.xlsx",
  "cells": [
    {
      "sheetName": "订单信息",
      "cellAddress": "B2",
      "value": "ORD-2024-001",
      "valueType": "STRING"
    },
    {
      "sheetName": "订单信息",
      "cellAddress": "B3",
      "value": "2024-10-24",
      "valueType": "STRING"
    },
    {
      "sheetName": "客户信息",
      "cellAddress": "B2",
      "value": "张三",
      "valueType": "STRING"
    },
    {
      "sheetName": "客户信息",
      "cellAddress": "B3",
      "value": "13800138000",
      "valueType": "STRING"
    },
    {
      "sheetName": "发货清单",
      "cellAddress": "A2",
      "value": "产品A",
      "valueType": "STRING"
    },
    {
      "sheetName": "发货清单",
      "cellAddress": "B2",
      "value": 10,
      "valueType": "NUMBER"
    },
    {
      "sheetName": "发货清单",
      "cellAddress": "C2",
      "value": "=B2*100",
      "valueType": "FORMULA"
    }
  ]
}
```

## 响应格式

读取多个Sheet时，响应中的每个单元格结果都包含 `sheetName` 字段：

```json
{
  "success": true,
  "message": "读取成功",
  "data": [
    {
      "sheetName": "销售数据",
      "cellAddress": "B1",
      "value": 1000.0,
      "valueType": "NUMERIC"
    },
    {
      "sheetName": "统计汇总",
      "cellAddress": "B1",
      "value": 1000.0,
      "formula": "=销售数据!B1",
      "valueType": "NUMERIC"
    },
    {
      "sheetName": "成本数据",
      "cellAddress": "C5",
      "value": 500.0,
      "valueType": "NUMERIC"
    }
  ]
}
```

## 注意事项

### ✅ 优先级规则
- 单元格级别的 `sheetName` **优先于**外层的 `sheetName`
- 如果单元格和外层都未指定 `sheetName`，会抛出错误

### ⚠️ 错误处理

**错误1: 未指定Sheet**
```json
{
  "success": false,
  "message": "单元格 A1 未指定sheet名称"
}
```

**解决**: 确保每个单元格都有Sheet名称（通过外层默认值或单元格级别指定）

**错误2: Sheet不存在**
```json
{
  "success": false,
  "message": "Sheet不存在: 不存在的Sheet"
}
```

**解决**: 
- 写入操作会自动创建不存在的Sheet
- 读取操作要求Sheet必须存在，请检查Sheet名称拼写

### 💡 性能建议

1. **合理分组**: 将同一Sheet的操作放在一起，提高效率
2. **避免重复**: 不要在同一请求中多次写入同一单元格
3. **公式计算**: 跨Sheet公式可能需要更多计算时间
4. **批量操作**: 尽量使用单次请求处理多个单元格，减少网络开销

## 向后兼容性

### ✅ 完全兼容

旧版本的请求格式仍然有效：

**旧格式**（只操作单个Sheet）:
```json
{
  "fileName": "data.xlsx",
  "sheetName": "Sheet1",
  "cells": [
    {
      "cellAddress": "A1",
      "value": "test",
      "valueType": "STRING"
    }
  ]
}
```

**新格式**（支持多Sheet）:
```json
{
  "fileName": "data.xlsx",
  "sheetName": "Sheet1",
  "cells": [
    {
      "cellAddress": "A1",
      "value": "test",
      "valueType": "STRING"
    },
    {
      "sheetName": "Sheet2",
      "cellAddress": "A1",
      "value": "test2",
      "valueType": "STRING"
    }
  ]
}
```

## Web测试工具

在 `http://localhost:8080/admin/test` 页面测试多Sheet功能：

1. 选择接口：`写入Excel` 或 `写入并读取（组合操作）`
2. 编辑JSON请求，为不同单元格指定不同的 `sheetName`
3. 点击"🚀 发送请求"
4. 查看响应中的 `sheetName` 字段

## 相关文档

- [API测试指南](API_TEST_GUIDE.md)
- [快速开始](QUICK_START.md)
- [API示例](API_EXAMPLES.md)
- [更新日志](CHANGELOG.md)

## 总结

✅ **v1.2.2新增**: 支持单次请求操作多个Sheet  
✅ **向后兼容**: 不影响现有单Sheet操作  
✅ **灵活配置**: 全局默认 + 单元格级别指定  
✅ **响应增强**: 返回结果包含Sheet信息  

---

**立即升级使用多Sheet功能！** 🚀

