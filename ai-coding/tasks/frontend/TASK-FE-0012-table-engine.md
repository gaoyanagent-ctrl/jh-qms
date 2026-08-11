# TASK-FE-0012 建立 table-engine 列表配置引擎

## 1. 任务目标

建立标准列表配置引擎，支持列配置、查询条件保存、用户视图和操作列权限控制。

## 2. 交付范围

新增：

```text
packages/table-engine/src/
  ConfigurableListPage.tsx
  ListViewDefinition.ts
  UserListViewPreference.ts
  ColumnSettings.tsx
  SearchPanelRenderer.tsx
  useListViewPreference.ts
```

## 3. 功能要求

- 支持列显示 / 隐藏。
- 支持列顺序和宽度。
- 支持固定列。
- 支持查询条件保存。
- 支持默认视图。
- 支持导出字段配置。
- 支持操作列权限控制。

## 4. 集成要求

- 必须使用 packages/permissions。
- 必须使用 packages/i18n。
- 必须使用 packages/theme。
- 不直接调用业务 API。

## 5. 验收标准

- 示例 WMS 列表能使用配置渲染。
- 修改列配置后刷新仍保留。
- 查询条件保存后可恢复。
- 无硬编码中文。
