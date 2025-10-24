# 参数格式迁移指南

## ✨ 新版本改进

### 核心变化

**从行列号 → Excel单元格地址**

- ✅ 更符合Excel用户习惯
- ✅ 无需计算行列索引
- ✅ 更直观易读
- ✅ 减少出错概率

---

## 📊 对比说明

### 旧格式（已废弃）

```json
{
  "cells": [
    {
      "row": 0,      // ❌ 需要计算：Excel第1行 = row 0
      "col": 0,      // ❌ 需要计算：Excel A列 = col 0
      "value": "数据"
    },
    {
      "row": 26,     // ❌ 不直观：这是第27行
      "col": 26,     // ❌ 不直观：这是AA列
      "value": "数据"
    }
  ]
}
```

### 新格式（推荐）

```json
{
  "cells": [
    {
      "cellAddress": "A1",   // ✅ 直接使用Excel地址
      "value": "数据"
    },
    {
      "cellAddress": "AA27", // ✅ 一目了然
      "value": "数据"
    }
  ]
}
```

---

## 🔄 迁移步骤

### 步骤1：写入请求迁移

#### 旧代码
```javascript
// JavaScript示例
{
  "fileName": "data.xlsx",
  "sheetName": "Sheet1",
  "cells": [
    {"row": 0, "col": 0, "value": "标题", "valueType": "STRING"},
    {"row": 1, "col": 0, "value": 100, "valueType": "NUMBER"},
    {"row": 2, "col": 0, "value": "A2*2", "valueType": "FORMULA"}
  ]
}
```

#### 新代码
```javascript
{
  "fileName": "data.xlsx",
  "sheetName": "Sheet1",
  "cells": [
    {"cellAddress": "A1", "value": "标题", "valueType": "STRING"},
    {"cellAddress": "A2", "value": 100, "valueType": "NUMBER"},
    {"cellAddress": "A3", "value": "A2*2", "valueType": "FORMULA"}
  ]
}
```

### 步骤2：读取请求迁移

#### 旧代码
```python
# Python示例
{
  "fileName": "data.xlsx",
  "sheetName": "Sheet1",
  "cells": [
    {"row": 0, "col": 0},
    {"row": 1, "col": 0},
    {"row": 2, "col": 0}
  ]
}
```

#### 新代码
```python
{
  "fileName": "data.xlsx",
  "sheetName": "Sheet1",
  "cells": [
    {"cellAddress": "A1"},
    {"cellAddress": "A2"},
    {"cellAddress": "A3"}
  ]
}
```

### 步骤3：响应处理迁移

#### 旧响应
```json
{
  "success": true,
  "data": [
    {
      "row": 0,
      "col": 0,
      "value": "数据",
      "cellAddress": "A1",
      "valueType": "STRING"
    }
  ]
}
```

#### 新响应
```json
{
  "success": true,
  "data": [
    {
      "cellAddress": "A1",  // ✅ 只保留cellAddress
      "value": "数据",
      "valueType": "STRING"
      // ❌ row和col字段已移除
    }
  ]
}
```

---

## 🛠️ 转换工具

### JavaScript转换函数

```javascript
// 行列号 → Excel地址
function rowColToAddress(row, col) {
  let colName = '';
  let colNum = col + 1; // 从1开始
  
  while (colNum > 0) {
    const remainder = (colNum - 1) % 26;
    colName = String.fromCharCode(65 + remainder) + colName;
    colNum = Math.floor((colNum - 1) / 26);
  }
  
  return colName + (row + 1);
}

// 使用示例
console.log(rowColToAddress(0, 0));   // "A1"
console.log(rowColToAddress(0, 26));  // "AA1"
console.log(rowColToAddress(26, 0));  // "A27"

// Excel地址 → 行列号（如需要）
function addressToRowCol(address) {
  const match = address.match(/^([A-Z]+)(\d+)$/);
  if (!match) return null;
  
  const col = match[1];
  const row = parseInt(match[2]) - 1;
  
  let colNum = 0;
  for (let i = 0; i < col.length; i++) {
    colNum = colNum * 26 + (col.charCodeAt(i) - 64);
  }
  
  return { row, col: colNum - 1 };
}

console.log(addressToRowCol("A1"));   // {row: 0, col: 0}
console.log(addressToRowCol("AA1"));  // {row: 0, col: 26}
```

### Python转换函数

```python
import re

def row_col_to_address(row, col):
    """行列号转Excel地址"""
    col_name = ''
    col_num = col + 1
    
    while col_num > 0:
        remainder = (col_num - 1) % 26
        col_name = chr(65 + remainder) + col_name
        col_num = (col_num - 1) // 26
    
    return f"{col_name}{row + 1}"

# 使用示例
print(row_col_to_address(0, 0))   # "A1"
print(row_col_to_address(0, 26))  # "AA1"
print(row_col_to_address(26, 0))  # "A27"

def address_to_row_col(address):
    """Excel地址转行列号"""
    match = re.match(r'^([A-Z]+)(\d+)$', address)
    if not match:
        return None
    
    col_str = match.group(1)
    row = int(match.group(2)) - 1
    
    col = 0
    for char in col_str:
        col = col * 26 + (ord(char) - 64)
    
    return {'row': row, 'col': col - 1}

print(address_to_row_col("A1"))   # {'row': 0, 'col': 0}
print(address_to_row_col("AA1"))  # {'row': 0, 'col': 26}
```

### Java转换函数

```java
public class ExcelUtils {
    
    // 行列号 → Excel地址
    public static String rowColToAddress(int row, int col) {
        StringBuilder colName = new StringBuilder();
        int colNum = col + 1;
        
        while (colNum > 0) {
            int remainder = (colNum - 1) % 26;
            colName.insert(0, (char)(65 + remainder));
            colNum = (colNum - 1) / 26;
        }
        
        return colName.toString() + (row + 1);
    }
    
    // 使用Apache POI的CellReference（推荐）
    public static String rowColToAddressPOI(int row, int col) {
        return new org.apache.poi.ss.util.CellReference(row, col)
                .formatAsString();
    }
    
    // 测试
    public static void main(String[] args) {
        System.out.println(rowColToAddress(0, 0));   // "A1"
        System.out.println(rowColToAddress(0, 26));  // "AA1"
        System.out.println(rowColToAddress(26, 0));  // "A27"
    }
}
```

---

## 📦 批量迁移脚本

### Node.js迁移脚本

```javascript
const fs = require('fs');

// 转换函数
function rowColToAddress(row, col) {
  let colName = '';
  let colNum = col + 1;
  
  while (colNum > 0) {
    const remainder = (colNum - 1) % 26;
    colName = String.fromCharCode(65 + remainder) + colName;
    colNum = Math.floor((colNum - 1) / 26);
  }
  
  return colName + (row + 1);
}

// 迁移请求对象
function migrateRequest(oldRequest) {
  const newRequest = { ...oldRequest };
  
  if (newRequest.cells) {
    newRequest.cells = newRequest.cells.map(cell => {
      if ('row' in cell && 'col' in cell) {
        const { row, col, ...rest } = cell;
        return {
          cellAddress: rowColToAddress(row, col),
          ...rest
        };
      }
      return cell;
    });
  }
  
  return newRequest;
}

// 读取旧格式JSON文件并转换
const oldData = JSON.parse(fs.readFileSync('old_request.json', 'utf8'));
const newData = migrateRequest(oldData);
fs.writeFileSync('new_request.json', JSON.stringify(newData, null, 2));

console.log('迁移完成！');
```

### Python迁移脚本

```python
import json

def row_col_to_address(row, col):
    col_name = ''
    col_num = col + 1
    while col_num > 0:
        remainder = (col_num - 1) % 26
        col_name = chr(65 + remainder) + col_name
        col_num = (col_num - 1) // 26
    return f"{col_name}{row + 1}"

def migrate_request(old_request):
    """迁移请求对象"""
    new_request = old_request.copy()
    
    if 'cells' in new_request:
        new_cells = []
        for cell in new_request['cells']:
            if 'row' in cell and 'col' in cell:
                new_cell = {k: v for k, v in cell.items() if k not in ['row', 'col']}
                new_cell['cellAddress'] = row_col_to_address(cell['row'], cell['col'])
                new_cells.append(new_cell)
            else:
                new_cells.append(cell)
        new_request['cells'] = new_cells
    
    return new_request

# 批量迁移文件
def migrate_file(input_file, output_file):
    with open(input_file, 'r', encoding='utf-8') as f:
        old_data = json.load(f)
    
    new_data = migrate_request(old_data)
    
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(new_data, f, ensure_ascii=False, indent=2)
    
    print(f'已迁移: {input_file} -> {output_file}')

# 使用示例
migrate_file('old_request.json', 'new_request.json')
```

---

## ✅ 检查清单

迁移完成后，请检查：

- [ ] 所有API调用已更新为新格式
- [ ] 测试用例已更新
- [ ] 文档已更新
- [ ] 响应处理代码已适配（移除row/col依赖）
- [ ] 批量操作已测试
- [ ] 边界情况已测试（如AA列、ZZ列等）

---

## 🎯 最佳实践

### 1. 使用常量或配置

```javascript
// 推荐：使用常量
const CELLS = {
  TITLE: 'A1',
  PRICE: 'B2',
  QUANTITY: 'C2',
  TOTAL: 'D2'
};

const request = {
  cells: [
    { cellAddress: CELLS.PRICE, value: 100 },
    { cellAddress: CELLS.QUANTITY, value: 5 },
    { cellAddress: CELLS.TOTAL, value: `${CELLS.PRICE}*${CELLS.QUANTITY}`, valueType: 'FORMULA' }
  ]
};
```

### 2. 动态生成地址

```javascript
// 批量生成单元格地址
function generateColumn(colLetter, startRow, endRow, values) {
  return values.map((value, index) => ({
    cellAddress: `${colLetter}${startRow + index}`,
    value: value
  }));
}

const cells = generateColumn('A', 1, 10, ['数据1', '数据2', '数据3']);
```

### 3. 区间操作

```javascript
// 生成A1:A10的所有单元格地址
function generateRange(startCell, endCell) {
  // 实现区间生成逻辑
  // 例如："A1" 到 "A10" 生成 ["A1", "A2", ..., "A10"]
}
```

---

## 🆘 常见问题

### Q1: 为什么要改用Excel地址格式？
**A:** 更符合用户习惯，减少计算错误，提高可读性。

### Q2: 旧格式还能用吗？
**A:** 不能，请尽快迁移到新格式。

### Q3: 如何快速迁移大量代码？
**A:** 使用上面提供的迁移脚本进行批量转换。

### Q4: 响应中还有row和col字段吗？
**A:** 没有了，只保留cellAddress字段。

### Q5: 大小写敏感吗？
**A:** Excel地址建议使用大写字母（如"A1"），但系统会自动转换。

---

## 📞 需要帮助？

如果迁移过程中遇到问题：
1. 查看[QUICK_START.md](QUICK_START.md)获取快速示例
2. 参考[API_EXAMPLES.md](API_EXAMPLES.md)了解完整用法
3. 提交Issue获取支持

**祝迁移顺利！** 🚀

