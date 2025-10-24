# Excel模板功能使用指南

## 📋 功能概述

Excel模板功能允许您：
1. 📤 **上传模板文件** - 上传预设格式和公式的Excel模板
2. ✍️ **填充数据** - 通过API向模板指定位置写入数据
3. 📥 **下载结果** - 导出填充后的Excel文件

### 应用场景

- 📊 **批量生成报表** - 基于统一模板生成不同数据的报表
- 📄 **发票/合同生成** - 自动填充客户信息和金额
- 📈 **财务报告** - 填充财务数据自动计算
- 🎓 **成绩单/证书** - 批量生成个性化文档
- 📦 **订单/物流单** - 动态生成业务单据

---

## 🚀 快速开始

### 步骤1：准备模板文件

创建一个Excel模板，例如 `invoice_template.xlsx`：

```
A列            B列             C列
----------------------------------------
1  发票抬头      [公司名称]      
2  发票编号      [发票号]
3  日期          [日期]
4  
5  项目          单价            数量          金额
6  项目1         100            5             =B6*C6
7  项目2         200            3             =B7*C7
8  
9  合计                                       =SUM(D6:D7)
```

### 步骤2：上传模板

```bash
curl -X POST "http://localhost:8080/api/excel/upload" \
  -F "file=@invoice_template.xlsx" \
  -F "fileName=invoice_template.xlsx"
```

### 步骤3：填充模板并下载

```bash
curl -X POST "http://localhost:8080/api/excel/template/fill-and-download" \
  -H "Content-Type: application/json" \
  -d '{
    "templateFileName": "invoice_template.xlsx",
    "downloadFileName": "invoice_2024001.xlsx",
    "sheetName": "Sheet1",
    "cells": [
      {"cellAddress": "B1", "value": "ABC科技有限公司", "valueType": "STRING"},
      {"cellAddress": "B2", "value": "INV-2024-001", "valueType": "STRING"},
      {"cellAddress": "B3", "value": "2024-10-24", "valueType": "STRING"},
      {"cellAddress": "A6", "value": "软件开发服务", "valueType": "STRING"},
      {"cellAddress": "B6", "value": 5000, "valueType": "NUMBER"},
      {"cellAddress": "C6", "value": 1, "valueType": "NUMBER"},
      {"cellAddress": "A7", "value": "技术支持", "valueType": "STRING"},
      {"cellAddress": "B7", "value": 2000, "valueType": "NUMBER"},
      {"cellAddress": "C7", "value": 2, "valueType": "NUMBER"}
    ]
  }' \
  --output invoice_2024001.xlsx
```

**结果**：公式自动计算，D6=5000, D7=4000, D9=9000

---

## 📖 API接口详解

### 1. 下载Excel文件

```http
GET /api/excel/download/{fileName}
```

**功能**：下载服务器上已存在的Excel文件

**示例**：
```bash
curl -O http://localhost:8080/api/excel/download/report.xlsx
```

---

### 2. 基于模板生成文件（保存到服务器）

```http
POST /api/excel/template/generate
```

**功能**：基于模板创建新文件并保存到服务器

**请求体**：
```json
{
  "templateFileName": "template.xlsx",     // 模板文件名
  "outputFileName": "output_001.xlsx",     // 输出文件名（可选）
  "sheetName": "Sheet1",                   // Sheet名称
  "cells": [                               // 要填充的数据
    {"cellAddress": "A1", "value": "数据", "valueType": "STRING"}
  ],
  "keepFile": true                         // 是否保留文件（默认true）
}
```

**响应**：直接返回生成的Excel文件供下载

**示例**：
```bash
curl -X POST "http://localhost:8080/api/excel/template/generate" \
  -H "Content-Type: application/json" \
  -d '{
    "templateFileName": "sales_template.xlsx",
    "outputFileName": "sales_2024_q1.xlsx",
    "sheetName": "Sales",
    "cells": [
      {"cellAddress": "B2", "value": "2024-Q1", "valueType": "STRING"},
      {"cellAddress": "B3", "value": 1000000, "valueType": "NUMBER"}
    ]
  }' \
  --output sales_2024_q1.xlsx
```

---

### 3. 填充模板并下载（不保存）

```http
POST /api/excel/template/fill-and-download
```

**功能**：填充模板后直接下载，不在服务器保留（推荐）

**请求体**：
```json
{
  "templateFileName": "template.xlsx",     // 模板文件名
  "downloadFileName": "download.xlsx",     // 下载文件名（可选）
  "sheetName": "Sheet1",                   // Sheet名称
  "cells": [                               // 要填充的数据
    {"cellAddress": "A1", "value": "数据", "valueType": "STRING"}
  ]
}
```

**响应**：直接返回Excel文件流

**示例**：
```bash
curl -X POST "http://localhost:8080/api/excel/template/fill-and-download" \
  -H "Content-Type: application/json" \
  -d '{
    "templateFileName": "contract_template.xlsx",
    "downloadFileName": "contract_xiaoming.xlsx",
    "sheetName": "Contract",
    "cells": [
      {"cellAddress": "B3", "value": "张小明", "valueType": "STRING"},
      {"cellAddress": "B4", "value": "130****8888", "valueType": "STRING"},
      {"cellAddress": "B8", "value": 50000, "valueType": "NUMBER"}
    ]
  }' \
  --output contract_xiaoming.xlsx
```

---

## 💼 实际应用场景

### 场景1：批量生成员工工资条

#### 模板文件：`salary_template.xlsx`

```
A列              B列
--------------------------
1  姓名            [员工姓名]
2  工号            [工号]
3  基本工资        [基本工资]
4  绩效奖金        [奖金]
5  应发工资        =B3+B4
6  个人所得税      =B5*0.1
7  实发工资        =B5-B6
```

#### Python批量生成脚本

```python
import requests
import json

# 员工数据
employees = [
    {"name": "张三", "id": "E001", "base": 10000, "bonus": 2000},
    {"name": "李四", "id": "E002", "base": 12000, "bonus": 3000},
    {"name": "王五", "id": "E003", "base": 15000, "bonus": 5000},
]

url = 'http://localhost:8080/api/excel/template/fill-and-download'

for emp in employees:
    payload = {
        'templateFileName': 'salary_template.xlsx',
        'downloadFileName': f'salary_{emp["id"]}.xlsx',
        'sheetName': 'Sheet1',
        'cells': [
            {'cellAddress': 'B1', 'value': emp['name'], 'valueType': 'STRING'},
            {'cellAddress': 'B2', 'value': emp['id'], 'valueType': 'STRING'},
            {'cellAddress': 'B3', 'value': emp['base'], 'valueType': 'NUMBER'},
            {'cellAddress': 'B4', 'value': emp['bonus'], 'valueType': 'NUMBER'}
        ]
    }
    
    response = requests.post(url, json=payload)
    
    with open(f'output/salary_{emp["id"]}.xlsx', 'wb') as f:
        f.write(response.content)
    
    print(f'✅ 已生成: {emp["name"]} 的工资条')
```

---

### 场景2：动态生成销售报表

#### 模板文件：`sales_report_template.xlsx`

```
A列          B列        C列        D列
---------------------------------------------
1  销售报表    
2  报表期间    [期间]     销售员     [姓名]
3
4  产品        数量       单价       金额
5  产品A       [数量]     [单价]     =B5*C5
6  产品B       [数量]     [单价]     =B6*C6
7  产品C       [数量]     [单价]     =B7*C7
8
9  总计                              =SUM(D5:D7)
10 佣金(5%)                          =D9*0.05
```

#### JavaScript生成示例

```javascript
const axios = require('axios');
const fs = require('fs');

async function generateSalesReport(salesData) {
  const payload = {
    templateFileName: 'sales_report_template.xlsx',
    downloadFileName: `sales_report_${salesData.period}.xlsx`,
    sheetName: 'Report',
    cells: [
      {cellAddress: 'B2', value: salesData.period, valueType: 'STRING'},
      {cellAddress: 'D2', value: salesData.salesPerson, valueType: 'STRING'},
      {cellAddress: 'B5', value: salesData.productA.quantity, valueType: 'NUMBER'},
      {cellAddress: 'C5', value: salesData.productA.price, valueType: 'NUMBER'},
      {cellAddress: 'B6', value: salesData.productB.quantity, valueType: 'NUMBER'},
      {cellAddress: 'C6', value: salesData.productB.price, valueType: 'NUMBER'},
      {cellAddress: 'B7', value: salesData.productC.quantity, valueType: 'NUMBER'},
      {cellAddress: 'C7', value: salesData.productC.price, valueType: 'NUMBER'}
    ]
  };
  
  const response = await axios.post(
    'http://localhost:8080/api/excel/template/fill-and-download',
    payload,
    { responseType: 'arraybuffer' }
  );
  
  fs.writeFileSync(`output/sales_report_${salesData.period}.xlsx`, response.data);
  console.log(`✅ 报表已生成: ${salesData.period}`);
}

// 使用示例
const salesData = {
  period: '2024-10',
  salesPerson: '张销售',
  productA: { quantity: 100, price: 99 },
  productB: { quantity: 50, price: 199 },
  productC: { quantity: 30, price: 299 }
};

generateSalesReport(salesData);
```

---

### 场景3：自动生成合同文档

#### 模板文件：`contract_template.xlsx`

```
合同编号：[合同号]
甲方：[甲方名称]
乙方：[乙方名称]

合同金额：[金额] 元
签订日期：[日期]
有效期至：[到期日期]

付款方式：
  首付款（30%）：=B5*0.3
  中期款（40%）：=B5*0.4
  尾款（30%）：=B5*0.3
```

#### Java生成示例

```java
public class ContractGenerator {
    
    private static final String API_URL = 
        "http://localhost:8080/api/excel/template/fill-and-download";
    
    public void generateContract(ContractData data) throws IOException {
        // 构建请求
        Map<String, Object> request = new HashMap<>();
        request.put("templateFileName", "contract_template.xlsx");
        request.put("downloadFileName", "contract_" + data.getContractNo() + ".xlsx");
        request.put("sheetName", "Contract");
        
        List<Map<String, Object>> cells = new ArrayList<>();
        cells.add(createCell("B1", data.getContractNo(), "STRING"));
        cells.add(createCell("B2", data.getPartyA(), "STRING"));
        cells.add(createCell("B3", data.getPartyB(), "STRING"));
        cells.add(createCell("B5", data.getAmount(), "NUMBER"));
        cells.add(createCell("B6", data.getSignDate(), "STRING"));
        cells.add(createCell("B7", data.getExpireDate(), "STRING"));
        
        request.put("cells", cells);
        
        // 发送请求并保存文件
        // ... HTTP客户端代码
        
        System.out.println("✅ 合同已生成: " + data.getContractNo());
    }
    
    private Map<String, Object> createCell(String address, Object value, String type) {
        Map<String, Object> cell = new HashMap<>();
        cell.put("cellAddress", address);
        cell.put("value", value);
        cell.put("valueType", type);
        return cell;
    }
}
```

---

### 场景4：批量生成学生成绩单

#### 模板文件：`transcript_template.xlsx`

包含学校logo、成绩表格、签章位置等

#### 批量生成脚本

```python
import pandas as pd
import requests

# 从数据库或CSV读取学生数据
students = pd.read_csv('students.csv')

for _, student in students.iterrows():
    payload = {
        'templateFileName': 'transcript_template.xlsx',
        'downloadFileName': f'transcript_{student["student_id"]}.xlsx',
        'sheetName': 'Transcript',
        'cells': [
            {'cellAddress': 'B2', 'value': student['name'], 'valueType': 'STRING'},
            {'cellAddress': 'B3', 'value': student['student_id'], 'valueType': 'STRING'},
            {'cellAddress': 'B5', 'value': student['math_score'], 'valueType': 'NUMBER'},
            {'cellAddress': 'B6', 'value': student['english_score'], 'valueType': 'NUMBER'},
            {'cellAddress': 'B7', 'value': student['physics_score'], 'valueType': 'NUMBER'},
            # 总分由模板中的公式自动计算
        ]
    }
    
    response = requests.post(
        'http://localhost:8080/api/excel/template/fill-and-download',
        json=payload
    )
    
    with open(f'transcripts/{student["student_id"]}.xlsx', 'wb') as f:
        f.write(response.content)

print(f'✅ 已生成 {len(students)} 份成绩单')
```

---

## 🎨 模板设计技巧

### 1. 使用命名区域（可选）

虽然API使用单元格地址，但在模板中使用命名区域可以提高可读性：

```excel
定义名称：
- CustomerName -> B3
- InvoiceAmount -> D10
```

### 2. 预设公式

在模板中提前设置好所有计算公式：

```excel
=SUM(D5:D9)          // 总计
=IF(B10>1000,B10*0.9,B10)  // 折扣计算
=VLOOKUP(A2,PriceTable,2,FALSE)  // 价格查询
```

### 3. 条件格式

模板可以包含条件格式，填充数据后自动生效：

```excel
如果金额>10000，单元格背景变红
如果成绩<60，显示为红色
```

### 4. 数据验证

为防止错误数据，可在模板中设置数据验证：

```excel
B列：只能输入1-100的数字
C列：只能选择下拉列表中的选项
```

### 5. 保护工作表

保护某些单元格不被修改：

```excel
锁定：标题行、公式单元格、logo
不锁定：数据输入区域
```

---

## 🔧 高级用法

### 1. 动态Sheet名称

```bash
curl -X POST ".../template/fill-and-download" \
  -d '{
    "templateFileName": "multi_sheet_template.xlsx",
    "sheetName": "2024-Q1",  // 动态指定Sheet
    "cells": [...]
  }'
```

### 2. 多Sheet填充

需要填充多个Sheet时，分别调用API：

```python
sheets_data = {
    'Summary': [...],
    'Details': [...],
    'Chart': [...]
}

for sheet_name, cells in sheets_data.items():
    # 先生成中间文件
    # 然后继续填充下一个Sheet
```

### 3. 结合图表

模板中包含图表，数据填充后图表自动更新：

```excel
模板包含：
- 数据表（A1:B10）
- 基于数据表的柱状图

填充数据后，图表自动更新
```

### 4. 合并单元格处理

向合并单元格写入数据时，使用合并区域的左上角单元格地址：

```json
{
  "cellAddress": "A1",  // A1:C1是合并单元格，使用A1
  "value": "标题"
}
```

---

## ⚠️ 注意事项

### 1. 安全考虑

```java
// ❌ 不安全：直接使用用户输入
fileName = userInput;

// ✅ 安全：验证和清理
if (fileName.contains("..") || fileName.contains("/")) {
    throw new SecurityException("非法文件名");
}
```

### 2. 文件大小限制

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 100MB  # 上传限制
```

### 3. 并发处理

- 模板文件：只读，可多用户并发访问
- 生成文件：建议使用 `fill-and-download`（临时文件）

### 4. 临时文件清理

建议配置定时任务清理临时目录：

```java
@Scheduled(cron = "0 0 2 * * ?")  // 每天凌晨2点
public void cleanTempFiles() {
    // 删除超过24小时的临时文件
}
```

---

## 📊 性能优化

### 1. 模板缓存

对于频繁使用的模板，可以考虑缓存：

```java
@Cacheable("templates")
public File getTemplate(String fileName) {
    return new File(storagePath, fileName);
}
```

### 2. 异步生成

对于大批量生成，使用异步处理：

```java
@Async
public CompletableFuture<File> generateAsync(TemplateRequest req) {
    // 异步生成
}
```

### 3. 批量生成优化

```python
# 使用线程池并发生成
from concurrent.futures import ThreadPoolExecutor

with ThreadPoolExecutor(max_workers=5) as executor:
    futures = [executor.submit(generate_report, data) 
               for data in batch_data]
    
    for future in futures:
        result = future.result()
```

---

## 🆘 常见问题

### Q1: 模板中的图片会保留吗？
**A:** 会！模板中的图片、图表、格式都会完整保留。

### Q2: 可以修改模板中已有的公式吗？
**A:** 可以。使用 `valueType: "FORMULA"` 覆盖原有公式。

### Q3: 如何批量生成几百个文件？
**A:** 使用 `fill-and-download` 接口，配合线程池并发生成。

### Q4: 生成的文件可以再次编辑吗？
**A:** 可以！生成的是标准Excel文件，可以在Excel中正常打开编辑。

### Q5: 支持.xls格式吗？
**A:** 建议使用.xlsx格式，对.xls支持有限。

---

## 📚 相关文档

- [API使用示例](API_EXAMPLES.md)
- [快速开始](QUICK_START.md)
- [公式支持说明](FORMULA_SUPPORT.md)
- [完整文档](README.md)

---

**开始使用模板功能，让您的Excel自动化更上一层楼！** 🚀

