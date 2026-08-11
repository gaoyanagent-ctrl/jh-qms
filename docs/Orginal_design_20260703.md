1.  A和D  但是我的疑问是 开发的脚手架本身也是会变成产品的一部分吧？
2.  工程重复 这个肯定是要在脚手架中具备的基础功能， 业务建模困难 这个也存在，就是需要建立很多业务的场景模板 这两个都要解决，但是 业务建模我们可以暂时先创建WMS相关的，后面在扩展，而 工程重复里面提到的基础功能都要尽量完善，这样同事在这个基础上开发功能就很快速
3. 我们未来要改变组织架构你提到的哲学分开的角色很快就都会消失了 我们主要用 AI coding 工具 生成代码，业务顾问 可以用这个平台生成原型。
4.  主要使用  Cursor  Codex CLI   Claude Code
5. 团队中有java 开发人员， 没有react的开发人员   前端我倾向继续使用react 后端推荐是使用spring boot 还是 go的框架 ？
6.  第一阶段用 模块化单体 + 轻量 DDD，不要一开始上完整 Spring Cloud 微服务。
7.  你提到的 基础平台  制造企业通用能力 这两个里面所有内容都是需要的  AI coding 支撑能力 可以先不做那么多
8.  WMS  要有 从基础设置到采购收货 上架 等设计到 算法 规则 策略 和流程控制的基础功能
9. 框架提供“标准场景模板”，不是完整产品模块
10.  第一阶段选 B：轻量 DSL + 代码生成，不做完整运行时元数据平台。
11. DSL 生成源码，而不是运行时动态页面。
12. 审批流是框架核心能力之一，但不要暴露 Flowable 细节。
13. 接受
14. 这些都要：制造企业应用常见权限包括：

权限类型	示例
菜单权限	是否能看到 WMS 菜单
按钮权限	是否能点击审核、关闭、作废
数据权限	只能看本工厂、本部门、本供应商数据
字段权限	某角色不能看成本字段
审批节点字段权限	审批节点只能编辑指定字段
单据操作权限	只有创建人可修改草稿
组织隔离	集团、事业部、工厂、车间
外部用户权限	供应商门户只能看自己的数据
15. 客户项目是否普遍需要多组织  多组织必须放在第一版底座里
16， 第一阶段使用 标准表 + ext_json + 代码生成 migration。
17.  按你的建议做， 但是平时同事可能也会直接用自然语言描述，然后新增和修改功能。
18. 必须包含。否则它只是普通脚手架，不是“AI coding 快速开发框架”。
19. React + TypeScript + Ant Design Pro / Ant Design X 风格组件体系。
20. 这几个是必须的：打印模板设计器
看板设计器
审批流程设计器
条码标签设计器
21.  第一版内置轻量 Outbox，但不必一开始上 NATS/Kafka。
22. 规则主要给给业务人员配置
23.  需要内置报表能力 先做高级查询 + 导出 Excel + 简单统计卡片 + 简单图表
24. 企微
钉钉
飞书 这几个基础要有， ERP 这些 要单独做一个接口配置和调用的模块吧
25 单客户私有化部署
模块化单体
支持多组织
预留多租户字段
26. 你说的这些都要

下面我基于你的回答，先给出一个**第一版系统框架定位、架构设计、技术选型和建设路线**。后续可以继续拆成：

1. 架构蓝图
2. 技术选型报告
3. Codex/Cursor/Claude Code 可执行开发说明书
4. WMS 样板模块设计
5. MVP 版本计划和工作量估算

---

# 1. 先回答你的关键疑问：脚手架会不会变成产品的一部分？

会，但要分阶段看。

你现在要做的不是一个普通脚手架，而是一个：

> **可产品化的 AI Coding 工业应用开发框架**

它初期表现为“脚手架 + 模板 + 基础平台 + AI coding 规范”，但随着项目沉淀，会逐步变成产品底座。

可以分成三层理解。

---

## 1.1 普通脚手架

普通脚手架只负责创建项目：

```text
create project
generate CRUD
generate page
generate api
generate database table
```

这类东西通常不会成为产品核心，更多是开发工具。

---

## 1.2 平台型脚手架

你要做的是平台型脚手架，它不只是生成代码，还内置：

```text
用户
组织
角色
权限
字典
附件
审计
审批
状态机
规则
打印
条码
报表
接口集成
AI coding 规范
制造业务模板
```

这类能力会直接进入最终系统运行时。

所以它一定会成为产品的一部分。

---

## 1.3 产品化开发框架

最终它会演进为：

```text
工业应用开发框架
+ 基础平台
+ 业务运行底座
+ 代码生成体系
+ 场景模板市场
+ AI coding 工程规范
```

也就是说：

```text
早期：脚手架
中期：项目开发框架
后期：工业应用产品底座
长期：IAOS 的轻量前置形态
```

你现在要避免的是：

> 一开始就把它做成完整 IAOS 或完整低代码平台。

正确路线是：

```text
先让项目能快速交付
再让模板不断沉淀
再让模板产品化
最后再向 IAOS 的语义化、Agent 化、运行时化演进
```

---

# 2. 这个框架的建议定位

我建议这个系统的正式定位是：

> **面向制造企业管理系统的 AI Coding 应用开发框架**

它的目标不是先做 MES/WMS/SRM 产品，而是先建立一个能快速生成这些系统的统一底座。

可以命名为：

```text
Industrial AI Coding Application Framework
```

中文：

```text
工业 AI 应用开发框架
```

或者更贴近你们公司内部使用：

```text
制造业 AI Coding 快速开发框架
```

我建议内部代号可以叫：

```text
MAF
Manufacturing Application Framework
```

或者：

```text
AIF
AI Industrial Framework
```

但如果希望和 IAOS 形成关系，可以叫：

```text
IAF
Industrial Application Framework
```

关系如下：

```text
IAOS = 长期工业操作系统
IAF = 当前可落地的工业应用开发框架
```

---

# 3. 总体结论

根据你的回答，我建议第一版采用下面这个方向：

```text
React + TypeScript + Ant Design Pro
Spring Boot 3.x 模块化单体
轻量 DDD
标准表 + ext_json
轻量 DSL + 源码生成
Flowable 内核 + 自研 Approval DSL
状态机 / 审批 / 权限 / 规则 / 报表 / 打印 / 条码 / 看板
WMS 作为第一业务模板
AI Coding 规范包作为核心资产
单客户私有化部署
支持多组织，预留多租户字段
```

一句话总结：

> 第一版不要做“低代码平台”，而要做“AI Coding 友好的制造业应用框架”。

---

# 4. 后端选型：Spring Boot 还是 Go？

你的场景下，我明确建议：

> **第一版继续使用 Spring Boot，不建议切 Go。**

---

## 4.1 为什么不建议第一版用 Go？

Go 的优势是：

```text
高性能
部署简单
并发模型好
二进制发布方便
适合基础设施、中间件、网关、采集服务
```

但你的核心需求是：

```text
制造企业管理系统
复杂业务建模
权限
审批
状态机
规则
报表
事务一致性
多组织
表单单据
AI coding 代码生成
```

这类系统更适合 Java 生态。

Go 在这些方面的问题是：

| 维度              | Go 的问题                    |
| --------------- | ------------------------- |
| 企业管理系统生态        | 不如 Java 成熟                |
| ORM / 事务 / 复杂查询 | Java 更成熟                  |
| 工作流 / 审批流       | Java 有 Flowable / Camunda |
| 权限框架            | Spring Security 生态成熟      |
| 报表 / Excel / 文档 | Java 生态更丰富                |
| AI coding 样例    | Java + Spring Boot 样例更多   |
| 团队能力            | 你们已有 Java 开发人员            |
| 制造企业系统          | 国内外 Java 使用更普遍            |

如果用 Go，你们会面临：

```text
框架自研成本上升
审批流集成成本上升
复杂业务代码规范更难统一
团队学习成本增加
AI 生成代码质量未必稳定
```

---

## 4.2 Go 可以放在哪里？

不是完全不用 Go，而是不要让 Go 承担主业务系统。

Go 更适合放在这些位置：

```text
设备数据采集服务
边缘网关
条码/PDA 轻量服务
消息转发服务
接口代理服务
高并发数据接入服务
文件转换服务
```

也就是说：

```text
主业务平台：Spring Boot
边缘/网关/采集：Go 可选
```

---

## 4.3 推荐后端结论

```text
第一版主框架：Spring Boot 3.x
架构形态：模块化单体
业务结构：轻量 DDD
后续扩展：可拆分 Spring Cloud 或 Go 边缘服务
```

---

# 5. 整体架构设计

建议整体分成 8 层。

```text
┌──────────────────────────────────────────────┐
│  AI Coding Layer                              │
│  Prompt / DSL / Task Spec / Code Generator    │
├──────────────────────────────────────────────┤
│  Web Application Layer                        │
│  React + Ant Design Pro + Design Tools        │
├──────────────────────────────────────────────┤
│  Application Service Layer                    │
│  UseCase / Command / Query / Workflow Adapter │
├──────────────────────────────────────────────┤
│  Domain Layer                                 │
│  Business Object / Document / State / Rule    │
├──────────────────────────────────────────────┤
│  Platform Capability Layer                    │
│  Auth / Org / Permission / Approval / Rule    │
├──────────────────────────────────────────────┤
│  Manufacturing Common Layer                   │
│  Material / Supplier / Customer / Plant / WH  │
├──────────────────────────────────────────────┤
│  Scenario Template Layer                      │
│  WMS / SRM / MES / QMS / Cost Templates       │
├──────────────────────────────────────────────┤
│  Infrastructure Layer                         │
│  DB / Redis / MinIO / Outbox / Integration    │
└──────────────────────────────────────────────┘
```

---

# 6. 第一版模块划分

建议项目采用模块化单体。

后端结构：

```text
backend/
  platform-core/
  platform-auth/
  platform-org/
  platform-permission/
  platform-dictionary/
  platform-attachment/
  platform-audit/
  platform-message/
  platform-workflow/
  platform-statemachine/
  platform-rule/
  platform-codegen/
  platform-report/
  platform-print/
  platform-barcode/
  platform-dashboard/
  platform-integration/

  manufacturing-master/
  manufacturing-org/
  manufacturing-material/
  manufacturing-bom/
  manufacturing-process/
  manufacturing-warehouse/
  manufacturing-quality/

  scenario-wms/
  scenario-srm/
  scenario-mes/
  scenario-qms/

  app-server/
```

前端结构：

```text
frontend/
  src/
    app/
    layouts/
    routes/
    services/
    components/
    permissions/
    modules/
      platform/
      manufacturing/
      wms/
      srm/
      mes/
      qms/
    designers/
      approval-designer/
      print-designer/
      barcode-label-designer/
      dashboard-designer/
    generated/
    ai/
```

AI coding 规范：

```text
ai-coding/
  01-project-rules.md
  02-backend-rules.md
  03-frontend-rules.md
  04-database-rules.md
  05-ddd-rules.md
  06-permission-rules.md
  07-approval-rules.md
  08-state-machine-rules.md
  09-rule-engine-rules.md
  10-wms-template-rules.md
  11-code-review-rules.md
  12-testing-rules.md
  task-templates/
  prompt-templates/
  examples/
```

DSL：

```text
dsl/
  business-object/
  business-document/
  approval/
  state-machine/
  rule/
  permission/
  page/
  report/
  print/
  barcode/
  dashboard/
  integration/
```

---

# 7. 技术选型建议

## 7.1 后端技术栈

| 能力        | 推荐选型                            |
| --------- | ------------------------------- |
| 主框架       | Spring Boot 3.x                 |
| 架构        | 模块化单体 + 轻量 DDD                  |
| Java 版本   | Java 21                         |
| ORM       | MyBatis Plus 或 JPA              |
| 数据库       | PostgreSQL 优先，MySQL 可兼容         |
| Migration | Flyway                          |
| 权限认证      | Spring Security + JWT           |
| 缓存        | Redis                           |
| 文件存储      | MinIO                           |
| 审批流       | Flowable                        |
| 状态机       | 自研轻量状态机                         |
| 规则引擎      | 自研规则模型 + JSON Logic / Aviator   |
| Outbox    | 自研 Outbox 表 + 调度任务              |
| Excel     | EasyExcel                       |
| OpenAPI   | springdoc-openapi               |
| 测试        | JUnit 5 + Testcontainers        |
| 部署        | Docker Compose 起步，后续 Kubernetes |

---

## 7.2 ORM 选择：MyBatis Plus 还是 JPA？

我建议第一版用：

> **MyBatis Plus + XML/Wrapper 复杂查询补充**

原因：

```text
国内 Java 开发人员熟悉
AI coding 生成更稳定
复杂报表查询可控
SQL 可读性强
制造系统里大量列表、报表、台账查询
```

JPA 在复杂业务模型上有优势，但制造企业系统经常有复杂查询、报表、单据明细、库存台账，MyBatis 系更直接。

建议规范是：

```text
简单 CRUD：MyBatis Plus
复杂查询：Mapper XML
跨模块查询：Application Query Service
禁止 Controller 直接调用 Mapper
```

---

## 7.3 前端技术栈

| 能力         | 推荐选型                               |
| ---------- | ---------------------------------- |
| 主框架        | React 18/19 + TypeScript           |
| UI         | Ant Design Pro + Ant Design X 风格   |
| 路由         | React Router 或 Umi Max             |
| 请求         | TanStack Query / ahooks useRequest |
| 表格         | ProTable                           |
| 表单         | ProForm                            |
| 状态         | Zustand                            |
| 权限         | 前端权限指令 + 后端权限校验                    |
| 图表         | ECharts                            |
| 拖拽设计器      | React Flow / 自研 Canvas             |
| 打印设计器      | 可先自研轻量模板，后续扩展                      |
| 条码         | JsBarcode / QRCode                 |
| Excel 导入导出 | 后端 EasyExcel，前端上传下载                |

如果你们没有 React 开发人员，我不建议一开始搞太现代、太灵活的前端体系。

建议用：

```text
Ant Design Pro + ProComponents
```

原因：

```text
后台系统能力完整
AI coding 样例多
列表/表单/详情/权限/菜单都有范式
对 Java 团队更友好
```

---

# 8. 平台基础能力设计

你说基础平台和制造通用能力都需要完整，这里建议第一版分成“必须完整”和“先轻量可用”。

---

## 8.1 基础平台模块

第一版必须做完整：

```text
用户管理
组织管理
岗位管理
角色管理
权限管理
菜单管理
按钮权限
API 权限
数据权限
字段权限
数据字典
系统参数
编码规则
附件管理
操作日志
审计日志
登录日志
消息通知
站内信
审批流
状态机
业务规则
导入导出
打印模板
条码标签
看板
报表
接口集成
```

---

## 8.2 制造企业通用能力

第一版建议做到“数据模型完整，业务功能轻量”。

```text
集团
公司
事业部
工厂
车间
产线
工作中心
仓库
库区
库位
客户
供应商
物料
物料分类
计量单位
BOM
工艺路线
工序
设备
人员
班组
批次
序列号
库存台账
库存事务
```

注意：

> 不要一开始把 BOM、工艺路线、设备管理都做成完整产品模块。

第一版它们主要作为 WMS 和后续 MES 的基础数据。

---

# 9. 多组织模型设计

你明确要求第一版支持多组织。

建议组织模型不要只做一个 `sys_org` 树，而是分成两套：

## 9.1 管理组织

```text
Tenant
Group
Company
BusinessUnit
Department
Position
User
```

用于：

```text
人员归属
审批人解析
角色授权
数据权限
组织隔离
```

---

## 9.2 制造运营组织

```text
Plant
Workshop
ProductionLine
WorkCenter
Warehouse
WarehouseArea
Location
```

用于：

```text
生产
仓储
库存
工艺
质量
设备
```

---

## 9.3 为什么要分开？

因为制造企业里：

```text
一个部门可能管理多个仓库
一个工厂可能有多个车间
一个车间可能有多条产线
一个员工属于部门，但操作某个工作中心
一个仓库可能服务多个车间
```

如果只用一棵组织树，会很快混乱。

建议数据模型：

```text
sys_org
sys_user
sys_position
sys_role
sys_user_org
sys_user_position

mfg_plant
mfg_workshop
mfg_production_line
mfg_work_center

wm_warehouse
wm_warehouse_area
wm_location
```

---

# 10. 权限模型设计

你要求的权限粒度比较完整，第一版可以设计成：

```text
菜单权限
按钮权限
API 权限
数据权限
字段权限
单据操作权限
审批节点字段权限
外部用户权限
组织隔离权限
```

---

## 10.1 权限核心模型

```text
User
Role
Permission
Menu
Resource
DataScope
FieldPermission
OperationPermission
ExternalPrincipal
```

---

## 10.2 权限编码规范

建议所有权限点统一编码：

```text
模块:对象:动作
```

例如：

```text
wms:receipt:view
wms:receipt:create
wms:receipt:update
wms:receipt:submit
wms:receipt:approve
wms:receipt:post
wms:receipt:cancel
wms:receipt:export
```

字段权限：

```text
wms:receipt:field:supplierPrice:hide
wms:receipt:field:costAmount:readonly
```

---

## 10.3 数据权限类型

```text
ALL
TENANT
COMPANY
BUSINESS_UNIT
PLANT
DEPARTMENT
WAREHOUSE
SELF
CUSTOM
SUPPLIER_SELF
CUSTOMER_SELF
```

其中：

```text
SUPPLIER_SELF
CUSTOMER_SELF
```

是给 SRM/CRM/门户准备的。

---

# 11. 状态机设计

你已经接受状态分层，这是非常关键的设计。

所有业务单据建议统一包含：

```text
document_status
approval_status
execution_status
settlement_status
```

不是所有单据都需要全部字段，但框架要支持这种范式。

---

## 11.1 状态分层

### 单据状态

```text
DRAFT
SUBMITTED
EFFECTIVE
CLOSED
CANCELLED
```

### 审批状态

```text
NOT_REQUIRED
PENDING
APPROVED
REJECTED
WITHDRAWN
```

### 执行状态

```text
NOT_STARTED
PARTIAL
COMPLETED
EXCEPTION
```

### 结算状态

```text
NOT_SETTLED
PARTIAL_SETTLED
SETTLED
```

---

## 11.2 WMS 收货单示例

```text
document_status:
  DRAFT -> SUBMITTED -> EFFECTIVE -> CLOSED

approval_status:
  NOT_REQUIRED / PENDING / APPROVED / REJECTED

execution_status:
  NOT_RECEIVED -> PARTIAL_RECEIVED -> FULL_RECEIVED

posting_status:
  NOT_POSTED -> POSTED -> REVERSED
```

库存类单据还建议加：

```text
posting_status
```

因为 WMS 里“单据审批”和“库存过账”必须分开。

---

# 12. 审批流设计

审批流是框架核心能力。

建议对业务开发者暴露自研模型，不暴露 Flowable。

---

## 12.1 对外模型

```text
ApprovalDefinition
ApprovalNode
ApprovalCondition
ApprovalAssigneeRule
ApprovalAction
ApprovalTask
ApprovalInstance
ApprovalFieldPermission
ApprovalStateMapping
```

---

## 12.2 内部实现

```text
Approval DSL
   ↓
BPMN Generator
   ↓
Flowable Process Definition
   ↓
Flowable Runtime
   ↓
业务单据状态同步
```

---

## 12.3 审批人解析

支持：

```text
固定用户
角色
岗位
部门负责人
上级领导
工厂负责人
仓库负责人
发起人上级
字段指定人员
表达式解析
自定义 Bean
```

---

## 12.4 审批动作

```text
提交
同意
拒绝
退回
撤回
转办
加签
减签
委托
作废
```

第一版可以先做：

```text
提交
同意
拒绝
退回
撤回
转办
```

---

# 13. 规则引擎设计

你明确说规则主要给业务人员配置，所以不能只用 Java 代码或 SpEL。

建议做一套轻量业务规则引擎。

---

## 13.1 第一版规则类型

```text
字段校验规则
必填规则
审批触发规则
审批路由规则
库存策略规则
上架策略规则
分配策略规则
预警规则
编码规则
打印选择规则
消息通知规则
```

---

## 13.2 表达式引擎选择

我建议第一版使用：

> **JSON Logic + 可视化规则配置**

原因：

```text
比 SpEL 更安全
比 Drools 更轻量
适合前端可视化配置
适合 JSON/YAML DSL
适合 AI 生成
业务人员可以通过条件编辑器配置
```

规则表达式示例：

```json
{
  "and": [
    { ">": [ { "var": "amount" }, 100000 ] },
    { "==": [ { "var": "plantCode" }, "PLANT_01" ] }
  ]
}
```

---

## 13.3 规则执行模型

```text
RuleDefinition
RuleCondition
RuleAction
RuleBinding
RuleExecutionLog
```

绑定对象：

```text
业务对象
业务动作
审批节点
状态迁移
页面字段
库存策略
消息通知
```

---

# 14. 数据模型策略

你选择：

> 标准表 + ext_json + 代码生成 migration

这是正确的。

---

## 14.1 标准表原则

所有核心业务对象都有真实表。

例如：

```text
wms_receipt_order
wms_receipt_order_line
wms_putaway_task
wms_inventory_balance
wms_inventory_transaction
```

---

## 14.2 扩展字段原则

每张业务主表建议包含：

```text
ext_json jsonb
```

如果用 PostgreSQL：

```sql
ext_json jsonb
```

如果用 MySQL：

```sql
ext_json json
```

---

## 14.3 字段升级机制

```text
临时字段 → ext_json
高频查询字段 → 真实列
核心业务字段 → 一开始就真实列
```

---

## 14.4 通用审计字段

所有核心表建议统一：

```text
id
tenant_id
company_id
plant_id
created_by
created_at
updated_by
updated_at
deleted
version
ext_json
```

业务单据主表再加：

```text
document_no
document_status
approval_status
execution_status
posting_status
source_type
source_id
remark
```

---

# 15. DSL 设计

你选择轻量 DSL + 源码生成，不做动态运行时。

这是第一版最合理的路径。

---

## 15.1 DSL 类型

建议第一版有这些 DSL：

```text
business-object.yaml
business-document.yaml
state-machine.yaml
approval.yaml
permission.yaml
page.yaml
rule.yaml
report.yaml
print-template.yaml
barcode-template.yaml
dashboard.yaml
integration.yaml
```

---

## 15.2 示例：WMS 收货单 DSL

```yaml
module: wms
type: business-document
name: ReceiptOrder
table: wms_receipt_order
title: 收货单

fields:
  - name: documentNo
    title: 单据编号
    type: string
    required: true
    readonly: true
    generator: receipt_order_no

  - name: supplierId
    title: 供应商
    type: reference
    ref: Supplier
    required: true

  - name: warehouseId
    title: 仓库
    type: reference
    ref: Warehouse
    required: true

  - name: receiptDate
    title: 收货日期
    type: date
    required: true

  - name: documentStatus
    title: 单据状态
    type: enum
    enum: DocumentStatus

  - name: approvalStatus
    title: 审批状态
    type: enum
    enum: ApprovalStatus

  - name: postingStatus
    title: 过账状态
    type: enum
    enum: PostingStatus

lines:
  name: ReceiptOrderLine
  table: wms_receipt_order_line
  fields:
    - name: materialId
      title: 物料
      type: reference
      ref: Material
      required: true

    - name: expectedQty
      title: 应收数量
      type: decimal
      required: true

    - name: receivedQty
      title: 实收数量
      type: decimal
      required: true

actions:
  - create
  - update
  - submit
  - approve
  - reject
  - receive
  - post
  - cancel
  - close

permissions:
  prefix: wms:receipt

stateMachine:
  documentStatus:
    DRAFT:
      - action: submit
        target: SUBMITTED
    SUBMITTED:
      - action: approve
        target: EFFECTIVE
      - action: reject
        target: DRAFT
    EFFECTIVE:
      - action: close
        target: CLOSED
      - action: cancel
        target: CANCELLED
```

---

# 16. AI Coding 支撑体系

你说 AI coding 支撑能力先不做太多，但 AI coding 规范必须包含。

我建议第一版做“轻工具、重规范”。

---

## 16.1 第一版先不急着做复杂生成器

可以先做：

```text
项目模板
模块模板
DSL 模板
Prompt 模板
Codex 开发任务模板
代码规范
目录规范
接口规范
测试规范
Review 规范
```

不急着做完整可视化生成器。

---

## 16.2 为什么？

因为你们现在主要用：

```text
Cursor
Codex CLI
Claude Code
```

这类工具最需要的是：

```text
清晰的任务说明
稳定的项目结构
明确的代码规范
足够多的参考样例
边界清楚的模块模板
```

而不是一开始就做复杂的可视化代码生成平台。

---

## 16.3 同事自然语言开发如何兼容？

你提到同事平时可能直接用自然语言新增和修改功能。

建议框架提供一个“自然语言需求转结构化开发任务”的中间层。

流程：

```text
自然语言需求
   ↓
AI 转换为标准需求模板
   ↓
AI 生成/修改 DSL
   ↓
AI 根据 DSL 修改代码
   ↓
AI 生成测试
   ↓
AI 自检
```

不要让 AI 直接从自然语言改代码。

应该强制它先输出：

```text
变更说明
影响范围
DSL 变更
数据库变更
后端变更
前端变更
权限变更
测试变更
```

这会明显提高质量。

---

# 17. WMS 第一阶段范围

你选择 WMS 作为第一场景模板，而且要覆盖从基础设置到采购收货、上架，以及算法、规则、策略、流程控制。

建议 WMS 第一版不要做成完整产品，但要做出核心业务闭环。

---

## 17.1 WMS 基础设置

```text
仓库
库区
库位
库位类型
存储区
货主
物料
物料包装
批次规则
序列号规则
条码规则
上架策略
拣货策略
库存冻结原因
库存调整原因
作业人员
PDA 用户
```

---

## 17.2 库存核心

```text
库存余额
库存明细
库存事务
库存占用
库存冻结
库存调整
库存转移
库存盘点
批次库存
序列号库存
包装单元 Logistic Unit
```

---

## 17.3 入库流程

```text
采购订单同步/录入
到货通知
收货单
质检触发
待上架库存
上架任务
上架确认
库存过账
```

第一版可以先做：

```text
采购收货
收货确认
生成上架任务
上架确认
库存增加
库存事务记录
```

---

## 17.4 上架策略

支持基础规则：

```text
按物料固定库位
按物料分类推荐库区
按库位容量推荐
按批次属性推荐
按先进先出推荐
按供应商/客户限制推荐
按危险品/特殊物料规则推荐
```

第一版可以先做：

```text
固定库位策略
物料分类库区策略
空库位优先策略
同物料集中策略
```

---

## 17.5 WMS 第一版业务闭环

建议第一版样板模块闭环是：

```text
基础资料设置
   ↓
采购订单
   ↓
到货/收货
   ↓
收货审批/确认
   ↓
质检状态预留
   ↓
生成上架任务
   ↓
上架推荐
   ↓
PDA/页面确认上架
   ↓
库存增加
   ↓
库存事务
   ↓
库存查询/报表
```

---

# 18. 打印、条码、看板、审批设计器

你明确这几个必须有：

```text
打印模板设计器
看板设计器
审批流程设计器
条码标签设计器
```

建议分阶段。

---

## 18.1 审批流程设计器

第一版必须做。

能力：

```text
节点拖拽
条件分支
审批人配置
字段权限配置
动作配置
状态映射
发布版本
转换 Flowable BPMN
```

---

## 18.2 条码标签设计器

第一版也建议做轻量版。

支持：

```text
文本
二维码
条形码
物料编码
批次号
供应商
数量
单位
生产日期
收货日期
自定义字段
```

输出：

```text
PDF
ZPL 可后续支持
```

第一版可以先 PDF 打印，后续支持 Zebra ZPL。

---

## 18.3 打印模板设计器

用于：

```text
收货单
上架单
拣货单
盘点单
标签
审批单
报价单
```

建议第一版先做：

```text
基于 HTML 模板 + 变量绑定 + PDF 输出
```

不要一开始做复杂所见即所得报表设计器。

---

## 18.4 看板设计器

第一版做简单配置式看板：

```text
指标卡片
列表
柱状图
折线图
饼图
数据源配置
刷新频率
权限控制
```

底层图表：

```text
ECharts
```

---

# 19. 报表能力

你要求：

```text
高级查询
导出 Excel
简单统计卡片
简单图表
```

建议第一版做成通用报表模块。

---

## 19.1 报表模型

```text
ReportDefinition
ReportDataset
ReportQueryField
ReportColumn
ReportChart
ReportPermission
```

---

## 19.2 数据源类型

第一版支持：

```text
固定 SQL
后端 Query Service
业务对象查询
```

不建议第一版让业务人员随便写 SQL。

可以让开发配置 SQL，业务人员配置筛选条件和展示字段。

---

# 20. 接口集成模块

你提到：

```text
企微
钉钉
飞书
ERP
```

判断是对的：

> 企微、钉钉、飞书可以作为基础消息/组织集成能力；ERP 应该单独做接口配置和调用模块。

---

## 20.1 协同平台集成

第一版基础集成：

```text
企业微信
钉钉
飞书
```

能力：

```text
消息通知
待办推送
审批提醒
登录集成预留
组织同步预留
用户绑定
```

---

## 20.2 ERP 集成模块

ERP 不应该写死成某一个系统接口，而应该做：

```text
Integration Center
```

能力：

```text
接口定义
接口认证
接口调用
字段映射
定时同步
手工重试
错误日志
调用日志
数据转换
入站接口
出站接口
Webhook
```

第一版先支持：

```text
REST API
数据库视图/中间表
Excel 导入
```

后续再支持：

```text
SOAP
MQ
ESB
SAP RFC
```

---

# 21. Outbox 事件机制

你接受轻量 Outbox，这很适合第一版。

---

## 21.1 事件模型

```text
DomainEvent
OutboxMessage
EventHandler
EventSubscription
EventPublishLog
```

---

## 21.2 使用场景

```text
收货单已过账
库存已增加
上架任务已生成
审批已通过
供应商报价已提交
消息通知待发送
接口同步待执行
```

---

## 21.3 第一版实现

```text
业务事务内写 outbox 表
后台调度器扫描待发送事件
执行本地 handler 或 HTTP 调用
失败重试
记录日志
```

第一版不必引入 Kafka/NATS。

---

# 22. 推荐项目工程结构

后端建议：

```text
com.company.iaf
  IafApplication.java

  platform
    auth
    org
    permission
    dictionary
    attachment
    audit
    message
    workflow
    statemachine
    rule
    report
    print
    barcode
    dashboard
    integration
    outbox

  manufacturing
    master
    organization
    material
    bom
    process
    warehouse
    quality

  wms
    master
    receipt
    putaway
    inventory
    transaction
    strategy
    report

  shared
    domain
    exception
    result
    security
    tenant
    utils
```

每个模块内部：

```text
interfaces
  controller
  request
  response

application
  command
  query
  service
  assembler

domain
  model
  repository
  service
  event
  rule

infrastructure
  persistence
  mapper
  entity
  integration
```

---

# 23. MVP 建设路线

你说所有文档都要，那可以先按 MVP 分阶段。

---

## 阶段 0：架构骨架

目标：先建立统一工程结构。

范围：

```text
Spring Boot 模块化单体
React + Ant Design Pro
登录认证
用户
组织
角色
菜单
权限
字典
附件
审计日志
数据库 migration
基础代码规范
AI coding 规范目录
```

产出：

```text
可运行基础平台
前后端工程规范
第一个基础 CRUD 模板
Codex 开发规则
```

---

## 阶段 1：业务开发底座

目标：解决工程重复。

范围：

```text
编码规则
数据权限
字段权限
按钮权限
导入导出
状态机
审批流基础
消息通知
Outbox
业务单据模板
业务对象 DSL 初版
```

产出：

```text
可以快速开发普通业务单据
可以生成列表/表单/详情/接口/权限
可以跑审批和状态流转
```

---

## 阶段 2：制造通用主数据

目标：建立制造企业通用模型。

范围：

```text
集团/公司/事业部/工厂
车间/产线/工作中心
仓库/库区/库位
客户
供应商
物料
单位
BOM 轻量
工艺路线轻量
批次规则
序列号规则
```

产出：

```text
制造企业基础数据底座
WMS/MES/SRM 都可复用
```

---

## 阶段 3：WMS 样板模板

目标：验证框架对真实业务的支撑能力。

范围：

```text
采购订单
收货单
收货确认
上架任务
上架策略
库存余额
库存明细
库存事务
库存调整
库存查询
库存报表
条码标签
打印模板
```

产出：

```text
WMS 入库闭环样板
标准场景模板
AI coding 可复用示例
```

---

## 阶段 4：设计器与配置能力

目标：让业务顾问可以生成原型。

范围：

```text
审批流程设计器
打印模板设计器
条码标签设计器
看板设计器
规则配置器
报表配置器
```

产出：

```text
业务顾问可配置流程、规则、打印、看板
开发同事基于配置和 DSL 生成代码
```

---

## 阶段 5：接口集成和协同平台

目标：支持真实客户项目集成。

范围：

```text
企微通知
钉钉通知
飞书通知
接口定义
字段映射
接口调用日志
错误重试
ERP 中间表/REST 接口集成
```

产出：

```text
项目交付可用的集成中心
```

---

# 24. 第一版优先级建议

虽然你希望很多能力都有，但第一版必须控制优先级。

我建议优先级如下。

---

## P0：必须先做

```text
项目工程骨架
用户/组织/角色/权限
菜单/按钮/API 权限
多组织模型
字典
编码规则
附件
审计日志
状态机
审批流基础
标准业务单据模板
AI coding 规范
WMS 基础资料
WMS 收货/上架/库存事务闭环
```

---

## P1：第一版后半段做

```text
字段权限
审批节点字段权限
数据权限增强
规则配置器
高级查询
Excel 导入导出
打印模板
条码标签
Outbox
消息通知
```

---

## P2：第二阶段增强

```text
看板设计器
报表设计器
企微/钉钉/飞书集成
ERP 接口中心
PDA 端
库存策略增强
盘点
移库
冻结
库存调整审批
```

---

# 25. 这个框架和 IAOS 的边界

一定要控制好边界。

---

## 25.1 当前框架不做什么？

第一版不要做：

```text
完整语义注册中心
完整 Capability Runtime
完整 Agent Runtime
完整动态页面运行时
完整低代码平台
完整微服务平台
复杂事件编排 DAG
工业知识图谱
自动业务建模 Agent
多租户 SaaS 计费体系
```

---

## 25.2 当前框架做什么？

第一版做：

```text
代码工程标准化
基础平台完整化
制造主数据通用化
业务单据模板化
审批/状态/规则平台化
WMS 场景样板化
AI coding 流程规范化
```

---

# 26. 我对第一版的核心建议

最关键的设计原则是这几个。

---

## 原则一：源码生成优先，不做动态运行时

```text
DSL → 生成源码 → AI 修改源码 → 提交 Git
```

不要做：

```text
DSL → 运行时动态解释 → 动态页面
```

否则复杂度会接近低代码平台。

---

## 原则二：先做“开发框架”，再做“产品模块”

WMS 第一版不是完整产品，而是：

```text
场景模板
业务范式
代码样例
可复用模型
```

---

## 原则三：AI coding 要被约束

同事可以自然语言描述需求，但系统流程必须要求 AI 先结构化：

```text
自然语言
→ 标准需求说明
→ DSL 变更
→ 代码变更计划
→ 执行开发
→ 测试
→ Review
```

---

## 原则四：Java 主体，Go 边缘

```text
Spring Boot：主业务系统
Go：采集、网关、接口代理、边缘服务
```

---

## 原则五：多组织第一版就内置

制造企业应用如果不从第一版设计多组织，后面会非常痛苦。

---

# 27. 建议的最终蓝图名称

我建议这个框架的正式名称暂定为：

## IAF：Industrial Application Framework

中文：

## 工业应用开发框架

副标题：

> 面向制造企业的 AI Coding 应用开发底座

完整定位：

```text
IAF 是一个面向制造企业管理系统的 AI Coding 应用开发框架，
通过统一的基础平台、制造通用模型、业务单据范式、审批/状态/规则能力、
轻量 DSL、源码生成和 AI coding 规范，帮助团队快速开发 WMS、MES、SRM、QMS 等工业管理应用。
```

---

# 28. 下一步我建议直接产出 5 个文档

你说我前面提到的都要，那么建议按下面顺序做。

---

## 文档 1：总体架构蓝图

内容：

```text
系统定位
设计原则
总体架构
模块划分
技术栈
IAOS 关系
演进路线
```

---

## 文档 2：技术选型报告

内容：

```text
Spring Boot vs Go
模块化单体 vs 微服务
MyBatis Plus vs JPA
Ant Design Pro vs shadcn/ui
Flowable 选型
规则引擎选型
报表/打印/条码/看板选型
部署方案
```

---

## 文档 3：核心平台模块设计

内容：

```text
用户组织权限
多组织模型
数据权限
字段权限
审批流
状态机
规则引擎
编码规则
附件
审计
消息
Outbox
集成中心
```

---

## 文档 4：WMS 样板场景设计

内容：

```text
WMS 基础资料
采购收货
上架
库存事务
库存余额
批次/序列号
上架策略
条码标签
打印
报表
权限
审批
规则
```

---

## 文档 5：Codex/Cursor/Claude Code 开发说明书

内容：

```text
项目目录结构
代码规范
DSL 规范
开发任务模板
模块生成流程
后端开发规则
前端开发规则
数据库规则
测试规则
Review 规则
第一个 WMS 模块开发任务
```

---

# 29. 我建议下一轮先做什么

建议下一步不要继续泛泛讨论，而是先输出：

> **《IAF 工业应用开发框架总体架构蓝图 v1.0》**

这份文档应该先把方向定下来，后续所有技术选型、代码开发、WMS 样板都基于它展开。

我可以下一步直接给你写这份架构蓝图，结构如下：

```text
1. 背景与目标
2. 系统定位
3. 与 IAOS 的关系
4. 设计原则
5. 总体架构
6. 技术栈选择
7. 模块架构
8. AI Coding 支撑体系
9. 基础平台能力
10. 制造通用能力
11. WMS 第一场景模板
12. 权限/审批/状态/规则设计
13. 数据模型策略
14. 部署架构
15. MVP 建设路线
16. 后续演进路线
```

我的建议是：先定这份蓝图，再拆 Codex 开发说明书。




