# AGENTS.md - IAF 平台 AI 开发总规则

## 1. 项目定位

IAF（Industrial Application Framework）是面向制造企业管理系统的 AI Coding 应用开发框架。目标是通过统一的基础平台、制造通用模型、业务单据范式、审批/状态/规则能力、轻量 DSL、源码生成和 AI coding 规范，帮助团队快速开发 WMS、MES、SRM、QMS 等工业管理应用。

本项目不是普通 CRUD 后台，也不是完整低代码平台。开发时必须同时满足：

- 企业级工程质量。
- 制造业务可扩展性。
- AI coding 可维护性。
- 项目交付可落地性。

## 2. 技术栈锁定

后端：

- Java 21。
- Spring Boot 3.x。
- 模块化单体。
- 轻量 DDD 分层。
- MyBatis Plus + Mapper XML。
- Flyway migration。
- PostgreSQL 优先，MySQL 兼容预留。
- Spring Security + JWT。
- Redis。
- MinIO。
- Flowable 作为审批流内核，但业务代码不得直接暴露 Flowable 概念。

前端：

- React + TypeScript。
- Ant Design Pro / ProComponents。
- ECharts。
- Zustand 或轻量状态管理。
- 统一 request client。
- 权限按钮和状态驱动 UI。

禁止未经批准引入新的核心框架、ORM、UI 框架、工作流引擎、规则引擎。

## 3. 架构原则

### 3.1 模块化单体

所有模块在一个部署单元内运行，但必须保持清晰模块边界。禁止为了方便直接跨模块访问对方 infrastructure/entity/mapper。

### 3.2 轻量 DDD 分层

每个业务模块必须使用以下分层：

```text
interfaces     对外接口层：Controller、Request、Response
application    应用层：Command、Query、ApplicationService、Assembler
domain         领域层：Model、DomainService、Repository interface、DomainEvent
infrastructure 基础设施层：Entity、Mapper、RepositoryImpl、Integration Adapter
```

### 3.3 Controller 规则

Controller 只负责：

- 参数接收。
- 参数基本校验。
- 调用 ApplicationService / QueryService。
- 返回 Result<T>。

禁止：

- Controller 直接调用 Mapper。
- Controller 直接写业务逻辑。
- Controller 直接修改状态。
- Controller 直接访问其他模块 infrastructure。

### 3.4 ApplicationService 规则

ApplicationService 负责业务用例编排：

- 事务边界。
- 权限检查。
- 状态机调用。
- 领域服务调用。
- 事件发布。
- 审计记录。

复杂领域规则必须下沉到 DomainService 或领域模型。

### 3.5 Domain 规则

领域模型必须表达核心业务约束。禁止把制造业务规则全部写成贫血 CRUD。

例如 WMS 库存变更必须通过 `InventoryPostingService`，不得在任意 Service 中直接修改库存余额。

## 4. 数据库规则

所有业务表必须包含：

```text
id
租户字段：tenant_id
组织字段：company_id、plant_id 视业务需要
审计字段：created_by、created_at、updated_by、updated_at
软删除字段：deleted
乐观锁字段：version
扩展字段：ext_json
```

所有数据库变更必须通过 Flyway migration。禁止仅修改 Entity 不写 migration。

业务主表命名规则：

```text
模块_业务对象
```

示例：

```text
wms_receipt_order
wms_receipt_order_line
wms_inventory_balance
wms_inventory_transaction
```

## 5. API 规则

API 路径必须遵守：

```text
/api/{module}/{resource}
```

示例：

```text
/api/wms/receipt-orders
/api/platform/users
/api/platform/roles
```

所有返回值必须使用统一结构：

```java
Result<T>
PageResult<T>
```

错误必须使用统一异常码，不允许直接抛出裸 RuntimeException 给前端。

## 6. 权限规则

所有后端写操作必须校验权限。前端隐藏按钮不能替代后端权限校验。

权限编码：

```text
模块:对象:动作
```

示例：

```text
wms:receipt:view
wms:receipt:create
wms:receipt:submit
wms:receipt:confirm
```

## 7. 状态机规则

所有业务单据状态变更必须通过 StateMachineService。禁止直接 setStatus 后保存。

状态必须分层：

- document_status：单据生命周期状态。
- approval_status：审批状态。
- execution_status：执行状态。
- posting_status：库存/财务过账状态，按需使用。
- settlement_status：结算状态，按需使用。

## 8. 审批规则

业务模块不得直接依赖 Flowable API。必须通过 ApprovalApplicationService 交互。

审批业务模型包括：

- ApprovalDefinition。
- ApprovalInstance。
- ApprovalTask。
- ApprovalAction。
- ApprovalAssigneeRule。
- ApprovalFieldPermission。
- ApprovalStateMapping。

## 9. 规则引擎规则

业务人员可配置规则必须优先使用 RuleEngineService。表达式建议使用 JSON Logic。禁止让业务人员配置任意 Java/Groovy/SpEL 脚本。

## 10. WMS 特殊规则

库存余额、库存明细、库存事务是 WMS 核心。所有库存变化必须满足：

- 通过库存过账服务统一执行。
- 同一业务操作必须同时生成库存事务。
- 库存余额变更必须具备幂等键。
- 不允许绕过库存服务直接 update 库存表。
- 收货、上架、调整、移库、冻结、解冻都必须记录库存事务。

## 11. AI 执行流程

Codex / Cursor / Claude Code 执行任何开发任务前，必须：

1. 读取本文件。
2. 读取相关 ai-coding/rules 文件。
3. 读取相关 docs/module-specs 文件。
4. 读取当前 task 文件。
5. 输出任务理解和实施计划。
6. 检查现有代码结构。
7. 小步修改代码。
8. 新增或更新测试。
9. 执行测试和构建。
10. 输出变更摘要和自检清单。

## 12. 禁止事项

禁止：

- 在没有 migration 的情况下修改实体字段。
- 在没有测试的情况下提交核心业务逻辑。
- Controller 直接访问 Mapper。
- 前端硬编码 API host。
- 前端只做权限控制，后端不校验。
- 绕过状态机直接修改业务状态。
- 绕过审批服务直接修改审批状态。
- 绕过库存过账服务直接修改库存余额。
- 跨模块直接访问对方 infrastructure/entity/mapper。
- 引入未经批准的新框架。
- 为了通过测试删除核心校验。

## 13. 完成标准

每个任务完成必须满足：

- 后端测试通过。
- 前端 typecheck/build 通过。
- Flyway migration 可在空库执行。
- 权限点、菜单、字典、初始数据已处理。
- OpenAPI 可看到新增接口。
- 代码符合分层规则。
- 任务文件中的验收标准逐项满足。
