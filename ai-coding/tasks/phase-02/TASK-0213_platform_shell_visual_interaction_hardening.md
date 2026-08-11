# TASK-0213 平台 Shell 视觉与交互硬化

## 1. 任务目标

在 TASK-0204 和 TASK-0207 的基础上，继续打磨 IAF PC 管理端外壳、主题、侧栏、用户档案、抽屉/弹窗/独立页面交互，让平台管理端同时具备“企业后台专业工具风”和“工业制造感 / 科技感”，并形成可被后续菜单、权限、审批、Kanban、流程设计器复用的稳定 Shell 基座。

本任务不是新增业务能力任务，而是平台前端体验与基础组件硬化任务。完成后，用户/组织/角色/配置/审批任务中心/平台 Kanban 示例页面应作为后续平台页面的交互样板。

## 2. 必须先阅读

- `AGENTS.md`
- `ai-coding/rules/03_frontend_rules.md`
- `ai-coding/rules/12_code_map_rules.md`
- `ai-coding/rules/13_agent_parallel_work_rules.md`
- `docs/code-map/README.md`
- `docs/code-map/frontend.md`
- `docs/frontend/00_README.md`
- `docs/frontend/02_PC端布局与交互规范.md`
- `docs/frontend/03_PC多标签页与工作台规范.md`
- `docs/frontend/06_多主题设计规范.md`
- `docs/frontend/08_权限与状态驱动UI规范.md`
- `docs/frontend/10_组件体系规范.md`
- `docs/frontend/16_平台管理页面交互规范.md`
- `docs/frontend/17_平台管理视觉设计规范.md`
- `docs/frontend/18_复杂视图与设计器规范.md`
- `docs/frontend/19_平台应用外壳与导航规范.md`
- `ai-coding/tasks/phase-02/TASK-0204_platform_ux_interaction_design_system.md`
- `ai-coding/tasks/phase-02/TASK-0207_platform_theme_i18n_branding.md`

## 3. 业务范围

本任务实现：

- 修正浅色主题下左侧菜单可读性问题，确保默认、悬停、选中、禁用、折叠状态均有足够对比度。
- 深色主题与浅色主题使用同一套 Shell token 能力，交互动效、选中态、边框、阴影、背景层级保持一致，仅色彩语义不同。
- 侧栏具备稳定布局：视口高度固定，内容滚动只发生在菜单区，底部用户档案入口始终固定在可视区域内。
- 侧栏支持搜索、折叠、拖拽宽度、宽度持久化，并受用户偏好控制。
- 右上或左下用户档案入口具备统一菜单：个人信息、个人偏好、主题/密度/语言入口、退出登录。
- 抽屉、弹窗、独立页的宽度和交互规则统一由用户偏好或页面元数据决定，不在业务页面散落硬编码。
- 新增/编辑打开时，列表上下文处理规则明确：抽屉/弹窗保留列表，独立页隐藏列表并通过 TabWorkspace/路由返回。
- 视觉 token 覆盖 Shell、侧栏、顶部栏、页面标题区、查询区、工具栏、表格、表单容器、抽屉、弹窗、状态标签、操作按钮。
- 安装并接入 Playwright 视觉检查，至少覆盖登录页、工作台、用户列表、角色列表、配置页面、Kanban 示例在 light/dark 主题下的关键截图。

本任务不实现：

- 菜单管理后端模型和动态菜单 API，该能力属于 TASK-0205。
- Kanban 后端模型和真实拖拽持久化，该能力属于 TASK-0211。
- 流程设计器和审批流设计器，该能力属于 TASK-0209。
- 大屏设计器，该能力属于 TASK-0212。
- WMS 业务功能。

## 4. 需要新增/修改的文件

前端应用：

```text
frontend/apps/pc-admin/src/layouts/MainLayout.tsx
frontend/apps/pc-admin/src/global.css
frontend/apps/pc-admin/src/pages/
frontend/apps/pc-admin/src/modules/platform/*/
```

前端包：

```text
frontend/packages/theme/src/index.tsx
frontend/packages/ui-core/src/index.tsx
frontend/packages/table-engine/src/
frontend/packages/form-engine/src/
frontend/packages/i18n/src/index.ts
```

测试与工具：

```text
frontend/apps/pc-admin/src/**/*.test.tsx
frontend/e2e/
frontend/playwright.config.ts
frontend/package.json
frontend/pnpm-lock.yaml
```

文档：

```text
docs/code-map/frontend.md
docs/frontend/16_平台管理页面交互规范.md
docs/frontend/17_平台管理视觉设计规范.md
docs/frontend/19_平台应用外壳与导航规范.md
```

## 5. 数据库设计

本任务不新增数据库表。

如发现必须新增用户偏好字段，应优先复用 TASK-0207 已提供的 `sys_user_experience_preference.settings` JSON 配置，不新增迁移。只有当后端需要强约束或查询索引时，才允许新增 Flyway migration，并必须更新 `docs/code-map/database.md`。

## 6. 后端设计

默认不修改后端。

如前端偏好保存需要新增语义接口，应在 `iaf-platform-system` 中新增 application service 方法和 controller API，并使用已有权限 `platform:preference:me`；不得绕过统一 `Result<T>`、租户上下文、权限切面。

## 7. API 清单

优先复用现有 API：

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/platform/theme/current` | 获取当前租户主题配置 |
| GET | `/api/platform/brand/current` | 获取当前租户品牌配置 |
| GET | `/api/platform/preferences/me` | 获取当前用户体验偏好 |
| PUT | `/api/platform/preferences/me` | 保存当前用户体验偏好 |

本任务不应新增业务 API。确需新增时，必须先更新本任务文档和 code map。

## 8. Code Map 更新计划

必须更新：

- `docs/code-map/frontend.md`

按实际影响更新：

- `docs/code-map/api.md`
- `docs/code-map/backend.md`
- `docs/code-map/database.md`

如果只修改样式 token、测试或文档，仍需在 `docs/code-map/frontend.md` 记录 Shell、主题、偏好、视觉测试入口的变化。

## 9. 前端设计

### 9.1 Shell 布局

- 页面根容器高度必须锁定为视口高度，避免右侧抽屉或长表单撑高左侧侧栏。
- 侧栏分为品牌区、搜索区、菜单滚动区、用户档案区。
- 菜单滚动区独立滚动，用户档案区固定在侧栏底部。
- 顶部栏承载全局动作，避免堆放业务页面操作。
- TabWorkspace 区域独立管理页面上下文和 dirty state。

### 9.2 主题与配色

- `light-industrial` 以清晰浅色企业后台为基底，工业感通过深色侧栏、克制的青蓝/钢蓝强调色、状态色和信息密度体现。
- `dark-industrial` 以深色工作台为基底，但仍需保证表格、表单、弹窗、抽屉在长时间使用下可读。
- 禁止只调整局部 hover 态导致默认态不可读。
- 所有主题必须覆盖默认、hover、active、selected、focus、disabled、danger 状态。

### 9.3 用户偏好

偏好项至少覆盖：

- 主题：`light-industrial` / `dark-industrial`
- 侧栏模式：`dark` / `light`
- 侧栏宽度
- 侧栏折叠
- 页面密度
- 字号
- 动效强度
- 表单交互模式：`drawer` / `modal` / `page`
- 表单容器宽度：`standard` / `wide` / `fullscreen`
- 工作台模式：`simple` / `expert`

偏好保存规则：

- 登录后读取后端偏好。
- 修改偏好时优先保存后端，失败时保存本地并给出提示。
- 重置偏好必须同时重置本地和后端，不能只重置本地。

### 9.4 抽屉、弹窗、独立页

- 抽屉默认宽度不低于 `720px`，复杂表单使用 `960px` 或 `min(90vw, 1080px)`。
- 弹窗默认宽度不低于 `640px`，复杂配置弹窗不低于 `800px`。
- 独立页用于复杂编辑、流程设计器、Kanban 配置、大屏编辑器等需要完整工作区的场景。
- 抽屉/弹窗打开时保留列表上下文。
- 独立页打开时列表不应与编辑画布同时拥挤显示，应通过路由或 TabWorkspace 切换。

### 9.5 视觉验收

必须用 Playwright 截图检查：

- 登录页品牌区与表单区。
- 工作台首页。
- 用户列表页。
- 角色列表页。
- 平台配置页。
- Kanban 示例页。
- light/dark 两个主题。
- 1366x768、1440x900、1920x1080、390x844 视口。

截图不要求提交大体积二进制产物，但测试代码、断言和运行说明必须提交。

## 10. 权限点

本任务默认不新增权限点。

涉及页面访问时继续使用已有权限：

```text
platform:user:view
platform:org:view
platform:role:view
platform:theme:view
platform:brand:view
platform:i18n:view
platform:preference:me
```

## 11. 业务规则

- 前端隐藏不等于权限控制，敏感读写仍以后端权限为准。
- Shell 和主题配置不得硬编码客户品牌信息；品牌名、Logo、登录背景优先来自品牌配置。
- 页面交互模式不得由单个业务页面私自决定，应由主题/用户偏好/页面元数据共同决定。
- 不得为“好看”牺牲信息密度、可读性和后台操作效率。
- 不得引入新的 UI 框架。继续基于 React、TypeScript、Ant Design / ProComponents、现有 `@iaf/*` 包。

## 12. 测试要求

必须新增或更新：

- `MainLayout` 单元测试：菜单可见性、偏好保存/重置、侧栏折叠或宽度行为。
- `theme` 包测试：默认偏好、偏好合并、主题 token 输出。
- `table-engine` 或平台页面测试：表单交互模式对 drawer/modal/page 的影响。
- Playwright smoke/visual tests：关键页面在 light/dark 主题下不空白、不重叠、菜单可读、用户档案固定在视口内。

必须运行：

```bash
./scripts/check-quality.sh
```

如果新增 Playwright：

```bash
cd frontend
pnpm exec playwright install
pnpm exec playwright test
```

## 13. 验收标准

- 浅色主题左侧菜单默认态、hover 态、选中态均清晰可读。
- 深色主题与浅色主题交互行为一致，不出现只有浅色主题被打磨的情况。
- 右侧长抽屉/长页面不再撑高左侧侧栏；用户档案入口始终在可视区域内。
- 侧栏搜索、折叠、拖拽宽度、底部用户档案入口可用，并能保存偏好。
- 抽屉/弹窗宽度符合规范，复杂表单不再显得拥挤。
- 新增/编辑的 drawer/modal/page 行为由偏好或页面元数据驱动。
- Playwright 覆盖关键页面和 light/dark 主题。
- `./scripts/check-quality.sh` 通过。
- `docs/code-map/frontend.md` 已更新。

## 14. Codex 执行要求

- 必须在独立 worktree 中开发，分支名建议：`feature/TASK-0213-platform-shell-visual-hardening`。
- 开发前输出影响范围和实现计划。
- 不得与 TASK-0205、TASK-0209、TASK-0211、TASK-0212 并行改同一页面时直接互相覆盖。
- 若必须改公共组件，先确认调用方并补回归测试。
- 完成后提交并推送 feature 分支，由独立 reviewer 审核后再合并。
- 最终报告必须包含：变更摘要、文件清单、架构影响、数据库影响、权限影响、code map 影响、测试结果、质量门禁状态、已知风险。
