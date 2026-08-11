# TASK-FE-0022 平台页面架构对齐与权限组件化

## 1. 任务目标

重构 PC 端平台管理页面（用户列表、组织树、角色列表），将它们拆分为符合项目分层设计规则的模块目录结构。同时在 `@iaf/permissions` 中实现统一的权限 Hooks 与组件，消除页面内直接判断权限的脏逻辑。

## 2. 必须先阅读

- AGENTS.md
- ai-coding/rules/03_frontend_rules.md
- docs/frontend/01_前端总体架构.md
- docs/frontend/08_权限与状态驱动UI规范.md

## 3. 业务范围

本任务实现：

- 重构并移动平台用户、组织、角色页面至模块级目录下：
  - `frontend/apps/pc-admin/src/modules/platform/users/` (api.ts, hooks.ts, types.ts, UserListPage.tsx)
  - `frontend/apps/pc-admin/src/modules/platform/orgs/` (api.ts, hooks.ts, types.ts, OrgTreePage.tsx)
  - `frontend/apps/pc-admin/src/modules/platform/roles/` (api.ts, hooks.ts, types.ts, RoleListPage.tsx)
- 在 `@iaf/permissions` 包中实现并导出：
  - `useUserPermissions()`
  - `useHasPermission(code)`
  - `useHasAnyPermission(codes)`
  - `PermissionGate`（支持免传 permissions 参数，自动获取当前用户权限）
  - `PermissionButton`（包装 Antd Button）
  - `PermissionRoute`（实现基于 React Router 的权限防护路由）
- 页面与 Layout 中所有的按钮和权限判断全部迁移为统一的权限 Hooks 与组件。
- 删除原 monolith API 客户端 `frontend/apps/pc-admin/src/api/platform.ts`。
- 删除原 `frontend/apps/pc-admin/src/pages/platform/` 目录。
- 确保测试用例覆盖新的模块组件并能够全部通过。

## 4. 需要新增/修改的文件

前端：

```text
frontend/packages/permissions/package.json
frontend/packages/permissions/src/index.ts
frontend/packages/permissions/src/permissions.test.tsx
frontend/apps/pc-admin/src/App.tsx
frontend/apps/pc-admin/src/layouts/MainLayout.tsx
frontend/apps/pc-admin/src/modules/platform/users/api.ts [NEW]
frontend/apps/pc-admin/src/modules/platform/users/hooks.ts [NEW]
frontend/apps/pc-admin/src/modules/platform/users/types.ts [NEW]
frontend/apps/pc-admin/src/modules/platform/users/UserListPage.tsx [NEW]
frontend/apps/pc-admin/src/modules/platform/users/UserListPage.test.tsx [NEW]
frontend/apps/pc-admin/src/modules/platform/orgs/api.ts [NEW]
frontend/apps/pc-admin/src/modules/platform/orgs/hooks.ts [NEW]
frontend/apps/pc-admin/src/modules/platform/orgs/types.ts [NEW]
frontend/apps/pc-admin/src/modules/platform/orgs/OrgTreePage.tsx [NEW]
frontend/apps/pc-admin/src/modules/platform/roles/api.ts [NEW]
frontend/apps/pc-admin/src/modules/platform/roles/hooks.ts [NEW]
frontend/apps/pc-admin/src/modules/platform/roles/types.ts [NEW]
frontend/apps/pc-admin/src/modules/platform/roles/RoleListPage.tsx [NEW]
```

## 5. 验收标准

- 所有平台页面可正常渲染且各项操作（增删改查、分配权限）可用性与重构前一致。
- 页面代码中无硬编码的权限判断，全部使用统一 Hooks 或组件实现。
- 原 `pages/platform/` 和 `api/platform.ts` 被完全清理。
- `pnpm lint`、`pnpm typecheck`、`pnpm test` 以及 `pnpm build` 全流程通过。
- `docs/code-map/frontend.md` 完成更新。
