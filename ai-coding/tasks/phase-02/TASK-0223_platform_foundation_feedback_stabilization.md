# TASK-0223 平台基础能力反馈稳定期与增强路线计划

## 1. 任务目标

在 `TASK-0222 平台基础能力发布治理与长期维护计划` 完成后，继续停留在平台基础能力层，不进入 WMS、MES、SRM、QMS 等业务领域。

本任务目标是建立 Platform Foundation RC1 发布后的反馈稳定期机制：收集实施、测试、Agent 使用、设计验收和质量门禁反馈，按风险和价值归类，形成补丁节奏、稳定性指标、平台增强路线和进入下一阶段的准入条件。

这一步不追求新增大功能，而是让平台基础能力真正稳定下来，避免一边进入后续开发，一边反复返工组织、权限、菜单、样式、配置模板和文档入口。

## 2. 前置条件

必须确认 `TASK-0222` 已完成并通过验收：

- Platform Foundation RC1 范围已冻结。
- 发布验收清单可执行。
- smoke test 有文档或脚本。
- P0/P1 问题清零或明确阻塞发布。
- API、权限、菜单、配置模板兼容性规则明确。
- 设计系统长期治理规则明确。
- 平台基础文档入口清晰。
- 后续平台 backlog 已分类。
- `docs/code-map/*` 已同步或说明无需更新。

## 3. 必须先阅读

- `AGENTS.md`
- `ai-coding/tasks/phase-02/TASK-0220_platform_foundation_design_hardening.md`
- `ai-coding/tasks/phase-02/TASK-0221_platform_foundation_productization.md`
- `ai-coding/tasks/phase-02/TASK-0222_platform_foundation_release_governance.md`
- `docs/operations/platform-foundation-release-checklist.md`
- `docs/operations/platform-foundation-known-issues.md`
- `docs/operations/platform-foundation-document-index.md`
- `docs/operations/platform-foundation-smoke-test.md`
- `docs/code-map/README.md`
- `docs/code-map/backend.md`
- `docs/code-map/api.md`
- `docs/code-map/database.md`
- `docs/code-map/frontend.md`
- `ai-coding/rules/01_project_rules.md`
- `ai-coding/rules/02_backend_rules.md`
- `ai-coding/rules/03_frontend_rules.md`
- `ai-coding/rules/06_permission_rules.md`
- `ai-coding/rules/10_testing_rules.md`
- `ai-coding/rules/11_code_quality_rules.md`
- `ai-coding/rules/12_code_map_rules.md`
- `docs/frontend/16_平台管理页面交互规范.md`
- `docs/frontend/17_平台管理视觉设计规范.md`
- `docs/frontend/19_平台应用外壳与导航规范.md`
- `docs/quality/quality_gate.md`

## 4. 业务范围

本任务实现：

- RC1 反馈收集机制。
- 稳定期问题分类、修复节奏和补丁规则。
- 平台基础稳定性指标。
- 平台管理端 UX 反馈闭环。
- Agent 使用反馈闭环。
- 配置模板和实施 runbook 复盘。
- 后续平台增强路线。
- 进入业务领域前的平台准入条件清单。

本任务不实现：

- 不进入业务领域模块开发。
- 不新增业务单据、库存、采购、生产、质量等业务流程。
- 不直接拆微服务。
- 不新增完整低代码运行时。
- 不引入新 UI 框架、ORM、工作流引擎或规则引擎。
- 不以临时补丁绕过权限、租户隔离、测试或 code map。

## 5. 推荐拆分阶段

### 阶段 1：RC1 反馈收集机制

目标：把反馈来源标准化，避免问题散落在聊天、临时文档或个人记忆中。

任务：

- 定义反馈来源：
  - 手工验收。
  - 自动化测试。
  - smoke test。
  - 前端视觉/交互验收。
  - 实施 runbook 复现。
  - Agent 使用页面模板生成新页面。
  - 权限矩阵和菜单模板使用。
  - 配置快照导入/导出。
- 定义反馈记录模板：
  - 来源。
  - 页面/API/模块。
  - 复现步骤。
  - 期望结果。
  - 实际结果。
  - 严重级别。
  - 是否阻塞平台基础稳定。
  - owner。
  - 后续任务。
- 统一反馈存放位置，建议放在 `docs/operations/` 或项目 issue tracker，按当前仓库治理规则执行。

验收标准：

- 反馈入口明确。
- 反馈项可追踪到 owner 和后续任务。
- P0/P1 问题不会只停留在文字记录。

### 阶段 2：稳定期补丁节奏

目标：建立平台基础能力的稳定期修复规则，避免随意改动已冻结契约。

任务：

- 定义补丁类型：
  - `hotfix`: 修复 P0/P1。
  - `patch`: 修复 P2 或不破坏兼容的小问题。
  - `polish`: 体验和文案优化。
  - `deferred`: 有价值但不进入当前稳定期。
- 定义补丁合入要求：
  - 不修改已发布 API contract，除非有兼容策略。
  - 不改名 permission code、menu code、i18n key，除非有迁移策略。
  - 必须更新测试和 code map。
  - 必须运行相关质量门禁。
- 定义稳定期窗口：
  - RC1 发布后集中处理反馈。
  - 稳定期结束后只接受 P0/P1 hotfix。

验收标准：

- 平台基础稳定期有明确修复节奏。
- 兼容性变更受控。
- 补丁不引入新的平台基础回归。

### 阶段 3：平台基础稳定性指标

目标：用可观察指标判断平台基础能力是否真的稳定。

任务：

- 定义稳定性指标：
  - smoke test 通过率。
  - 后端测试通过率。
  - 前端 typecheck/lint/test/build 通过率。
  - P0/P1 未关闭数量。
  - 权限拒绝误报数量。
  - 菜单不可达页面数量。
  - i18n 缺失数量。
  - 主题 token 违规数量。
  - 直接 HTTP 调用违规数量。
  - code map 未同步数量。
- 明确指标收集方式：
  - 自动脚本。
  - 人工检查。
  - 代码搜索。
  - 验收记录。
- 定义平台基础稳定标准：
  - P0/P1 为 0。
  - smoke test 连续通过。
  - 核心页面无 blocking UX issue。
  - 权限/菜单/角色一致性通过。

验收标准：

- 稳定性指标可执行或可人工核验。
- 稳定标准清晰。
- 未达标项有后续任务。

### 阶段 4：UX 反馈闭环

目标：将平台管理端从“符合规范”推进到“实际可用、可理解、低误操作”。

任务：

- 对核心页面做 UX 复盘：
  - 登录页。
  - 工作台/首页。
  - 用户管理。
  - 组织管理。
  - 角色管理。
  - 权限管理。
  - 菜单管理。
  - 数据权限。
  - 字段权限。
  - 字典参数。
  - 审计日志。
  - 主题品牌/i18n。
- 检查：
  - 用户是否能理解当前页面目标。
  - 主操作是否明确。
  - 危险操作是否有确认。
  - 错误信息是否能指导恢复。
  - 空状态是否有下一步动作。
  - 表格筛选是否保留状态。
  - 表单校验是否定位到字段。
  - icon-only 按钮是否有 tooltip/aria-label。
  - 键盘焦点是否可见。
- 将 UX 问题拆成 P1/P2/P3，不在本任务中无限修。

验收标准：

- 核心页面有 UX 复盘记录。
- UX 问题有级别和 owner。
- 不通过新增大功能掩盖基础体验问题。

### 阶段 5：Agent 使用反馈闭环

目标：验证前面沉淀的模板和规则是否真的能指导后续 Agent 稳定开发平台页面。

任务：

- 选择 1 个低风险平台配置页作为模板验证，不进入业务领域。
- 让 Agent 按页面模板生成或改造页面。
- 检查 Agent 是否正确遵守：
  - `packages/api-client`。
  - i18n。
  - theme token。
  - permission guard。
  - table/form engine。
  - loading/error/empty。
  - tests。
  - code map 更新。
- 记录模板缺陷：
  - 哪些说明不清楚。
  - 哪些路径不准确。
  - 哪些组件缺少示例。
  - 哪些检查项难执行。

验收标准：

- 至少完成一次 Agent 模板使用复盘。
- 模板问题已更新或拆任务。
- 后续 Agent 不需要靠口头说明理解平台页规范。

### 阶段 6：实施 Runbook 复盘

目标：验证平台基础能力能被新环境、新租户、新人员复现。

任务：

- 按 runbook 从空环境或干净环境执行：
  - 数据库迁移。
  - 默认配置初始化。
  - 创建/确认租户。
  - 创建组织。
  - 创建用户。
  - 创建角色。
  - 分配权限和菜单。
  - 登录。
  - 验证菜单。
  - 验证审计。
  - 验证主题/i18n。
- 记录所有 runbook 中不准确的命令、路径、前置条件和截图。
- 修复 runbook，不直接修代码，除非发现 P0/P1。

验收标准：

- runbook 至少被完整执行一次。
- 新发现问题有记录。
- runbook 不依赖个人经验。

### 阶段 7：下一阶段平台增强路线

目标：在平台基础稳定后，明确后续仍属于平台层的增强方向，并区分哪些可以和业务开发并行。

候选方向：

- 平台安全增强：
  - 密码策略。
  - 登录失败锁定。
  - 会话管理。
  - 操作二次确认。
- 平台运维增强：
  - 审计检索增强。
  - 配置变更历史。
  - Outbox 运维页面。
  - 系统健康页。
- 平台体验增强：
  - 全局搜索。
  - 最近访问。
  - 收藏菜单。
  - 批量操作一致性。
- 平台配置增强：
  - 配置模板版本。
  - 配置差异对比。
  - 配置回滚。
- 平台 AI Coding 增强：
  - 页面上下文规范。
  - Agent 生成代码检查器。
  - 任务模板质量校验。

验收标准：

- 后续增强路线已分类。
- 每个增强项说明是否阻塞业务领域开工。
- 不把所有增强塞进一个大任务。

## 6. 需要新增/修改的文件

文档：

```text
docs/operations/platform-foundation-feedback-log.md
docs/operations/platform-foundation-stabilization-plan.md
docs/operations/platform-foundation-runbook-review.md
docs/operations/platform-foundation-next-backlog.md
docs/frontend/17_平台管理视觉设计规范.md
docs/frontend/19_平台应用外壳与导航规范.md
docs/code-map/backend.md
docs/code-map/api.md
docs/code-map/database.md
docs/code-map/frontend.md
```

脚本，按需：

```text
scripts/platform-foundation-smoke-test.sh
scripts/check-quality.sh
```

任务：

```text
ai-coding/tasks/phase-02/...
ai-coding/tasks/frontend/...
```

## 7. API 清单

本任务默认不新增 API。

如反馈稳定期发现必须新增平台诊断或验证 API，必须另拆任务，并满足：

- 路径遵循 `/api/{module}/{resources}`。
- 返回统一 `Result<T>`。
- 有后端权限校验。
- 有租户隔离。
- 更新 `docs/code-map/api.md`。

## 8. 权限点

本任务默认不新增权限点。

如新增平台诊断、验证、运维 API，可考虑：

```text
platform:foundation:diagnose
platform:foundation:verify
platform:operations:view
```

新增权限必须通过 migration seed 或平台配置模板授予平台管理员角色。

## 9. 数据库设计要求

本任务默认不新增数据库表。

如新增反馈或稳定性记录表，必须包含：

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

禁止修改已有 Flyway migration。所有数据库变更新增 migration。

## 10. 前端设计要求

如涉及前端修复，必须遵守：

- 管理端保持信息密度和稳定布局。
- 不使用营销式 hero、装饰背景、大面积渐变。
- 颜色、间距、圆角、状态色全部来自 token。
- 所有用户可见文案使用 i18n key。
- 所有 API 经过 `packages/api-client`。
- 所有权限判断通过统一权限组件。
- 必须覆盖 loading、error、empty、permission denied 状态。
- icon-only 按钮必须有 tooltip 和可访问标签。
- 表单错误必须定位到字段附近。

## 11. 测试要求

必须新增或更新：

- 平台基础反馈日志。
- 稳定期补丁规则。
- 稳定性指标记录。
- UX 复盘记录。
- Agent 模板使用复盘。
- Runbook 复盘记录。
- 下一阶段 backlog。

如修改代码，必须运行：

```bash
cd backend
mvn test
```

如修改前端：

```bash
cd frontend
pnpm typecheck
pnpm lint
pnpm test
pnpm build
```

任务完成前必须执行：

```bash
./scripts/check-quality.sh
```

如脚本不可用，必须在最终报告中说明原因，并列出已执行的等价检查。

## 12. 验收标准

- RC1 反馈入口清晰。
- 稳定期补丁节奏明确。
- 平台基础稳定性指标明确。
- 核心页面 UX 复盘完成。
- Agent 页面模板已建立复盘机制，并至少完成一次针对低风险平台页面的 dry-run 使用复盘，实际产出和模板缺口有记录。
- 平台实施 runbook 已建立复盘记录并执行可用环境下的 replay；完整 smoke replay 通过前必须作为稳定期阻塞项或准入条件跟踪。
- 后续平台增强路线已分类。
- 不进入业务领域。
- `docs/code-map/*` 已同步或说明无需更新。

## 13. 推荐子任务顺序

建议拆成以下任务交给 Agent：

1. `TASK: Platform Foundation feedback log and intake`
2. `TASK: Platform Foundation stabilization patch policy`
3. `TASK: Platform Foundation stability metrics`
4. `TASK: Platform core page UX review`
5. `TASK: Agent platform page template validation`
6. `TASK: Platform runbook replay and correction`
7. `TASK: Platform next enhancement backlog`

优先级最高的第一张子任务：

```text
TASK: Platform Foundation feedback log and intake
```

范围：

- 建立反馈日志。
- 定义反馈模板。
- 归集 RC1 反馈来源。
- 标记 P0/P1/P2/P3。
- 生成后续修复任务。

## 14. Codex 执行要求

执行前必须输出：

- 任务目标摘要。
- 影响模块、文件类型、风险。
- 是否涉及数据库 migration、后端、前端、权限、测试、文档、code map。
- 当前 task branch / git worktree 状态。
- 简短实施计划。

执行中必须遵守：

- 使用独立 git worktree 和任务分支。
- 不进入业务领域模块开发。
- 不修改已有 Flyway migration。
- 不引入新核心框架、UI 框架、ORM、工作流引擎或规则引擎。
- 不绕过 `packages/api-client`。
- 不绕过后端权限检查。
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
