# Excel公式支持情况说明

## 📊 Apache POI 公式支持概览

Apache POI 5.2.5版本支持大部分常用Excel函数，但某些高级或特殊函数可能不被支持或仅部分支持。

### 支持程度分类

| 类别 | 支持率 | 说明 |
|------|--------|------|
| ✅ **完全支持** | ~85% | 常用函数，计算结果准确 |
| ⚠️ **部分支持** | ~10% | 函数可识别，但某些参数可能有限制 |
| ❌ **不支持** | ~5% | 无法识别或计算，返回错误 |

---

## ✅ 完全支持的函数

### 数学和三角函数

| 函数 | 说明 | 示例 |
|------|------|------|
| `SUM` | 求和 | `=SUM(A1:A10)` |
| `AVERAGE` | 平均值 | `=AVERAGE(A1:A10)` |
| `MAX` | 最大值 | `=MAX(A1:A10)` |
| `MIN` | 最小值 | `=MIN(A1:A10)` |
| `COUNT` | 计数（数字） | `=COUNT(A1:A10)` |
| `ROUND` | 四舍五入 | `=ROUND(A1,2)` |
| `ROUNDUP` | 向上舍入 | `=ROUNDUP(A1,2)` |
| `ROUNDDOWN` | 向下舍入 | `=ROUNDDOWN(A1,2)` |
| `ABS` | 绝对值 | `=ABS(A1)` |
| `POWER` | 幂运算 | `=POWER(2,3)` |
| `SQRT` | 平方根 | `=SQRT(16)` |
| `MOD` | 取余数 | `=MOD(10,3)` |
| `INT` | 取整数 | `=INT(5.9)` |
| `CEILING` | 向上取整 | `=CEILING(4.3,1)` |
| `FLOOR` | 向下取整 | `=FLOOR(4.7,1)` |
| `PRODUCT` | 乘积 | `=PRODUCT(A1:A5)` |
| `SUMPRODUCT` | 数组乘积求和 | `=SUMPRODUCT(A1:A5,B1:B5)` |
| `PI` | 圆周率 | `=PI()` |
| `SIN`, `COS`, `TAN` | 三角函数 | `=SIN(A1)` |

### 逻辑函数

| 函数 | 说明 | 示例 |
|------|------|------|
| `IF` | 条件判断 | `=IF(A1>60,"及格","不及格")` |
| `AND` | 逻辑与 | `=AND(A1>0,A1<100)` |
| `OR` | 逻辑或 | `=OR(A1="A",A1="B")` |
| `NOT` | 逻辑非 | `=NOT(A1>0)` |
| `TRUE` | 返回TRUE | `=TRUE()` |
| `FALSE` | 返回FALSE | `=FALSE()` |
| `IFERROR` | 错误处理 | `=IFERROR(A1/B1,0)` |
| `IFNA` | NA错误处理 | `=IFNA(VLOOKUP(...),0)` |

### 文本函数

| 函数 | 说明 | 示例 |
|------|------|------|
| `CONCATENATE` | 连接文本 | `=CONCATENATE(A1,B1)` |
| `CONCAT` | 连接文本（新版） | `=CONCAT(A1:A5)` |
| `LEFT` | 左侧字符 | `=LEFT(A1,3)` |
| `RIGHT` | 右侧字符 | `=RIGHT(A1,3)` |
| `MID` | 中间字符 | `=MID(A1,2,3)` |
| `LEN` | 字符长度 | `=LEN(A1)` |
| `UPPER` | 转大写 | `=UPPER(A1)` |
| `LOWER` | 转小写 | `=LOWER(A1)` |
| `PROPER` | 首字母大写 | `=PROPER(A1)` |
| `TRIM` | 去除空格 | `=TRIM(A1)` |
| `SUBSTITUTE` | 替换文本 | `=SUBSTITUTE(A1,"old","new")` |
| `REPLACE` | 替换字符 | `=REPLACE(A1,1,3,"new")` |
| `FIND` | 查找位置（区分大小写） | `=FIND("text",A1)` |
| `SEARCH` | 查找位置（不区分大小写） | `=SEARCH("text",A1)` |
| `TEXT` | 格式化数字 | `=TEXT(A1,"0.00")` |
| `VALUE` | 文本转数字 | `=VALUE("123")` |

### 日期和时间函数

| 函数 | 说明 | 示例 |
|------|------|------|
| `TODAY` | 今天日期 | `=TODAY()` |
| `NOW` | 当前时间 | `=NOW()` |
| `DATE` | 创建日期 | `=DATE(2024,10,24)` |
| `TIME` | 创建时间 | `=TIME(14,30,0)` |
| `YEAR` | 提取年份 | `=YEAR(A1)` |
| `MONTH` | 提取月份 | `=MONTH(A1)` |
| `DAY` | 提取日期 | `=DAY(A1)` |
| `HOUR` | 提取小时 | `=HOUR(A1)` |
| `MINUTE` | 提取分钟 | `=MINUTE(A1)` |
| `SECOND` | 提取秒 | `=SECOND(A1)` |
| `WEEKDAY` | 星期几 | `=WEEKDAY(A1)` |
| `DATEDIF` | 日期差 | `=DATEDIF(A1,B1,"D")` |
| `EDATE` | 月份加减 | `=EDATE(A1,3)` |
| `EOMONTH` | 月末日期 | `=EOMONTH(A1,0)` |

### 查找和引用函数

| 函数 | 说明 | 示例 |
|------|------|------|
| `VLOOKUP` | 垂直查找 | `=VLOOKUP(A1,B1:D10,2,FALSE)` |
| `HLOOKUP` | 水平查找 | `=HLOOKUP(A1,B1:J4,2,FALSE)` |
| `INDEX` | 返回索引值 | `=INDEX(A1:A10,5)` |
| `MATCH` | 返回位置 | `=MATCH(A1,B1:B10,0)` |
| `OFFSET` | 偏移引用 | `=OFFSET(A1,1,1)` |
| `INDIRECT` | 间接引用 | `=INDIRECT("A"&B1)` |
| `CHOOSE` | 选择值 | `=CHOOSE(2,"A","B","C")` |
| `ROW` | 行号 | `=ROW()` |
| `COLUMN` | 列号 | `=COLUMN()` |

### 统计函数

| 函数 | 说明 | 示例 |
|------|------|------|
| `COUNTA` | 计数（非空） | `=COUNTA(A1:A10)` |
| `COUNTBLANK` | 计数（空白） | `=COUNTBLANK(A1:A10)` |
| `COUNTIF` | 条件计数 | `=COUNTIF(A1:A10,">60")` |
| `COUNTIFS` | 多条件计数 | `=COUNTIFS(A1:A10,">60",B1:B10,"<80")` |
| `SUMIF` | 条件求和 | `=SUMIF(A1:A10,">60")` |
| `SUMIFS` | 多条件求和 | `=SUMIFS(C1:C10,A1:A10,">60",B1:B10,"男")` |
| `AVERAGEIF` | 条件平均值 | `=AVERAGEIF(A1:A10,">60")` |
| `AVERAGEIFS` | 多条件平均值 | `=AVERAGEIFS(C1:C10,A1:A10,">60",B1:B10,"男")` |
| `MEDIAN` | 中位数 | `=MEDIAN(A1:A10)` |
| `MODE` | 众数 | `=MODE(A1:A10)` |
| `STDEV` | 标准差 | `=STDEV(A1:A10)` |
| `VAR` | 方差 | `=VAR(A1:A10)` |

### 财务函数

| 函数 | 说明 | 示例 |
|------|------|------|
| `PMT` | 贷款月供 | `=PMT(0.05/12,360,1000000)` |
| `PV` | 现值 | `=PV(0.05,10,1000)` |
| `FV` | 终值 | `=FV(0.05,10,1000)` |
| `NPV` | 净现值 | `=NPV(0.1,A1:A10)` |
| `IRR` | 内部收益率 | `=IRR(A1:A10)` |

---

## ⚠️ 部分支持或有限制的函数

### 数组公式
```excel
=SUM(IF(A1:A10>60,B1:B10,0))  // ⚠️ 需要Ctrl+Shift+Enter，POI可能无法正确计算
```
**建议**：改用 SUMIF 或 SUMIFS

### 动态数组函数（Excel 365新功能）
```excel
=FILTER(A1:B10,C1:C10>60)     // ❌ 不支持
=SORT(A1:A10)                 // ❌ 不支持
=UNIQUE(A1:A10)               // ❌ 不支持
=SEQUENCE(10)                 // ❌ 不支持
```
**原因**：这些是Excel 365引入的新函数，POI尚未支持

### 复杂的文本函数
```excel
=TEXTJOIN(",",TRUE,A1:A10)    // ⚠️ 可能不支持
=TEXTSPLIT(A1,",")            // ❌ 不支持（Excel 365新功能）
```

### 高级统计函数
```excel
=FORECAST.LINEAR(x,known_y,known_x)  // ⚠️ 可能有限制
=TREND(known_y,known_x,new_x)        // ⚠️ 可能有限制
```

---

## ❌ 完全不支持的函数

### 1. Web和数据连接函数
```excel
=WEBSERVICE(url)              // ❌ 不支持
=FILTERXML(xml,xpath)         // ❌ 不支持
=ENCODEURL(text)              // ❌ 不支持
```
**原因**：需要外部网络连接，POI无法执行

### 2. 数据透视表函数
```excel
=GETPIVOTDATA(...)            // ❌ 不支持
```
**原因**：依赖数据透视表结构

### 3. 立方体函数（OLAP）
```excel
=CUBEVALUE(...)               // ❌ 不支持
=CUBEMEMBER(...)              // ❌ 不支持
=CUBESET(...)                 // ❌ 不支持
```
**原因**：需要连接到OLAP数据源

### 4. 特殊的Excel 365函数
```excel
=XLOOKUP(...)                 // ❌ 不支持（使用VLOOKUP替代）
=XMATCH(...)                  // ❌ 不支持（使用MATCH替代）
=LET(...)                     // ❌ 不支持
=LAMBDA(...)                  // ❌ 不支持
```

### 5. 某些信息函数
```excel
=INFO("osversion")            // ❌ 不支持
=CELL("filename")             // ⚠️ 可能返回错误结果
```

### 6. 部分数据库函数
```excel
=DSUM(database,field,criteria)      // ⚠️ 支持有限
=DAVERAGE(...)                      // ⚠️ 支持有限
```

---

## 🔍 如何检测函数支持情况

### 方法1：测试公式
```bash
curl -X POST "http://localhost:8080/api/excel/operation" \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "test.xlsx",
    "writeRequest": {
      "sheetName": "Test",
      "cells": [
        {"cellAddress": "A1", "value": 10, "valueType": "NUMBER"},
        {"cellAddress": "A2", "value": 20, "valueType": "NUMBER"},
        {"cellAddress": "A3", "value": "SUM(A1:A2)", "valueType": "FORMULA"}
      ]
    },
    "readRequest": {
      "sheetName": "Test",
      "cells": [{"cellAddress": "A3"}]
    }
  }'
```

如果返回：
- **数值结果** → ✅ 支持
- **`#NAME?`** → ❌ 函数名不识别
- **`#VALUE!`** → ⚠️ 参数错误或不支持
- **`#REF!`** → ⚠️ 引用错误
- **`#ERROR`** → ❌ 计算失败

### 方法2：查看POI官方文档
访问：https://poi.apache.org/components/spreadsheet/eval.html

---

## 💡 替代方案

### 不支持的函数替代方法

#### XLOOKUP → 使用 VLOOKUP
```excel
❌ =XLOOKUP(A1,B:B,C:C)
✅ =VLOOKUP(A1,B:C,2,FALSE)
```

#### TEXTJOIN → 使用 CONCATENATE
```excel
❌ =TEXTJOIN(",",TRUE,A1:A5)
✅ =CONCATENATE(A1,",",A2,",",A3,",",A4,",",A5)
```

#### FILTER → 使用多次IF或在应用层处理
```excel
❌ =FILTER(A1:B10,C1:C10>60)
✅ 在应用程序中过滤数据后再写入Excel
```

#### 数组公式 → 使用辅助列
```excel
❌ =SUM(IF(A1:A10>60,B1:B10,0))
✅ 方案1: =SUMIF(A1:A10,">60",B1:B10)
✅ 方案2: 使用辅助列计算IF，然后SUM辅助列
```

---

## 🧪 测试示例

### 测试常用函数
```bash
# 测试数学函数
curl -X POST "http://localhost:8080/api/excel/operation" \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "function_test.xlsx",
    "writeRequest": {
      "sheetName": "Math",
      "cells": [
        {"cellAddress": "A1", "value": 10, "valueType": "NUMBER"},
        {"cellAddress": "A2", "value": 20, "valueType": "NUMBER"},
        {"cellAddress": "B1", "value": "SUM(A1:A2)", "valueType": "FORMULA"},
        {"cellAddress": "B2", "value": "AVERAGE(A1:A2)", "valueType": "FORMULA"},
        {"cellAddress": "B3", "value": "MAX(A1:A2)", "valueType": "FORMULA"},
        {"cellAddress": "B4", "value": "MIN(A1:A2)", "valueType": "FORMULA"}
      ]
    },
    "readRequest": {
      "sheetName": "Math",
      "cells": [
        {"cellAddress": "B1"},
        {"cellAddress": "B2"},
        {"cellAddress": "B3"},
        {"cellAddress": "B4"}
      ]
    }
  }'
```

### 测试不支持的函数
```bash
# 测试XLOOKUP（预期会失败）
curl -X POST "http://localhost:8080/api/excel/write" \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "test.xlsx",
    "sheetName": "Test",
    "cells": [
      {"cellAddress": "A1", "value": "XLOOKUP(1,B:B,C:C)", "valueType": "FORMULA"}
    ]
  }'
```

---

## 📊 函数支持统计表

| 函数类别 | 总数 | 支持 | 部分支持 | 不支持 |
|---------|------|------|---------|--------|
| 数学和三角 | ~70 | ~65 | ~3 | ~2 |
| 统计 | ~90 | ~75 | ~10 | ~5 |
| 逻辑 | ~10 | ~10 | 0 | 0 |
| 文本 | ~30 | ~25 | ~3 | ~2 |
| 日期时间 | ~25 | ~22 | ~2 | ~1 |
| 查找引用 | ~20 | ~15 | ~3 | ~2 |
| 财务 | ~50 | ~40 | ~8 | ~2 |
| 信息 | ~15 | ~10 | ~3 | ~2 |
| 工程 | ~40 | ~35 | ~3 | ~2 |
| **合计** | **~350** | **~297** | **~35** | **~18** |

**支持率：约85%**

---

## 🔧 最佳实践

### 1. 使用成熟的函数
优先使用常见、稳定的函数，如：
- SUM, AVERAGE, MAX, MIN
- IF, AND, OR
- VLOOKUP, INDEX, MATCH
- COUNTIF, SUMIF

### 2. 测试新函数
在生产环境使用前，先测试函数是否支持：
```bash
# 创建测试Excel
# 尝试读取计算结果
# 检查是否返回错误
```

### 3. 提供降级方案
```javascript
// 应用层判断
if (result.valueType === 'ERROR') {
  // 函数不支持，使用应用层计算
  const manualResult = calculateInApp(data);
  return manualResult;
}
```

### 4. 记录不支持的函数
```javascript
const UNSUPPORTED_FUNCTIONS = [
  'XLOOKUP',
  'FILTER',
  'SORT',
  'UNIQUE',
  'TEXTJOIN',
  'WEBSERVICE'
];

function checkFormula(formula) {
  for (const func of UNSUPPORTED_FUNCTIONS) {
    if (formula.toUpperCase().includes(func)) {
      console.warn(`警告：公式包含可能不支持的函数 ${func}`);
    }
  }
}
```

---

## 📚 参考资源

- **POI官方函数支持列表**：https://poi.apache.org/components/spreadsheet/eval-devguide.html
- **Excel函数参考**：https://support.microsoft.com/zh-cn/office/excel-函数
- **POI GitHub Issues**：搜索特定函数的支持情况

---

## 🆘 遇到不支持的函数怎么办？

### 方案1：使用替代函数
查看本文档的"替代方案"章节

### 方案2：在应用层计算
```javascript
// 将复杂计算移到应用程序
const result = calculateInApp(data);
// 只将结果写入Excel
writeToExcel(result);
```

### 方案3：使用辅助列
将复杂公式拆分成多个简单公式

### 方案4：升级POI版本
关注POI最新版本，新版本可能增加对更多函数的支持

---

**最后更新：2024-10-24**  
**POI版本：5.2.5**

