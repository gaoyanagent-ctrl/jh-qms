# 用户、组织、角色、权限模块详细设计

> 版本：v1.1（规划增强）
> 目标：把用户、组织、角色、权限从“能跑”提升到“可持续治理与合规模型”，兼容工业企业多组织场景。

## 1. 目标

提供 IAF 平台的身份、组织、授权和访问控制基础能力，支撑多组织、多工厂、多角色、字段权限、数据权限和外部用户权限。

### 1.1 设计边界（阶段化）

- 第一阶段（MVP）：
  - 用户、组织、角色、权限点、菜单、角色与菜单/权限绑定、当前用户菜单。
- 第二阶段（推荐）：
  - 组织维度授权、用户组织多归属、数据权限、字段权限、组织级/用户级权限例外、权限变更审计增强。
- 第三阶段（平台化）：
  - 角色继承、权限策略版本化、动态组织隔离规则引擎。

## 2. 模块边界

本模块负责：

- 用户管理。
- 管理组织。
- 岗位。
- 角色。
- 权限点。
- 菜单。
- 用户角色关系。
- 角色权限关系。
- 数据权限（规划）。
- 字段权限（规划）。

本模块不负责：

- 制造运营组织，如工厂、车间、仓库、库位。它们属于 manufacturing/wms 模块。
- 审批人业务解析。审批模块调用组织能力。

## 3. 核心概念

| 概念 | 说明 |
|---|---|
| User | 系统用户 |
| Org | 管理组织，如集团、公司、事业部、部门 |
| OrgDimension | 管理组织维度（当前为 COMPANY/DEPARTMENT/DIVISION/TEAM） |
| Position | 岗位 |
| Role | 角色 |
| Permission | 权限点 |
| Menu | 菜单 |
| DataScope | 数据权限范围 |
| FieldPermission | 字段权限 |

### 3.1 设计理念（业界实践）

- 基础模型采用 RBAC，但菜单可见性与 API 鉴权分离。
- 菜单是“可见性入口”，API 是“执行入口”，两者均需受控。
- 优先级：身份与租户隔离 > 角色权限 > 组织权限 > 数据范围 > 字段权限。
- 前端隐藏只做 UX，后端鉴权才是安全边界。
- 权限和菜单绑定设计为显式配置（白名单模型），不依赖代码常量。

## 4. 数据库表（当前实现 + 规划补充）

### 4.1 当前实现基础表

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

建议补强字段（业务化）：

- `external_id`（对接 SSO/LDAP）
- `locale`、`timezone`
- `last_login_at`、`failed_login_count`、`locked_until`
- `is_external`（外部用户标记）
- `avatar_url`（可放 ext_json）

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

### sys_menu（TASK-0205）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| tenant_id | bigint | 租户 |
| parent_id | bigint | 上级菜单 |
| menu_code | varchar(128) | 菜单编码 |
| menu_type | varchar(32) | GROUP/MENU/BUTTON |
| title_key | varchar(128) | 前端 i18n 标题 key |
| route_path | varchar(255) | 前端路由 |
| component_key | varchar(128) | 前端组件 key |
| icon | varchar(64) | 图标 key |
| sort_no | int | 排序 |
| visible | boolean | 是否显示在导航 |
| enabled | boolean | 是否启用 |
| ext_json | jsonb | 扩展 |

### sys_user_role

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| tenant_id | bigint | 租户 |
| user_id | bigint | 用户 |
| role_id | bigint | 角色 |

### sys_role_permission

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| tenant_id | bigint | 租户 |
| role_id | bigint | 角色 |
| permission_id | bigint | 权限 |

### sys_menu_permission

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| tenant_id | bigint | 租户 |
| menu_id | bigint | 菜单 |
| permission_id | bigint | 权限 |

### sys_role_menu

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| tenant_id | bigint | 租户 |
| role_id | bigint | 角色 |
| menu_id | bigint | 菜单 |

### 关系表（规划）

- sys_user_org：用户在组织维度上的多归属（包括组织生效期、主组织标识）。
- sys_role_data_scope：角色对模块对象的数据域配置。
- sys_user_data_scope：用户级数据域例外。
- sys_role_field_permission：字段级权限。
- sys_user_permission：用户级权限例外（极少数审计和紧急场景）。

任务拆分映射：

- `TASK-0214`：`sys_user_org`（用户多组织归属与上下文）。
- `TASK-0215`：`sys_role_data_scope` / `sys_user_data_scope` / `sys_role_field_permission` / `sys_user_field_permission`。
- `TASK-0216`：菜单、路由、API 鉴权一致性闭环。

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
| PUT | /api/platform/roles/{id}/menus | 分配菜单 |
| GET | /api/platform/permissions | 权限点列表 |
| GET | /api/platform/auth/menus | 当前用户菜单 |
| GET | /api/platform/menus/tree | 菜单树 |
| POST | /api/platform/menus | 新增菜单 |
| PUT | /api/platform/menus/{id} | 修改菜单 |
| PUT | /api/platform/roles/{id}/data-scopes | 分配数据权限（推荐） |
| PUT | /api/platform/roles/{id}/field-permissions | 字段权限分配（推荐） |
| PUT | /api/platform/roles/{id}/users | 角色成员分配（推荐） |
| GET | /api/platform/roles/{id}/effective-permissions | 组合后权限（推荐） |
| GET | /api/platform/roles/{id}/data-scopes | 查询角色数据范围 |
| PUT | /api/platform/roles/{id}/data-scopes | 覆盖角色数据范围 |
| GET | /api/platform/users/{id}/data-scopes | 查询用户数据范围 |
| PUT | /api/platform/users/{id}/data-scopes | 覆盖用户数据范围 |
| GET | /api/platform/roles/{id}/field-permissions | 查询角色字段权限 |
| PUT | /api/platform/roles/{id}/field-permissions | 覆盖角色字段权限 |
| GET | /api/platform/users/{id}/field-permissions | 查询用户字段权限 |
| PUT | /api/platform/users/{id}/field-permissions | 覆盖用户字段权限 |
| GET | /api/platform/permissions/effective | 查询当前用户最终权限 |
| GET | /api/platform/permission-consistency/check | 菜单/路由/API 一致性检查 |

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
platform:role:assign-menu
platform:permission:view
platform:menu:view
platform:menu:create
platform:menu:update
platform:menu:disable
platform:data-scope:view
platform:data-scope:update
platform:field-permission:view
platform:field-permission:update
platform:permission-consistency:view
platform:permission-consistency:run
```

### 6.1 菜单权限落地建议

- 菜单 `platform:xxx` 的访问不应只依赖路由注册，必须同时要求该菜单的权限码命中或菜单未绑定权限。
- 菜单 `component_key + route_path` 是导航层资产；真实业务动作必须再走 API 鉴权。
- 建议在菜单树返回时携带 `permissionCodes`，前端进行 UX 过滤；后端 `/api/platform/auth/menus` 同时做安全过滤。

## 7. 多维组织与组织树治理

### 7.1 现状

当前 `sys_org` 提供管理组织树（`COMPANY/DEPARTMENT/DIVISION/TEAM`），用于“组织树管理页面”和权限上下文的基础能力。
制造/WMS 运营组织（工厂、车间、仓库、库位）继续在各业务模块建模，待平台化后通过统一组织上下文做跨域聚合。

### 7.2 规划：组织维度化模型

建议增加：

- `sys_user_org`
  - `user_id, org_id`
  - `is_primary`
  - `scope_weight`（上下文切换优先级）
  - `valid_from/valid_to`（临时组织归属）
- `sys_role_data_scope`
  - `role_id, object_type, scope_type, scope_object_ids_json`
- `sys_user_data_scope`
  - 用户级例外，支持“自己看自己/看部门/看全部”等细粒度。
- `sys_org_relation_cache`（可选）
  - 存储组织树派生路径/层级，优化树查询与授权查询性能。

### 7.3 数据权限枚举（与规则文件一致）

```text
ALL
TENANT
COMPANY
BUSINESS_UNIT
PLANT
DEPARTMENT
WAREHOUSE
SELF
CUSTOM
SUPPLIER_SELF
CUSTOMER_SELF
```

## 8. 领域规则

- username 在同一 tenant 下唯一。
- org_code 在同一 tenant 下唯一。
- role_code 在同一 tenant 下唯一。
- 禁用用户后不得登录。
- 删除组织前必须检查是否存在下级组织和用户引用。
- 角色删除前必须检查用户绑定。
- 菜单编码在同一 tenant 下唯一。
- 当前用户菜单必须按角色菜单绑定、菜单启用状态、可见性和当前用户权限过滤；有权限绑定的菜单必须命中至少一个绑定权限，无权限绑定的角色菜单可显示。
- 角色分配菜单时必须先验证全部菜单编码存在，再原子替换绑定。
- 菜单更新不得把父级设置为自身或自身后代，避免菜单树成环。
- 前端路由必须对用户、组织、角色、菜单等平台管理页做权限拦截，菜单隐藏不能作为唯一前端防线。
- 关键管理动作（用户、角色、权限点变更）建议记录审计事件。

## 9. 权限配置方法（推荐）

### 9.1 角色中心化（默认模式）

1. 建立角色（role）；
2. 分配权限点（permission）；
3. 分配菜单（menu）；
4. 组织用户到角色（user-role）；
5. 如需数据域再分配数据范围；
6. 前端使用统一组件进行权限可见性渲染。

### 9.2 数据驱动菜单模型

- 菜单树按 `/api/platform/menus/tree` 维护；
- 组件路由按 `permission_code` 做路由鉴权；
- 菜单与权限通过 `sys_menu_permission` 绑定（N:N）；
- 菜单无绑定权限视作“低风险展示入口”；有绑定权限时要求至少命中一条。

### 9.3 API 权限与按钮权限映射

- 平台菜单页能力：
  - 列表查看：`platform:x:view`
  - 新建：`platform:x:create`
  - 修改：`platform:x:update`
- 按钮和动作一律走统一权限组件（`PermissionButton/PermissionRoute/FieldPermissionWrapper`）；
- 路由直接访问也必须带权限注解拦截，不依赖前端隐藏。

### 9.4 运营期治理（推荐）

- 引入“权限变更单”审计（谁在何时给哪个角色加了什么权限）；
- 为敏感权限增加变更确认流；
- 定期扫描“无权限菜单、无成员角色、无效组织引用”告警。

## 10. 鉴权决策流程（实现建议）

1. 认证与租户上下文：用户 token 有效且租户一致。
2. 用户状态判断：仅 ENABLED 参与权限计算。
3. 角色集：取当前租户下有效角色。
4. 权限集：`role permissions + menu permissions + optional direct exceptions`。
5. 资源匹配：按 `module:resource:action` 与动作类型进行匹配。
6. 数据域与组织上下文：在资源是数据域对象时再进一步裁剪。
7. 拒绝优先：任一关键条件不满足直接返回 403，不返回空结果（防枚举风险）。

## 11. 测试用例

- 创建用户成功。
- 重复 username 创建失败。
- 禁用用户后登录失败。
- 创建组织树成功。
- 删除存在子节点失败。
- 角色分配权限成功。
- 当前用户菜单加载成功。
- 当前用户菜单按菜单权限过滤成功。
- 角色分配菜单成功。
- 菜单重复编码创建失败。
- 菜单设置后代为父级失败。
- 数据权限过滤生效（推荐场景）。

### 11.1 建议新增测试

- 组织树上下文切换场景下的菜单/API一致性。
- 前端绕过菜单直连 URL 的路由 403。
- 菜单被禁用后无权限缓存残留（热更新后立即失效）。
- 数据权限与字段权限联合生效（读写分离）。
