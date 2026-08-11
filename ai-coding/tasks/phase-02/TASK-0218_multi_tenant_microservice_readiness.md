# TASK-0218 多租户与微服务演进准备

## 1. 任务目标

将当前 IAF 后端从“具备租户字段的模块化单体”推进到“可安全支持多租户，并为未来微服务拆分降低重构成本”的架构基线。

本任务不要求把系统改造成微服务，也不引入 Spring Cloud。第一阶段仍保持当前 Maven 多模块、单体部署形态，重点补齐租户识别、租户隔离、认证 token、上下文传播、模块数据 ownership 和质量校验。

## 2. 必须先阅读

- `AGENTS.md`
- `docs/architecture/06_项目文档与规则目录治理.md`
- `docs/architecture/01_总体架构蓝图.md`
- `docs/architecture/02_技术选型报告.md`
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
- `ai-coding/rules/13_agent_parallel_work_rules.md`
- `docs/quality/quality_gate.md`

## 3. 业务范围

本任务实现：

- 租户感知登录，避免不同租户同名用户串租户。
- 已登录请求的租户来源标准化。
- 关键仓储的租户隔离测试与防漏机制。
- 认证 token 生产化准备，替代纯内存开发态作为生产默认方案。
- 异步、任务、消息场景的租户与安全上下文传播基线。
- 当前数据库表和未来服务边界的数据 ownership ADR。
- 数据权限落地方案和最小闭环。

本任务不实现：

- 不把系统拆成真实微服务。
- 不引入 Spring Cloud、Feign、Nacos、Gateway 等微服务框架。
- 不改变当前“模块化单体优先”的部署策略。
- 不修改已有 Flyway migration。
- 不绕过现有 `TenantContext`、`SecurityContext`、权限校验和应用服务层。

## 4. 需要新增/修改的文件

后端：

```text
backend/iaf-platform-auth/src/main/java/com/company/iaf/platform/auth/...
backend/iaf-platform-core/src/main/java/com/company/iaf/platform/core/...
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/...
backend/iaf-shared/src/main/java/com/company/iaf/shared/...
backend/iaf-app/src/main/java/com/company/iaf/app/...
```

测试：

```text
backend/iaf-platform-auth/src/test/java/...
backend/iaf-platform-core/src/test/java/...
backend/iaf-platform-permission/src/test/java/...
backend/iaf-platform-system/src/test/java/...
backend/iaf-platform-org/src/test/java/...
```

文档：

```text
docs/decisions/XXXX-multi-tenant-auth-and-token.md
docs/decisions/XXXX-modular-monolith-to-service-boundaries.md
docs/code-map/backend.md
docs/code-map/api.md
docs/code-map/database.md
```

前端：

```text
frontend/...
```

仅当登录页或 API client 需要传入 `tenantCode` 时修改前端。

数据库：

```text
backend/iaf-app/src/main/resources/db/migration/Vxxxx__*.sql
```

仅当需要新增表、字段、索引或约束时新增 migration。禁止修改已有 migration。

## 5. 数据库设计

本任务优先不要求新增业务表。若实现 Redis token store，则不需要数据库 migration。

如选择数据库型 token/session store，必须新增 migration，并满足：

- 表必须包含 `tenant_id`。
- 表必须包含审计字段、`deleted`、`version`、`ext_json`。
- token/session 查询索引必须覆盖 `tenant_id`、`user_id`、`expires_at`。
- 禁止明文保存访问 token，可保存 token hash。

必须确认并记录当前表 ownership：

- `sys_tenant`：需在 ADR 中明确 owner。
- `sys_user`、`sys_user_org`：`iaf-platform-auth`。
- `sys_org`：`iaf-platform-org`。
- `sys_role`、`sys_permission`、`sys_menu`、关联表：`iaf-platform-permission`。
- `sys_theme_config`、`sys_brand_config`、`sys_i18n_resource`、`sys_user_experience_preference`：`iaf-platform-system`。
- 未来 WMS 库存表：`iaf-wms-inventory`。
- 未来 WMS 入库单据表：`iaf-wms-inbound`。

## 6. 后端设计

### 阶段 1：租户感知登录

目标：解决当前登录只按 `username` 查询导致多租户用户名冲突的问题。

任务：

- 修改 `LoginRequest`，增加 `tenantCode` 字段。
- 登录逻辑先通过 `tenantCode` 查找启用租户，再用 `tenantId + username` 查询用户。
- 将 `AuthUserRepository.findByUsername(...)` 改为 `findByTenantIdAndUsername(long tenantId, String username)`。
- 登录失败时不要暴露“租户不存在 / 用户不存在 / 密码错误”的具体差异，统一返回认证失败。
- 校验 `sys_tenant.status`，禁用租户不可登录。
- token 的 `AuthenticatedUser` 必须包含明确 `tenantId`。

验收测试：

- 不同租户相同用户名可分别登录。
- 错误 `tenantCode` 登录失败。
- 禁用租户登录失败。
- 查询权限只来自当前租户。

### 阶段 2：租户上下文和租户解析标准化

目标：明确租户来源，减少后续 API 各自处理租户的风险。

任务：

- 定义租户解析策略：
  - 登录接口使用 request body 的 `tenantCode`。
  - 已登录接口使用 token 中的 `tenantId`。
  - 普通业务接口暂不允许通过 header 覆盖租户。
- 增加 `TenantInfo` 或等价轻量对象，包含 `tenantId`、`tenantCode`、`tenantStatus`。
- 增加租户查询仓储，归属模块必须在 ADR 中明确。
- 在 `BearerTokenAuthenticationFilter` 中继续从 token 设置 `TenantContext`。
- 补充异常、无 token、无效 token、上下文清理场景测试。
- 修正文档中 `TenantContext` 方法名和实际代码不一致的问题。

### 阶段 3：租户隔离防漏机制

目标：降低手写 SQL 漏 `tenant_id` 的概率。

任务：

- 建立租户隔离测试基类或测试 helper。
- 对已有仓储补跨租户数据隔离测试：
  - user
  - org
  - role
  - permission
  - menu
  - system config
- 梳理所有 `JdbcTemplate` 查询，确认业务表查询必须带 `tenant_id`。
- 对允许跨租户的查询加明确命名和注释，例如租户注册表查询。
- 可选：新增轻量 SQL 审查测试，扫描明显缺少 `tenant_id` 的 `sys_*` 查询。该测试不得依赖脆弱的复杂 SQL 字符串解析。

### 阶段 4：认证 token 生产化准备

目标：替换当前 in-memory token 对未来多实例和微服务不友好的问题。

任务：

- 先新增 ADR，决策 token 方案：
  - 方案 A：JWT，自包含租户、用户、权限版本。
  - 方案 B：Redis token store，可撤销、可集中失效。
- 推荐第一阶段采用 Redis token store，保留 JWT 接口抽象。原因是租户禁用、用户禁用、权限变更时更容易集中失效。
- 将 `AuthTokenStore` 扩展为可配置实现：
  - local/dev profile 可使用 in-memory。
  - prod profile 不得默认使用 in-memory。
- token 内容必须包含：
  - `tenantId`
  - `userId`
  - `username`
  - `currentOrgId`
  - permission codes
  - `expiresAt`
  - `tokenVersion` 或 `permissionVersion`，若暂不实现需记录风险。

验收测试：

- token 过期。
- token 不存在。
- 用户组织变更后 current org 刷新逻辑仍正确。
- 用户或租户禁用后的 token 行为按 ADR 执行。

### 阶段 5：异步/任务上下文传播基线

目标：为未来 Outbox、定时任务、消息消费、多线程处理准备租户上下文模型。

任务：

- 定义 `ExecutionContext` 或 `RequestContext`，包含：
  - `tenantId`
  - `userId`
  - `currentOrgId`
  - permissions 或 permission snapshot id
  - `traceId` / `correlationId`
- 提供从当前 `TenantContext` / `SecurityContext` 创建上下文快照的方法。
- 提供上下文恢复工具，例如 `ContextScope implements AutoCloseable`。
- 为线程池增加 task decorator，确保异步任务显式传播上下文。

验收测试：

- 上下文可传播到异步线程。
- 任务结束后上下文被清理。
- 无上下文任务不会继承上一个请求的 `ThreadLocal`。

### 阶段 6：模块数据 Ownership 和未来微服务边界 ADR

目标：不做微服务，但提前固定未来拆分边界。

任务：

- 新增 ADR：模块化单体到微服务的演进边界。
- 明确每类表的 owner。
- 写明未来拆服务时的禁止事项：
  - 禁止跨服务直接访问别的服务表。
  - 禁止跨服务数据库 join。
  - 跨服务写操作走 API、事件或 Outbox。
  - 查询聚合用读模型或应用层聚合。
- 写明当前单体阶段允许的临时妥协和迁移路径。

### 阶段 7：数据权限落地方案

目标：把权限从“接口权限码”推进到“组织/工厂/仓库/本人”等数据范围。

任务：

- 先写设计文档，不急着全量实现。
- 定义数据权限模型：
  - scope type：`TENANT`、`COMPANY`、`PLANT`、`WAREHOUSE`、`SELF`、`CUSTOM` 等。
  - scope resource id。
  - role/user 绑定方式。
- 定义应用服务如何请求数据权限过滤条件。
- 定义仓储层如何消费数据权限条件。
- 选择一个现有列表接口做 tracer bullet，例如用户列表或组织列表。

验收测试：

- 无权限不可访问。
- 有 API 权限但无数据范围时只能看到空结果或受限数据。
- 仓库/组织范围过滤正确。

## 7. API 清单

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/platform/auth/login` | 登录接口增加 `tenantCode` 入参 |

如阶段 7 新增数据权限管理 API，必须使用 `/api/{module}/{resources}` 路径规范，并在执行前补充到本任务或拆分为后续任务。

## 8. Code Map 更新计划

必须更新：

- `docs/code-map/backend.md`
- `docs/code-map/api.md`
- `docs/code-map/database.md`

按需更新：

- `docs/code-map/frontend.md`

必须记录：

- 登录入参变化。
- 租户解析规则。
- token store 实现策略。
- 新增上下文传播类。
- 表 ownership 和未来服务边界。
- 数据权限最小闭环。

## 9. 前端设计

如后端登录接口增加 `tenantCode`，前端必须同步：

- 登录表单增加租户编码输入。
- API client 请求体增加 `tenantCode`。
- 不得在页面硬编码默认租户。
- 如需本地记忆最近使用租户编码，必须使用用户体验配置或明确的 local storage key，并避免保存敏感信息。

## 10. 权限点

本任务至少涉及现有权限：

- `platform:auth:me`
- `platform:user:view`
- `platform:org:view`
- `platform:role:view`
- `platform:permission:view`

如新增数据权限管理 API，权限编码建议：

- `platform:data-permission:view`
- `platform:data-permission:create`
- `platform:data-permission:update`
- `platform:data-permission:assign`

新增权限必须通过 migration seed，并绑定到开发默认管理员角色。

## 11. 业务规则

- 登录必须租户感知。
- 普通业务请求不得通过 header 任意切换租户。
- token 中的 `tenantId` 是已登录请求的租户来源。
- 租户禁用后不得新登录。
- 用户禁用后不得继续认证通过，具体 token 失效策略按 ADR 执行。
- 业务表查询必须按租户隔离，除非是明确标注的系统级跨租户查询。
- 未来服务边界内，模块不得直接访问其他模块的 infrastructure、entity、mapper。
- 未来拆服务时禁止跨服务数据库 join。

## 12. 测试要求

必须新增或更新：

- `AuthApplicationServiceTest`
- `JdbcAuthUserRepository` 相关测试或等价仓储测试
- `BearerTokenAuthenticationFilter` 测试
- 租户隔离测试 helper
- user/org/role/permission/menu/system config 跨租户隔离测试
- token store 测试
- 上下文传播和清理测试
- 数据权限 tracer bullet 测试

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

任务完成前还必须执行项目质量门禁：

```bash
./scripts/check-quality.sh
```

如果质量门禁脚本暂不可用，必须在最终报告中说明原因，并列出已手动执行的等价检查。

## 13. 验收标准

- 登录不再只按 `username` 查询。
- 多租户同名用户不会串租户。
- 已登录请求的租户来源规则清晰且有测试。
- 关键仓储具备跨租户隔离测试。
- 生产 profile 不默认使用 in-memory token store。
- 异步上下文传播和清理有测试。
- 当前表 ownership 已写入 ADR 和 code map。
- 数据权限至少有一个后端最小闭环。
- 所有新增 API 都有后端权限校验。
- 所有结构、API、数据库、权限变化已更新 code map。
- 相关后端测试通过。
- 如修改前端，前端 typecheck、lint、build 通过。

## 14. Codex 执行要求

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

## 15. 推荐拆分顺序

建议不要一次性完成全部阶段。推荐拆成以下子任务逐步交付：

1. 租户感知登录。
2. 租户上下文标准化。
3. 租户隔离防漏测试。
4. 模块 ownership 和未来服务边界 ADR。
5. token 生产化。
6. 异步上下文传播。
7. 数据权限最小闭环。

优先级最高的第一张子任务：

```text
TASK: Make authentication tenant-aware
```

范围：

- `LoginRequest` 增加 `tenantCode`。
- 登录先解析租户，再按 `tenantId + username` 查询。
- 修复 `JdbcAuthUserRepository`。
- 增加多租户同名用户测试。
- 更新 API/code map 文档。
