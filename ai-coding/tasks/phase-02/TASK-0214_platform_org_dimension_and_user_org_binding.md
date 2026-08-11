# TASK-0214 组织维度与用户多组织归属

## 1. 任务目标

实现管理组织维度的可扩展增强，支持用户多组织归属、主组织切换和组织上下文对权限计算的输入，确保同一用户在不同组织上下文下可产生可预期的权限边界（MVP 先落地角色组织归属与主组织标记，组织生效期为后续增强）。

## 2. 必须先阅读

- `AGENTS.md`
- `docs/architecture/06_项目文档与规则目录治理.md`
- `docs/architecture/03_核心平台模块设计.md`
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

- `sys_user_org` 关系表（用户-组织）：
  - 支持一人多组织。
  - 支持主组织标识。
  - 支持组织归属生效策略（预留）。
- 用户组织归属管理 API。
- 用户详情返回组织归属快照（含主组织）。
- Token/上下文扩展支持“当前组织上下文”（用于菜单与菜单外 API 的后续扩展）。
- 管理后台角色/用户页面补充“组织归属”编辑入口（最小可用）。

本任务不实现：

- 数据范围（ALL/TENANT/SELF/DEPARTMENT）最终计算引擎（交给 `TASK-0215`）。
- 制造运营组织（工厂/仓库/库位）模型。

## 4. 需要新增/修改的文件

后端：

```text
backend/iaf-platform-auth/src/main/java/com/company/iaf/platform/auth/domain/model/PlatformUser.java
backend/iaf-platform-auth/src/main/java/com/company/iaf/platform/auth/domain/repository/PlatformUserRepository.java
backend/iaf-platform-auth/src/main/java/com/company/iaf/platform/auth/infrastructure/persistence/JdbcPlatformUserRepository.java
backend/iaf-platform-auth/src/main/java/com/company/iaf/platform/auth/interfaces/controller/UserController.java
backend/iaf-platform-auth/src/main/java/com/company/iaf/platform/auth/application/AuthApplicationService.java
backend/iaf-platform-auth/src/main/java/com/company/iaf/platform/auth/application/UserApplicationService.java
backend/iaf-platform-auth/src/main/java/com/company/iaf/platform/auth/interfaces/dto/UserResponse.java
backend/iaf-platform-auth/src/main/java/com/company/iaf/platform/auth/interfaces/dto/UserOrganizationsResponse.java
backend/iaf-platform-auth/src/main/java/com/company/iaf/platform/auth/interfaces/controller/AuthController.java
backend/iaf-platform-org/src/main/java/com/company/iaf/platform/org/application/OrgApplicationService.java
backend/iaf-platform-org/src/main/java/com/company/iaf/platform/org/interfaces/controller/OrgController.java
backend/iaf-platform-auth/src/main/java/com/company/iaf/platform/auth/domain/model/CurrentAuthContext.java
backend/iaf-platform-core/src/main/java/com/company/iaf/platform/core/security/SecurityContext.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/domain/model/UserOrg.java (或新增)
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/domain/repository/UserOrgRepository.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/infrastructure/persistence/JdbcUserOrgRepository.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/application/UserOrgApplicationService.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/interfaces/controller/UserOrgController.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/interfaces/dto/UserOrgAssignRequest.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/interfaces/dto/UserOrgItemResponse.java
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/interfaces/dto/UserOrgResponse.java
backend/iaf-app/src/main/java/com/company/iaf/Application.java
backend/iaf-app/src/main/java/com/company/iaf/shared/tenant/TenantContext.java
backend/iaf-app/src/main/resources/db/migration/V0006__platform_user_org.sql
```

前端：

```text
frontend/apps/pc-admin/src/modules/platform/users/api.ts
frontend/apps/pc-admin/src/modules/platform/users/types.ts
frontend/apps/pc-admin/src/modules/platform/users/hooks.ts
frontend/apps/pc-admin/src/modules/platform/users/UserListPage.tsx
frontend/apps/pc-admin/src/modules/platform/users/UserProfilePage.tsx（如有）
```

## 5. 数据库设计

### `sys_user_org`

字段建议（按 v1 规划）：

- `id bigint` PK
- `tenant_id bigint`（租户）
- `user_id bigint`（用户）
- `org_id bigint`（组织）
- `is_primary boolean`（是否主组织）
- `scope_weight int`（组织上下文优先级）
- `valid_from timestamp`
- `valid_to timestamp`
- `created_by bigint` `created_at timestamp`
- `updated_by bigint` `updated_at timestamp`
- `deleted boolean`
- `version int`
- `ext_json jsonb`

约束与索引：

- 唯一约束：`(tenant_id, user_id, org_id)`（在未删除数据上）
- `(tenant_id, user_id, is_primary, deleted)`，用于主组织判定与用户身份查询。
- `(tenant_id, org_id, deleted)`，用于组织成员统计与组织查询。

## 6. API 清单

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/platform/users/{id}/orgs` | 查询用户组织归属 |
| PUT | `/api/platform/users/{id}/orgs` | 覆盖式设置用户组织归属 |
| PATCH | `/api/platform/users/{id}/org-context` | 切换当前用户上下文组织 |
| GET | `/api/platform/users/me` | 当前用户（含组织快照） |
| GET | `/api/platform/orgs/{id}/users` | 查询组织成员（可选） |

## 7. 鉴权要求

- `platform:user:view`：查询组织归属。
- `platform:user:update`：更新用户组织归属。
- `platform:org:view`：组织上下文管理页查看。
- `platform:org:update`：组织归属关联修改。

## 8. 前端设计

- 用户列表增加组织归属列与管理入口（只读）。
- 用户编辑页增加“组织归属”抽屉或面板，支持复选组织 + 主组织开关。
- `CurrentAuthContext` 中新增 `currentOrgId` 与组织缓存（可选）。
- 菜单树与用户上下文联动不要求全量改造，保留现状但附带上下文字段。

## 9. 测试要求

后端：

- 用户组织归属新增/更新成功。
- 单一用户主组织约束校验（最多一个主组织）。
- 生效期外组织归属不可查询。
- 批量覆盖赋权（replace）幂等。

前端：

- 无权限不显示组织归属编辑入口。
- 编辑后前端列表与详情正确回显组织归属。

## 10. 验收标准

- 用户可在系统中具备多组织归属模型。
- 主组织标识可用于上下文推导。
- 用户组织归属 API 与前端用户管理页可用。
- `./scripts/check-quality.sh` 可通过（结合本任务涉及模块的现有规则）。

## 11. 变更边界

- 不直接处理数据权限裁剪语义（移交 `TASK-0215`）。
- 不直接处理菜单-路由鉴权一致性（移交 `TASK-0216`）。
- 不实现组织树可视化高级筛选策略（保留 `TASK-020?` 中的组织树体验）。
