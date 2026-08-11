# TASK-0217 平台页面视觉基线与样式收敛

## 1. 任务目标

在 TASK-0213 Shell 视觉硬化和 TASK-0214 登录模板/主题状态工作之后，建立 PC 管理端“登录后平台页面”的视觉基线，并完成第一轮样式收敛。

本任务的结果不是新增业务能力，而是让平台管理端关键页面达到可长期使用的企业后台质量：

- 页面结构一致。
- 信息层级清楚。
- 表格、筛选、工具栏、表单、抽屉、弹窗、状态标签可读。
- light/dark 主题下核心页面不出现弱对比、错位、横向溢出、卡片套卡片混乱。
- 视觉问题有可追踪清单和 Playwright 回归检查。

## 2. 前置依赖

本任务开始实现前，应先确认以下分支已经合并到 `main`，或当前工作分支已经基于包含这些改动的最新分支：

- `feature/TASK-0214-theme-status-login`
  - 提供登录模板、登录视觉 token、品牌配置入口、登录页视觉 E2E。
- `feature/TASK-PR-merge-rule`
  - 提供任务分支必须通过 Pull Request 进入 `main` 的 agent 协作规则。

如果任一前置分支尚未合并，implementation agent 必须在计划中说明基线分支来源，不得静默基于旧 `main` 实现。

## 3. 必须先阅读

- `AGENTS.md`
- `CLAUDE.md`
- `docs/architecture/06_项目文档与规则目录治理.md`
- `docs/code-map/README.md`
- `docs/code-map/frontend.md`
- `ai-coding/rules/03_frontend_rules.md`
- `ai-coding/rules/10_testing_rules.md`
- `ai-coding/rules/11_code_quality_rules.md`
- `ai-coding/rules/12_code_map_rules.md`
- `ai-coding/rules/13_agent_parallel_work_rules.md`
- `docs/frontend/00_README.md`
- `docs/frontend/02_PC端布局与交互规范.md`
- `docs/frontend/03_PC多标签页与工作台规范.md`
- `docs/frontend/06_多主题设计规范.md`
- `docs/frontend/08_权限与状态驱动UI规范.md`
- `docs/frontend/10_组件体系规范.md`
- `docs/frontend/16_平台管理页面交互规范.md`
- `docs/frontend/17_平台管理视觉设计规范.md`
- `docs/frontend/19_平台应用外壳与导航规范.md`
- `docs/quality/quality_gate.md`
- `ai-coding/tasks/phase-02/TASK-0207_platform_theme_i18n_branding.md`
- `ai-coding/tasks/phase-02/TASK-0213_platform_shell_visual_interaction_hardening.md`

## 4. 业务范围

本任务实现：

- 对 PC 管理端关键页面做视觉审查，形成可追踪问题清单。
- 修复第一轮 Blocking/Major 视觉问题。
- 统一标准平台页面的标题区、筛选区、工具栏、表格区、分页区、空态、错误态、加载态。
- 统一 Drawer、Modal、独立页面模式下的表单容器、标题、底部操作区、滚动区域和宽度规则。
- 统一 light/dark 主题下的控件默认、hover、active、selected、focus、disabled、danger 状态可读性。
- 补齐 Playwright 视觉基线检查，覆盖关键页面、核心主题和常用视口。
- 更新 `docs/code-map/frontend.md`，记录视觉测试入口、主题 token 和页面样式基线变化。
- 产出视觉审查记录，建议文件：`docs/operations/FRONTEND_VISUAL_AUDIT.md`。

本任务不实现：

- 新增业务模块或新业务 API。
- 后端菜单、权限、审批、Kanban、流程设计器、大屏设计器业务能力。
- 引入新的 UI 框架、CSS 框架或视觉系统。
- 完整设计系统重构。
- 大量提交截图二进制产物。
- 把所有未来主题一次性做完。

## 5. 需要新增/修改的文件

前端应用：

```text
frontend/apps/pc-admin/src/layouts/MainLayout.tsx
frontend/apps/pc-admin/src/global.css
frontend/apps/pc-admin/src/pages/WorkbenchPage.tsx
frontend/apps/pc-admin/src/modules/platform/users/UserListPage.tsx
frontend/apps/pc-admin/src/modules/platform/orgs/OrgTreePage.tsx
frontend/apps/pc-admin/src/modules/platform/roles/RoleListPage.tsx
frontend/apps/pc-admin/src/modules/platform/menus/PlatformMenuConsolePage.tsx
frontend/apps/pc-admin/src/modules/platform/config/PlatformConfigPages.tsx
frontend/apps/pc-admin/src/modules/platform/approval/ApprovalTaskCenterPage.tsx
frontend/apps/pc-admin/src/modules/platform/kanban/PlatformKanbanPage.tsx
```

前端包：

```text
frontend/packages/theme/src/index.tsx
frontend/packages/theme/src/themeDefaults.test.ts
frontend/packages/ui-core/src/index.tsx
frontend/packages/ui-core/src/statusTags.test.tsx
frontend/packages/table-engine/src/
frontend/packages/form-engine/src/
frontend/packages/i18n/src/index.ts
```

测试：

```text
frontend/e2e/platform-shell.spec.ts
frontend/e2e/platform-pages-visual.spec.ts
frontend/e2e/login-templates.spec.ts
frontend/apps/pc-admin/src/**/*.test.tsx
frontend/packages/**/*.test.tsx
```

文档：

```text
docs/code-map/frontend.md
docs/operations/FRONTEND_VISUAL_AUDIT.md
```

按实际影响修改，不要求机械触碰全部文件。

## 6. 数据库设计

本任务不新增数据库表，不修改现有 Flyway migration。

如发现必须新增用户偏好字段，优先复用 `sys_user_experience_preference.settings` JSON 配置；只有需要后端强约束或查询索引时才允许新增 migration，并必须单独说明风险、更新 `docs/code-map/database.md`。

## 7. 后端设计

默认不修改后端。

如视觉基线发现当前 mock 与真实 API 合同差异导致页面无法稳定测试，应优先修正前端 mock 或 API client 类型；不得为视觉任务绕过认证、权限或统一响应结构。

## 8. API 清单

本任务不新增 API。

可复用接口：

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/platform/auth/menus` | Shell 菜单与路由入口 |
| GET | `/api/platform/preferences/me` | 当前用户体验偏好 |
| PUT | `/api/platform/preferences/me` | 保存当前用户体验偏好 |
| GET | `/api/platform/theme/current` | 当前主题配置 |
| GET | `/api/platform/brand/current` | 当前品牌配置 |

如新增 API，必须先更新本任务文档、`docs/code-map/api.md`，并补充权限点。

## 9. Code Map 更新计划

必须更新：

- `docs/code-map/frontend.md`

按实际影响更新：

- `docs/code-map/api.md`
- `docs/code-map/backend.md`
- `docs/code-map/database.md`

如果只修改样式 token、测试或文档，仍需要在 `docs/code-map/frontend.md` 记录视觉测试入口、主题 token、页面骨架或关键组件行为变化。

## 10. 前端设计

### 10.1 视觉审查范围

至少覆盖：

- `/login?loginTemplate=standard-industrial`
- `/login?loginTemplate=cyber-ai`
- `/login?loginTemplate=immersive-glass`
- `/login?loginTemplate=minimal-technical`
- `/login?loginTemplate=bento-dashboard`
- `/`
- `/platform/users`
- `/platform/orgs`
- `/platform/roles`
- `/platform/menus`
- `/platform/dictionaries`
- `/platform/audit-logs`
- `/platform/approval/tasks`
- `/platform/kanban`

至少覆盖主题：

- `light-industrial`
- `dark-industrial`

至少覆盖视口：

- `1366x768`
- `1440x900`
- `1920x1080`
- `390x844`

### 10.2 视觉问题分级

Blocking：

- 页面主流程无法访问。
- 关键文字不可读或对比度明显不足。
- 主按钮、菜单、表单 label、输入内容、状态标签不可辨认。
- 移动端或常用桌面视口出现横向溢出。
- Drawer/Modal/表单遮挡主操作或无法提交。
- 暗色主题出现浅色文字压浅色背景、深色文字压深色背景。

Major：

- 页面结构与同类页面明显不一致。
- 标题区、筛选区、工具栏、表格间距混乱。
- 表格 hover/selected/fixed column 状态不清楚。
- 状态色与语义不一致或过度装饰。
- 空态、错误态、加载态缺失或视觉跳变明显。
- 表单模式 drawer/modal/page 在同类页面表现不一致。

Minor：

- 字号、间距、阴影、圆角、图标对齐等局部不一致。
- 视觉质感不足但不影响使用。
- 动效缺失或不够顺滑。

### 10.3 页面骨架收敛

标准平台页面应收敛为：

```text
页面标题区
查询 / 筛选区
工具栏
主体表格 / 树表 / Kanban / 配置区
分页 / 辅助信息区
```

规则：

- 页面标题区不做营销式 hero。
- 查询区默认显示高频条件，避免低频条件铺满首屏。
- 工具栏主操作明确，次操作克制。
- 表格区域边界清楚，避免卡片套卡片。
- 列表页默认紧凑但不能牺牲可读性。
- 禁止业务页面直接硬编码颜色；新增颜色必须进入 `@iaf/theme` token 或说明为何只能局部 CSS 覆盖。

### 10.4 表单与浮层收敛

必须检查：

- Drawer 默认宽度、最大宽度、移动端降级。
- Modal 默认宽度、底部按钮位置。
- 独立页面模式是否隐藏列表主体，避免列表和表单挤在同一主体区。
- 表单 label、必填标记、错误提示、help 文案、disabled/readOnly 状态。
- 长表单滚动只发生在内容区域，底部操作区固定。

### 10.5 主题 token 收敛

重点检查并补齐 token：

- Shell：sidebar、topbar、tab、resize handle、menu hover/selected/focus。
- Surface：页面背景、容器背景、抬升层、分割线。
- Text：正文、弱文本、禁用文本、反色文本。
- Action：主按钮、次按钮、danger、link、disabled。
- Form：label、control background、border、placeholder、focus ring、error。
- Table：header、row hover、selected、fixed column shadow、empty。
- Status：draft、pending、approved、rejected、enabled、disabled、warning。

### 10.6 Playwright 视觉基线

新增或增强：

```text
frontend/e2e/platform-pages-visual.spec.ts
```

最低断言：

- 页面根容器可见。
- 关键标题、菜单、主按钮、表单输入、表格或主体区可见。
- `document.documentElement.scrollWidth - clientWidth <= 1`。
- 截图 buffer 大小大于合理阈值，避免空白页。
- light/dark 下关键文字 computed color 与背景不能明显同色。
- 登录模板、Shell、平台页面均能在 mock 模式下访问。

截图产物不得提交；如 Playwright 生成 `frontend/test-results/`，任务结束前必须清理。

## 11. 权限点

本任务默认不新增权限点。

页面访问和按钮显示继续使用已有权限：

```text
platform:user:view
platform:user:create
platform:user:update
platform:org:view
platform:org:create
platform:org:update
platform:role:view
platform:role:create
platform:role:update
platform:menu:view
platform:menu:create
platform:menu:update
platform:theme:view
platform:brand:view
platform:i18n:view
platform:preference:me
```

如果新增页面或诊断入口，必须先补权限设计、后端鉴权和前端权限守卫。

## 12. 业务规则

- 前端视觉优化不得绕过权限、状态、字段权限或 view mode。
- 前端隐藏不等于权限控制，敏感读写仍以后端权限为准。
- 标准平台页面不得绕过 `table-engine` / `form-engine` 重写 CRUD 结构。
- HTTP 调用必须经过 `packages/api-client`。
- 用户可见文字必须使用 i18n key。
- 业务页面不得硬编码中文、英文、权限码判断、状态颜色、客户品牌信息。
- 状态标签必须使用统一状态组件或 theme semantic token。
- 视觉增强不得牺牲信息密度、键盘可达性、可读性或长时间使用舒适度。
- Dark theme 不是简单反色；必须独立检查背景、容器、边框、文字和状态色对比。

## 13. 测试要求

必须新增或更新：

- `frontend/e2e/platform-pages-visual.spec.ts`
  - 覆盖关键平台页面、light/dark 主题、桌面和移动视口。
- `frontend/e2e/login-templates.spec.ts`
  - 如 TASK-0214 已合并，保持 5 个登录模板覆盖。
- `frontend/packages/theme/src/themeDefaults.test.ts`
  - 覆盖新增或变更的 theme token。
- 相关页面或组件单元测试
  - 如果修改 `MainLayout`、`FormInteractionSurface`、`ConfigurableListPage`、状态标签或表格组件，必须补对应测试。

必须运行：

```bash
./scripts/check-quality.sh
```

建议额外运行：

```bash
cd frontend
bash ../scripts/run-frontend-checks.sh typecheck
bash ../scripts/run-frontend-checks.sh test
bash ../scripts/run-frontend-checks.sh build
./node_modules/.bin/playwright test
```

如环境无法运行 Playwright，需要在最终报告写明原因，并至少提供可复现的手动检查 URL 和视口清单。

## 14. 验收标准

- `docs/operations/FRONTEND_VISUAL_AUDIT.md` 已列出审查范围、问题分级、修复状态和遗留风险。
- Blocking 视觉问题全部修复。
- Major 问题已修复，或明确拆分为后续任务并说明原因。
- 登录页、Shell、用户、组织、角色、菜单、配置、审批任务、Kanban 页面在 light/dark 主题下可读。
- 1366、1440、1920、390 视口无关键横向溢出。
- Drawer、Modal、独立页模式表现一致。
- 关键按钮、表单 label、placeholder、状态标签、菜单选中态对比清晰。
- 没有新增页面级硬编码颜色；必要 CSS override 已限定 scope，并说明不能 token 化的原因。
- `docs/code-map/frontend.md` 已更新。
- `./scripts/check-quality.sh` 通过。
- PR 已由独立 review agent 审核，无 Blocking issue 后才可合并。

## 15. Agent 分工与 PR 控制

Implementation Agent：

- 只允许在 `feature/TASK-0217-platform-page-visual-baseline` 或等价任务分支工作。
- 负责视觉审查、修复、测试、文档和 PR 创建。
- 不允许合并 `main`。
- 最终报告必须提供 PR 地址、测试结果、视觉审查摘要和已知风险。

Review Agent：

- 只 review PR diff。
- 按 Blocking / Major / Minor 输出问题。
- 必须重点检查 contrast、theme token、硬编码颜色、i18n、权限守卫、测试覆盖和 code map。
- 不允许直接修改代码或合并。

Integration Agent 或仓库负责人：

- 只在 PR review 通过、CI/质量门通过、分支不落后 `main` 后执行合并。
- 默认使用 Squash merge。
- 合并后删除远端任务分支和本地 worktree。

## 16. Codex 执行要求

- 必须在独立 worktree 中开发。
- 分支名建议：`feature/TASK-0217-platform-page-visual-baseline`。
- 开发前输出影响范围、风险和实现计划。
- 先做视觉审查和问题清单，再做修复，避免无目标改样式。
- 修改公共 token 或公共组件前，必须检查调用方并补回归测试。
- 不得与其他 agent 并行改同一页面时直接覆盖对方改动。
- 提交前清理 Playwright 产物、构建产物和临时截图。
- 完成后 push 任务分支并创建 PR，不得直接合并 `main`。
- 最终报告必须包含：Summary、Files changed、Architecture impact、Database migration impact、Permission impact、Code map impact、Tests run and results、Quality gate status、Known risks、Suggested next steps，以及 `docs/quality/quality_gate.md` checklist。
