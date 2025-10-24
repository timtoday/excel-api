# 快速测试指南 🚀

## 5分钟快速上手API测试工具

### 步骤1: 启动应用
```bash
# Windows
start.bat

# Linux/Mac
./start.sh
```

### 步骤2: 登录管理后台
1. 访问：http://localhost:18081/admin/login
2. 输入账号：`admin` / `admin123`
3. 点击登录

### 步骤3: 创建API Token
1. 进入"Token管理"页面
2. 点击"创建新Token"
3. 输入名称（如：`测试Token`）
4. 点击"创建"

### 步骤4: 上传测试文件
1. 进入"Excel文件"页面
2. 点击"选择文件"按钮
3. 选择一个Excel文件（.xlsx 或 .xls）
4. 点击"上传"

### 步骤5: 测试API

#### 测试1: 写入数据 ✍️
1. 进入"API测试"页面
2. 选择刚创建的Token
3. 选择接口：`写入Excel`
4. 点击"📝 加载示例"
5. 点击"🚀 发送请求"
6. 查看响应结果

#### 测试2: 读取数据 📖
1. 选择接口：`读取Excel`
2. 点击"📝 加载示例"
3. 修改 `fileName` 为你上传的文件名
4. 点击"🚀 发送请求"
5. 查看读取结果

#### 测试3: 公式计算 🧮
1. 选择接口：`写入并读取（组合操作）`
2. 点击"📝 加载示例"（自动包含公式）
3. 点击"🚀 发送请求"
4. 查看计算结果

#### 测试4: 文件上传 📤
1. 选择接口：`上传文件`
2. 使用文件选择器选择本地Excel文件
3. 点击"🚀 发送请求"
4. 上传成功后，文件会出现在"Excel文件"列表中

#### 测试5: 文件下载 📥
1. 选择接口：`下载文件`
2. 在"要下载的文件名"输入框输入文件名（如：`test.xlsx`）
3. 点击"🚀 发送请求"
4. 浏览器自动下载文件

#### 测试6: 模板功能 📋
1. 先上传一个模板文件（如：`template.xlsx`）
2. 选择接口：`模板填充并下载`
3. 点击"📝 加载示例"
4. 修改 `templateFileName` 为你的模板文件名
5. 修改 `cells` 数组中的数据
6. 点击"🚀 发送请求"
7. 浏览器自动下载生成的文件

#### 测试7: 健康检查 🏥
1. 选择接口：`健康检查`
2. 点击"🚀 发送请求"（此接口不需要Token）
3. 查看服务状态

#### 测试8: 模板生成 📄
1. 选择接口：`模板生成（返回文件名）`
2. 点击"📝 加载示例"
3. 修改模板名称和输出文件名
4. 点击"🚀 发送请求"
5. 文件会保存在服务器，可以在"Excel文件"页面查看

## 📊 查看测试结果

### 方式1: 响应区域
- 状态码：HTTP状态码（200表示成功）
- 耗时：请求处理时间
- 响应体：JSON格式的详细结果

### 方式2: 请求日志
1. 进入"请求日志"页面
2. 查看所有测试请求的详细信息
3. 可以查看请求/响应的完整内容

### 方式3: 文件列表
1. 进入"Excel文件"页面
2. 查看上传和生成的所有文件
3. 可以下载文件验证内容

## 💡 常用技巧

### 技巧1: 快速选择文件
在"选择Excel文件"下拉框中选择文件，会自动填充到请求JSON的`fileName`字段

### 技巧2: 自定义测试数据
1. 点击"📝 加载示例"获取示例格式
2. 修改JSON中的值
3. 发送自定义请求

### 技巧3: 单元格地址格式
- 使用Excel标准格式：`A1`, `B2`, `C10`, `AZ100`
- 不区分大小写

### 技巧4: 公式格式
- 公式必须以 `=` 开头
- 示例：`=SUM(A1:A10)`, `=AVERAGE(B1:B5)`, `=A1+B1`

### 技巧5: 查看错误详情
如果请求失败：
1. 查看响应区域的错误信息
2. 到"请求日志"查看完整堆栈
3. 检查Token是否有效
4. 检查文件是否存在

## 🎯 测试场景示例

### 场景1: 学生成绩表
```json
{
  "fileName": "students.xlsx",
  "sheetName": "Sheet1",
  "cells": [
    {"cellAddress": "A1", "value": "姓名", "valueType": "STRING"},
    {"cellAddress": "B1", "value": "数学", "valueType": "STRING"},
    {"cellAddress": "C1", "value": "英语", "valueType": "STRING"},
    {"cellAddress": "D1", "value": "总分", "valueType": "STRING"},
    {"cellAddress": "A2", "value": "张三", "valueType": "STRING"},
    {"cellAddress": "B2", "value": 90, "valueType": "NUMBER"},
    {"cellAddress": "C2", "value": 85, "valueType": "NUMBER"},
    {"cellAddress": "D2", "value": "=B2+C2", "valueType": "FORMULA"}
  ]
}
```

### 场景2: 销售报表
```json
{
  "fileName": "sales.xlsx",
  "writeRequest": {
    "sheetName": "Sheet1",
    "cells": [
      {"cellAddress": "A1", "value": "产品", "valueType": "STRING"},
      {"cellAddress": "A2", "value": "单价", "valueType": "STRING"},
      {"cellAddress": "A3", "value": "数量", "valueType": "STRING"},
      {"cellAddress": "A4", "value": "总额", "valueType": "STRING"},
      {"cellAddress": "B2", "value": 99.5, "valueType": "NUMBER"},
      {"cellAddress": "B3", "value": 10, "valueType": "NUMBER"},
      {"cellAddress": "B4", "value": "=B2*B3", "valueType": "FORMULA"}
    ]
  },
  "readRequest": {
    "sheetName": "Sheet1",
    "cells": [{"cellAddress": "B4"}],
    "readFormula": false
  }
}
```

### 场景3: 财务报表模板
1. 准备模板文件 `financial_template.xlsx`，包含：
   - 标题样式
   - 表格框架
   - 预设公式

2. 使用模板生成：
```json
{
  "templateFileName": "financial_template.xlsx",
  "sheetName": "Sheet1",
  "cells": [
    {"cellAddress": "B1", "value": "2024年第四季度财务报表", "valueType": "STRING"},
    {"cellAddress": "B5", "value": 150000, "valueType": "NUMBER"},
    {"cellAddress": "B6", "value": 80000, "valueType": "NUMBER"},
    {"cellAddress": "B7", "value": "=B5-B6", "valueType": "FORMULA"}
  ]
}
```

## 🔗 下一步

完成基础测试后，你可以：

1. **集成到应用**: 使用Token在你的应用中调用API
2. **查看完整文档**: 阅读 [API_TEST_GUIDE.md](API_TEST_GUIDE.md)
3. **了解所有功能**: 查看 [README.md](README.md)
4. **学习模板功能**: 参考 [TEMPLATE_GUIDE.md](TEMPLATE_GUIDE.md)

## ❓ 常见问题

**Q: Token在哪里查看？**  
A: Token管理页面，创建时会显示完整Token，请妥善保存

**Q: 文件上传后在哪里？**  
A: 保存在 `excel-files/` 目录，可以在"Excel文件"页面查看

**Q: 如何测试自己的Excel文件？**  
A: 先上传文件，然后在测试页面的"选择Excel文件"中选择它

**Q: 支持哪些Excel函数？**  
A: 查看 [FORMULA_SUPPORT.md](FORMULA_SUPPORT.md) 了解详细支持情况

**Q: API测试失败怎么办？**  
A: 查看"请求日志"页面的错误详情，检查Token和文件是否有效

---

**开始你的第一次API测试吧！** 🎉

