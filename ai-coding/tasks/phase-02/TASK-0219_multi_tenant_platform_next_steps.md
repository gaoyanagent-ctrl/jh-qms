# TASK-0219 多租户平台化与服务化治理下一步计划

## 1. 任务目标

在 `TASK-0218 多租户与微服务演进准备` 完成后，继续把 IAF 从“具备多租户安全基线的模块化单体”推进到“可运营、可扩展、可观测、可逐步服务化拆分”的平台底座。

本任务仍不要求直接拆成微服务。目标是补齐多租户运营能力、租户级配置治理、事件与 Outbox 基线、服务边界验证、可观测性、运维治理和 WMS 业务模块接入多租户基线。

## 2. 前置条件

必须确认 `TASK-0218` 已完成并通过验收：

- 登录已租户感知。
- 已登录请求的租户来源已标准化。
- 关键仓储已有租户隔离测试。
- token 生产化方案已有 ADR，并已具备非 in-memory 的生产可用路径。
- 异步上下文传播已有基线。
- 当前表 ownership 和未来服务边界已有 ADR。
- 数据权限已有至少一个后端最小闭环。
- `docs/code-map/*` 已同步。
- 后端测试与质量门禁已通过，或失败原因已明确记录。

## 3. 必须先阅读

- `AGENTS.md`
- `ai-coding/tasks/phase-02/TASK-0218_multi_tenant_microservice_readiness.md`
- `docs/decisions/` 中由 TASK-0218 新增的 ADR
- `docs/architecture/01_总体架构蓝图.md`
- `docs/architecture/03_核心平台模块设计.md`
- `docs/code-map/README.md`
- `docs/code-map/backend.md`
- `docs/code-map/api.md`
- `docs/code-map/database.md`
- `ai-coding/rules/01_project_rules.md`
- `ai-coding/rules/02_backend_rules.md`
- `ai-coding/rules/04_database_rules.md`
- `ai-coding/rules/05_api_rules.md`
- `ai-coding/rules/06_permission_rules.md`
- `ai-coding/rules/10_testing_rules.md`
- `ai-coding/rules/11_code_quality_rules.md`
- `ai-coding/rules/12_code_map_rules.md`
- `docs/quality/quality_gate.md`

## 4. 业务范围

本任务实现：

- 租户生命周期管理能力。
- 租户级平台配置与默认数据初始化治理。
- 租户级资源配额、限制与审计基础。
- Outbox 与领域事件平台基线。
- 服务边界契约验证和禁止跨边界依赖的自动化检查。
- 多租户可观测性基线。
- WMS/制造模块接入租户、组织、权限、事件基线的样板。

本任务不实现：

- 不把所有模块拆成独立服务。
- 不引入 Spring Cloud 全家桶。
- 不做完整计费系统。
- 不做完整租户自助开通门户。
- 不把所有已有查询一次性改成复杂读模型。
- 不跳过应用服务层直接操作其他模块表。

## 5. 推荐拆分阶段

### 阶段 1：租户生命周期管理

目标：让平台可以安全创建、启用、停用和初始化租户。

任务：

- 明确 `sys_tenant` owner。若 TASK-0218 已决策，遵循已有 ADR。
- 增加租户应用服务：
  - 创建租户。
  - 启用租户。
  - 停用租户。
  - 查询租户列表。
  - 查询租户详情。
- 租户创建时必须初始化：
  - 默认管理员或管理员邀请机制。
  - 默认角色。
  - 默认权限绑定。
  - 默认菜单。
  - 默认主题、品牌、i18n、用户体验配置。
- 停用租户后：
  - 不允许新登录。
  - 已有 token 的处理策略必须遵循 TASK-0218 token ADR。
  - 业务写操作必须拒绝。
- 新增权限点：
  - `platform:tenant:view`
  - `platform:tenant:create`
  - `platform:tenant:update`
  - `platform:tenant:disable`
  - `platform:tenant:enable`

验收标准：

- 可通过后端 API 创建一个新租户并完成基础数据初始化。
- 新租户管理员可以登录并只看到本租户数据。
- 停用租户不可继续登录。
- 跨租户初始化数据互不污染。

### 阶段 2：租户级配置模板和初始化编排

目标：把“默认租户 seed 数据”从硬编码迁移为可治理的初始化模板。

任务：

- 设计租户初始化模板模型，覆盖：
  - 初始角色。
  - 初始权限绑定。
  - 初始菜单绑定。
  - 初始系统配置。
  - 初始组织结构模板。
- 明确模板数据放在 migration、JSON 资源、DSL 或数据库中的策略，并写 ADR。
- 初始化过程必须幂等。
- 初始化失败必须可重试，不能留下半初始化租户。
- 初始化结果必须有审计记录。

验收标准：

- 创建租户时不依赖散落在多个 migration 中的手写 seed 逻辑。
- 同一租户重复初始化不会产生重复角色、权限、菜单或配置。
- 初始化失败可定位、可重试。

### 阶段 3：租户级资源配额与限制

目标：为未来 SaaS 运营、项目交付和防滥用提供基础控制点。

任务：

- 定义租户配额模型：
  - 用户数上限。
  - 组织数上限。
  - 仓库数上限。
  - 存储容量上限。
  - API 速率限制预留字段。
  - 扩展配置 `ext_json`。
- 先选择一个能力做最小闭环，例如用户数上限。
- 在应用服务层执行配额校验。
- 配额不足必须返回统一业务错误码。
- 配额变化必须有审计记录。

验收标准：

- 租户达到用户数上限后不能继续创建用户。
- 错误码稳定且不会暴露内部异常。
- 配额校验不写在 Controller 或 Mapper 中。

### 阶段 4：Outbox 与领域事件基线

目标：为未来模块拆分、外部集成和最终一致性提供基础能力。

任务：

- 设计 Outbox 表，必须包含：
  - `tenant_id`
  - event id
  - aggregate type
  - aggregate id
  - event type
  - payload json
  - status
  - retry count
  - next retry time
  - created/updated audit fields
  - `deleted`
  - `version`
  - `ext_json`
- 实现 Outbox 写入应用服务或基础设施组件。
- 应用服务事务中写业务数据时同步写 Outbox。
- 实现最小 dispatcher：
  - 查询待发送事件。
  - 调用 handler。
  - 成功标记已发送。
  - 失败增加 retry。
- 先选择一个平台事件做 tracer bullet，例如 `TenantCreatedEvent` 或 `UserCreatedEvent`。

验收标准：

- 业务写操作和 Outbox 写入在同一事务中完成。
- dispatcher 支持幂等重试。
- event payload 包含 tenant 信息。
- 有单元测试或集成测试覆盖成功、失败、重试。

### 阶段 5：服务边界契约与依赖检查

目标：在单体阶段提前阻止未来难拆的跨边界耦合。

任务：

- 根据 TASK-0218 ADR，建立模块边界检查。
- 可采用 ArchUnit 或 Maven enforcer 等轻量方案。引入新工具需符合项目规则，若有争议先写 ADR。
- 检查规则至少覆盖：
  - 平台模块不得依赖制造或 WMS 模块。
  - 制造模块不得依赖 WMS 模块。
  - 业务模块不得直接访问其他模块 `infrastructure`。
  - Controller 不得注入 Repository、Mapper、JdbcTemplate。
  - 应用服务不得绕过 owner 模块直接操作其他模块表。
- 为跨模块调用定义 published API 或 application contract 包策略。

验收标准：

- 自动化测试能发现非法模块依赖。
- 现有合法依赖通过检查。
- 新增跨模块调用有明确 owner 和 contract。

### 阶段 6：多租户可观测性基线

目标：让日志、审计、错误定位和未来运营指标都带上租户维度。

任务：

- 日志 MDC 增加：
  - `tenantId`
  - `userId`
  - `currentOrgId`
  - `traceId` / `correlationId`
- 全局异常处理返回统一错误，不暴露堆栈。
- 审计日志必须记录 tenant、user、org、operation、resource。
- 指标预留租户维度，但避免高基数指标直接打满监控系统。
- Outbox、登录、权限拒绝、租户停用等关键事件必须可追踪。

验收标准：

- 典型请求日志包含 tenant 和 trace 信息。
- 异步任务日志也能关联 tenant 和 trace。
- 权限拒绝和租户禁用有可审计记录。

### 阶段 7：WMS/制造模块多租户样板接入

目标：用一个真实业务模块验证平台多租户基线不是只服务平台管理页。

任务：

- 选择一个 WMS 或制造样板模块作为 tracer bullet：
  - 优先选择库存主数据或入库单据中最小闭环。
- 新增业务表必须包含：
  - `tenant_id`
  - company/plant/warehouse 等适用组织维度字段
  - audit fields
  - `deleted`
  - `version`
  - `ext_json`
- API 必须：
  - 使用 `/api/{module}/{resources}`。
  - 使用统一 `Result<T>` / `PageResult<T>`。
  - 有后端权限校验。
  - 有数据权限过滤。
- 业务写操作必须：
  - 在 ApplicationService 开事务。
  - 写审计。
  - 必要时写 Outbox。
  - 库存余额变更必须通过库存 posting service 和库存事务记录。

验收标准：

- 一个业务模块完成租户隔离、组织隔离、权限、审计、事件的完整样板。
- 该样板可作为后续 WMS/MES/SRM/QMS 模块复制模板。

## 6. 需要新增/修改的文件

后端：

```text
backend/iaf-platform-auth/src/main/java/com/company/iaf/platform/auth/...
backend/iaf-platform-org/src/main/java/com/company/iaf/platform/org/...
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/...
backend/iaf-platform-system/src/main/java/com/company/iaf/platform/system/...
backend/iaf-platform-integration/src/main/java/com/company/iaf/platform/integration/...
backend/iaf-platform-core/src/main/java/com/company/iaf/platform/core/...
backend/iaf-shared/src/main/java/com/company/iaf/shared/...
backend/iaf-wms-*/src/main/java/...
backend/iaf-manufacturing-*/src/main/java/...
```

数据库：

```text
backend/iaf-app/src/main/resources/db/migration/Vxxxx__tenant_lifecycle.sql
backend/iaf-app/src/main/resources/db/migration/Vxxxx__tenant_quota.sql
backend/iaf-app/src/main/resources/db/migration/Vxxxx__platform_outbox.sql
```

文档：

```text
docs/decisions/XXXX-tenant-lifecycle-and-initialization.md
docs/decisions/XXXX-outbox-domain-events.md
docs/decisions/XXXX-module-boundary-checks.md
docs/code-map/backend.md
docs/code-map/api.md
docs/code-map/database.md
docs/code-map/frontend.md
```

## 7. API 清单

建议新增或完善：

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/platform/tenants` | 租户列表 |
| POST | `/api/platform/tenants` | 创建租户 |
| GET | `/api/platform/tenants/{id}` | 租户详情 |
| PUT | `/api/platform/tenants/{id}` | 更新租户基础信息 |
| POST | `/api/platform/tenants/{id}/enable` | 启用租户 |
| POST | `/api/platform/tenants/{id}/disable` | 停用租户 |
| GET | `/api/platform/tenants/{id}/quotas` | 查询租户配额 |
| PUT | `/api/platform/tenants/{id}/quotas` | 更新租户配额 |
| GET | `/api/platform/outbox-events` | Outbox 事件查询，仅管理/运维用途 |
| POST | `/api/platform/outbox-events/{id}/retry` | 手动重试失败事件 |

如新增 WMS 样板 API，必须在具体子任务中补齐路径、权限点、请求和响应。

## 8. Code Map 更新计划

必须更新：

- `docs/code-map/backend.md`
- `docs/code-map/api.md`
- `docs/code-map/database.md`

按需更新：

- `docs/code-map/frontend.md`

必须记录：

- 租户生命周期 API。
- 租户初始化模板。
- 配额表和配额应用服务。
- Outbox 表、dispatcher、事件 contract。
- 模块边界检查规则。
- 日志、审计、上下文传播变化。
- WMS/制造样板模块的表、API、权限、事件。

## 9. 前端设计

如实现租户运营页面，必须遵循 `ai-coding/rules/03_frontend_rules.md` 和 `docs/frontend/*`。

建议页面：

- 租户列表页。
- 租户详情页。
- 租户配额配置页。
- 租户初始化状态页。
- Outbox 事件运维页。

前端要求：

- 通过 `packages/api-client` 调用后端。
- 不在页面硬编码权限、状态颜色、中文 copy、租户值。
- 所有敏感按钮必须由后端权限兜底。
- 失败状态必须显示统一错误消息，不暴露后端堆栈。

## 10. 权限点

建议新增：

```text
platform:tenant:view
platform:tenant:create
platform:tenant:update
platform:tenant:disable
platform:tenant:enable
platform:tenant-quota:view
platform:tenant-quota:update
platform:outbox:view
platform:outbox:retry
platform:audit:view
```

如新增 WMS 样板：

```text
wms:<object>:view
wms:<object>:create
wms:<object>:update
wms:<object>:submit
wms:<object>:approve
```

权限必须通过 migration seed，并绑定到默认平台管理员角色。不得只做前端隐藏。

## 11. 数据库设计要求

所有新增业务表必须包含：

```sql
id bigint primary key,
tenant_id bigint not null,
created_by bigint,
created_at timestamp not null,
updated_by bigint,
updated_at timestamp not null,
deleted boolean not null default false,
version int not null default 0,
ext_json jsonb
```

必须按查询场景增加索引：

- `tenant_id`
- `tenant_id, status`
- `tenant_id, created_at`
- `tenant_id, user_id`
- `tenant_id, aggregate_type, aggregate_id`
- `tenant_id, event_type, status`

禁止修改已有 migration。所有数据库变更使用新增 migration。

## 12. 测试要求

必须新增或更新：

- 租户生命周期应用服务测试。
- 租户初始化幂等测试。
- 租户停用后的登录和写操作拒绝测试。
- 配额校验测试。
- Outbox 写入、发送、失败、重试测试。
- 模块边界自动化检查测试。
- MDC / 上下文传播测试。
- WMS/制造样板模块租户隔离测试。

必须运行：

```bash
cd backend
mvn test
```

如修改前端：

```bash
cd frontend
npm run typecheck
npm run lint
npm run build
```

任务完成前还必须执行：

```bash
./scripts/check-quality.sh
```

如脚本不可用，必须在最终报告中说明原因，并列出已执行的等价检查。

## 13. 验收标准

- 可以通过 API 创建、启用、停用租户。
- 新租户初始化过程幂等、可追踪、可重试。
- 租户禁用后登录和业务写操作符合 ADR。
- 至少一个租户配额能力完成闭环。
- Outbox 表、写入、dispatcher、重试完成最小闭环。
- 模块边界违规能被自动化测试发现。
- 典型请求和异步任务日志带 tenant/trace 信息。
- 至少一个 WMS 或制造业务样板完成多租户、组织、权限、审计、事件闭环。
- `docs/code-map/*` 已同步。
- 相关后端测试通过。
- 如修改前端，前端质量门禁通过。

## 14. 推荐子任务顺序

建议按以下顺序拆分给 Agent：

1. `TASK: Tenant lifecycle API and initialization`
2. `TASK: Tenant initialization template and idempotency`
3. `TASK: Tenant quota baseline`
4. `TASK: Platform Outbox and domain event baseline`
5. `TASK: Module boundary automated checks`
6. `TASK: Tenant-aware observability baseline`
7. `TASK: WMS tenant-aware tracer bullet`

优先级最高的第一张子任务：

```text
TASK: Tenant lifecycle API and initialization
```

范围：

- 创建、查询、启用、停用租户 API。
- 初始化默认管理员、角色、权限、菜单、系统配置。
- 停用租户后的登录拒绝和写操作拒绝。
- 新增权限点和 migration seed。
- 更新 `docs/code-map/*`。

## 15. Codex 执行要求

执行前必须输出：

- 任务目标摘要。
- 影响模块、文件类型、风险。
- 是否涉及 DSL、数据库 migration、后端、前端、权限、测试、文档、code map。
- 当前 task branch / git worktree 状态。
- 简短实施计划。

执行中必须遵守：

- 使用独立 git worktree 和任务分支。
- 不修改已有 Flyway migration。
- 不引入新核心框架、ORM、工作流引擎或规则引擎。
- 不绕过模块 owner 直接访问其他模块 infrastructure。
- 不绕过 `packages/api-client` 发起前端 HTTP 请求。
- 不降低测试或质量门禁标准。
- 遇到已有未提交变更时，不得回滚非本人修改。

执行后必须输出：

- Summary
- Files changed
- Architecture impact
- Database migration impact
- Permission impact
- Code map impact
- Tests run and results
- Quality gate status
- Known risks
- Suggested next steps

并逐项填写质量门禁清单：

```text
[ ] 已读取 AGENTS.md
[ ] 已读取相关 rules
[ ] 已读取相关 code map
[ ] 已读取相关 module spec
[ ] 已完成代码修改
[ ] 已新增/更新 code map，或说明无需更新
[ ] 已新增/更新 migration
[ ] 已新增/更新测试
[ ] 后端测试通过
[ ] 前端构建通过
[ ] 权限点已处理
[ ] 菜单/路由已处理
[ ] 自检无 blocking issue
```
