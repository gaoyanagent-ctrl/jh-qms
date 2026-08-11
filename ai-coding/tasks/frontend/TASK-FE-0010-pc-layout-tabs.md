# TASK-FE-0010 建立 PC 布局与多标签页

## 1. 任务目标

建立 PC 端主布局和多标签页工作区，支持多个单据和页面并行工作。

## 2. 交付范围

新增：

```text
apps/pc-admin/src/layouts/MainLayout.tsx
apps/pc-admin/src/workspace/TabWorkspace.tsx
apps/pc-admin/src/workspace/RouteTabStore.ts
apps/pc-admin/src/workspace/DirtyStateRegistry.ts
apps/pc-admin/src/workspace/KeepAliveRoute.tsx
```

## 3. 功能要求

- 路由打开时自动创建 Tab。
- 支持关闭当前 / 关闭其他 / 关闭右侧。
- 支持固定 Tab。
- 支持刷新当前 Tab。
- 支持未保存表单关闭提醒。
- 支持最近访问。

## 4. 禁止事项

- 禁止业务页面自行实现标签页。
- 禁止把 Tab 状态散落在页面组件中。
- 禁止关闭未保存页面时不提示用户。

## 5. 验收标准

- 能打开多个不同单据详情 Tab。
- 编辑页 dirty 状态关闭时有确认提示。
- 刷新浏览器后可恢复最近工作区。
- TypeScript、lint、测试通过。
