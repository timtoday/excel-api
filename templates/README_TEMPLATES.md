# Excel模板示例

本目录包含常用的Excel模板示例，可直接上传到服务器使用。

## 📁 模板列表

### 1. 工资条模板 (salary_template.xlsx)

**用途**：批量生成员工工资条

**模板结构**：
```
A列              B列
--------------------------
1  姓名            [待填充]
2  工号            [待填充]
3  基本工资        [待填充]
4  绩效奖金        [待填充]
5  应发工资        =B3+B4
6  个人所得税      =B5*0.1
7  实发工资        =B5-B6
```

**使用示例**：
```bash
curl -X POST "http://localhost:8080/api/excel/template/fill-and-download" \
  -H "Content-Type: application/json" \
  -d '{
    "templateFileName": "salary_template.xlsx",
    "downloadFileName": "salary_zhangsan.xlsx",
    "sheetName": "Sheet1",
    "cells": [
      {"cellAddress": "B1", "value": "张三", "valueType": "STRING"},
      {"cellAddress": "B2", "value": "E001", "valueType": "STRING"},
      {"cellAddress": "B3", "value": 10000, "valueType": "NUMBER"},
      {"cellAddress": "B4", "value": 2000, "valueType": "NUMBER"}
    ]
  }' --output salary_zhangsan.xlsx
```

---

### 2. 发票模板 (invoice_template.xlsx)

**用途**：生成标准发票

**模板结构**：
```
A列            B列        C列        D列
------------------------------------------------
1  发票编号      [待填充]
2  开票日期      [待填充]
3  客户名称      [待填充]
4
5  项目          单价       数量       金额
6  [项目1]       [单价]     [数量]     =B6*C6
7  [项目2]       [单价]     [数量]     =B7*C7
8
9  合计                                =SUM(D6:D7)
10 税额(13%)                           =D9*0.13
11 价税合计                            =D9+D10
```

---

### 3. 销售报表模板 (sales_report_template.xlsx)

**用途**：生成月度/季度销售报表

**模板结构**：
```
A列          B列        C列        D列
---------------------------------------------
1  销售报表
2  报表期间    [待填充]   销售员     [待填充]
3
4  产品类别    销售额     占比       环比
5  产品A       [待填充]   =B5/B9     [待填充]
6  产品B       [待填充]   =B6/B9     [待填充]
7  产品C       [待填充]   =B7/B9     [待填充]
8
9  总计         =SUM(B5:B7)
```

---

### 4. 成绩单模板 (transcript_template.xlsx)

**用途**：批量生成学生成绩单

**模板结构**：
```
包含：
- 学校logo（图片）
- 学生信息（姓名、学号）
- 成绩表格（各科成绩、总分、平均分）
- 签章位置
```

---

### 5. 合同模板 (contract_template.xlsx)

**用途**：自动生成标准合同

**模板结构**：
```
包含：
- 合同编号、日期
- 甲乙方信息
- 合同金额及付款方式（含公式）
- 法律条款
```

---

## 🎨 创建自己的模板

### 步骤1：设计模板

在Excel中设计模板，包括：
1. **标题和logo**：固定内容
2. **数据区域**：标记需要填充的位置
3. **公式**：设置自动计算的公式
4. **格式**：设置单元格格式、条件格式

### 步骤2：标记填充位置

有两种方式标记：
```
方式1：使用占位符
  姓名: [姓名]
  金额: [金额]

方式2：留空或使用示例数据
  姓名: 
  金额: 0
```

### 步骤3：测试模板

```bash
# 1. 上传模板
curl -X POST "http://localhost:8080/api/excel/upload" \
  -F "file=@my_template.xlsx"

# 2. 测试填充
curl -X POST "http://localhost:8080/api/excel/template/fill-and-download" \
  -H "Content-Type: application/json" \
  -d '{
    "templateFileName": "my_template.xlsx",
    "sheetName": "Sheet1",
    "cells": [
      {"cellAddress": "B2", "value": "测试数据", "valueType": "STRING"}
    ]
  }' --output test.xlsx
```

---

## 💡 模板设计技巧

### 1. 公式设计

```excel
✅ 推荐：使用相对引用
  =B5*C5    // 可以复制到其他行

❌ 避免：过多的绝对引用
  =$B$5*$C$5  // 不易维护
```

### 2. 格式设置

```
- 使用条件格式：金额>10000显示红色
- 数字格式：#,##0.00（千分位）
- 日期格式：yyyy-mm-dd
- 百分比：0.00%
```

### 3. 数据验证

```
- 下拉列表：部门选择
- 数字范围：0-100
- 日期范围：2024年度
```

### 4. 保护工作表

```
锁定：
  - 标题行
  - 公式单元格
  - Logo和图片

不锁定：
  - 数据输入区域
```

---

## 📦 批量生成示例

### Python脚本

```python
import requests
import pandas as pd

# 从CSV读取数据
data = pd.read_csv('data.csv')

# 批量生成
for index, row in data.iterrows():
    payload = {
        'templateFileName': 'template.xlsx',
        'downloadFileName': f'output_{row["id"]}.xlsx',
        'sheetName': 'Sheet1',
        'cells': [
            {'cellAddress': 'B1', 'value': row['name'], 'valueType': 'STRING'},
            {'cellAddress': 'B2', 'value': row['amount'], 'valueType': 'NUMBER'}
        ]
    }
    
    response = requests.post(
        'http://localhost:8080/api/excel/template/fill-and-download',
        json=payload
    )
    
    with open(f'output/file_{row["id"]}.xlsx', 'wb') as f:
        f.write(response.content)
    
    print(f'✅ {row["name"]} 完成')
```

---

## 🔗 相关文档

- [模板功能完整指南](../TEMPLATE_GUIDE.md)
- [API使用示例](../API_EXAMPLES.md)
- [快速开始](../QUICK_START.md)

---

**开始创建您的Excel模板吧！** 🚀

