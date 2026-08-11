# TASK-0222 平台基础能力发布治理与长期维护计划

## 1. 任务目标

在 `TASK-0221 平台基础能力产品化与交付固化计划` 完成后，继续停留在平台基础能力层，不进入 WMS、MES、SRM、QMS 等业务领域。

本任务目标是把平台基础能力从“可交付、可配置、可复用”推进到“可发布、可验收、可长期维护”。重点不是继续堆功能，而是建立平台基础版本的发布候选、验收闸口、缺陷分级、文档归档、设计系统治理、API/权限兼容性治理和后续迭代节奏。

## 2. 前置条件

必须确认 `TASK-0221` 已完成并通过验收：

- 平台默认配置模板清晰、可复用、可幂等初始化。
- 菜单、角色、权限交付包完整。
- 平台设计系统有逐页验收结果。
- 基础配置可导出/导入，或有明确 ADR 说明暂不实现。
- 平台基础页面回归测试矩阵建立。
- 平台实施 runbook 可被新 Agent 复现。
- Agent 页面生成模板可用于后续平台页开发。
- `docs/code-map/*` 已同步。
- 后端和前端质量门禁通过，或失败原因已明确记录。

## 3. 必须先阅读

- `AGENTS.md`
- `ai-coding/tasks/phase-02/TASK-0220_platform_foundation_design_hardening.md`
- `ai-coding/tasks/phase-02/TASK-0221_platform_foundation_productization.md`
- `docs/architecture/06_项目文档与规则目录治理.md`
- `docs/architecture/01_总体架构蓝图.md`
- `docs/architecture/03_核心平台模块设计.md`
- `docs/code-map/README.md`
- `docs/code-map/backend.md`
- `docs/code-map/api.md`
- `docs/code-map/database.md`
- `docs/code-map/frontend.md`
- `ai-coding/rules/01_project_rules.md`
- `ai-coding/rules/02_backend_rules.md`
- `ai-coding/rules/03_frontend_rules.md`
- `ai-coding/rules/04_database_rules.md`
- `ai-coding/rules/05_api_rules.md`
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

- 平台基础能力 release candidate 定义。
- 发布验收闸口。
- 阻塞问题分级与修复闭环。
- 平台 API、权限、菜单、配置模板的兼容性治理。
- 设计系统和页面质量的长期治理。
- 平台基础文档归档与索引。
- 平台基础能力 smoke test 和回归脚本固化。
- 后续平台迭代 backlog 分类。

本任务不实现：

- 不进入业务领域模块开发。
- 不新增业务单据、库存、采购、生产、质量等业务流程。
- 不直接拆微服务。
- 不新增完整低代码运行时。
- 不引入新 UI 框架、ORM、工作流引擎或规则引擎。
- 不用“先发布再补权限/测试/文档”的方式绕过质量门禁。

## 5. 推荐拆分阶段

### 阶段 1：平台基础 Release Candidate 范围冻结

目标：明确平台基础能力第一版到底包含什么，不再无限追加范围。

任务：

- 定义 Platform Foundation RC1 范围：
  - 登录与租户上下文。
  - 应用外壳、菜单、导航、多标签页。
  - 组织、用户。
  - 角色、权限、菜单管理。
  - 数据权限、字段权限最小闭环。
  - 字典、参数。
  - 审计日志。
  - 主题、品牌、i18n、用户偏好。
  - 配置模板、权限矩阵、实施 runbook。
- 明确 RC1 不包含：
  - WMS/MES/SRM/QMS 业务模块。
  - 完整流程设计器增强。
  - 完整低代码运行时。
  - 商业化计费。
  - 微服务拆分。
- 为每个能力标记状态：
  - `READY`
  - `NEEDS_FIX`
  - `DEFERRED`
  - `BLOCKED`
- `DEFERRED` 项必须有原因和后续任务。

验收标准：

- RC1 范围有文档。
- 所有平台基础能力都有状态。
- 没有未归类的“顺手再做”范围。

### 阶段 2：发布验收闸口

目标：把“能不能发布”变成可执行检查，而不是主观判断。

任务：

- 建立发布验收清单：
  - 后端测试通过。
  - 前端 typecheck/lint/test/build 通过。
  - `./scripts/check-quality.sh` 通过。
  - 数据库空库 migration 可执行。
  - 初始化模板可重复执行。
  - 默认管理员可登录。
  - 菜单可加载。
  - 权限不足能被后端拒绝。
  - 审计记录可查询。
  - 主题/i18n 可加载。
  - 配置快照导入/导出按 TASK-0221 决策可用或明确延期。
- 建立 smoke test：
  - 登录。
  - 加载当前用户。
  - 加载菜单。
  - 访问用户管理。
  - 访问组织管理。
  - 访问角色管理。
  - 访问菜单管理。
  - 修改一个非敏感配置。
  - 查询审计。
- 将 smoke test 脚本纳入质量门禁或独立 runbook。

验收标准：

- 发布验收清单可执行。
- smoke test 结果可记录。
- 失败项会阻止 RC 发布，除非明确降级并记录风险。

### 阶段 3：阻塞问题分级与修复闭环

目标：建立平台基础阶段的问题处理规则，避免低价值问题阻塞发布，也避免安全和架构问题被忽略。

任务：

- 定义缺陷级别：
  - `P0`: 安全、租户串数据、无法登录、数据损坏、迁移失败。
  - `P1`: 权限绕过、核心页面不可用、配置初始化失败、质量门禁失败。
  - `P2`: 页面体验明显不一致、错误状态缺失、i18n 缺失、可访问性问题。
  - `P3`: 文案优化、轻微布局问题、非核心操作便利性。
- 建立修复策略：
  - P0/P1 必须在 RC 发布前修复。
  - P2 必须评估是否影响实施演示。
  - P3 可进入后续 backlog。
- 建立缺陷记录模板：
  - 问题描述。
  - 影响范围。
  - 复现步骤。
  - 期望行为。
  - 实际行为。
  - 严重级别。
  - owner 模块。
  - 修复任务。

验收标准：

- 所有已知问题有级别。
- P0/P1 无未处理项。
- 延期问题有后续任务和风险说明。

### 阶段 4：API、权限、菜单和配置兼容性治理

目标：后续 Agent 修改平台基础能力时，不破坏已有交付配置。

任务：

- 建立兼容性规则：
  - 已发布 API 路径不得随意改名。
  - 已发布 permission code 不得随意改名。
  - 已发布 menu code 不得随意改名。
  - 已发布 i18n key 不得随意删除。
  - 配置模板字段变更必须有兼容策略。
- 建立变更记录：
  - API 变更。
  - permission code 变更。
  - menu code 变更。
  - i18n key 变更。
  - 配置模板 schema 变更。
- 如必须破坏兼容，必须新增 ADR 或 migration/兼容脚本，并更新 runbook。

验收标准：

- 平台基础能力有兼容性治理文档。
- 破坏性变更有审批和迁移路径。
- code map 能反映当前对外契约。

### 阶段 5：设计系统长期治理

目标：防止后续平台页和业务页风格漂移。

任务：

- 建立设计系统治理文件：
  - token 使用规则。
  - 表格密度规则。
  - 表单布局规则。
  - 抽屉和弹窗规则。
  - 状态标签规则。
  - 图标规则。
  - 空状态和错误状态规则。
  - loading skeleton 规则。
- 为后续 Agent 建立检查项：
  - 不得硬编码颜色。
  - 不得硬编码中文。
  - 不得直接判断权限数组。
  - 不得绕过 table-engine/form-engine 写标准页。
  - 不得创建与现有组件重复的组件。
- 建立视觉回归方案：
  - 如当前没有自动化视觉回归，先用截图验收清单和关键页面截图归档。
  - 后续可评估 Playwright screenshot baseline，但不得阻塞当前 RC，除非已有工程基础。

验收标准：

- 设计系统治理文档可被 Agent 执行。
- 核心页面截图或验收记录已归档。
- 后续页面开发有明确风格约束。

### 阶段 6：文档归档与入口整理

目标：让其他 Agent、实施人员和评审人员能快速找到平台基础能力的权威资料。

任务：

- 整理文档入口：
  - 平台基础能力总览。
  - 平台实施 runbook。
  - 平台配置模板说明。
  - 权限矩阵。
  - 菜单模板。
  - 角色模板。
  - 设计系统验收清单。
  - API code map。
  - 数据库 code map。
  - 前端 code map。
- 在 `docs/operations/` 或合适目录新增索引文件。
- 避免同一规则多处重复维护，明确权威来源：
  - 可执行规则在 `ai-coding/rules/`。
  - 任务在 `ai-coding/tasks/`。
  - 架构说明在 `docs/architecture/`。
  - 前端规格在 `docs/frontend/`。
  - 当前实现索引在 `docs/code-map/`。

验收标准：

- 新 Agent 能从一个入口找到平台基础资料。
- 文档没有明显互相冲突。
- code map 与当前实现一致。

### 阶段 7：后续平台 Backlog 分类

目标：在平台基础 RC1 完成后，明确下一轮平台能力该怎么排，而不是直接跳业务开发。

任务：

- 建立后续 backlog 分类：
  - 平台安全增强。
  - 平台体验优化。
  - 平台配置治理。
  - 平台运维能力。
  - 平台设计器能力。
  - 平台集成能力。
  - 平台 AI coding 辅助能力。
- 每个 backlog 项必须说明：
  - 目标。
  - owner 模块。
  - 影响 API/DB/前端/权限。
  - 风险。
  - 是否阻塞业务模块进入。
- 明确哪些能力是业务模块开工前必须完成，哪些可以并行。

验收标准：

- 后续 backlog 分类清晰。
- 业务模块开工前置条件明确。
- 不再用单个大任务承接所有平台优化。

## 6. 需要新增/修改的文件

文档：

```text
docs/operations/platform-foundation-release-checklist.md
docs/operations/platform-foundation-known-issues.md
docs/operations/platform-foundation-document-index.md
docs/operations/platform-foundation-smoke-test.md
docs/frontend/17_平台管理视觉设计规范.md
docs/frontend/19_平台应用外壳与导航规范.md
docs/code-map/backend.md
docs/code-map/api.md
docs/code-map/database.md
docs/code-map/frontend.md
```

脚本，按需：

```text
scripts/check-quality.sh
scripts/platform-foundation-smoke-test.sh
```

任务：

```text
ai-coding/tasks/phase-02/...
ai-coding/tasks/frontend/...
```

如本任务发现必须修复的 P0/P1 问题，应拆成独立任务，不应混在发布治理文档中偷偷修改。

## 7. API 清单

本任务默认不新增业务 API。

如为了 smoke test 或平台验证需要新增内部验证 API，必须先评估是否真的必要。优先使用已有 API 完成验证。任何新增 API 必须：

- 使用 `/api/{module}/{resources}`。
- 使用统一 `Result<T>`。
- 有后端权限校验。
- 有租户隔离。
- 更新 `docs/code-map/api.md`。

## 8. 权限点

本任务默认不新增权限点。

如新增平台验证或发布检查 API，可考虑：

```text
platform:foundation:verify
platform:foundation:release-check
```

新增权限必须通过 migration seed 或平台配置模板授予平台管理员角色。

## 9. 数据库设计要求

本任务默认不新增数据库表。

如新增发布检查记录或验收记录表，必须包含：

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

本任务如涉及前端调整，必须遵守：

- 管理端保持信息密度和稳定布局。
- 不使用营销式 hero、装饰背景、大面积渐变。
- 颜色、间距、圆角、状态色全部来自 token。
- 所有用户可见文案使用 i18n key。
- 所有 API 经过 `packages/api-client`。
- 所有权限判断通过统一权限组件。
- 必须覆盖 loading、error、empty、permission denied 状态。
- 关键页面应支持键盘导航和可见 focus 状态。

## 11. 测试要求

必须新增或更新：

- 平台基础 smoke test。
- 后端质量门禁说明。
- 前端质量门禁说明。
- 发布验收清单。
- 已知问题清单。

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

- Platform Foundation RC1 范围已冻结。
- 发布验收清单可执行。
- smoke test 有文档或脚本。
- P0/P1 问题清零或明确阻塞发布。
- API、权限、菜单、配置模板兼容性规则明确。
- 设计系统长期治理规则明确。
- 平台基础文档入口清晰。
- 后续平台 backlog 已分类。
- 不进入业务领域。
- `docs/code-map/*` 已同步或说明无需更新。

## 13. 推荐子任务顺序

建议拆成以下任务交给 Agent：

1. `TASK: Platform Foundation RC1 scope freeze`
2. `TASK: Platform Foundation release checklist and smoke test`
3. `TASK: Platform Foundation known issue triage`
4. `TASK: Platform API permission menu compatibility governance`
5. `TASK: Platform design system governance documentation`
6. `TASK: Platform foundation document index`
7. `TASK: Platform next backlog classification`

优先级最高的第一张子任务：

```text
TASK: Platform Foundation release checklist and smoke test
```

范围：

- 输出发布验收清单。
- 输出 smoke test 步骤或脚本。
- 覆盖登录、菜单、用户、组织、角色、权限、配置、审计。
- 明确失败项如何阻塞发布。
- 更新 operations 文档入口。

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
