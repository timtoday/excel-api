# Excel API 使用示例

## 基础示例

### 1. 创建一个简单的财务报表

#### 步骤1：上传模板文件
```bash
curl -X POST "http://localhost:8080/api/excel/upload" \
  -F "file=@financial_template.xlsx" \
  -F "fileName=financial_report.xlsx"
```

#### 步骤2：写入数据和公式
```bash
curl -X POST "http://localhost:8080/api/excel/write" \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "financial_report.xlsx",
    "sheetName": "2024报表",
    "cells": [
      {"cellAddress": "A1", "value": "项目", "valueType": "STRING"},
      {"cellAddress": "B1", "value": "金额", "valueType": "STRING"},
      
      {"cellAddress": "A2", "value": "收入", "valueType": "STRING"},
      {"cellAddress": "B2", "value": 100000, "valueType": "NUMBER"},
      
      {"cellAddress": "A3", "value": "成本", "valueType": "STRING"},
      {"cellAddress": "B3", "value": 60000, "valueType": "NUMBER"},
      
      {"cellAddress": "A4", "value": "利润", "valueType": "STRING"},
      {"cellAddress": "B4", "value": "B2-B3", "valueType": "FORMULA"},
      
      {"cellAddress": "A5", "value": "利润率", "valueType": "STRING"},
      {"cellAddress": "B5", "value": "B4/B2", "valueType": "FORMULA"}
    ]
  }'
```

#### 步骤3：读取计算结果
```bash
curl -X POST "http://localhost:8080/api/excel/read" \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "financial_report.xlsx",
    "sheetName": "2024报表",
    "cells": [
      {"cellAddress": "B4"},
      {"cellAddress": "B5"}
    ],
    "readFormula": false
  }'
```

响应：
```json
{
  "success": true,
  "data": [
    {"cellAddress": "B4", "value": 40000, "formula": "B2-B3", "valueType": "NUMERIC"},
    {"cellAddress": "B5", "value": 0.4, "formula": "B4/B2", "valueType": "NUMERIC"}
  ]
}
```

---

## 高级示例

### 2. 使用SUM函数计算总和

```bash
curl -X POST "http://localhost:8080/api/excel/operation" \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "sales.xlsx",
    "writeRequest": {
      "sheetName": "月度销售",
      "cells": [
        {"cellAddress": "A1", "value": "1月", "valueType": "STRING"},
        {"cellAddress": "B1", "value": 10000, "valueType": "NUMBER"},
        
        {"cellAddress": "A2", "value": "2月", "valueType": "STRING"},
        {"cellAddress": "B2", "value": 12000, "valueType": "NUMBER"},
        
        {"cellAddress": "A3", "value": "3月", "valueType": "STRING"},
        {"cellAddress": "B3", "value": 15000, "valueType": "NUMBER"},
        
        {"cellAddress": "A4", "value": "总计", "valueType": "STRING"},
        {"cellAddress": "B4", "value": "SUM(B1:B3)", "valueType": "FORMULA"}
      ]
    },
    "readRequest": {
      "sheetName": "月度销售",
      "cells": [{"cellAddress": "B4"}],
      "readFormula": false
    }
  }'
```

### 3. 使用AVERAGE和IF函数

```bash
curl -X POST "http://localhost:8080/api/excel/operation" \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "scores.xlsx",
    "writeRequest": {
      "sheetName": "成绩单",
      "cells": [
        {"cellAddress": "A1", "value": "姓名", "valueType": "STRING"},
        {"cellAddress": "B1", "value": "数学", "valueType": "STRING"},
        {"cellAddress": "C1", "value": "英语", "valueType": "STRING"},
        {"cellAddress": "D1", "value": "平均分", "valueType": "STRING"},
        {"cellAddress": "E1", "value": "是否及格", "valueType": "STRING"},
        
        {"cellAddress": "A2", "value": "张三", "valueType": "STRING"},
        {"cellAddress": "B2", "value": 85, "valueType": "NUMBER"},
        {"cellAddress": "C2", "value": 90, "valueType": "NUMBER"},
        {"cellAddress": "D2", "value": "AVERAGE(B2:C2)", "valueType": "FORMULA"},
        {"cellAddress": "E2", "value": "IF(D2>=60,\"及格\",\"不及格\")", "valueType": "FORMULA"},
        
        {"cellAddress": "A3", "value": "李四", "valueType": "STRING"},
        {"cellAddress": "B3", "value": 55, "valueType": "NUMBER"},
        {"cellAddress": "C3", "value": 60, "valueType": "NUMBER"},
        {"cellAddress": "D3", "value": "AVERAGE(B3:C3)", "valueType": "FORMULA"},
        {"cellAddress": "E3", "value": "IF(D3>=60,\"及格\",\"不及格\")", "valueType": "FORMULA"}
      ]
    },
    "readRequest": {
      "sheetName": "成绩单",
      "cells": [
        {"cellAddress": "D2"},
        {"cellAddress": "E2"},
        {"cellAddress": "D3"},
        {"cellAddress": "E3"}
      ],
      "readFormula": false
    }
  }'
```

### 4. 使用VLOOKUP查找

```bash
curl -X POST "http://localhost:8080/api/excel/write" \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "products.xlsx",
    "sheetName": "产品表",
    "cells": [
      {"cellAddress": "A1", "value": "产品ID", "valueType": "STRING"},
      {"cellAddress": "B1", "value": "产品名称", "valueType": "STRING"},
      {"cellAddress": "C1", "value": "单价", "valueType": "STRING"},
      
      {"cellAddress": "A2", "value": "P001", "valueType": "STRING"},
      {"cellAddress": "B2", "value": "笔记本电脑", "valueType": "STRING"},
      {"cellAddress": "C2", "value": 5999, "valueType": "NUMBER"},
      
      {"cellAddress": "A3", "value": "P002", "valueType": "STRING"},
      {"cellAddress": "B3", "value": "鼠标", "valueType": "STRING"},
      {"cellAddress": "C3", "value": 99, "valueType": "NUMBER"},
      
      {"cellAddress": "A6", "value": "查询ID", "valueType": "STRING"},
      {"cellAddress": "B6", "value": "P001", "valueType": "STRING"},
      
      {"cellAddress": "A7", "value": "产品名称", "valueType": "STRING"},
      {"cellAddress": "B7", "value": "VLOOKUP(B6,A2:C3,2,FALSE)", "valueType": "FORMULA"},
      
      {"cellAddress": "A8", "value": "价格", "valueType": "STRING"},
      {"cellAddress": "B8", "value": "VLOOKUP(B6,A2:C3,3,FALSE)", "valueType": "FORMULA"}
    ]
  }'
```

---

## 实际应用场景

### 5. 库存管理系统

```javascript
// JavaScript示例 - 在前端应用中使用
async function updateInventory(productId, quantity) {
  // 1. 写入新的库存数据
  const writeResponse = await fetch('http://localhost:8080/api/excel/write', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({
      fileName: 'inventory.xlsx',
      sheetName: '库存表',
      cells: [
        {cellAddress: 'C2', value: quantity, valueType: 'NUMBER'}
      ]
    })
  });
  
  // 2. 读取计算后的库存金额（数量 * 单价）
  const readResponse = await fetch('http://localhost:8080/api/excel/read', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({
      fileName: 'inventory.xlsx',
      sheetName: '库存表',
      cells: [{cellAddress: 'D2'}],  // 金额列
      readFormula: false
    })
  });
  
  const result = await readResponse.json();
  return result.data[0].value;
}
```

### 6. 薪资计算系统

```python
# Python示例
import requests

def calculate_salary(employee_id, base_salary, bonus):
    url = 'http://localhost:8080/api/excel/operation'
    
    payload = {
        'fileName': 'payroll.xlsx',
        'writeRequest': {
            'sheetName': f'员工_{employee_id}',
            'cells': [
                {'cellAddress': 'A1', 'value': '基本工资', 'valueType': 'STRING'},
                {'cellAddress': 'B1', 'value': base_salary, 'valueType': 'NUMBER'},
                
                {'cellAddress': 'A2', 'value': '奖金', 'valueType': 'STRING'},
                {'cellAddress': 'B2', 'value': bonus, 'valueType': 'NUMBER'},
                
                {'cellAddress': 'A3', 'value': '应发工资', 'valueType': 'STRING'},
                {'cellAddress': 'B3', 'value': 'B1+B2', 'valueType': 'FORMULA'},
                
                {'cellAddress': 'A4', 'value': '个税(20%)', 'valueType': 'STRING'},
                {'cellAddress': 'B4', 'value': 'B3*0.2', 'valueType': 'FORMULA'},
                
                {'cellAddress': 'A5', 'value': '实发工资', 'valueType': 'STRING'},
                {'cellAddress': 'B5', 'value': 'B3-B4', 'valueType': 'FORMULA'}
            ]
        },
        'readRequest': {
            'sheetName': f'员工_{employee_id}',
            'cells': [
                {'cellAddress': 'B3'},  # 应发工资
                {'cellAddress': 'B4'},  # 个税
                {'cellAddress': 'B5'}   # 实发工资
            ],
            'readFormula': False
        }
    }
    
    response = requests.post(url, json=payload)
    result = response.json()
    
    return {
        'gross_salary': result['data'][0]['value'],
        'tax': result['data'][1]['value'],
        'net_salary': result['data'][2]['value']
    }

# 使用示例
salary_info = calculate_salary('E001', 10000, 2000)
print(f"实发工资: {salary_info['net_salary']}")
```

### 7. 报价单生成

```bash
# 批量更新产品价格和计算总价
curl -X POST "http://localhost:8080/api/excel/operation" \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "quotation.xlsx",
    "writeRequest": {
      "sheetName": "报价单",
      "cells": [
        {"cellAddress": "A1", "value": "产品", "valueType": "STRING"},
        {"cellAddress": "B1", "value": "单价", "valueType": "STRING"},
        {"cellAddress": "C1", "value": "数量", "valueType": "STRING"},
        {"cellAddress": "D1", "value": "小计", "valueType": "STRING"},
        
        {"cellAddress": "A2", "value": "商品A", "valueType": "STRING"},
        {"cellAddress": "B2", "value": 99.9, "valueType": "NUMBER"},
        {"cellAddress": "C2", "value": 10, "valueType": "NUMBER"},
        {"cellAddress": "D2", "value": "B2*C2", "valueType": "FORMULA"},
        
        {"cellAddress": "A3", "value": "商品B", "valueType": "STRING"},
        {"cellAddress": "B3", "value": 199.9, "valueType": "NUMBER"},
        {"cellAddress": "C3", "value": 5, "valueType": "NUMBER"},
        {"cellAddress": "D3", "value": "B3*C3", "valueType": "FORMULA"},
        
        {"cellAddress": "A4", "value": "商品C", "valueType": "STRING"},
        {"cellAddress": "B4", "value": 299.9, "valueType": "NUMBER"},
        {"cellAddress": "C4", "value": 3, "valueType": "NUMBER"},
        {"cellAddress": "D4", "value": "B4*C4", "valueType": "FORMULA"},
        
        {"cellAddress": "A6", "value": "总计", "valueType": "STRING"},
        {"cellAddress": "D6", "value": "SUM(D2:D4)", "valueType": "FORMULA"},
        
        {"cellAddress": "A7", "value": "折扣(10%)", "valueType": "STRING"},
        {"cellAddress": "D7", "value": "D6*0.1", "valueType": "FORMULA"},
        
        {"cellAddress": "A8", "value": "应付金额", "valueType": "STRING"},
        {"cellAddress": "D8", "value": "D6-D7", "valueType": "FORMULA"}
      ]
    },
    "readRequest": {
      "sheetName": "报价单",
      "cells": [
        {"cellAddress": "D6"},
        {"cellAddress": "D7"},
        {"cellAddress": "D8"}
      ],
      "readFormula": false
    }
  }'
```

---

## 🎨 模板功能示例

### 8. 批量生成工资条

```bash
# 步骤1：准备模板 salary_template.xlsx
# 模板包含：姓名、工号、基本工资、奖金、应发工资（公式）、实发工资（公式）

# 步骤2：批量生成（Python脚本）
```

```python
import requests

employees = [
    {"name": "张三", "id": "E001", "base": 10000, "bonus": 2000},
    {"name": "李四", "id": "E002", "base": 12000, "bonus": 3000},
]

for emp in employees:
    response = requests.post(
        'http://localhost:8080/api/excel/template/fill-and-download',
        json={
            'templateFileName': 'salary_template.xlsx',
            'downloadFileName': f'salary_{emp["id"]}.xlsx',
            'sheetName': 'Sheet1',
            'cells': [
                {'cellAddress': 'B1', 'value': emp['name'], 'valueType': 'STRING'},
                {'cellAddress': 'B2', 'value': emp['id'], 'valueType': 'STRING'},
                {'cellAddress': 'B3', 'value': emp['base'], 'valueType': 'NUMBER'},
                {'cellAddress': 'B4', 'value': emp['bonus'], 'valueType': 'NUMBER'}
                # B5的公式在模板中已定义：=B3+B4（应发工资）
                # B6的公式在模板中已定义：=B5*0.9（实发工资，扣除10%）
            ]
        }
    )
    
    with open(f'output/salary_{emp["id"]}.xlsx', 'wb') as f:
        f.write(response.content)
    
    print(f'✅ 已生成: {emp["name"]}')
```

### 9. 动态生成销售报表

```bash
curl -X POST "http://localhost:8080/api/excel/template/fill-and-download" \
  -H "Content-Type: application/json" \
  -d '{
    "templateFileName": "sales_report_template.xlsx",
    "downloadFileName": "sales_report_2024_q3.xlsx",
    "sheetName": "Report",
    "cells": [
      {"cellAddress": "B1", "value": "2024年第三季度", "valueType": "STRING"},
      {"cellAddress": "B2", "value": "销售部", "valueType": "STRING"},
      {"cellAddress": "B5", "value": 100000, "valueType": "NUMBER"},
      {"cellAddress": "B6", "value": 150000, "valueType": "NUMBER"},
      {"cellAddress": "B7", "value": 200000, "valueType": "NUMBER"}
    ]
  }' \
  --output sales_report_2024_q3.xlsx
```

### 10. 批量生成证书

```javascript
// Node.js示例：批量生成学员证书
const axios = require('axios');
const fs = require('fs');

const students = [
  {name: '张小明', course: 'Java开发', score: 95, date: '2024-10-24'},
  {name: '李小红', course: 'Python开发', score: 92, date: '2024-10-24'},
  // ... 更多学员
];

async function generateCertificates() {
  for (const student of students) {
    const response = await axios.post(
      'http://localhost:8080/api/excel/template/fill-and-download',
      {
        templateFileName: 'certificate_template.xlsx',
        downloadFileName: `certificate_${student.name}.xlsx`,
        sheetName: 'Certificate',
        cells: [
          {cellAddress: 'B5', value: student.name, valueType: 'STRING'},
          {cellAddress: 'B7', value: student.course, valueType: 'STRING'},
          {cellAddress: 'B9', value: student.score, valueType: 'NUMBER'},
          {cellAddress: 'B11', value: student.date, valueType: 'STRING'}
        ]
      },
      {responseType: 'arraybuffer'}
    );
    
    fs.writeFileSync(`certificates/${student.name}.xlsx`, response.data);
    console.log(`✅ ${student.name} 的证书已生成`);
  }
}

generateCertificates();
```

> 📖 **更多模板示例**：查看 [TEMPLATE_GUIDE.md](TEMPLATE_GUIDE.md) 了解详细的模板功能

---

## 错误处理示例

### 处理并发锁超时

```javascript
async function safeWriteExcel(data) {
  try {
    const response = await fetch('http://localhost:8080/api/excel/write', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(data)
    });
    
    const result = await response.json();
    
    if (!result.success) {
      if (result.message.includes('锁')) {
        console.log('文件被占用，5秒后重试...');
        await new Promise(resolve => setTimeout(resolve, 5000));
        return safeWriteExcel(data);  // 重试
      }
      throw new Error(result.message);
    }
    
    return result;
  } catch (error) {
    console.error('写入失败:', error);
    throw error;
  }
}
```

---

## 性能优化建议

### 批量操作
```bash
# 不推荐：多次调用
for i in {1..100}; do
  curl -X POST "http://localhost:8080/api/excel/write" \
    -H "Content-Type: application/json" \
    -d "{...}"
done

# 推荐：一次性批量写入
curl -X POST "http://localhost:8080/api/excel/write" \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "data.xlsx",
    "sheetName": "Sheet1",
    "cells": [
      {"cellAddress": "A1", "value": "数据1"},
      {"cellAddress": "A2", "value": "数据2"},
      ...
      {"cellAddress": "A100", "value": "数据100"}
    ]
  }'
```

