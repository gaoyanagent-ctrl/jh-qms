# TASK-0101 用户组织角色权限模块开发

## 1. 任务目标

开发平台基础的用户、组织、角色、权限模块，支持用户管理、组织树、角色管理、权限点管理、用户分配角色、角色分配权限。

## 2. 必须先阅读

- AGENTS.md
- ai-coding/rules/02_backend_rules.md
- ai-coding/rules/03_frontend_rules.md
- ai-coding/rules/04_database_rules.md
- ai-coding/rules/06_permission_rules.md
- docs/module-specs/platform/01_user_org_permission_spec.md

## 3. 本任务实现

- sys_user。
- sys_org。
- sys_role。
- sys_permission。
- sys_user_role。
- sys_role_permission。
- 用户 API。
- 组织 API。
- 角色 API。
- 前端用户、组织、角色页面。

## 4. 本任务不实现

- SSO。
- 企微/钉钉/飞书组织同步。
- 复杂字段权限。
- 完整数据权限表达式。

## 5. 后端包结构

```text
com.company.iaf.platform.identity
com.company.iaf.platform.org
com.company.iaf.platform.permission
```

每个包内遵守 interfaces/application/domain/infrastructure。

## 6. API 清单

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/platform/users | 用户分页 |
| POST | /api/platform/users | 新增用户 |
| PUT | /api/platform/users/{id} | 修改用户 |
| POST | /api/platform/users/{id}/disable | 禁用用户 |
| GET | /api/platform/orgs/tree | 组织树 |
| POST | /api/platform/orgs | 新增组织 |
| PUT | /api/platform/orgs/{id} | 修改组织 |
| GET | /api/platform/roles | 角色分页 |
| POST | /api/platform/roles | 新增角色 |
| PUT | /api/platform/roles/{id}/permissions | 分配权限 |

## 7. 前端页面

```text
src/modules/platform/user/UserList.tsx
src/modules/platform/org/OrgTreePage.tsx
src/modules/platform/role/RoleList.tsx
```

## 8. 权限点

```text
platform:user:view
platform:user:create
platform:user:update
platform:user:disable
platform:org:view
platform:org:create
platform:org:update
platform:role:view
platform:role:create
platform:role:update
platform:role:assign-permission
```

## 9. 测试

- 创建用户成功。
- 重复 username 失败。
- 禁用用户成功。
- 创建组织成功。
- 删除存在子组织的组织失败，若实现删除。
- 角色分配权限成功。

## 10. 验收标准

- Migration 可执行。
- 后端测试通过。
- 前端页面可访问。
- 权限点初始化。
- Controller 未直接访问 Mapper。
