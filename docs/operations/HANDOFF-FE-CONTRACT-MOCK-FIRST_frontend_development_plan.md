# HANDOFF-FE-CONTRACT-MOCK-FIRST 前端契约优先与 Mock 优先开发计划

## 1. 计划目标

在后端业务能力尚未全部完成的情况下，继续推进 IAF 前端平台建设。

本计划要求前端按“契约优先、Mock 优先、可替换真实 API”的方式开发，避免前端脱离后端领域模型自行创造字段、状态、权限和业务语义。

本计划不是正式任务文件。Agent 执行前，应将对应阶段拆成 `ai-coding/tasks/frontend/TASK-FE-*.md`，或在已有任务文件中补充明确范围。

## 2. 必须遵守的规则

执行前必须阅读：

1. `AGENTS.md`
2. `docs/architecture/06_项目文档与规则目录治理.md`
3. `ai-coding/rules/03_frontend_rules.md`
4. `docs/frontend/00_README.md`
5. 与任务相关的 `docs/frontend/*`
6. 当前 `ai-coding/tasks/frontend/TASK-FE-*.md`
7. `docs/code-map/frontend.md`
8. `docs/quality/quality_gate.md`

关键规则：

- `ai-coding/rules/03_frontend_rules.md` 是唯一可执行前端规则源。
- 正式任务必须放在 `ai-coding/tasks/frontend/`。
- 本文件只作为 handoff 计划，不作为长期任务源。
- 后端未完成时，前端只能基于明确契约和 Mock 数据开发。
- 契约字段、权限码、状态枚举、错误码不得由页面随意创造。
- 写完代码后必须更新 `docs/code-map/frontend.md`。

## 3. 当前前端状态

已具备：

- `frontend` pnpm workspace。
- `apps/pc-admin`。
- `packages/api-client`。
- `packages/auth`。
- `packages/permissions`。
- `packages/i18n`。
- `packages/theme`。
- `packages/domain-types`。
- `packages/ui-core`。
- `packages/ui-business`。
- PC 登录、受保护路由、主布局、平台用户/组织/角色权限页面。

主要缺口：

- 根 `pnpm` 脚本在当前环境下存在 pnpm 包装器路径问题，需要治理。
- 页面未按模块拆分 `api.ts`、`hooks.ts`、`types.ts`。
- 权限按钮和菜单仍有页面内直接 `hasPermission` 判断，需要统一组件化。
- PC 工作区未实现 `TabWorkspace`、dirty state、keep-alive。
- 标准列表未使用 `table-engine`。
- 标准表单未使用 `form-engine`。
- 未建立统一 Mock API 运行时。
- 未建立前端契约文档与 Mock 数据目录。
- 未实现 `mobile-work`、`scan-runtime`、`offline-runtime`、`ai-assistant`。

## 4. 总体执行策略

### 4.1 契约优先

每个业务页面或前端能力开始前，先确定契约：

- DTO 类型放在 `frontend/packages/domain-types/src/`。
- API 方法放在模块 `api.ts` 或共享 API 包中。
- hooks 放在模块 `hooks.ts`。
- 枚举、状态、权限码必须和后端规划一致。
- 业务错误使用统一错误码，不在页面硬编码临时错误文案。
- 契约变更必须同步更新 `docs/code-map/frontend.md`，必要时更新 `docs/code-map/api.md`。

### 4.2 Mock 优先

后端未完成的 API 必须有 Mock：

- Mock 数据应模拟真实分页、权限、状态、错误码、空数据、加载失败。
- Mock 只替代网络返回，不改变页面调用方式。
- 页面仍通过 `packages/api-client` 和模块 hooks 访问数据。
- Mock 开关应由环境变量控制，例如 `VITE_IAF_MOCK_API=true`。
- Mock 不得散落在页面组件内。

推荐目录：

```text
frontend/
  packages/
    api-client/
      src/
        mock/
          MockApiAdapter.ts
          MockRouteRegistry.ts
          types.ts
    mock-data/
      platform/
      wms/
```

如果选择 MSW，需要先补 ADR 或任务说明，确认引入理由、作用范围和替换策略。

### 4.3 真实 API 可替换

Mock 和真实 API 必须共享同一前端调用入口：

```text
Page -> hooks.ts -> api.ts -> @iaf/api-client -> real HTTP or mock adapter
```

禁止：

- 页面直接 import mock 数据。
- 页面根据环境变量分支处理真实/Mock API。
- Mock 数据字段与 DTO 不一致。
- 为了 Mock 临时放宽 TypeScript 类型。

## 5. 阶段计划

## Phase FE-A 质量入口与规范收敛

目标：让当前前端工程具备稳定质量入口，并把已有平台页面收敛到当前规则。

建议拆分任务：

- `TASK-FE-0021-fix-frontend-quality-scripts`
- `TASK-FE-0022-align-platform-pages-with-rules`

交付范围：

- 修复 `frontend/package.json` 根脚本，确保 `pnpm lint`、`pnpm typecheck`、`pnpm test`、`pnpm build` 在仓库环境中可执行。
- 补充或调整工作区脚本，避免依赖本机异常 pnpm wrapper。
- 将平台用户、组织、角色页面拆分为模块目录：

```text
frontend/apps/pc-admin/src/modules/platform/users/
  api.ts
  hooks.ts
  types.ts
  UserListPage.tsx

frontend/apps/pc-admin/src/modules/platform/orgs/
  api.ts
  hooks.ts
  types.ts
  OrgTreePage.tsx

frontend/apps/pc-admin/src/modules/platform/roles/
  api.ts
  hooks.ts
  types.ts
  RoleListPage.tsx
```

- 新增或完善 `PermissionButton`、`PermissionRoute`、`PermissionMenuItem` 或等价统一权限组件。
- 页面移除直接 `hasPermission(...)` 渲染分支。
- 保持现有页面行为不变。

验收标准：

- 根 `frontend` 下质量脚本可运行。
- 现有平台页面测试通过。
- 页面不直接判断权限。
- 页面调用 hooks，不直接调用 API 方法。
- `docs/code-map/frontend.md` 已更新。

## Phase FE-B 契约与 Mock API 基础设施

目标：建立后端未完成时可持续推进前端的 Mock 运行时。

建议拆分任务：

- `TASK-FE-0023-contract-first-api-mock-runtime`
- `TASK-FE-0024-platform-mock-contract-fixtures`

交付范围：

- 在 `packages/api-client` 建立 mock adapter 能力。
- 支持按 method + path 注册 Mock handler。
- 支持分页、查询参数、路径参数。
- 支持模拟 401、403、业务错误、参数错误、网络失败。
- 建立 `mock-data` 或 `fixtures` 目录。
- 给平台用户、组织、角色权限 API 提供 Mock handler。
- 给未完成的 WMS 收货契约预留 Mock handler 结构。

建议结构：

```text
frontend/packages/api-client/src/mock/
  MockApiAdapter.ts
  MockRouteRegistry.ts
  MockResponse.ts
  createMockApiClient.ts

frontend/mock-data/platform/
  users.ts
  orgs.ts
  roles.ts
  permissions.ts

frontend/mock-data/wms/
  receiptOrders.ts
```

验收标准：

- `VITE_IAF_MOCK_API=true` 时前端不依赖后端即可打开平台页面。
- `VITE_IAF_MOCK_API=false` 时仍走真实后端代理。
- Mock 返回结构与 `Result<T>`、`PageResult<T>` 一致。
- Mock handler 有单元测试。
- `docs/code-map/frontend.md` 记录 Mock 入口和切换方式。

## Phase FE-C PC 工作区与多标签页

目标：实现符合 IAF PC 作业习惯的多标签工作区。

正式任务参考：

- `ai-coding/tasks/frontend/TASK-FE-0010-pc-layout-tabs.md`

交付范围：

```text
frontend/apps/pc-admin/src/workspace/
  TabWorkspace.tsx
  RouteTabStore.ts
  DirtyStateRegistry.ts
  KeepAliveRoute.tsx
```

功能：

- 路由打开自动创建 Tab。
- 支持关闭当前、关闭其他、关闭右侧。
- 支持固定 Tab。
- 支持刷新当前 Tab。
- 支持未保存 dirty state 关闭提醒。
- 支持最近访问恢复。

验收标准：

- 平台用户、组织、角色页面通过 TabWorkspace 打开。
- 编辑弹窗或编辑页能注册 dirty state。
- 关闭未保存页面有确认提示。
- 测试覆盖 tab 创建、关闭、dirty 拦截。

## Phase FE-D table-engine 与平台列表迁移

目标：建立标准列表能力，并迁移现有平台列表作为样板。

正式任务参考：

- `ai-coding/tasks/frontend/TASK-FE-0012-table-engine.md`

交付范围：

```text
frontend/packages/table-engine/src/
  ConfigurableListPage.tsx
  ListViewDefinition.ts
  UserListViewPreference.ts
  ColumnSettings.tsx
  SearchPanelRenderer.tsx
  useListViewPreference.ts
```

功能：

- 列显示 / 隐藏。
- 列顺序和宽度。
- 固定列。
- 查询条件保存。
- 默认视图。
- 导出字段配置。
- 操作列权限控制。

迁移范围：

- 用户列表。
- 角色列表。
- 组织列表可部分复用，但树结构保留业务组件。

验收标准：

- 平台用户和角色列表不再直接使用 Ant Design `Table`。
- 列偏好刷新后保留。
- 查询条件可保存和恢复。
- 权限操作列由统一机制计算。

## Phase FE-E form-engine 与字段权限

目标：建立标准表单能力，为 WMS 单据和平台基础资料复用。

建议拆分任务：

- `TASK-FE-0025-form-engine-field-permission`
- `TASK-FE-0026-platform-forms-migrate-to-form-engine`

交付范围：

```text
frontend/packages/form-engine/src/
  ConfigurableFormPage.tsx
  FormDefinition.ts
  FieldPermissionWrapper.tsx
  ViewModeFieldResolver.ts
  FormSectionRenderer.tsx
```

功能：

- 表单 schema 渲染。
- 字段权限：`VISIBLE_EDITABLE`、`VISIBLE_READONLY`、`HIDDEN`、`MASKED`。
- 简洁 / 专家模式字段控制。
- loading、readonly、disabled。
- 表单校验和错误显示。

验收标准：

- 用户、组织、角色的创建/编辑表单可迁移或具备迁移样板。
- 字段权限测试通过。
- 页面不直接写字段权限分支。

## Phase FE-F i18n、Theme、ViewMode 深化

目标：把基础包从“可用”提升到“可扩展”。

正式任务参考：

- `ai-coding/tasks/frontend/TASK-FE-0006-i18n.md`

建议补充任务：

- `TASK-FE-0027-theme-token-and-switcher`
- `TASK-FE-0028-view-mode-foundation`

交付范围：

- i18n 资源拆为 `common`、`platform`、`wms`、`errors`。
- 支持语言切换。
- 缺失 key 有 fallback。
- theme token 拆为基础 token、语义 token、状态 token。
- 支持主题切换和品牌配置预留。
- 建立 `ViewMode = 'simple' | 'expert'` 基础类型与 resolver。

验收标准：

- PC 页面无硬编码文案。
- 状态颜色来自 semantic token。
- 简洁 / 专家模式不复制页面。

## Phase FE-G 移动扫码与离线运行时

目标：在后端未完成时先建立移动作业基础能力。

正式任务参考：

- `ai-coding/tasks/frontend/TASK-FE-0015-scan-runtime.md`

建议补充任务：

- `TASK-FE-0029-mobile-work-shell`
- `TASK-FE-0030-offline-runtime-foundation`

交付范围：

```text
frontend/apps/mobile-work/

frontend/packages/scan-runtime/src/
  ScanService.ts
  ScanDeviceAdapter.ts
  CameraScanAdapter.ts
  KeyboardScanAdapter.ts
  BluetoothScanAdapter.ts
  PdaNativeScanAdapter.ts
  WeComScanAdapter.ts
  DingTalkScanAdapter.ts
  FeishuScanAdapter.ts
  ScanParser.ts
  ScanActionRouter.ts
  types.ts

frontend/packages/offline-runtime/src/
  OfflineQueue.ts
  OfflineSubmissionStore.ts
  SyncStateMachine.ts
  Idempotency.ts
  types.ts
```

功能：

- 移动应用壳和路由。
- 键盘扫码模拟。
- 扫码类型解析：物料、库位、任务、单据。
- 手工输入兜底。
- 离线队列。
- `clientOperationId`、`clientDeviceId`、`clientSubmittedAt`。
- 同步状态 UI。

验收标准：

- 不直接调用 vendor JS SDK。
- mobile-work 可用测试输入模拟扫码。
- 离线提交可进入队列并显示同步状态。

## Phase FE-H AI Assistant 前端框架

目标：建立页面上下文和 AI 助手面板，但不依赖后端 AI 能力完成。

建议拆分任务：

- `TASK-FE-0031-ai-assistant-page-context`

交付范围：

```text
frontend/packages/ai-assistant/src/
  PageAIContext.ts
  AssistantPanel.tsx
  usePageAIContext.ts
  sanitizeAIContext.ts
```

功能：

- 页面注册 `PageAIContext`。
- AI 助手不得直接读取 DOM。
- 过滤隐藏字段、无权限字段、不可见动作。
- 支持 Mock AI 响应。

验收标准：

- 平台用户或 WMS 收货样板页面能输出 `PageAIContext`。
- 无权限字段不会进入 AI 上下文。
- 有测试覆盖权限过滤。

## Phase FE-I WMS 收货 PC 样板

目标：基于契约和 Mock 先实现 WMS 收货 PC 样板，后端完成后替换真实 API。

正式任务参考：

- `ai-coding/tasks/frontend/TASK-FE-0018-wms-receipt-pc.md`

交付范围：

```text
frontend/apps/pc-admin/src/modules/wms/receipt/
  ReceiptOrderListPage.tsx
  ReceiptOrderFormPage.tsx
  ReceiptOrderDetailPage.tsx
  api.ts
  hooks.ts
  types.ts
  i18n.ts
  mock.ts
```

契约必须覆盖：

- 收货单主表。
- 收货单明细。
- 单据状态。
- 审批状态。
- 执行状态。
- 操作日志。
- 附件摘要。
- 可执行动作。

权限点：

```text
wms:receipt:view
wms:receipt:create
wms:receipt:update
wms:receipt:submit
wms:receipt:confirm
wms:receipt:cancel
wms:receipt:export
```

验收标准：

- 列表走 `table-engine`。
- 表单走 `form-engine`。
- 权限按钮使用统一权限组件。
- 页面提供 `PageAIContext`。
- Mock 可覆盖列表、详情、保存草稿、提交、确认、取消。

## Phase FE-J WMS 移动收货与上架样板

目标：验证移动扫码、离线和 WMS 作业体验。

正式任务参考：

- `TASK-FE-0019` WMS 收货移动页面，需补正式任务文件。
- `TASK-FE-0020` WMS 上架移动页面，需补正式任务文件。

交付范围：

```text
frontend/apps/mobile-work/src/modules/wms/receipt/
frontend/apps/mobile-work/src/modules/wms/putaway/
```

功能：

- 收货任务列表。
- 扫码收货。
- 异常数量录入。
- 上架任务列表。
- 扫码库位。
- 离线暂存。
- 同步结果反馈。

验收标准：

- 一屏只做一个核心动作。
- 扫码优先，手工输入兜底。
- 离线提交带幂等字段。
- 页面不直接调用后端未定义 API。

## 6. 契约文档要求

每个后端未完成但前端先开发的业务模块，必须补充契约说明。

建议位置：

```text
docs/module-specs/<module>/
docs/code-map/api.md
docs/code-map/frontend.md
```

最小内容：

- API 路径。
- 请求 DTO。
- 响应 DTO。
- 权限点。
- 状态枚举。
- 错误码。
- Mock 场景。
- 后端对接注意事项。

## 7. 推荐执行顺序

1. Phase FE-A：质量入口与规范收敛。
2. Phase FE-B：契约与 Mock API 基础设施。
3. Phase FE-C：PC 工作区与多标签页。
4. Phase FE-D：table-engine 与平台列表迁移。
5. Phase FE-E：form-engine 与字段权限。
6. Phase FE-F：i18n、Theme、ViewMode 深化。
7. Phase FE-I：WMS 收货 PC 样板。
8. Phase FE-G：移动扫码与离线运行时。
9. Phase FE-J：WMS 移动收货与上架样板。
10. Phase FE-H：AI Assistant 前端框架。

说明：AI Assistant 可提前建立基础类型，但建议在权限、字段权限和页面上下文稳定后再接入业务页面。

## 8. 每个任务的完成定义

每个正式任务完成时必须满足：

- 代码符合 `ai-coding/rules/03_frontend_rules.md`。
- 页面不直接 HTTP 调用。
- 页面不直接写权限判断。
- 用户可见文案使用 i18n。
- 颜色和状态样式来自 theme 或状态组件。
- 后端未完成的 API 有 Mock handler。
- Mock handler 与 DTO 类型一致。
- 新增或修改测试。
- 更新 `docs/code-map/frontend.md`。
- 如新增 API 契约，更新 `docs/code-map/api.md` 或模块规格。
- 运行前端质量门，失败时修复或记录明确原因。

## 9. 风险与控制

| 风险 | 控制方式 |
|---|---|
| 前端自行创造业务字段 | 先写 DTO 和契约，必要时同步模块规格 |
| Mock 与真实 API 偏离 | Mock handler 复用 `domain-types`，对接后不得改页面调用方式 |
| 页面先快写导致规则失效 | 每阶段先补组件/engine，再迁移页面 |
| WMS 业务语义不清 | 只先做契约样板和交互壳，不实现库存业务规则 |
| 权限和字段权限遗漏 | 所有动作统一走权限组件和 action resolver |
| 离线提交后端未就绪 | 离线队列只模拟同步结果，真实提交接口单独对接 |

## 10. 交给 Agent 的第一批建议任务

优先交付以下正式任务：

1. `TASK-FE-0021-fix-frontend-quality-scripts`
2. `TASK-FE-0022-align-platform-pages-with-rules`
3. `TASK-FE-0023-contract-first-api-mock-runtime`
4. `TASK-FE-0010-pc-layout-tabs`
5. `TASK-FE-0012-table-engine`

其中 `TASK-FE-0021`、`TASK-FE-0022`、`TASK-FE-0023` 需要先新建正式任务文件到 `ai-coding/tasks/frontend/`，再交给执行 agent。
