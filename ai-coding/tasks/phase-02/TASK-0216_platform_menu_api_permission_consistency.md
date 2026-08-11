# TASK-0216 菜单-路由-API 鉴权一致性

## 1. 任务目标

在 TASK-0205 落地基础上，补齐前端路由、菜单可见性、后端接口鉴权之间的闭环，避免“菜单隐藏 ≠ 无权限”，并为运营侧提供权限一致性可观测能力。

## 2. 必须先阅读

- `AGENTS.md`
- `docs/architecture/06_项目文档与规则目录治理.md`
- `docs/code-map/README.md`
- `docs/code-map/backend.md`
- `docs/code-map/api.md`
- `ai-coding/rules/02_backend_rules.md`
- `ai-coding/rules/03_frontend_rules.md`
- `ai-coding/rules/05_api_rules.md`
- `ai-coding/rules/06_permission_rules.md`
- `ai-coding/rules/10_testing_rules.md`
- `docs/module-specs/platform/01_user_org_permission_spec.md`

## 3. 业务范围

本任务实现：

- `/api/platform/auth/menus` 返回 `permission_codes`、`menu_type` 与 `route_path` 的一致模型。
- 新增“菜单与路由权限映射”校验服务（服务端）用于启动期巡检。
- 前端路由守卫增强，基于 `permission_codes` 做 403 分流，禁止越权直连。
- 角色/用户上下文下菜单与可访问路由一致性检查接口与告警。
- `MenuPermissionRouteMismatch` 工具接口（内部使用，便于 UI 级修复）。

本任务不实现：

- 全量重构现有所有页面路由。
- 引入 CASL / OPA / Cedar 等外部策略引擎。
- 把菜单树作为权限总账来源替代 RBAC 主模型。

## 4. 需要新增/修改的文件

后端：

```text
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/domain/model/PermissionInconsistency.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/application/MenuPermissionConsistencyService.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/application/AuthMenuService.java（增强）
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/interfaces/controller/PermissionConsistencyController.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/interfaces/dto/MenuPermissionCheckRequest.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/interfaces/dto/MenuPermissionConsistencyResponse.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/interfaces/dto/RouteAccessCheckRequest.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/interfaces/dto/RouteAccessCheckResponse.java
backend/iaf-platform-permission/src/main/resources/db/migration/V0008__platform_permission_consistency_audit.sql
```

前端：

```text
frontend/apps/pc-admin/src/packages/permissions/PermissionRouteGuard.tsx
frontend/apps/pc-admin/src/packages/permissions/PermissionRouteEntry.tsx
frontend/apps/pc-admin/src/packages/permissions/routePermissions.ts
frontend/apps/pc-admin/src/modules/platform/menus/PermissionDiagnosticsPage.tsx
frontend/apps/pc-admin/src/modules/platform/menus/permissionDiagnosticsApi.ts
frontend/apps/pc-admin/src/modules/platform/menus/permissionDiagnosticsMock.ts
frontend/apps/pc-admin/src/modules/platform/menus/types.ts
```

## 5. API 清单

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/platform/auth/menus` | 菜单返回包含 `permission_codes` |
| GET | `/api/platform/permission-consistency/check` | 检查菜单-路由-按钮一致性 |
| POST | `/api/platform/permission-consistency/route-check` | 上报/检查前端可达路由列表 |
| GET | `/api/platform/permissions/effective` | 当前用户最终权限（含菜单/角色合并） |
| GET | `/api/platform/permissions/effective/{routePath}` | 单路由权限校验 |

## 6. 鉴权点

- `platform:auth-menu:view`（读取当前用户菜单）
- `platform:permission-consistency:view`
- `platform:permission-consistency:run`

## 7. 业务规则

- 菜单带 `permission_codes` 时，前端不得无条件展示；无权限则不展示或显示仅限可见。
- `GET /api/platform/auth/menus` 必须再次按用户权限做服务端过滤，防止 token 篡改导致“前端越权菜单”。
- 路由级 403 必须优先于页面内按钮隐藏判断。
- 仅系统管理员可查看一致性检测结果；普通管理员只能查看自己的权限范围结果。

## 8. 测试要求

- `/api/platform/auth/menus` 响应与数据库绑定权限完全一致。
- 不带权限访问高风险路由返回 403。
- 前端通过浏览器直接访问无权限地址会被拦截为 403 页面。
- 一致性检测接口能识别以下异常场景：
  - 菜单绑定权限已失效。
  - 路由存在权限要求但菜单端缺失对应绑定。
  - 同 route 的 menu 与 API 权限不一致。
- 前后端一致性报告支持 JSON 输出并可被脚本消费。

## 9. 验收标准

- 菜单可见性、路由可达性、后端鉴权实现一体化校验闭环。
- 通过一致性检查且可回收已发现异常。
- `./scripts/check-quality.sh` 通过。
- 现网场景下支持灰度开关关闭新拦截逻辑（如必要）。
