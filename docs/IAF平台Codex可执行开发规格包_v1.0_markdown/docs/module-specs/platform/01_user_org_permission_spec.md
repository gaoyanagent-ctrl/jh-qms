# 用户、组织、角色、权限模块详细设计

## 1. 目标

提供 IAF 平台的身份、组织、授权和访问控制基础能力，支撑多组织、多工厂、多角色、字段权限、数据权限和外部用户权限。

## 2. 模块边界

本模块负责：

- 用户管理。
- 管理组织。
- 岗位。
- 角色。
- 菜单。
- 权限点。
- 用户角色关系。
- 角色权限关系。
- 数据权限。
- 字段权限。

本模块不负责：

- 制造运营组织，如工厂、车间、仓库、库位。它们属于 manufacturing/wms 模块。
- 审批人业务解析。审批模块调用组织能力。

## 3. 核心概念

| 概念 | 说明 |
|---|---|
| User | 系统用户 |
| Org | 管理组织，如集团、公司、事业部、部门 |
| Position | 岗位 |
| Role | 角色 |
| Permission | 权限点 |
| Menu | 菜单 |
| DataScope | 数据权限范围 |
| FieldPermission | 字段权限 |

## 4. 数据库表

### sys_user

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| tenant_id | bigint | 租户 |
| username | varchar(64) | 登录名 |
| display_name | varchar(128) | 显示名 |
| mobile | varchar(32) | 手机 |
| email | varchar(128) | 邮箱 |
| password_hash | varchar(255) | 密码哈希 |
| status | varchar(32) | ENABLED/DISABLED/LOCKED |
| primary_org_id | bigint | 主组织 |
| created_by/created_at | - | 审计 |
| updated_by/updated_at | - | 审计 |
| deleted | boolean | 软删除 |
| version | int | 乐观锁 |
| ext_json | jsonb | 扩展 |

### sys_org

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| tenant_id | bigint | 租户 |
| parent_id | bigint | 上级组织 |
| org_code | varchar(64) | 组织编码 |
| org_name | varchar(128) | 组织名称 |
| org_type | varchar(32) | GROUP/COMPANY/BU/DEPARTMENT |
| status | varchar(32) | 状态 |
| sort_no | int | 排序 |
| ext_json | jsonb | 扩展 |

### sys_role

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| tenant_id | bigint | 租户 |
| role_code | varchar(64) | 角色编码 |
| role_name | varchar(128) | 角色名称 |
| role_type | varchar(32) | INTERNAL/EXTERNAL |
| status | varchar(32) | 状态 |

### sys_permission

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| tenant_id | bigint | 租户 |
| permission_code | varchar(128) | 权限编码 |
| permission_name | varchar(128) | 权限名称 |
| resource_type | varchar(32) | MENU/BUTTON/API/FIELD |
| module_code | varchar(64) | 模块 |
| action_code | varchar(64) | 动作 |

### 关系表

- sys_user_role。
- sys_role_permission。
- sys_user_org。
- sys_user_position。
- sys_role_data_scope。
- sys_role_field_permission。

## 5. API 清单

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/platform/users | 用户分页 |
| POST | /api/platform/users | 新增用户 |
| PUT | /api/platform/users/{id} | 修改用户 |
| POST | /api/platform/users/{id}/disable | 禁用用户 |
| POST | /api/platform/users/{id}/reset-password | 重置密码 |
| GET | /api/platform/orgs/tree | 组织树 |
| POST | /api/platform/orgs | 新增组织 |
| PUT | /api/platform/orgs/{id} | 修改组织 |
| GET | /api/platform/roles | 角色分页 |
| POST | /api/platform/roles | 新增角色 |
| PUT | /api/platform/roles/{id}/permissions | 分配权限 |
| PUT | /api/platform/roles/{id}/data-scopes | 分配数据权限 |

## 6. 权限点

```text
platform:user:view
platform:user:create
platform:user:update
platform:user:disable
platform:user:reset-password
platform:org:view
platform:org:create
platform:org:update
platform:role:view
platform:role:create
platform:role:update
platform:role:assign-permission
```

## 7. 领域规则

- username 在同一 tenant 下唯一。
- org_code 在同一 tenant 下唯一。
- role_code 在同一 tenant 下唯一。
- 禁用用户后不得登录。
- 删除组织前必须检查是否存在下级组织和用户引用。
- 角色删除前必须检查用户绑定。

## 8. 测试用例

- 创建用户成功。
- 重复 username 创建失败。
- 禁用用户后登录失败。
- 创建组织树成功。
- 删除存在子节点的组织失败。
- 角色分配权限成功。
- 数据权限过滤生效。
