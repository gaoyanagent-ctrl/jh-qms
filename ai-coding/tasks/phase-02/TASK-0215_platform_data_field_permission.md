# TASK-0215 数据范围与字段权限模型

## 1. 任务目标

在现有用户/角色/菜单权限基础上，完成数据裁剪与字段可见性控制模型，并形成可落地的“角色优先 + 用户例外”权限扩展能力，为审批、WMS 等业务能力预留一致的鉴权输入。

本任务范围是治理能力建设，不直接改造所有业务查询。

## 2. 必须先阅读

- `AGENTS.md`
- `docs/architecture/06_项目文档与规则目录治理.md`
- `docs/code-map/README.md`
- `docs/code-map/backend.md`
- `docs/code-map/api.md`
- `ai-coding/rules/02_backend_rules.md`
- `ai-coding/rules/04_database_rules.md`
- `ai-coding/rules/06_permission_rules.md`
- `docs/module-specs/platform/01_user_org_permission_spec.md`

## 3. 业务范围

本任务实现：

- 角色级数据权限配置能力（含组织维度/自定义对象列表）。
- 用户级数据权限例外（用于“数据审计例外”或“看全部”场景）。
- 字段级权限配置能力（字段可见/只读/掩码）。
- 鉴权决策输出 `PermissionContext`（菜单/API 鉴权之外）的基础计算输入。
- 与前端平台能力页打通配置页面（列表、查看、编辑）。

本任务不实现：

- 业务模块逐条改造所有查询语句（后续任务）。
- 审批流、工单、生产流程的特例规则。
- 跨租户越权策略。

## 4. 需要新增/修改的文件

后端（`iaf-platform-permission`）：

```text
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/domain/model/DataScopeType.java（或枚举）
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/domain/model/UserDataScope.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/domain/model/RoleDataScope.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/domain/model/UserFieldPermission.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/domain/model/RoleFieldPermission.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/domain/repository/RoleDataScopeRepository.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/domain/repository/RoleFieldPermissionRepository.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/domain/repository/UserDataScopeRepository.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/domain/repository/UserFieldPermissionRepository.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/infrastructure/persistence/JdbcRoleDataScopeRepository.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/infrastructure/persistence/JdbcRoleFieldPermissionRepository.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/infrastructure/persistence/JdbcUserDataScopeRepository.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/infrastructure/persistence/JdbcUserFieldPermissionRepository.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/application/PermissionScopeService.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/application/RolePermissionService.java（扩展）
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/interfaces/controller/PermissionScopeController.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/interfaces/dto/DataScopeAssignRequest.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/interfaces/dto/DataScopeResponse.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/interfaces/dto/FieldPermissionAssignRequest.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/interfaces/dto/FieldPermissionResponse.java
backend/iaf-platform-permission/src/main/resources/db/migration/V0007__platform_permission_scopes.sql
```

前端：

```text
frontend/apps/pc-admin/src/modules/platform/permissions/api.ts
frontend/apps/pc-admin/src/modules/platform/permissions/types.ts
frontend/apps/pc-admin/src/modules/platform/permissions/schema.ts
frontend/apps/pc-admin/src/modules/platform/permissions/pages/DataScopePage.tsx
frontend/apps/pc-admin/src/modules/platform/permissions/pages/FieldPermissionPage.tsx
frontend/apps/pc-admin/src/modules/platform/permissions/components/ScopeBadge.tsx
```

## 5. 数据库设计

### `sys_role_data_scope`

- `id bigint` PK
- `tenant_id bigint`
- `role_id bigint`
- `object_type varchar(64)`（如 PURCHASE_ORDER, INVENTORY_DOC, USER_MANAGEMENT）
- `scope_type varchar(32)`（ALL, TENANT, COMPANY, DEPARTMENT, SELF, CUSTOM）
- `scope_object_ids jsonb`（可选的组织/对象 ID 列表）
- `include_descendants boolean`（组织树向下透传）
- `valid_from timestamp` `valid_to timestamp`（生效/失效）
- `created_by bigint` `created_at timestamp`
- `updated_by bigint` `updated_at timestamp`
- `deleted boolean`
- `version int`
- `ext_json jsonb`

唯一约束：`(tenant_id, role_id, object_type, scope_type)`（未删）。

### `sys_user_data_scope`

- `id bigint` PK
- `tenant_id bigint`
- `user_id bigint`
- `object_type varchar(64)`
- `scope_type varchar(32)`
- `scope_object_ids jsonb`
- `include_descendants boolean`
- `override_mode varchar(16)`（REPLACE/EXTEND）
- `source_role_id bigint`（来源角色，仅用于审计）
- 同样的审计/版本/软删/扩展字段。

唯一约束：`(tenant_id, user_id, object_type)`（未删）。

### `sys_role_field_permission`

- `id bigint` PK
- `tenant_id bigint`
- `role_id bigint`
- `resource_type varchar(64)`（MODULE/ENTITY）
- `resource_code varchar(128)`（表/实体名）
- `field_key varchar(128)`（字段 key）
- `access_level varchar(16)`（VISIBLE/HIDE/READ_ONLY/MASK）
- `mask_pattern varchar(64)`（可选）
- 审计/软删/版本/扩展字段。

联合唯一：`(tenant_id, role_id, resource_type, resource_code, field_key)`。

### `sys_user_field_permission`

- `id bigint` PK
- `tenant_id bigint`
- `user_id bigint`
- `resource_type varchar(64)`
- `resource_code varchar(128)`
- `field_key varchar(128)`
- `access_level varchar(16)`
- `mask_pattern varchar(64)`
- `scope_type varchar(16)`（APPEND/REPLACE，默认 REPLACE）
- 审计/软删/版本/扩展字段。

联合唯一：`(tenant_id, user_id, resource_type, resource_code, field_key)`。

必要索引：

- `(tenant_id, role_id, object_type)`（角色范围查询）
- `(tenant_id, user_id, object_type)`（用户范围查询）
- `(tenant_id, resource_type, resource_code, field_key)`（字段权限缓存）

## 6. API 清单

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/platform/roles/{roleId}/data-scopes` | 获取角色数据范围 |
| PUT | `/api/platform/roles/{roleId}/data-scopes` | 覆盖/替换角色数据范围 |
| GET | `/api/platform/users/{userId}/data-scopes` | 获取用户数据范围 |
| PUT | `/api/platform/users/{userId}/data-scopes` | 覆盖/替换用户数据范围 |
| GET | `/api/platform/roles/{roleId}/field-permissions` | 获取角色字段权限 |
| PUT | `/api/platform/roles/{roleId}/field-permissions` | 覆盖/替换角色字段权限 |
| GET | `/api/platform/users/{userId}/field-permissions` | 获取用户字段权限 |
| PUT | `/api/platform/users/{userId}/field-permissions` | 覆盖/替换用户字段权限 |
| POST | `/api/platform/permissions/evaluate` | 评估当前用户最终权限上下文（管理与测试用途） |

## 7. 鉴权点

- `platform:data-scope:view`
- `platform:data-scope:update`
- `platform:field-permission:view`
- `platform:field-permission:update`
- `platform:permission:evaluate`

## 8. 前端设计

- 平台角色详情页新增“数据范围”标签页；展示按资源类型分组。
- 用户详情页新增“字段权限（直接生效）”标签页。
- 数据范围编辑器支持：
  - `scope_type` 下拉（ALL/TENANT/SELF/CUSTOM）。
  - `scope_object_ids` 多选树（对象树）。
  - 有效期、作用域权重（preview）。
- 字段权限编辑支持 `VISIBLE/HIDE/READ_ONLY/MASK` 组合预览。
- 所有配置变更带变更人/时间快照（仅展示在详情）。

## 9. 测试要求

- 角色数据权限 CRUD 成功与幂等更新。
- 用户级例外可覆盖角色同 object。
- `override_mode` 与 `scope_type` 合法性校验。
- 有效期外策略不生效。
- `evaluate` 在角色+用户例外场景下返回去重集合。
- 关键页面路由权限校验（仅有 `platform:data-scope:view` 可见）。

## 10. 验收标准

- 平台可独立配置数据范围与字段权限策略。
- 角色策略 + 用户例外策略可被计算服务产出一致上下文。
- `./scripts/check-quality.sh` 在本任务涉及范围内通过。
- 不影响现有菜单与基础角色分配回归。
