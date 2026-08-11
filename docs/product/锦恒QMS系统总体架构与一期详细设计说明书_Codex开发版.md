# 锦恒 QMS 质量管理系统
## 总体架构与一期详细设计说明书（Codex 开发版）

**文档版本**：V1.0  
**文档用途**：作为 React + Spring Boot + Python AI Service 项目的开发输入，可直接交给 Codex / Cursor / Claude Code 分阶段实现。  
**适用范围**：锦恒集团 QMS 项目，重点覆盖“图纸智能解析 → 质量特性 → 检查基准书 → 试验验证计划 → 变更影响分析”，并兼顾后续 APQP、问题管理、体系审核、知识库、质量成本和质量运营分析扩展。  
**优先级说明**：P0 = 一期必须；P1 = 一期后半段/二期；P2 = 后续扩展。

---

# 1. 文档依据与设计原则

## 1.1 本文档的业务依据

本文档基于客户提供的以下资料整理：

1. 《锦恒QMS系统解决方案V3.0.pptx》
2. 《检查基准书与各板块关联关系(1).pptx》
3. 《检查基准书.pdf》
4. 《检具验收报告.pdf》
5. 《实验验证计划.pdf》
6. 《图纸.pdf》
7. 《J50431175一汽D511 PAB支架总成-Z（J864).dwg》
8. 《质量成本分析逻辑-用于QMS.pptx》

其中，客户样例已经明确以下关键业务事实：

- 检查基准书零件号应与图纸零件号关联。
- 检具编号应与检具验收报告中的检具编号关联。
- 检查基准书履历需要和图纸图样标记、版本建立关联。
- 检查基准书中的批次检验尺寸来自图纸中的检验尺寸。
- 年度尺寸原则上覆盖图纸全部标注尺寸，但参考尺寸、理想尺寸除外。
- 检查基准书的外观、尺寸、性能项目中的特性符号需与图纸保持一致。
- 检查基准书性能项中的“批次”内容与试验验证计划中的“批次”勾选项目建立对应。
- 检查基准书性能项中的“年度”内容与试验验证计划中的“型式”勾选项目建立对应。
- 样例检查基准书存在关键特性[A]、重要特性[B]、法规特性[R]等结构化特性标识。
- 原 QMS 方案已经规划 APQP、问题管理、体系审核、知识库、AI 辅助、BI，并与 PLM/MES/ERP/OA/LIMS 集成。

## 1.2 设计结论

本系统不能仅做成“传统 QMS + OCR”。

系统的核心数据主线必须调整为：

```text
Product / Part
    ↓
Drawing
    ↓
DrawingRevision
    ↓
QualityCharacteristic
    ↓
InspectionStandard
    ↓
ValidationPlan
    ↓
Inspection / Test Result
    ↓
NCR / 8D / CAPA / FMEA
    ↓
Knowledge
    ↓
反哺新图纸、新项目、新检查基准书
```

其中 **QualityCharacteristic（质量特性）** 是贯穿设计、验证、检验、问题和知识的核心业务对象。

## 1.3 设计原则

1. **工程数据结构化优先于文档生成**  
   图纸识别结果必须先形成结构化质量特性，检查基准书只是结构化数据的一个业务视图和正式受控文件。

2. **AI 只生成草稿，不直接发布受控文件**  
   AI 不允许绕过人工复核和审批。

3. **每一个 AI 提取字段必须可追溯到 Source Evidence**  
   用户必须能看到“这个值来自图纸哪里”。

4. **LLM/VLM 负责理解，Rule Engine 负责决定**  
   规则、计算、状态流转、强制校验不能交给大模型自由生成。

5. **PDF 与 DWG 走不同解析路径，最终进入同一 Drawing Intermediate Model**。

6. **版本、数据血缘、审计必须从第一天设计**。

7. **一期采用模块化单体 QMS Core + 独立 AI Service + 独立 CAD Parser Adapter**，避免过早拆成大量微服务。

---

# 2. 系统目标与范围

## 2.1 一期核心目标 P0

实现如下完整闭环：

```text
上传 PDF/DWG 图纸
    ↓
识别图纸主信息、版本、尺寸、公差、技术要求、特殊特性
    ↓
形成 Quality Characteristic Draft
    ↓
人工复核 + 规则校验
    ↓
生成检查基准书草稿
    ↓
人工补充抽样、检验方法、检具等非图纸字段
    ↓
检查基准书覆盖性/一致性检查
    ↓
审批发布
    ↓
根据性能项生成试验验证计划草稿
    ↓
人工补充试验机构、时间、数量、等效信息
    ↓
审批发布
    ↓
图纸新版本进入后自动做 Revision Diff
    ↓
分析受影响的检查基准书/验证计划/检具/CP/FMEA 等对象
```

## 2.2 一期 P1

- APQP 项目档案与图纸/基准书/验证计划挂接。
- 检具验收与检具有效性管理。
- 质量知识库基础版。
- AI Copilot：查询、解释、生成草稿、相似记录查询。
- 基础质量成本数据模型和报表接口。

## 2.3 后续 P2

- FMEA / Control Plan / PPAP 深度管理。
- MES 检验结果和 SPC。
- LIMS 试验执行和结果回传。
- NCR / 8D / CAPA 闭环。
- 体系审核。
- 集团级质量运营和知识推送。

---

# 3. 总体业务架构

```text
┌─────────────────────────────────────────────────────┐
│                    用户工作层                        │
│ React Web / Drawing Viewer / Review Workbench       │
└───────────────────────┬─────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────┐
│                  QMS Application Core               │
│                                                     │
│ Part / Drawing / Characteristic / Inspection        │
│ Validation / Gauge / Change / APQP / Problem        │
│ Knowledge / Cost / System / Integration             │
└───────────────────────┬─────────────────────────────┘
                        │
         ┌──────────────┼───────────────┐
         │              │               │
┌────────▼──────┐ ┌─────▼─────┐ ┌──────▼──────────┐
│ AI Service    │ │ CAD Parser │ │ Rule Engine     │
│ OCR/VLM/LLM   │ │ DWG/DXF    │ │ Mapping/Check   │
└────────┬──────┘ └─────┬─────┘ └──────┬──────────┘
         │              │               │
         └──────────────┼───────────────┘
                        │
┌───────────────────────▼─────────────────────────────┐
│ 数据层                                              │
│ PostgreSQL / Redis / Object Storage / Vector Index  │
└─────────────────────────────────────────────────────┘
```

---

# 4. 技术选型

## 4.1 PC 前端

- React
- TypeScript
- Ant Design
- React Router
- TanStack Query 或同类服务端状态管理
- Zustand 或同类轻量客户端状态
- PDF.js：PDF Viewer
- SVG/Canvas Overlay：图纸标注、高亮、Bounding Box
- AG Grid 或 Ant Design Table：超大检查项目列表可按性能测试决定
- ECharts：BI/趋势分析

### 前端关键要求

- 多标签页工作台。
- 复杂表格可编辑。
- 左右分栏：左侧结构化数据，右侧图纸证据。
- Viewer 支持缩放、定位、坐标高亮、切换版本、对比版本。
- 保存用户列配置、筛选条件。
- 所有 AI 字段显示：
  - 来源
  - 置信度
  - 规则校验状态
  - 人工确认状态

## 4.2 QMS Core 后端

- Java
- Spring Boot
- Spring Security
- MyBatis-Plus 或 JPA（二选一，项目统一，不混用）
- PostgreSQL
- Flyway
- Redis
- MinIO/S3 兼容对象存储
- OpenAPI
- Flowable：审批/受控流程
- RabbitMQ 或 Kafka：P1 引入；P0 可使用 DB Job + Redis Queue，接口预留 EventBus

### 架构模式

一期推荐：

**模块化单体 + 异步 Worker + 独立 AI 服务**

严禁一期拆 20 个微服务。

## 4.3 AI Service

- Python
- FastAPI
- Pydantic
- OCR Adapter
- VLM Adapter
- LLM Adapter
- Embedding Adapter
- Reranker Adapter
- OpenCV / PDF parsing library
- Worker：Celery/RQ/自研 Job Worker 任一，项目统一

AI Service 不直接写核心业务发布表，仅能：
1. 写解析 Job 结果；
2. 写 Draft/Staging 数据；
3. 通过 QMS Core API 请求生成业务草稿。

## 4.4 CAD Parser

建立独立 Provider SPI：

```text
CadParserProvider
├── CommercialDwgSdkProvider
├── DxfProvider
└── MockProvider
```

P0 开发时必须先实现统一接口，不允许在业务代码中直接绑定某一个 DWG SDK。

统一输出 `DrawingIntermediateModel`。

## 4.5 数据库与知识检索

P0：

- PostgreSQL：主业务数据
- JSONB：解析中间结果、半结构化几何/模型信息
- pgvector 或等价向量能力：初期知识检索
- Object Storage：原始文件、缩略图、解析附件
- Redis：缓存、分布式锁、短任务状态

P2 数据量和全文检索复杂度明显增加后，再引入独立搜索引擎。

---

# 5. 系统模块与菜单

```text
首页
├── 我的工作台
├── 我的待办
├── 我的审批
├── 我的解析任务
└── 风险提醒

质量工程数据
├── 产品/零件
├── 图纸管理
├── 图纸版本
├── 图纸智能解析
├── 质量特性中心
├── 特性符号字典
├── 技术要求库
└── 图纸变更影响

检查基准书
├── 基准书列表
├── 新建基准书
├── AI生成草稿
├── 基准书复核
├── 覆盖性检查
├── 版本履历
├── 发布管理
└── 模板管理

试验验证
├── 验证计划
├── AI生成验证计划
├── 验证项目库
├── 试验标准库
├── 试验机构库
├── 验证结果（P1）
└── 版本履历

检具/量具
├── 检具档案
├── 检具验收
├── 检具有效性
├── 检具关联特性
└── 校准/到期（P1）

APQP（P1）
├── 项目质量档案
├── Gate
├── FMEA关联
├── Control Plan关联
└── PPAP关联

问题管理（P2）
├── NCR
├── 客诉
├── 8D
├── CAPA
├── MRB
└── 相似问题

质量知识库
├── 原始资料
├── 失效模式库
├── 问题案例库
├── 措施方案库
├── 经验教训
├── Design Guidelines
├── Best Practice
└── 知识审核

质量成本（P1/P2）
├── 成本分类
├── 成本数据
├── 问题成本关联
├── 质量成本分析
└── 报表

AI Copilot
├── 图纸助手
├── 基准书助手
├── 验证计划助手
├── 变更影响助手
├── 知识助手
└── 分析助手

系统管理
├── 用户
├── 组织
├── 角色
├── 权限
├── 字典
├── 附件
├── 审批流程
├── 审计日志
├── AI模型配置
├── 规则配置
└── 接口管理
```

---

# 6. 核心领域模型

## 6.1 Product / Part

### Part

```text
id
tenantId
orgId
partNo
materialNo
partName
customerId
vehicleModel
supplierId
importanceLevel
status
createdAt
updatedAt
```

### 约束

- `partNo + orgId` 唯一。
- materialNo 可以为空，但如果存在必须做主数据映射。
- Part 不存“当前图纸文件路径”，通过 Drawing/Revision 管理。

---

## 6.2 Drawing

### Drawing

```text
id
partId
drawingNo
drawingName
drawingType
sourceSystem
status
```

### DrawingRevision

```text
id
drawingId
revisionCode
revisionSeq
fileId
fileType
effectiveDate
releaseDate
supersedesRevisionId
parseStatus
reviewStatus
status
checksum
createdBy
createdAt
releasedBy
releasedAt
```

### 状态

```text
DRAFT
UPLOADED
PARSING
PARSED
REVIEWING
CONFIRMED
RELEASED
SUPERSEDED
OBSOLETE
FAILED
```

---

# 7. Drawing Intermediate Model

所有 PDF/DWG 必须转换成统一中间模型。

```json
{
  "documentId": "DRAW-xxx",
  "revision": "Z3",
  "sheets": [
    {
      "sheetNo": "1",
      "width": 0,
      "height": 0,
      "titleBlock": {},
      "views": [],
      "entities": [],
      "notes": [],
      "characteristicCandidates": []
    }
  ]
}
```

## 7.1 TitleBlock

```json
{
  "drawingNo": "J50431157",
  "partName": "C柱导向架L",
  "material": "DC51D+Z50/50",
  "revision": "Z3",
  "scale": "1:1",
  "company": "...",
  "evidence": {}
}
```

## 7.2 DrawingEntity

```text
entityId
sourceEntityHandle
entityType
layer
sheetNo
bbox
geometry
rawText
normalizedText
style
```

entityType：

```text
TEXT
MTEXT
DIMENSION
LEADER
MLEADER
BLOCK
ATTRIBUTE
LINE
POLYLINE
ARC
CIRCLE
SYMBOL
TABLE
IMAGE
OTHER
```

## 7.3 Dimension

```json
{
  "entityId": "DIM-001",
  "nominalValue": 8.0,
  "upperTolerance": 0.5,
  "lowerTolerance": -0.5,
  "upperLimit": 8.5,
  "lowerLimit": 7.5,
  "unit": "mm",
  "displayText": "8±0.5",
  "dimensionType": "LINEAR",
  "symbols": ["B", "INSPECTION"],
  "evidence": {}
}
```

dimensionType：

```text
LINEAR
ANGLE
DIAMETER
RADIUS
POSITION
FLATNESS
PROFILE
OTHER
```

---

# 8. Source Evidence 模型

所有 AI/Parser 自动产生的重要字段都必须挂 Evidence。

```text
SourceEvidence
--------------
id
sourceFileId
drawingRevisionId
sheetNo
pageNo
entityId
entityHandle
bboxX
bboxY
bboxW
bboxH
rawText
normalizedText
extractorType
extractorVersion
modelName
modelVersion
confidence
createdAt
```

extractorType：

```text
PDF_VECTOR
OCR
DWG_ENTITY
VLM
LLM
RULE
MANUAL
```

## 8.1 UI 要求

用户点击任意自动提取字段：

```text
尺寸5   126.9±0.5   [B]
```

右侧 Viewer 必须自动跳转并高亮来源。

P0 禁止存在“AI 生成字段但无法定位来源”的发布路径。

---

# 9. Quality Characteristic 质量特性中心

## 9.1 核心实体

```text
QualityCharacteristic
---------------------
id
partId
drawingRevisionId
characteristicCode
characteristicType
sourceEntityId
name
description
nominalValue
upperTolerance
lowerTolerance
upperLimit
lowerLimit
unit
specialCharacteristicCode
inspectionDimension
referenceDimension
idealDimension
fitDimension
locationDimension
regulatoryFlag
mandatoryInspection
status
confidence
reviewStatus
evidenceId
```

## 9.2 characteristicType

```text
DIMENSION
APPEARANCE
MATERIAL
PERFORMANCE
REGULATORY
LABEL
PACKAGING
TECHNICAL_REQUIREMENT
OTHER
```

## 9.3 特性标识

一期至少支持：

```text
A = 关键特性
B = 重要特性
R = 法规特性
```

同时支持客户自定义符号，不在代码中硬编码图形。

建立：

```text
CharacteristicSymbolDefinition
------------------------------
id
customerId
code
name
symbolType
symbolValue
meaning
riskLevel
active
```

---

# 10. 图纸解析 Pipeline

## 10.1 总流程

```text
File Upload
   ↓
File Classifier
   ↓
Checksum / Virus / Duplicate Check
   ↓
┌────────────────────┬─────────────────────┐
│ PDF Pipeline       │ DWG Pipeline        │
└─────────┬──────────┴──────────┬──────────┘
          ↓                     ↓
   Canonical Entity Layer / DrawingIntermediateModel
                         ↓
                  TitleBlock Parser
                         ↓
                  Dimension Parser
                         ↓
                   Symbol Parser
                         ↓
                  Technical Notes
                         ↓
               Characteristic Builder
                         ↓
                   Rule Validation
                         ↓
                     Review UI
```

## 10.2 PDF

### Vector PDF

优先：

- 文字和坐标抽取
- Path/Line/BBox
- 表格/标题栏识别
- 字符与尺寸线空间关联

### Scan PDF

- OCR
- 版面分析
- VLM 作为复杂符号/工程语义补充
- 输出 OCR Block + Bounding Box

## 10.3 DWG

不能通过 OCR 作为主路径。

DWG Provider 输出：

- TEXT / MTEXT
- DIMENSION
- BLOCK / ATTRIBUTE
- LEADER / MLEADER
- Layer
- Geometry
- Entity Handle
- Title Block Candidate

## 10.4 解析 Job

```text
DrawingParseJob
---------------
id
drawingRevisionId
jobType
status
progress
startedAt
finishedAt
errorCode
errorMessage
parserVersion
modelVersion
resultFileId
```

status：

```text
PENDING
RUNNING
PARTIAL_SUCCESS
SUCCESS
FAILED
CANCELLED
```

---

# 11. 图纸 Review Workbench

页面结构：

```text
┌────────────────────────────────────────────────────────┐
│ 图纸信息 / Revision / Parse Status                     │
├───────────────────────┬────────────────────────────────┤
│ Characteristic List   │ Drawing Viewer                 │
│                       │                                │
│ 1 246±2               │      [高亮来源]                │
│ 2 8±0.5 [B]           │                                │
│ 3 6.2±0.5 [B]         │                                │
│ ...                   │                                │
├───────────────────────┴────────────────────────────────┤
│ Missing / Conflict / Confidence / Rule Check           │
└────────────────────────────────────────────────────────┘
```

功能：

- 自动特性候选列表
- 单项确认/拒绝/修改
- 批量确认
- 高/中/低置信度筛选
- 按类型筛选
- 证据定位
- 手工补充特性
- Merge/Split
- 重复检测
- 发布前 Coverage Check

---

# 12. 检查基准书

## 12.1 实体

### InspectionStandard

```text
id
standardNo
partId
drawingRevisionId
supplierId
vehicleModel
gaugeId
version
status
sourceType
effectiveDate
releaseDate
reactionPlan
createdBy
approvedBy
```

### InspectionStandardItem

```text
id
inspectionStandardId
sequenceNo
category
itemName
requirement
characteristicId
nominalValue
lowerLimit
upperLimit
specialCharacteristicCode

internalBatchSampling
internalBatchMethod

supplierBatchSampling
supplierBatchMethod

supplierAnnualSampling
supplierAnnualMethod

remark
dataCategory
sourceType
evidenceId
confidence
reviewStatus
```

category：

```text
PACKAGING
APPEARANCE
DIMENSION
PERFORMANCE
OTHER
```

## 12.2 生成原则

AI/Rule 生成的是 `InspectionStandardDraft`。

禁止直接生成 RELEASED。

来源分三类：

```text
DRAWING_AUTO
RULE_AUTO
HUMAN
```

### 图纸自动字段

- 零件号
- 图号
- 图纸版本
- 材料
- 尺寸值
- 公差
- 上下限
- 特性符号
- 部分技术要求
- 部分外观/材料/性能要求

### 规则自动字段

- 是否进入批次尺寸
- 是否进入年度尺寸
- 上下限计算
- 默认项目分类
- 推荐检验方法
- 默认模板

### 人工字段

- 最终抽样方案
- 最终检查手段
- 最终检具
- 供应商侧要求
- 特殊备注
- 反应计划
- 责任人

---

# 13. 检查基准书生成规则

建立规则表：

```text
InspectionRule
--------------
id
ruleCode
ruleName
scope
conditionExpression
actionExpression
priority
effectiveFrom
effectiveTo
status
version
```

第一批内置规则：

## RULE-IS-001 批次尺寸

```text
IF characteristic.type = DIMENSION
AND characteristic.inspectionDimension = true
THEN includeInSupplierBatch = true
```

## RULE-IS-002 年度尺寸

```text
IF characteristic.type = DIMENSION
AND referenceDimension = false
AND idealDimension = false
THEN includeInSupplierAnnual = true
```

## RULE-IS-003 特性一致性

```text
InspectionStandardItem.specialCharacteristicCode
=
QualityCharacteristic.specialCharacteristicCode
```

## RULE-IS-004 公差上下限

```text
upperLimit = nominalValue + upperTolerance
lowerLimit = nominalValue + lowerTolerance
```

## RULE-IS-005 法规特性

```text
IF specialCharacteristicCode = R
THEN mandatoryReview = true
AND allowAutoApprove = false
```

---

# 14. Coverage / Consistency Checker

这是 P0 核心功能。

## 14.1 Coverage

输出：

```text
Drawing Dimension Count
Eligible Batch Dimension Count
Eligible Annual Dimension Count
Inspection Standard Batch Covered
Inspection Standard Annual Covered
Missing Count
Extra Count
Ignored Count
```

示例：

```text
图纸尺寸总数                 39
参考尺寸                      6
理想尺寸                      2
年度理论应检                  31
基准书年度尺寸                30
疑似遗漏                       1
```

## 14.2 校验规则

### COV-001

图纸中所有 Batch Eligible Characteristic 必须有对应基准书项。

### COV-002

年度 Eligible Characteristic 必须覆盖。

### COV-003

参考尺寸不得因为 OCR 错误进入强制年度尺寸。

### COV-004

理想尺寸不得默认成为普通检验尺寸。

### COV-005

特性符号必须一致。

### COV-006

公差必须一致。

### COV-007

DrawingRevision 必须是当前基准书声明的 Revision。

### COV-008

基准书引用的检具必须存在。

### COV-009

受控发布时，所有 HIGH ERROR 必须为 0。

---

# 15. 试验验证计划

## 15.1 ValidationPlan

```text
id
planNo
partId
inspectionStandardId
drawingRevisionId
version
status
supplierId
createdBy
releaseDate
```

## 15.2 ValidationPlanItem

```text
id
validationPlanId
sequenceNo
testItem
standardSource
methodAcceptanceCriteria
laboratoryId

dvRequired
pvRequired
typeRequired
batchRequired

quantity
startDate
endDate
equivalentInfo
sourceInspectionItemId
sourceCharacteristicId
evidenceId
```

## 15.3 生成规则

### VAL-001 批次映射

```text
IF InspectionStandard.performanceItem.supplierBatchSampling is not empty
THEN ValidationPlanItem.batchRequired = true
```

### VAL-002 年度映射

```text
IF InspectionStandard.performanceItem.supplierAnnualSampling is not empty
THEN ValidationPlanItem.typeRequired = true
```

### VAL-003 标准继承

性能项 requirement / standard 可以作为 `standardSource` 和 `methodAcceptanceCriteria` 的初始候选。

### VAL-004 人工补充字段

以下字段不得强制由 AI 决策：

- 试验机构
- DV/PV 最终勾选
- 试验数量
- 开始/结束日期
- 等效信息

AI 可推荐，但必须人工确认。

---

# 16. 检具管理

## 16.1 Gauge

```text
id
gaugeNo
gaugeName
partId
supplierId
status
acceptanceStatus
acceptanceDate
calibrationStatus
validFrom
validTo
fileId
```

## 16.2 GaugeAcceptance

```text
id
gaugeId
reportNo
purpose
basis
conclusion
inspectionDate
inspector
reviewer
approver
```

## 16.3 GaugeMeasurementItem

```text
id
gaugeAcceptanceId
sequenceNo
standardValue
upperTolerance
lowerTolerance
equipment
measured1
measured2
measured3
averageValue
conclusion
```

## 16.4 发布校验

当 InspectionStandard 引用 Gauge 时：

```text
Gauge exists
AND acceptanceStatus = PASS
AND status = ACTIVE
```

P1 再加入 calibrationStatus 和 validTo 强制锁定。

---

# 17. 图纸变更影响分析

## 17.1 DrawingRevisionDiff

```text
id
drawingId
fromRevisionId
toRevisionId
status
createdAt
```

### DrawingDiffItem

```text
id
diffId
diffType
entityType
oldEntityId
newEntityId
oldValue
newValue
riskLevel
autoMatched
confidence
reviewStatus
```

diffType：

```text
ADDED
REMOVED
MODIFIED
MOVED
SYMBOL_CHANGED
TOLERANCE_CHANGED
NOTE_CHANGED
MATERIAL_CHANGED
REVISION_CHANGED
```

## 17.2 影响对象

```text
InspectionStandard
ValidationPlan
Gauge
ControlPlan
PFMEA
PPAP
InspectionTask
SupplierStandard
KnowledgeItem
```

## 17.3 ChangeImpactItem

```text
id
diffItemId
targetType
targetId
impactType
riskLevel
suggestedAction
owner
status
verifiedBy
verifiedAt
```

impactType：

```text
NO_IMPACT
REVIEW_REQUIRED
REVISION_REQUIRED
REAPPROVAL_REQUIRED
RETEST_REQUIRED
REVALIDATION_REQUIRED
```

## 17.4 变更分析流程

```text
新 Revision 上传
    ↓
解析并确认
    ↓
旧/新 Characteristic Match
    ↓
Diff
    ↓
查找 Data Lineage
    ↓
生成 Impact Items
    ↓
工程师确认
    ↓
生成变更任务
    ↓
受影响对象修订
    ↓
验证关闭
```

---

# 18. Data Lineage

建立统一关系表：

```text
ObjectRelation
--------------
id
sourceType
sourceId
relationType
targetType
targetId
effectiveFrom
effectiveTo
status
```

relationType 示例：

```text
DERIVED_FROM
USES_CHARACTERISTIC
VERIFIED_BY
MEASURED_BY
CONTROLLED_BY
SUPERSEDES
GENERATED_FROM
IMPACTS
RELATED_TO
```

P0 不强制上图数据库。

PostgreSQL 即可实现关系查询。

---

# 19. 受控状态机

## 19.1 InspectionStandard

```text
DRAFT
AI_GENERATED
REVIEWING
READY_FOR_APPROVAL
APPROVING
RELEASED
SUPERSEDED
OBSOLETE
REJECTED
```

## 19.2 ValidationPlan

```text
DRAFT
AI_GENERATED
REVIEWING
APPROVING
RELEASED
SUPERSEDED
CANCELLED
```

## 19.3 发布原则

- AI_GENERATED 不能进入业务执行。
- RELEASED 必须经过人工 Review + Approval。
- 新版本发布后旧版本进入 SUPERSEDED。
- 业务记录引用历史版本时必须保留历史关系，不允许物理覆盖。

---

# 20. AI Agent 总体设计

不设计“七个 Agent 互相聊天”。

采用：

```text
QMS AI Orchestrator
       │
       ├── Tool Registry
       ├── Context Builder
       ├── Permission Guard
       ├── Workflow Engine
       ├── Prompt Registry
       └── Model Router
```

用户看到多个业务助手，底层共享一个 Orchestrator。

## 20.1 Drawing Intelligence Agent

Tools：

```text
drawing.get
drawing.parse
drawing.getParseResult
drawing.listCharacteristics
drawing.compareRevision
drawing.locateEvidence
drawing.runCoverageCheck
```

允许：

- 解析
- 查询
- 解释
- 生成 Draft

不允许：

- 发布 DrawingRevision
- 覆盖人工确认值

## 20.2 Inspection Standard Agent

Tools：

```text
inspection.get
inspection.generateDraft
inspection.applyRules
inspection.coverageCheck
inspection.findSimilar
inspection.recommendMethod
inspection.createReviewTask
```

## 20.3 Validation Planning Agent

```text
validation.get
validation.generateDraft
validation.mapFromInspection
validation.searchStandard
validation.findHistoricalPlan
```

## 20.4 Change Impact Agent

```text
change.compare
change.findDependencies
change.analyzeImpact
change.createImpactDraft
```

## 20.5 Knowledge Agent

```text
knowledge.search
knowledge.get
knowledge.findSimilarCases
knowledge.findFailureModes
knowledge.findMeasures
```

---

# 21. AI Tool 安全约束

任何 AI Tool 必须声明：

```text
toolName
description
inputSchema
outputSchema
requiredPermission
writeMode
auditEnabled
timeout
```

writeMode：

```text
READ_ONLY
DRAFT_WRITE
BUSINESS_WRITE
CONTROLLED_WRITE
```

一期 AI 只允许：

```text
READ_ONLY
DRAFT_WRITE
```

禁止 AI 直接调用 CONTROLLED_WRITE。

---

# 22. AI 输出统一格式

所有提取类输出：

```json
{
  "value": "...",
  "confidence": 0.97,
  "evidence": {
    "page": 1,
    "entityId": "xxx",
    "bbox": [0,0,0,0],
    "rawText": "..."
  },
  "warnings": [],
  "extractor": {
    "type": "VLM",
    "version": "..."
  }
}
```

所有推荐类输出：

```json
{
  "recommendation": "...",
  "reason": "...",
  "references": [],
  "confidence": 0.82,
  "requiresHumanConfirmation": true
}
```

---

# 23. 知识库架构

```text
L4 AI Semantic / RAG
Embedding / Hybrid Search / Rerank
                ▲
L3 Relationship Knowledge
Part→Drawing→Characteristic→Problem
                ▲
L2 Structured Knowledge
Failure / Standard / Rule / Case / Measure
                ▲
L1 Source Document
PDF / DWG / FMEA / 8D / CP / Test Report
```

## 23.1 知识类型

```text
FAILURE_MODE
PROBLEM_CASE
CORRECTIVE_ACTION
PREVENTIVE_ACTION
LESSON_LEARNED
DESIGN_GUIDELINE
BEST_PRACTICE
TEST_STANDARD
CUSTOMER_REQUIREMENT
INSPECTION_METHOD
PROCESS_KNOWLEDGE
```

## 23.2 KnowledgeItem

```text
id
knowledgeType
title
summary
content
applicableProduct
applicablePart
applicableProcess
customerId
sourceObjectType
sourceObjectId
status
owner
reviewer
version
effectiveDate
confidence
```

## 23.3 知识状态

```text
RAW
EXTRACTED
REVIEWED
LESSON_LEARNED
DESIGN_GUIDELINE
BEST_PRACTICE
RETIRED
```

AI 不允许直接把 RAW 升级为 BEST_PRACTICE。

---

# 24. 质量成本模型

与客户现有成本分析保持四大类：

```text
PREVENTION
APPRAISAL
INTERNAL_FAILURE
EXTERNAL_FAILURE
```

## 24.1 QualityCostEntry

```text
id
orgId
businessUnit
costDate
costCategory
costSubCategory
amount
sourceSystem
sourceDocumentNo
partId
projectId
supplierId
customerId
qualityProblemId
remark
```

## 24.2 关键关系

```text
QualityProblem
     ↓
QualityCostEntry
```

必须支持未来查询：

- 某失效模式造成多少内部/外部损失？
- 某客户投诉对应多少索赔/筛选/退货费用？
- 某项目预防成本与故障成本趋势？
- 某供应商导致的质量成本？

---

# 25. 外部系统边界

## 25.1 PLM

权威数据：

```text
Project
Part
BOM
Drawing
DrawingRevision
DFMEA
PFMEA
ControlPlan
ECN/ECR
PPAP
```

QMS 不篡改 PLM Engineering Definition。

## 25.2 QMS

权威数据：

```text
QualityCharacteristic Review
InspectionStandard
ValidationPlan
QualityGate
Gauge Quality Status
QualityProblem
Knowledge
QualityCost
QualityAudit
```

## 25.3 MES

```text
ProductionOrder
Batch
ProcessParameter
FAI/IPQC/OQC
SPC
Defect
```

## 25.4 LIMS

```text
TestTask
Sample
TestResult
TestReport
Lab
Method
Instrument
```

## 25.5 ERP

```text
Material
Supplier
Purchase
InventoryBatch
Cost
CustomerOrder
Return
```

## 25.6 OA

- 统一待办
- 审批通知
- 消息
- 组织/人员同步（若客户最终确定由 OA 主数据负责）

---

# 26. API 设计

统一前缀：

```text
/api/v1
```

## 26.1 Drawing

```http
POST   /drawings
POST   /drawings/{drawingId}/revisions
POST   /drawing-revisions/{revisionId}/parse
GET    /drawing-revisions/{revisionId}
GET    /drawing-revisions/{revisionId}/parse-job
GET    /drawing-revisions/{revisionId}/characteristics
POST   /drawing-revisions/{revisionId}/review
POST   /drawing-revisions/{revisionId}/confirm
GET    /drawing-revisions/{revisionId}/evidence/{evidenceId}
POST   /drawing-revisions/{revisionId}/compare/{targetRevisionId}
```

## 26.2 Characteristic

```http
GET    /characteristics
GET    /characteristics/{id}
PATCH  /characteristics/{id}
POST   /characteristics/{id}/confirm
POST   /characteristics/{id}/reject
POST   /characteristics/batch-confirm
```

## 26.3 Inspection Standard

```http
POST   /inspection-standards
POST   /inspection-standards/generate-from-drawing
GET    /inspection-standards/{id}
PATCH  /inspection-standards/{id}
POST   /inspection-standards/{id}/coverage-check
POST   /inspection-standards/{id}/submit-review
POST   /inspection-standards/{id}/submit-approval
POST   /inspection-standards/{id}/release
POST   /inspection-standards/{id}/create-revision
```

## 26.4 Validation

```http
POST   /validation-plans/generate-from-inspection-standard
GET    /validation-plans/{id}
PATCH  /validation-plans/{id}
POST   /validation-plans/{id}/submit-approval
POST   /validation-plans/{id}/release
```

## 26.5 Change

```http
POST   /changes/drawing-revision-diff
GET    /changes/{id}
POST   /changes/{id}/analyze-impact
POST   /changes/{id}/confirm-impact
POST   /changes/{id}/create-actions
```

---

# 27. Event 设计

P0 先定义事件接口，即使底层仍是本地事件。

```text
DrawingRevisionUploaded
DrawingParseStarted
DrawingParseCompleted
DrawingReviewCompleted
DrawingRevisionReleased

InspectionStandardDraftGenerated
InspectionStandardCoverageFailed
InspectionStandardReleased

ValidationPlanDraftGenerated
ValidationPlanReleased

DrawingRevisionChanged
ChangeImpactGenerated

GaugeExpired
KnowledgeApproved
```

Event payload 统一：

```json
{
  "eventId": "...",
  "eventType": "...",
  "occurredAt": "...",
  "tenantId": "...",
  "orgId": "...",
  "actorId": "...",
  "objectType": "...",
  "objectId": "...",
  "payload": {}
}
```

---

# 28. 权限设计

采用：

```text
RBAC + Organization Scope + Data Permission
```

权限粒度：

```text
drawing:view
drawing:upload
drawing:review
drawing:release

characteristic:view
characteristic:review

inspection:create
inspection:edit
inspection:review
inspection:approve
inspection:release

validation:create
validation:edit
validation:approve
validation:release

ai:use
ai:admin
rule:admin
knowledge:review
```

数据范围：

```text
SELF
DEPARTMENT
ORG
FACTORY
BUSINESS_UNIT
GROUP
CUSTOM
```

---

# 29. 审计

所有以下动作必须有 AuditLog：

- 原文件上传/删除
- Revision 创建
- AI 解析
- AI 值修改
- Characteristic Confirm/Reject
- 基准书草稿生成
- 覆盖性检查
- 人工修改
- 审批
- 发布
- 作废
- 变更影响确认
- AI Tool 调用

AuditLog：

```text
id
actorId
action
objectType
objectId
beforeJson
afterJson
source
traceId
ip
createdAt
```

source：

```text
USER
AI
RULE
INTEGRATION
SYSTEM
```

---

# 30. 数据库表建议

P0 至少建立：

```text
qms_part
qms_drawing
qms_drawing_revision
qms_drawing_parse_job
qms_drawing_entity
qms_source_evidence

qms_characteristic
qms_characteristic_symbol_def

qms_inspection_standard
qms_inspection_standard_item
qms_inspection_rule
qms_inspection_check_result

qms_validation_plan
qms_validation_plan_item
qms_validation_rule

qms_gauge
qms_gauge_acceptance
qms_gauge_measurement_item

qms_revision_diff
qms_revision_diff_item
qms_change_impact
qms_change_impact_item

qms_object_relation

qms_file
qms_attachment
qms_audit_log

qms_knowledge_item
qms_knowledge_relation

sys_user
sys_org
sys_role
sys_permission
sys_dict
```

---

# 31. 前端页面清单

P0：

1. 零件列表
2. 零件详情
3. 图纸列表
4. 图纸 Revision 列表
5. 图纸上传页
6. 解析任务页
7. Drawing Review Workbench
8. 质量特性列表
9. 质量特性详情
10. 检查基准书列表
11. 检查基准书编辑页
12. 基准书 AI 生成 Wizard
13. Coverage Check 页面
14. 基准书只读发布页
15. 试验验证计划列表
16. 验证计划编辑页
17. 验证计划生成 Wizard
18. 检具列表
19. 检具验收详情
20. Drawing Revision Diff
21. Change Impact 页面
22. AI Copilot Side Panel
23. 系统字典
24. 规则配置
25. 审计日志

---

# 32. 检查基准书页面交互

页面建议采用：

```text
Header
├── Part / Drawing / Revision / Supplier / Gauge / Status
│
Tab 1 基本信息
Tab 2 包装/外观
Tab 3 尺寸
Tab 4 性能
Tab 5 履历
Tab 6 Coverage
Tab 7 Evidence
Tab 8 Approval
```

尺寸 Tab 必须支持：

- 200+ 行
- 固定列
- 批量编辑
- 过滤“AI未确认”
- 过滤“规则冲突”
- 过滤“特殊特性”
- 点击行定位图纸
- 显示来源 Revision
- 对比原始值和人工值

---

# 33. AI 置信度与人工复核

系统级默认阈值可配置：

```text
HIGH     >= 0.95
MEDIUM   >= 0.80 and < 0.95
LOW      < 0.80
```

但不同字段必须可单独配置。

建议：

- 图号/零件号/Revision：HIGH 阈值更高
- 数值尺寸/公差：HIGH 阈值更高
- 技术要求语义分类：允许 MEDIUM 进入人工 Review
- 特殊特性：任何 LOW 都必须人工确认

发布规则：

```text
LOW unresolved count = 0
HIGH severity conflict count = 0
mandatory review completed = true
approval completed = true
```

---

# 34. AI 验收指标

这些是项目验收目标，不是模型承诺。

## 34.1 数据集

客户提供真实历史图纸构建：

```text
Train / Prompt Set
Validation Set
Golden Test Set
```

Golden Set 必须由质量工程师逐项确认。

## 34.2 指标

建议第一阶段目标：

```text
标题栏关键字段 Exact Match >= 99%
数值尺寸提取 F1 >= 98%
公差提取 F1 >= 98%
特殊特性 Recall >= 99%
图纸->基准书 Eligible Item Recall >= 99%
受控发布时未确认高风险遗漏 = 0
```

注意：

最终业务验收不是“模型准确率达到多少就自动发布”，而是：

**AI + Rule + Human Review 后发布结果必须 100% 通过业务确认。**

---

# 35. 异常场景必须覆盖

1. PDF 是扫描件。
2. PDF 内嵌图片。
3. DWG 无法解析。
4. 图纸标题栏不是固定模板。
5. 同一图纸多个 Sheet。
6. 一个尺寸文本跨多个 Text Entity。
7. OCR 把 `8±0.5` 识别为 `8+0.5`。
8. `[B]` 和 Dimension 距离较远。
9. 旧图纸使用不同客户符号。
10. 同一 Revision 重复上传。
11. Revision Code 相同但文件 Checksum 不同。
12. 图纸删除了旧尺寸。
13. 旧基准书人工加入了图纸不存在的管理要求。
14. 检具未验收。
15. 检具编号存在但状态失效。
16. AI 生成性能项但没有验证规则。
17. 用户修改 AI 值后重新解析。
18. 新版本解析结果和人工修正冲突。
19. PLM 返回 Revision 和上传文件 Revision 不一致。
20. 系统集成中断后重试导致重复消息。

每一个异常必须有明确状态，不得静默失败。

---

# 36. 后端 Maven 工程结构

建议：

```text
qms/
├── pom.xml
├── qms-bootstrap/
├── qms-platform/
├── qms-masterdata/
├── qms-drawing/
├── qms-characteristic/
├── qms-inspection/
├── qms-validation/
├── qms-gauge/
├── qms-change/
├── qms-apqp/
├── qms-problem/
├── qms-knowledge/
├── qms-cost/
├── qms-ai-gateway/
├── qms-integration/
└── qms-test-support/
```

每个模块内部：

```text
domain/
application/
infrastructure/
interfaces/
```

禁止：

- Controller 直接操作 Mapper。
- AI Service 直接连接 QMS 数据库。
- 业务模块跨模块直接操作对方 Mapper。

---

# 37. 前端工程结构

```text
qms-web/
├── src/
│   ├── app/
│   ├── pages/
│   ├── features/
│   │   ├── drawing/
│   │   ├── characteristic/
│   │   ├── inspection/
│   │   ├── validation/
│   │   ├── gauge/
│   │   ├── change/
│   │   └── ai/
│   ├── entities/
│   ├── shared/
│   ├── services/
│   ├── hooks/
│   └── stores/
```

Viewer 单独：

```text
features/drawing-viewer/
├── PdfViewer
├── CadPreview
├── EvidenceOverlay
├── BoundingBoxLayer
├── RevisionCompareLayer
└── CharacteristicMarkerLayer
```

---

# 38. AI Service 工程结构

```text
qms-ai/
├── app/
│   ├── api/
│   ├── jobs/
│   ├── agents/
│   ├── tools/
│   ├── parsers/
│   │   ├── pdf/
│   │   ├── ocr/
│   │   ├── cad/
│   │   └── common/
│   ├── extractors/
│   ├── validators/
│   ├── knowledge/
│   ├── model_router/
│   └── schemas/
├── tests/
└── prompts/
```

Prompt 不允许散落在 Python 业务代码中。

---

# 39. Codex 开发规则

## 39.1 总原则

Codex 必须按阶段执行，不允许“一次性实现整个 QMS”。

每阶段：

```text
设计确认
→ 数据模型
→ Migration
→ Domain
→ API
→ Unit Test
→ Integration Test
→ Frontend
→ E2E
→ Documentation
```

## 39.2 强制约束

1. 所有 DB Schema 使用 Flyway 管理。
2. 所有 API 写 OpenAPI。
3. 所有业务状态使用 Enum + State Transition 校验。
4. 所有受控对象带 version。
5. 所有 AI 生成数据带 evidence/confidence。
6. 所有 AI 写操作只能进入 Draft。
7. 所有 Mapper 只在本模块 Infrastructure 使用。
8. 所有业务规则可配置，不硬编码客户符号。
9. 所有日期时间统一存 UTC，显示按用户时区。
10. 所有接口写 Idempotency 策略。
11. 所有 Integration Event 带 eventId。
12. 所有文件保存 checksum。
13. 删除业务受控数据默认软删除/状态作废，不物理删除。
14. 发布后的 Revision 不允许原地编辑。
15. 任何图纸变更必须产生新 Revision 或明确 Change Log。

---

# 40. Codex 禁止事项

Codex 不允许：

- 用 OCR 作为 DWG 主解析方案。
- AI 直接更新 RELEASED 数据。
- 用一个 JSON 大字段代替所有核心业务表。
- 为赶进度把 SourceEvidence 省掉。
- 把特性符号 `[A]/[B]/[R]` 直接写死在多个页面。
- 把规则写在前端。
- Controller 中写完整业务逻辑。
- 把所有模块塞进一个 package。
- 第一阶段引入 Kubernetes、Service Mesh 等不必要复杂度。
- 先做漂亮聊天机器人而没有图纸数据结构化能力。

---

# 41. 实施阶段拆分

## Phase 0：工程底座

- Maven 多模块
- React 基础框架
- Spring Security
- 用户/组织/角色
- PostgreSQL/Flyway
- 文件服务
- 审计
- OpenAPI
- Docker Compose

验收：

- 能启动
- 能登录
- 能上传文件
- 权限有效
- Migration 自动执行
- CI 可运行测试

---

## Phase 1：Part / Drawing / Revision

开发：

- Part
- Drawing
- DrawingRevision
- 上传
- Checksum
- Revision 状态
- Viewer

验收：

- 可创建零件
- 可挂多个 Drawing
- 可上传多个 Revision
- 可查看历史版本

---

## Phase 2：Source Evidence + Drawing Intermediate Model

开发：

- DrawingEntity
- SourceEvidence
- JSON Schema
- PDF Preview
- Evidence Overlay

验收：

- 后端返回 BBox
- 前端点击 evidence 能定位图纸

---

## Phase 3：PDF Parser

优先支持样例 PDF：

- 标题栏
- 图号
- 零件名
- Revision
- 线性尺寸
- 公差
- A/B/R
- 技术要求

先不要追求所有 GD&T。

验收：

- 用客户样例输出结构化 JSON
- 人工可 Review

---

## Phase 4：DWG Parser Adapter

- CadParserProvider SPI
- DWG Provider
- Entity 标准化
- DIMENSION / TEXT / BLOCK / ATTRIBUTE
- Source Handle

验收：

- DWG 和 PDF 输出同一 Intermediate Model

---

## Phase 5：Quality Characteristic Center

- Characteristic CRUD
- Auto Candidate
- Review/Confirm/Reject
- Evidence
- Symbol Dictionary

验收：

- 用户能完整确认一张图的 Characteristic

---

## Phase 6：Inspection Standard

- 实体
- 页面
- 从 Characteristic 生成 Draft
- Rule Engine
- 人工补充
- Version
- Approval

验收：

使用客户“检查基准书.pdf”样例，能够生成结构相近的草稿数据。

---

## Phase 7：Coverage Checker

实现：

- Batch Eligible
- Annual Eligible
- Missing
- Extra
- Special Characteristic
- Tolerance
- Revision
- Gauge

验收：

- 能明确报告遗漏/冲突
- HIGH ERROR 未关闭不能发布

---

## Phase 8：Validation Plan

- 从性能项生成
- Batch/Type Mapping
- 人工 DV/PV
- Lab
- Time
- Quantity
- Version/Approval

验收：

用客户“实验验证计划.pdf”逻辑验证映射正确。

---

## Phase 9：Gauge

- Gauge Master
- Acceptance
- Inspection Standard Link
- Gauge Validity Check

---

## Phase 10：Drawing Revision Diff

- Characteristic Match
- Added/Removed/Changed
- Change Impact

验收：

- 新 Revision 能列出基准书影响清单

---

## Phase 11：Knowledge Base

- Source Document
- Knowledge Item
- RAG
- Similar Search
- AI Copilot Read-only

---

# 42. 第一批开发 Epic

```text
EPIC-001 Platform Foundation
EPIC-002 Part & Drawing
EPIC-003 Drawing Viewer & Evidence
EPIC-004 PDF Parsing
EPIC-005 CAD Parsing
EPIC-006 Quality Characteristic
EPIC-007 Inspection Standard
EPIC-008 Inspection Coverage Checker
EPIC-009 Validation Planning
EPIC-010 Gauge
EPIC-011 Revision Diff & Impact
EPIC-012 Knowledge & AI Copilot
```

---

# 43. 第一批 User Story 示例

## US-001 上传图纸

作为质量工程师，我可以给一个 Part 上传 PDF/DWG，并指定 Drawing No 和 Revision，以便系统进入自动解析。

Acceptance：

- PDF/DWG 格式校验
- 计算 checksum
- 同 Revision 同 checksum 禁止重复
- 创建 Parse Job
- 全程审计

## US-002 查看解析结果

作为质量工程师，我可以查看系统识别到的尺寸和特殊特性，并定位其原图来源。

Acceptance：

- 每项有 evidence
- 可以高亮
- 可以 Confirm/Reject/Edit

## US-003 生成检查基准书

作为 SQE，我可以根据已确认的 DrawingRevision 自动生成检查基准书草稿。

Acceptance：

- 只使用 CONFIRMED Characteristic
- 批次/年度按规则生成
- 显示来源
- 状态为 AI_GENERATED/DRAFT
- 不允许自动 RELEASE

## US-004 Coverage Check

作为审批人，我在发布检查基准书前可以检查图纸要求是否完整覆盖。

Acceptance：

- 输出 missing/extra/conflict
- HIGH ERROR = 0 才可提交审批

## US-005 生成验证计划

作为产品/质量工程师，我可以根据基准书性能项生成试验验证计划草稿。

Acceptance：

- 批次映射 batch
- 年度映射 type
- DV/PV 默认不强行决策
- 用户可补充实验机构/数量/日期

---

# 44. 单元测试要求

每个 Domain Service 至少：

- 正常路径
- 边界
- 状态非法
- 权限
- 幂等
- 版本冲突

重点测试：

```text
ToleranceCalculatorTest
InspectionEligibilityRuleTest
CoverageCheckerTest
CharacteristicSymbolConsistencyTest
RevisionStateMachineTest
ValidationMappingRuleTest
GaugeValidityRuleTest
DrawingDiffMatcherTest
```

---

# 45. 集成测试

使用真实 PostgreSQL Test Container 或 CI 数据库。

必须验证：

- Flyway
- Transaction
- API
- Object Storage Adapter
- AI Stub
- CAD Parser Stub

AI/Parser 的测试应同时存在：

```text
unit fixture
golden sample
regression dataset
```

---

# 46. E2E 核心用例

## E2E-001

```text
创建 Part
→ 上传图纸
→ Parse
→ Review Characteristic
→ Confirm
→ Generate Inspection Standard
→ Coverage Check
→ Human Edit
→ Approval
→ Release
```

## E2E-002

```text
Released Inspection Standard
→ Generate Validation Plan
→ Human Edit
→ Approval
→ Release
```

## E2E-003

```text
Upload New Drawing Revision
→ Parse
→ Confirm
→ Diff
→ Impact Analysis
→ Create Inspection Revision
```

---

# 47. 数据迁移原则

历史 OA 表单不要直接“原样导入业务表”。

分两阶段：

### Stage A

导入：

- Part
- Drawing No
- Revision
- Inspection Standard No
- Gauge No
- Validation Plan No
- 正式文件附件

### Stage B

解析历史附件并结构化：

- Characteristic
- Inspection Item
- Performance Item
- Knowledge

所有迁移数据必须有：

```text
migrationBatchId
sourceSystem
sourceRecordId
sourceFileId
```

---

# 48. MVP 不做范围

P0 暂不实现：

- 完整 3D CAD
- 所有 GD&T 自动理解
- CMM 程序自动生成
- MES SPC 完整功能
- 完整 LIMS
- 全量 FMEA 编辑器
- 自动批准
- 自训练大模型平台
- 多 Agent 自主循环执行
- 知识图谱专用数据库
- Kubernetes

---

# 49. 项目完成定义

一期不是“页面做完”。

一期 Done Definition：

1. PDF/DWG 都能进入统一 Drawing Model。
2. 样例图纸能解析出关键主信息和主要尺寸。
3. 所有自动字段有 SourceEvidence。
4. 能形成 QualityCharacteristic。
5. 能从 Characteristic 生成基准书草稿。
6. 能做 Coverage/Consistency Check。
7. 人工可补充非图纸字段。
8. 能审批发布版本。
9. 能从性能项生成验证计划草稿。
10. 能管理检具关联。
11. 新 Revision 能 Diff。
12. 能生成 Change Impact。
13. AI 不能绕过人工审批。
14. 全流程可审计。
15. 核心 E2E 自动测试通过。

---

# 50. 给 Codex 的首条执行指令建议

将以下内容复制给 Codex 作为第一个开发任务：

```text
请严格按照《锦恒 QMS 质量管理系统总体架构与一期详细设计说明书》开发。

不要一次性实现全部系统。

第一阶段仅执行 Phase 0 + Phase 1：

1. 创建 Maven 多模块 Spring Boot 工程；
2. 创建 React + TypeScript 前端工程；
3. 使用 PostgreSQL + Flyway；
4. 完成 User/Org/Role 最小权限框架；
5. 完成文件上传与 Object Storage Adapter；
6. 完成 Part、Drawing、DrawingRevision 领域模型；
7. 完成 Drawing Revision 上传、列表、详情、状态管理；
8. 完成 PDF 基础 Viewer；
9. 所有 API 输出 OpenAPI；
10. 编写单元测试与集成测试；
11. 提供 docker-compose 本地环境；
12. 完成后停止，不要进入 AI Parsing 开发。

开发过程中遵循：
- Controller 不直接调用 Mapper；
- 发布数据不可原地修改；
- 使用 Flyway；
- 不要提前引入微服务/Kubernetes；
- 所有文件记录 checksum；
- 所有受控操作写 AuditLog；
- 目录和模块边界严格按照说明书；
- 每完成一个模块先运行测试和构建，再继续。
```

---

# 51. 后续详细设计文档建议

在进入 Phase 3 之前，应继续补充三份开发级子文档：

1. 《Drawing Intermediate Model JSON Schema 与解析规范》
2. 《检查基准书字段级 Mapping Matrix 与 Rule DSL》
3. 《AI Agent Tool Contract / Prompt / Evaluation 规范》

这三份文档决定后续 PDF/DWG、AI、检查基准书生成是否能稳定落地。

---

**END**