建议选择：

# **Maven 多模块工程 + 当前目录结构**

不要做“单 Maven 工程 + 包内模块”。

原因很明确：你这个 IAF 平台不是普通业务系统，而是一个会长期演进的 **工业应用开发框架 + 平台底座 + WMS 场景模板 + AI Coding 工程样板**。如果用单 Maven 工程，短期简单，长期会失控。

---

# 1. 推荐结论

## 推荐方案

```text
Maven 多模块工程
+ 模块化单体架构
+ 轻量 DDD 包结构
+ 一个部署单元
+ 多个 Maven 子模块
```

也就是：

```text
开发上是多模块
运行上是单体应用
部署上是一个 Spring Boot App
```

这和你之前确定的“模块化单体 + 轻量 DDD”是一致的。

---

# 2. 两种方案对比

## 方案 A：单 Maven 工程 + 包内模块

结构类似：

```text
backend/
  pom.xml
  src/main/java/com/company/iaf/
    platform/
      auth/
      org/
      permission/
      workflow/
    manufacturing/
      material/
      supplier/
      warehouse/
    wms/
      receipt/
      putaway/
      inventory/
```

优点：

```text
简单
启动快
配置少
适合小项目
Codex 初期容易理解
```

缺点：

```text
模块边界弱
依赖关系不可控
容易互相直接调用
很容易跨模块访问 Entity / Mapper
代码量变大后维护困难
无法单独测试模块
无法逐步产品化
不利于沉淀标准场景模板
```

对于普通管理系统可以接受，但对 IAF 不太合适。

---

## 方案 B：Maven 多模块工程 + 当前目录结构

结构类似：

```text
backend/
  pom.xml

  iaf-app/
  iaf-shared/

  iaf-platform/
    iaf-platform-auth/
    iaf-platform-org/
    iaf-platform-permission/
    iaf-platform-dictionary/
    iaf-platform-attachment/
    iaf-platform-audit/
    iaf-platform-workflow/
    iaf-platform-statemachine/
    iaf-platform-rule/
    iaf-platform-message/
    iaf-platform-outbox/
    iaf-platform-integration/

  iaf-manufacturing/
    iaf-manufacturing-master/
    iaf-manufacturing-org/
    iaf-manufacturing-material/
    iaf-manufacturing-supplier/
    iaf-manufacturing-warehouse/

  iaf-wms/
    iaf-wms-master/
    iaf-wms-inventory/
    iaf-wms-receipt/
    iaf-wms-putaway/
    iaf-wms-strategy/
```

优点：

```text
模块边界清晰
依赖关系可控
适合长期演进
适合 AI Coding 分任务开发
适合后续产品化
适合沉淀 WMS/MES/SRM 模板
适合做架构约束
适合后续拆微服务
```

缺点：

```text
初期配置稍复杂
pom 管理更严格
Codex 需要遵守模块依赖规则
模块划分过细时会增加维护成本
```

但这些缺点可以通过规范控制。

---

# 3. 为什么 IAF 更适合 Maven 多模块？

## 3.1 IAF 是平台，不是单一项目

IAF 至少包含：

```text
基础平台能力
制造通用模型
WMS 场景模板
审批流
状态机
规则引擎
Outbox
集成中心
AI Coding 规范
代码生成体系
```

这些能力未来会被不同项目复用。如果所有代码都塞在一个 `src/main/java` 里，后续很难判断：

```text
哪些是平台核心？
哪些是制造通用？
哪些是 WMS 模板？
哪些是客户项目定制？
哪些可以复用？
哪些不能复用？
```

多模块可以天然表达这些边界。

---

## 3.2 更适合 Codex 分任务开发

你希望用 Codex / Cursor / Claude Code 开发这个平台。

多模块工程更适合 AI coding，因为每个任务可以限定文件范围：

```text
本任务只允许修改：
- iaf-platform/iaf-platform-org
- iaf-app
- db/migration
```

或者：

```text
本任务只允许修改：
- iaf-wms/iaf-wms-receipt
- iaf-wms/iaf-wms-inventory
- iaf-platform/iaf-platform-statemachine
```

这样可以降低 Codex 乱改代码的概率。

单 Maven 工程里，AI 很容易跨包修改、绕过边界。

---

## 3.3 更容易做架构治理

多模块可以通过 Maven 依赖规则限制架构。

例如：

```text
iaf-platform-auth
  可以依赖 iaf-shared
  不允许依赖 iaf-wms

iaf-wms-receipt
  可以依赖 iaf-platform-statemachine
  可以依赖 iaf-platform-workflow
  可以依赖 iaf-wms-inventory-api
  不允许直接依赖 iaf-wms-inventory 的 infrastructure
```

这种约束在单 Maven 工程里很难 enforce。

---

## 3.4 更适合后续拆分

你现在不做 Spring Cloud 微服务是正确的，但未来可能会拆：

```text
WMS
SRM
MES
审批服务
规则服务
集成中心
```

如果一开始就是 Maven 多模块，未来拆分路径更自然。

---

# 4. 但不要过度拆模块

虽然推荐 Maven 多模块，但第一版不要拆得太细。

错误做法：

```text
一个业务对象一个 Maven module
一个功能点一个 Maven module
一个接口一个 module
```

这样会导致复杂度过高。

建议按“能力域”拆，而不是按 CRUD 对象拆。

---

# 5. 推荐的 backend 目录结构

我建议第一版采用这个结构。

```text
backend/
  pom.xml                         # parent pom

  iaf-app/                        # Spring Boot 启动模块

  iaf-shared/                     # 通用基础能力

  iaf-platform/                   # 平台能力聚合父模块
    pom.xml
    iaf-platform-core/
    iaf-platform-auth/
    iaf-platform-org/
    iaf-platform-permission/
    iaf-platform-dictionary/
    iaf-platform-attachment/
    iaf-platform-audit/
    iaf-platform-message/
    iaf-platform-outbox/
    iaf-platform-statemachine/
    iaf-platform-workflow/
    iaf-platform-rule/
    iaf-platform-report/
    iaf-platform-print/
    iaf-platform-barcode/
    iaf-platform-dashboard/
    iaf-platform-integration/

  iaf-manufacturing/              # 制造通用能力聚合父模块
    pom.xml
    iaf-manufacturing-core/
    iaf-manufacturing-org/
    iaf-manufacturing-master/
    iaf-manufacturing-material/
    iaf-manufacturing-supplier/
    iaf-manufacturing-customer/
    iaf-manufacturing-warehouse/
    iaf-manufacturing-bom/
    iaf-manufacturing-process/

  iaf-wms/                        # WMS 场景模板聚合父模块
    pom.xml
    iaf-wms-core/
    iaf-wms-master/
    iaf-wms-inventory/
    iaf-wms-receipt/
    iaf-wms-putaway/
    iaf-wms-strategy/
    iaf-wms-report/
```

---

# 6. 更现实的第一阶段简化版

如果你担心一开始模块太多，可以第一阶段采用这个较克制的版本：

```text
backend/
  pom.xml

  iaf-app/
  iaf-shared/

  iaf-platform-core/
  iaf-platform-auth/
  iaf-platform-org/
  iaf-platform-permission/
  iaf-platform-workflow/
  iaf-platform-rule/
  iaf-platform-system/

  iaf-manufacturing-core/
  iaf-manufacturing-master/

  iaf-wms-core/
  iaf-wms-master/
  iaf-wms-inventory/
  iaf-wms-inbound/
```

其中：

```text
iaf-platform-system
```

可以先承载：

```text
字典
附件
审计
编码规则
消息
参数
菜单
```

后面代码量上来后再拆成独立模块。

```text
iaf-wms-inbound
```

可以先承载：

```text
收货
上架
入库过账
```

后面再拆成：

```text
iaf-wms-receipt
iaf-wms-putaway
```

---

# 7. 推荐第一版最终结构

我建议你们第一版直接采用这个折中结构：

```text
backend/
  pom.xml

  iaf-app/

  iaf-shared/

  iaf-platform-core/
  iaf-platform-auth/
  iaf-platform-org/
  iaf-platform-permission/
  iaf-platform-system/
  iaf-platform-workflow/
  iaf-platform-statemachine/
  iaf-platform-rule/
  iaf-platform-integration/

  iaf-manufacturing-core/
  iaf-manufacturing-master/

  iaf-wms-core/
  iaf-wms-master/
  iaf-wms-inventory/
  iaf-wms-inbound/
  iaf-wms-strategy/
```

这个结构比较平衡：

```text
模块边界清楚
数量不至于失控
Codex 容易理解
后续可以继续拆分
```

---

# 8. 各模块职责

## iaf-app

只做启动和装配。

包含：

```text
Spring Boot 启动类
全局配置装配
模块扫描配置
OpenAPI 聚合
安全配置入口
```

不放业务代码。

---

## iaf-shared

放真正无业务含义的通用能力。

例如：

```text
Result<T>
PageResult<T>
BaseEntity
BusinessException
ErrorCode
IdGenerator
DateUtils
JsonUtils
TenantContext
UserContext
CommonEnums
```

禁止放：

```text
用户业务逻辑
权限业务逻辑
WMS 业务逻辑
审批业务逻辑
```

---

## iaf-platform-core

平台基础内核。

包含：

```text
租户上下文
多组织基础上下文
通用审计字段
通用领域事件接口
通用应用服务接口
基础异常体系
```

---

## iaf-platform-auth

包含：

```text
登录
JWT
Token
密码
认证过滤器
当前用户上下文
```

---

## iaf-platform-org

包含：

```text
用户
组织
部门
岗位
用户组织关系
用户岗位关系
```

---

## iaf-platform-permission

包含：

```text
角色
权限点
菜单权限
按钮权限
API 权限
数据权限
字段权限
单据操作权限
```

---

## iaf-platform-system

第一版合并承载系统通用能力：

```text
数据字典
系统参数
编码规则
附件
操作日志
审计日志
登录日志
消息通知
站内信
```

后续可以拆成：

```text
iaf-platform-dictionary
iaf-platform-attachment
iaf-platform-audit
iaf-platform-message
```

---

## iaf-platform-workflow

包含审批流抽象，不暴露 Flowable 细节。

```text
ApprovalDefinition
ApprovalInstance
ApprovalTask
ApprovalAction
ApprovalAssigneeResolver
ApprovalFieldPermission
FlowableAdapter
```

---

## iaf-platform-statemachine

包含业务状态机。

```text
StateMachineDefinition
StateTransition
StateAction
StateTransitionGuard
StateTransitionLog
```

---

## iaf-platform-rule

包含业务规则引擎。

```text
RuleDefinition
RuleCondition
RuleAction
RuleBinding
RuleExecutionLog
JSON Logic Adapter
```

---

## iaf-platform-integration

包含：

```text
企微
钉钉
飞书
ERP 接口定义
接口调用日志
字段映射
重试
Webhook
```

---

## iaf-manufacturing-core

制造通用基础对象。

```text
Plant
Workshop
ProductionLine
WorkCenter
ManufacturingContext
```

---

## iaf-manufacturing-master

制造通用主数据。

```text
Material
Supplier
Customer
Unit
BOM 轻量
ProcessRoute 轻量
Warehouse 基础引用
```

---

## iaf-wms-core

WMS 公共模型。

```text
WmsContext
WarehouseRef
LocationRef
InventoryEnums
WmsException
```

---

## iaf-wms-master

WMS 基础设置。

```text
Warehouse
WarehouseArea
Location
LocationType
StorageZone
Packaging
BatchRule
SerialRule
BarcodeRule
```

---

## iaf-wms-inventory

库存核心。

```text
InventoryBalance
InventoryDetail
InventoryTransaction
InventoryReservation
InventoryFreeze
InventoryPostingService
```

---

## iaf-wms-inbound

入库业务。

```text
PurchaseReceipt
ReceiptOrder
ReceiptOrderLine
PutawayTask
InboundPosting
```

---

## iaf-wms-strategy

策略能力。

```text
PutawayStrategy
PickingStrategy 预留
InventoryAllocationStrategy 预留
StrategyRule
StrategyExecutionLog
```

---

# 9. Maven 依赖关系建议

## 9.1 基础依赖方向

```text
iaf-app
  depends on all runtime modules

business modules
  depends on platform modules
  depends on manufacturing modules
  depends on shared

platform modules
  depends on platform-core
  depends on shared

shared
  depends on nothing business-specific
```

---

## 9.2 禁止依赖方向

必须禁止：

```text
iaf-shared 依赖任何业务模块
iaf-platform-* 依赖 iaf-wms-*
iaf-platform-* 依赖 iaf-manufacturing-*
iaf-manufacturing-* 依赖 iaf-wms-*
iaf-wms-inventory 依赖 iaf-wms-inbound
```

允许：

```text
iaf-wms-inbound 依赖 iaf-wms-inventory
iaf-wms-inbound 依赖 iaf-wms-master
iaf-wms-inbound 依赖 iaf-platform-workflow
iaf-wms-inbound 依赖 iaf-platform-statemachine
iaf-wms-inbound 依赖 iaf-platform-rule
```

---

# 10. 是否使用聚合父模块？

建议第一阶段可以不用嵌套太深。

也就是说，不一定要：

```text
iaf-platform/
  pom.xml
  iaf-platform-auth/
```

可以先平铺：

```text
backend/
  iaf-platform-auth/
  iaf-platform-org/
  iaf-platform-permission/
```

这样 Codex 更容易操作。

等模块稳定后，再整理成：

```text
iaf-platform/
iaf-manufacturing/
iaf-wms/
```

---

# 11. 我建议采用“平铺多模块”

第一版推荐：

```text
backend/
  pom.xml
  iaf-app/
  iaf-shared/

  iaf-platform-core/
  iaf-platform-auth/
  iaf-platform-org/
  iaf-platform-permission/
  iaf-platform-system/
  iaf-platform-workflow/
  iaf-platform-statemachine/
  iaf-platform-rule/
  iaf-platform-integration/

  iaf-manufacturing-core/
  iaf-manufacturing-master/

  iaf-wms-core/
  iaf-wms-master/
  iaf-wms-inventory/
  iaf-wms-inbound/
  iaf-wms-strategy/
```

这是我最推荐的结构。

原因：

```text
多模块边界清楚
没有嵌套 Maven 父子模块的复杂度
Codex 容易生成
IDE 识别简单
后续可以重构成分组目录
```

---

# 12. 每个 Maven Module 内部仍然使用 DDD 分层

例如：

```text
iaf-wms-inbound/
  src/main/java/com/company/iaf/wms/inbound/
    interfaces/
    application/
    domain/
    infrastructure/
```

不要在 Maven 模块里再按功能散乱放。

---

# 13. 给 Codex 的明确决策

你可以直接这样写进 `AGENTS.md`：

```markdown
## Backend Project Structure Decision

The backend MUST use a Maven multi-module structure.

The system is a modular monolith:
- one deployable Spring Boot application
- multiple Maven modules
- clear module boundaries
- no Spring Cloud microservices in phase 1

Do NOT implement the backend as a single Maven project with only package-level modules.

The first version MUST use a flat Maven multi-module layout:

- iaf-app
- iaf-shared
- iaf-platform-core
- iaf-platform-auth
- iaf-platform-org
- iaf-platform-permission
- iaf-platform-system
- iaf-platform-workflow
- iaf-platform-statemachine
- iaf-platform-rule
- iaf-platform-integration
- iaf-manufacturing-core
- iaf-manufacturing-master
- iaf-wms-core
- iaf-wms-master
- iaf-wms-inventory
- iaf-wms-inbound
- iaf-wms-strategy

Each module MUST follow the internal package structure:

- interfaces
- application
- domain
- infrastructure

Business modules MUST NOT directly access another module's infrastructure package.
Cross-module calls MUST go through application services, domain services, published APIs, or events.
```

---

# 14. 最终建议

你的项目应该采用：

```text
Maven 多模块工程
+ 平铺模块目录
+ 模块化单体部署
+ 每个模块内部轻量 DDD
```

不要采用：

```text
单 Maven 工程 + 包内模块
```

第一版推荐结构：

```text
backend/
  pom.xml
  iaf-app/
  iaf-shared/
  iaf-platform-core/
  iaf-platform-auth/
  iaf-platform-org/
  iaf-platform-permission/
  iaf-platform-system/
  iaf-platform-workflow/
  iaf-platform-statemachine/
  iaf-platform-rule/
  iaf-platform-integration/
  iaf-manufacturing-core/
  iaf-manufacturing-master/
  iaf-wms-core/
  iaf-wms-master/
  iaf-wms-inventory/
  iaf-wms-inbound/
  iaf-wms-strategy/
```

这是当前最适合你们的选择：既能保持单体部署的简单性，又能给 Codex、团队协作、平台演进和未来产品化留下足够清晰的边界。
