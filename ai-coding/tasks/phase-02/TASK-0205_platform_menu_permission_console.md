# TASK-0205 动态菜单与权限管理闭环

## 1. 任务目标

实现菜单、路由、按钮、字段、API 权限的统一管理闭环，使平台菜单不再由前端硬编码，角色授权后前后端权限同步生效。

## 2. 必须先阅读

- `AGENTS.md`
- `ai-coding/rules/02_backend_rules.md`
- `ai-coding/rules/03_frontend_rules.md`
- `ai-coding/rules/04_database_rules.md`
- `ai-coding/rules/06_permission_rules.md`
- `docs/module-specs/platform/01_user_org_permission_spec.md`
- `docs/code-map/backend.md`
- `docs/code-map/api.md`
- `docs/code-map/frontend.md`

## 3. 业务范围

本任务实现：

- 菜单表。
- 菜单和权限点关联。
- 角色菜单授权。
- 当前用户菜单 API。
- 权限点查询 API。
- 菜单管理页面。
- 角色授权页面升级。
- 前端动态菜单渲染。
- 前端权限路由接入。

本任务不实现：

- 工作流审批。
- 多租户套餐授权。
- 外部系统单点登录。

## 4. 需要新增/修改的文件

后端：

```text
backend/iaf-platform-permission/src/main/java/.../menu/
backend/iaf-app/src/main/resources/db/migration/V00xx__platform_menu_permission.sql
```

前端：

```text
frontend/apps/pc-admin/src/modules/platform/menus/
frontend/apps/pc-admin/src/modules/platform/roles/
frontend/packages/permissions/
frontend/packages/domain-types/
```

## 5. 数据库设计

新增表建议：

- `sys_menu`
- `sys_menu_permission`
- `sys_role_menu`

菜单字段至少包含：

- `menu_code`
- `parent_id`
- `menu_type`
- `title_key`
- `route_path`
- `component_key`
- `icon`
- `sort_no`
- `visible`
- `enabled`

## 6. API 清单

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/platform/menus/tree` | 菜单树 |
| POST | `/api/platform/menus` | 新增菜单 |
| PUT | `/api/platform/menus/{id}` | 修改菜单 |
| GET | `/api/platform/permissions` | 权限点列表 |
| GET | `/api/platform/auth/menus` | 当前用户菜单 |
| PUT | `/api/platform/roles/{id}/menus` | 分配角色菜单 |
| PUT | `/api/platform/roles/{id}/permissions` | 分配角色权限 |

## 7. 权限点

```text
platform:menu:view
platform:menu:create
platform:menu:update
platform:menu:disable
platform:permission:view
platform:role:assign-menu
platform:role:assign-permission
```

## 8. 测试要求

- 后端菜单 CRUD。
- 当前用户菜单按角色过滤。
- 角色菜单授权生效。
- 前端菜单权限隐藏。
- 前端路由权限拦截。

## 9. 验收标准

- 前端主菜单不再硬编码平台菜单。
- 角色授权后菜单和按钮权限生效。
- `./scripts/check-quality.sh` 通过。
