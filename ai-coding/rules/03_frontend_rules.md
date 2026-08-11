# 前端开发规则

本规则是 IAF 前端开发的可执行规则。详细体验、架构、组件、移动作业、离线、i18n、主题和 AI 助手规范见 `docs/frontend/`。

## 1. 必读文档

前端任务开始前必须阅读：

1. `AGENTS.md`
2. `docs/architecture/06_项目文档与规则目录治理.md`
3. 本文件：`ai-coding/rules/03_frontend_rules.md`
4. `docs/frontend/00_README.md`
5. `docs/frontend/01_前端总体架构.md`
6. 与任务相关的 `docs/frontend/*`
7. 当前 `ai-coding/tasks/frontend/TASK-FE-xxxx*.md`
8. `docs/code-map/frontend.md`
9. `docs/quality/quality_gate.md`

说明：本文件是前端唯一可执行规则源。`docs/frontend/14_Codex前端开发规则.md` 只是索引文件，不维护独立规则。

## 2. 技术栈锁定

- 包管理：pnpm workspace。
- 构建：Vite。
- 主框架：React + TypeScript。
- PC UI：Ant Design + ProComponents。
- 移动 UI：Ant Design Mobile。
- 路由：React Router。
- 服务端状态：TanStack Query。
- 全局 UI 状态：Zustand。
- 图表：ECharts。
- 国际化：i18next。
- 移动离线存储：IndexedDB + Dexie。
- PWA：Workbox / Vite PWA，按离线任务需要启用。

未经明确架构任务和 ADR 批准，不得替换这些基础框架。

## 3. Frontend Workspace

第一版前端必须使用以下结构：

```text
frontend/
  apps/
    pc-admin/
    mobile-work/
    dashboard-view/
    supplier-portal/
  packages/
    api-client/
    auth/
    permissions/
    i18n/
    theme/
    domain-types/
    ui-core/
    ui-business/
    table-engine/
    form-engine/
    lowcode-engine/
    scan-runtime/
    offline-runtime/
    ai-assistant/
```

依赖方向：

- `apps/*` 可以依赖 `packages/*`。
- `packages/*` 禁止依赖 `apps/*`。
- PC 页面放在 `apps/pc-admin/src/modules/**`。
- 移动页面放在 `apps/mobile-work/src/modules/**`。
- 共享组件、API、权限、主题、i18n、状态标签、扫码、离线、AI 助手必须放到 `packages/*`。
- PC 和移动不得写成同一个 app。

## 4. API 与状态管理

- 所有 HTTP 调用必须经过 `packages/api-client`。
- 页面不得直接使用 `axios`、`fetch` 或第三方请求库。
- 模块内使用 `api.ts`、`hooks.ts`、`types.ts` 分层；页面调用 hooks，不直接拼 URL。
- 服务端状态必须使用 TanStack Query。
- 禁止把服务端列表数据放进 Zustand。
- 页面不得直接读写 token、刷新 token 或直接读写 localStorage。
- API 错误必须走统一错误处理，识别认证过期、权限不足、业务错误、参数校验错误、后端异常、网络异常和离线提交失败。

## 5. i18n 规则

- 第一版必须支持 `zh-CN` 和 `en-US`。
- 所有用户可见文字必须使用 i18n key。
- 禁止在页面和组件中硬编码中文或英文业务文案。
- 菜单、字段、枚举、错误、低代码配置均应使用 i18n key。
- 后端返回 `messageKey` 时，前端优先用 `messageKey` 翻译；缺翻译时显示后端 message 和 code。

## 6. Theme 规则

- 必须通过 `ThemeProvider` 和 theme token 使用颜色、间距、圆角、字号。
- 禁止在业务页面中硬编码颜色值。
- 状态颜色必须来自 semantic token。
- 业务状态必须使用统一状态组件，不允许页面自行拼接颜色和文案。
- 客户 Logo、品牌名、主色、登录背景和 favicon 必须来自配置接口或主题配置，不得写死在业务组件中。

## 7. 权限与状态驱动 UI

- 前端权限隐藏只是 UX，不能替代后端权限检查。
- 页面不得直接写 `permissions.includes(...)` 判断。
- 权限控制必须通过 `PermissionGuard`、`PermissionRoute`、`PermissionButton`、`FieldPermissionWrapper` 等统一组件或 hooks。
- 按钮可用性必须同时考虑权限、业务状态、字段权限和 view mode。
- 业务动作必须通过 `BusinessActionBar`、`StateTransitionButton` 或 `useBusinessActions` 统一计算。
- 字段权限必须通过统一包装处理 `VISIBLE_EDITABLE`、`VISIBLE_READONLY`、`HIDDEN`、`MASKED`。

## 8. PC 页面规则

PC 页面类型：

- `WorkspacePage`
- `ConfigurableListPage`
- `ConfigurableFormPage`
- `BusinessDetailPage`
- `OperationPage`
- `DesignerPage`
- `ReportPage`

标准列表、表单、详情页必须优先使用 `table-engine`、`form-engine` 和标准业务组件。

所有 PC 路由页面必须接入 `TabWorkspace`：

- 路由打开时创建 Tab。
- 编辑页必须注册 dirty state。
- 关闭未保存页面必须提示。
- 业务页面不得自己实现 Tab 状态。

每个业务页面必须注册 `PageContext`，复杂业务页面必须提供 `PageAIContext`。

## 9. 移动作业规则

- 移动端服务于现场作业，第一版优先 WMS。
- 一屏只做一个核心动作。
- 扫码优先，手工输入兜底。
- 扫码输入必须通过 `packages/scan-runtime`。
- PDA、蓝牙扫码枪必须通过 `KeyboardScanAdapter` 等统一适配器处理。
- 企业微信、钉钉、飞书扫码必须通过 adapter 封装，业务页面不得直接调用 vendor JS SDK。
- 移动离线能力必须通过 `packages/offline-runtime`。
- 离线提交必须包含 `clientOperationId`、`clientDeviceId`、`clientSubmittedAt`。

## 10. 组件规则

组件分层：

- `ui-core`：纯 UI 与布局组件。
- `ui-business`：通用业务组件。
- `module-components`：模块内组件。
- `page-components`：页面组合组件。

公共组件必须具备：

- 完整 TypeScript 类型。
- i18n 支持。
- theme token 支持。
- disabled / readonly / loading 支持。
- test id 支持。
- 不直接调用业务 API，除非它是明确的 selector 组件。

禁止在业务页面重复实现已有 selector、状态标签、审批时间线、附件面板、库位树、物料选择等组件。

## 11. 简洁模式与专家模式

- 标准列表页、表单页、详情页必须预留 `ViewMode = 'simple' | 'expert'`。
- 不允许复制两套页面实现简洁/专家模式。
- 字段、按钮、查询条件、详情块和业务动作都应支持 view mode 条件。

## 12. AI Assistant 规则

- AI 助手是前端一等能力。
- 复杂业务页面必须实现 `buildPageAIContext`。
- AI 助手不得直接读取 DOM。
- AI 助手调用后端必须经过 `packages/api-client`。
- 不允许将隐藏字段、无权限字段、不可见动作或不可访问数据传给 AI 上下文。

## 13. 每个页面最低交付项

新增页面必须同时交付：

- 页面组件。
- `api.ts`。
- `hooks.ts`。
- `types.ts`。
- i18n key。
- 权限控制。
- 状态控制。
- loading / error / empty 状态。
- 基础测试。
- Story 或示例数据，按当前前端工程能力提供。
- `PageAIContext`，复杂页面必须提供。

## 14. 禁止事项

- 禁止页面直接 `axios` / `fetch`。
- 禁止硬编码中文或英文业务文案。
- 禁止硬编码颜色。
- 禁止硬编码权限判断。
- 禁止重复实现已有业务组件。
- 禁止绕过 `table-engine` / `form-engine` 写标准 CRUD 页面。
- 禁止把服务端状态放进 Zustand。
- 禁止页面直接处理 token 刷新。
- 禁止将无权限字段传给 AI 助手上下文。

## 15. 自检清单

提交前必须确认：

- `pnpm lint` 通过。
- `pnpm typecheck` 通过。
- `pnpm test` 通过。
- `pnpm build` 通过。
- `./scripts/check-quality.sh` 通过或说明不能运行的原因。
- 页面无硬编码文案。
- 页面无直接 HTTP 调用。
- 权限按钮使用统一权限组件。
- 状态标签使用统一状态组件。
- 字段权限使用统一字段权限组件。
- 标准列表支持列偏好和查询保存。
- `docs/code-map/frontend.md` 已同步更新。
