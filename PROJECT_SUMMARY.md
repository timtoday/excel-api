# Excel API 项目总结

## 📋 项目概述

一个高可用的Excel公式计算API服务，将带公式的Excel文件封装成RESTful API，支持多用户并发访问和公式自动计算。

### 核心价值

1. **共享复杂Excel计算** - 将本地Excel的复杂公式计算能力云端化
2. **多用户并发** - 支持多用户同时读写，自动处理并发冲突
3. **公式自动计算** - 写入数据后自动计算所有公式，返回结果
4. **易于集成** - RESTful API，可与任何应用系统集成

---

## 🏗️ 技术架构

### 技术选型

| 组件 | 技术 | 说明 |
|------|------|------|
| **框架** | Spring Boot 3.2.0 | 现代化Java企业级框架 |
| **Excel处理** | Apache POI 5.2.5 | 业界标准Excel操作库 |
| **并发控制** | ReadWriteLock | 读写分离锁机制 |
| **分布式锁** | Redisson | Redis分布式锁（可选）|
| **API文档** | SpringDoc OpenAPI | Swagger自动生成 |
| **容器化** | Docker | 便捷部署 |

### 为什么选择Apache POI？

✅ **优点：**
- 最成熟的Java Excel库
- 支持.xlsx和.xls格式
- 完善的公式计算引擎
- 活跃的社区支持
- 支持大部分Excel函数

📊 **公式支持情况（POI 5.2.5）：**

**支持率：约85% (~300+函数)**

✅ **完全支持：**
- 数学函数：SUM, AVERAGE, MAX, MIN, ROUND, ABS, POWER, SQRT, MOD等（65+）
- 逻辑函数：IF, AND, OR, NOT, IFERROR, IFNA（全部支持）
- 文本函数：CONCATENATE, LEFT, RIGHT, MID, LEN, TRIM, UPPER, LOWER等（25+）
- 查找函数：VLOOKUP, HLOOKUP, INDEX, MATCH, OFFSET, INDIRECT（15+）
- 日期函数：TODAY, NOW, DATE, YEAR, MONTH, DAY, WEEKDAY等（22+）
- 统计函数：COUNT, COUNTA, COUNTIF, SUMIF, AVERAGEIF, MEDIAN等（75+）
- 财务函数：PMT, PV, FV, NPV, IRR等（40+）

⚠️ **部分支持或有限制：**
- 数组公式（需Ctrl+Shift+Enter的复杂数组）
- 某些高级统计函数（FORECAST.LINEAR, TREND）
- 数据库函数（DSUM, DAVERAGE）
- 部分文本函数（TEXTJOIN可能不支持）

❌ **不支持（约5%）：**
- **Excel 365新函数**：XLOOKUP, XMATCH, FILTER, SORT, UNIQUE, SEQUENCE
- **Web函数**：WEBSERVICE, FILTERXML, ENCODEURL
- **数据透视**：GETPIVOTDATA
- **OLAP函数**：CUBEVALUE, CUBEMEMBER, CUBESET
- **动态数组**：LET, LAMBDA

> 📖 **详细文档**：[FORMULA_SUPPORT.md](FORMULA_SUPPORT.md) - 完整函数列表、测试方法和替代方案

### 其他选择考虑

| 库 | 优点 | 缺点 | 适用场景 |
|---|------|------|---------|
| **Apache POI** | ✅ 功能全面<br>✅ 公式计算强 | ⚠️ 内存占用较大 | 推荐用于本项目 |
| **EasyExcel** | ✅ 性能好<br>✅ 内存占用小 | ❌ 公式支持弱 | 适合大数据导入导出 |
| **JExcelApi** | ✅ 轻量级 | ❌ 只支持.xls<br>❌ 已停止维护 | 不推荐 |
| **FastExcel** | ✅ 超高性能 | ❌ 只支持写入<br>❌ 无公式计算 | 仅适合数据导出 |

---

## 🔐 并发控制方案

### 锁机制设计

```
┌─────────────────────────────────────────┐
│           Excel File                    │
└─────────────────────────────────────────┘
                 ▲
                 │
         ┌───────┴───────┐
         │   File Lock   │
         │  (per file)   │
         └───────┬───────┘
                 │
     ┌───────────┴───────────┐
     │                       │
┌────▼─────┐          ┌─────▼────┐
│   Read   │          │  Write   │
│   Lock   │          │   Lock   │
│(Multiple)│          │ (Single) │
└──────────┘          └──────────┘
```

### 本地锁 vs 分布式锁

#### 本地锁（LocalExcelLockManager）
- 使用 `ReentrantReadWriteLock`
- 适用于单机部署
- 零依赖，性能最优
- 配置：`excel.lock.type=local`

#### 分布式锁（RedisExcelLockManager）
- 使用 Redisson
- 适用于集群部署
- 支持跨服务器锁
- 配置：`excel.lock.type=redis`

### 并发策略

| 操作 | 锁类型 | 并发数 | 说明 |
|------|--------|--------|------|
| **读取** | 读锁 | 50（默认） | 多个用户可同时读 |
| **写入** | 写锁 | 1（推荐） | 同时只允许1个用户写 |
| **锁超时** | - | 30秒 | 避免死锁 |
| **等待时间** | - | 10秒 | 最长等待获取锁时间 |

---

## 📊 API设计

### 核心接口

#### 1. 写入接口
```http
POST /api/excel/write
```
向Excel写入数据和公式

#### 2. 读取接口
```http
POST /api/excel/read
```
读取Excel数据（含公式计算）

#### 3. 组合操作接口
```http
POST /api/excel/operation
```
先写后读（最常用）

#### 4. 上传接口
```http
POST /api/excel/upload
```
上传Excel模板文件

### 参数设计亮点

#### ✨ 使用Excel原生地址格式

**旧设计（不好）：**
```json
{"row": 0, "col": 0}  // 需要计算，不直观
```

**新设计（推荐）：**
```json
{"cellAddress": "A1"}  // 直观，符合Excel习惯
```

**优势：**
- ✅ 用户无需转换行列号
- ✅ 可读性强
- ✅ 减少出错
- ✅ 更符合Excel用户习惯

---

## 🎯 核心功能

### 1. 模板输出功能（新增）⭐

支持基于Excel模板批量生成文件：

```python
# 批量生成工资条
for employee in employees:
    generate_salary_slip(template='salary_template.xlsx', data=employee)
```

**应用场景：**
- 📊 批量生成报表（销售、财务、统计）
- 📄 动态生成文档（发票、合同、证书）
- 🎓 批量生成个性化材料（成绩单、通知书）
- 📦 业务单据生成（订单、物流、入库单）

**核心优势：**
- ✅ 保留模板格式、公式、图片
- ✅ 支持直接下载，不占服务器空间
- ✅ 自动计算填充后的公式结果
- ✅ 支持批量并发生成

### 2. 自动公式计算

```json
{
  "cells": [
    {"cellAddress": "A1", "value": 10, "valueType": "NUMBER"},
    {"cellAddress": "A2", "value": 20, "valueType": "NUMBER"},
    {"cellAddress": "A3", "value": "A1+A2", "valueType": "FORMULA"}
  ]
}
```

写入后自动计算：A3 = 30

### 3. 版本控制

自动备份Excel文件历史版本：
```
excel-files/
├── report.xlsx              (当前版本)
├── backup_report_20241024_143022.xlsx
├── backup_report_20241024_142010.xlsx
└── ...
```

配置：
```yaml
excel:
  storage:
    version-control: true
    max-versions: 10  # 保留最近10个版本
```

### 4. 多Sheet支持

同一文件可以有多个Sheet：
```json
{
  "fileName": "report.xlsx",
  "sheetName": "2024Q1"  // 指定Sheet名称
}
```

### 5. 多种数据类型

| valueType | 说明 | 示例 |
|-----------|------|------|
| STRING | 字符串 | "Hello" |
| NUMBER | 数字 | 123.45 |
| BOOLEAN | 布尔值 | true |
| FORMULA | 公式 | "SUM(A1:A10)" |

---

## 🚀 部署方案

### 单机部署

```yaml
# application.yml
excel:
  lock:
    type: local
```

适用于：
- 小型团队（<50人）
- 并发不高的场景
- 开发测试环境

### 集群部署

```yaml
# application-prod.yml
excel:
  lock:
    type: redis
spring:
  redis:
    host: redis-cluster
```

配合负载均衡：
```
     ┌──────────┐
     │  Nginx   │
     └────┬─────┘
          │
    ┌─────┼─────┐
    │     │     │
┌───▼─┐ ┌─▼──┐ ┌▼───┐
│API-1│ │API-2│ │API-3│
└──┬──┘ └──┬──┘ └──┬─┘
   │       │       │
   └───────┼───────┘
           │
      ┌────▼────┐
      │  Redis  │
      └─────────┘
```

适用于：
- 大型企业
- 高并发场景
- 需要高可用

### Docker部署

```bash
docker-compose up -d
```

包含：
- Excel API服务
- Redis缓存
- 数据持久化

---

## 📈 性能考虑

### 性能指标（参考）

| 场景 | 响应时间 | 并发支持 |
|------|----------|----------|
| 写入10个单元格 | <200ms | 5并发 |
| 读取10个单元格 | <100ms | 50并发 |
| 公式计算(简单) | <100ms | - |
| 公式计算(复杂) | <1s | - |

### 优化建议

1. **批量操作** - 一次请求包含多个单元格
2. **缓存策略** - 启用公式计算缓存
3. **异步处理** - 大文件操作使用异步
4. **连接池** - 合理配置数据库连接池
5. **文件分片** - 超大Excel考虑拆分

---

## 🛡️ 安全建议

### 生产环境必做

1. **认证授权**
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-security</artifactId>
   </dependency>
   ```

2. **文件路径验证**
   ```java
   // 防止路径遍历攻击
   if (fileName.contains("..") || fileName.contains("/")) {
       throw new SecurityException("Invalid file name");
   }
   ```

3. **公式注入防护**
   ```java
   // 限制危险公式
   if (formula.startsWith("=") || formula.contains("EXEC")) {
       throw new SecurityException("Dangerous formula");
   }
   ```

4. **文件大小限制**
   ```yaml
   spring:
     servlet:
       multipart:
         max-file-size: 100MB
   ```

5. **HTTPS部署**
   ```yaml
   server:
     ssl:
       enabled: true
   ```

---

## 📚 文档结构

```
excel-api/
├── README.md              # 项目主文档
├── QUICK_START.md         # 快速开始指南（新）
├── API_EXAMPLES.md        # API使用示例
├── MIGRATION_GUIDE.md     # 参数格式迁移指南（新）
├── PROJECT_SUMMARY.md     # 项目总结（本文档）
├── pom.xml               # Maven配置
├── Dockerfile            # Docker镜像
├── docker-compose.yml    # Docker编排
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/excel/api/
    │   │       ├── controller/     # API控制器
    │   │       ├── service/        # 业务服务
    │   │       ├── lock/          # 锁管理
    │   │       ├── model/         # 数据模型
    │   │       ├── config/        # 配置
    │   │       └── exception/     # 异常处理
    │   └── resources/
    │       ├── application.yml
    │       └── application-prod.yml
    └── test/                      # 测试用例
```

---

## 🎓 使用场景

### 典型应用

1. **批量报表生成（模板功能）** ⭐
   - 基于统一模板生成个性化报表
   - 批量生成工资条、发票、合同
   - 自动填充数据和计算
   - 支持几百上千份文档生成

2. **财务报表计算**
   - 复杂的财务公式
   - 多维度数据分析
   - 自动生成报告

3. **薪资计算系统**
   - 工资公式计算
   - 个税计算
   - 社保扣除

4. **库存管理**
   - 库存金额计算
   - 预警提醒
   - 统计分析

5. **报价单生成**
   - 动态价格计算
   - 折扣处理
   - 税费计算

6. **数据分析**
   - 统计分析
   - 趋势预测
   - 可视化数据源

7. **文档自动化** ⭐
   - 证书/证明批量生成
   - 成绩单/通知书
   - 订单/物流单

---

## ✅ 项目优势

1. **稳定可靠**
   - 基于成熟的Apache POI
   - 完善的异常处理
   - 自动版本备份

2. **高性能**
   - 读写锁优化
   - 支持分布式部署
   - 批量操作优化

3. **易用性**
   - RESTful API设计
   - Swagger文档
   - 详细的使用示例

4. **可扩展**
   - 模块化设计
   - 支持自定义扩展
   - 灵活的配置

5. **生产就绪**
   - Docker支持
   - 完整日志
   - 健康检查

---

## 🔮 未来规划

### 短期（v1.1）
- [ ] 支持更多Excel函数
- [ ] 添加公式验证
- [ ] 性能监控面板

### 中期（v1.2）
- [ ] 支持CSV格式
- [ ] WebSocket实时推送
- [ ] 公式调试模式

### 长期（v2.0）
- [ ] 可视化编辑器
- [ ] Excel模板管理
- [ ] 在线协同编辑

---

## 📞 支持与反馈

- **项目地址**: [GitHub链接]
- **API文档**: http://localhost:8080/swagger-ui.html
- **问题反馈**: 提交Issue
- **技术交流**: [社区链接]

---

## 🙏 致谢

感谢以下开源项目：
- Apache POI
- Spring Boot
- Redisson
- SpringDoc OpenAPI

---

**版本**: 1.0.0  
**更新日期**: 2024-10-24  
**作者**: Excel API Team

