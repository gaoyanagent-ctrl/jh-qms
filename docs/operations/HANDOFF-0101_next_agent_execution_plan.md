# HANDOFF-0101 Next Agent Execution Plan

## 1. Purpose

This document is the handoff plan for the next agent.

The next task is to continue `TASK-0101 用户组织角色权限模块开发` and implement the backend minimum viable loop for platform user, organization, role, and permission management.

Frontend pages can be handled after the backend loop is stable unless the assigned agent has enough time to complete both.

## 2. Required Reading

Read these files before editing:

1. `AGENTS.md`
2. `CLAUDE.md`
3. `docs/code-map/README.md`
4. `docs/code-map/backend.md`
5. `docs/code-map/api.md`
6. `docs/code-map/database.md`
7. `ai-coding/rules/02_backend_rules.md`
8. `ai-coding/rules/04_database_rules.md`
9. `ai-coding/rules/06_permission_rules.md`
10. `ai-coding/rules/12_code_map_rules.md`
11. `ai-coding/tasks/phase-01/TASK-0101_user_org_permission.md`
12. `docs/module-specs/platform/01_user_org_permission_spec.md`

## 3. Current Baseline

Already implemented and pushed:

- `4c42489 feat: add platform auth skeleton`
- `e9910f9 feat: add platform permission guard`

Available backend capabilities:

- `POST /api/platform/auth/login`
- `GET /api/platform/auth/me`
- `V0001__init_platform_auth_schema.sql`
- Tables:
  - `sys_tenant`
  - `sys_user`
  - `sys_org`
  - `sys_role`
  - `sys_permission`
  - `sys_user_role`
  - `sys_role_permission`
- `@RequiresPermission`
- `PermissionChecker`
- `RequiresPermissionAspect`
- `SecurityContext` with current user id and permission codes.

Important limitation:

- The current token store is in-memory and development-only.
- The initial admin password is `{noop}admin123`.
- Management APIs are not implemented yet.

## 4. Recommended Implementation Order

### Step 1: User Management Backend

Recommended module: `iaf-platform-auth`.

Reason: user login identity already lives in this module, and `JdbcAuthUserRepository` already reads `sys_user`.

Implement APIs:

| Method | Path | Permission |
|---|---|---|
| `GET` | `/api/platform/users` | `platform:user:view` |
| `POST` | `/api/platform/users` | `platform:user:create` |
| `PUT` | `/api/platform/users/{id}` | `platform:user:update` |
| `POST` | `/api/platform/users/{id}/disable` | `platform:user:disable` |
| `POST` | `/api/platform/users/{id}/reset-password` | `platform:user:reset-password` |

Required rules:

- Use `@RequiresPermission` on sensitive APIs or application methods.
- `username` must be unique under the same `tenant_id`.
- Disabled users must not be able to log in.
- Controllers must not access JDBC, Mapper, Repository implementations, or Entities directly.
- Request and response objects must be DTOs or Java records.
- Do not expose `password_hash`.

Suggested package layout:

```text
backend/iaf-platform-auth/src/main/java/com/company/iaf/platform/auth/
  interfaces/controller/UserController.java
  interfaces/dto/UserCreateRequest.java
  interfaces/dto/UserUpdateRequest.java
  interfaces/dto/UserResponse.java
  interfaces/dto/UserPageResponse.java
  application/UserApplicationService.java
  domain/model/UserStatus.java
  domain/model/PlatformUser.java
  domain/repository/PlatformUserRepository.java
  infrastructure/persistence/JdbcPlatformUserRepository.java
```

### Step 2: Organization Management Backend

Module: `iaf-platform-org`.

Implement APIs:

| Method | Path | Permission |
|---|---|---|
| `GET` | `/api/platform/orgs/tree` | `platform:org:view` |
| `POST` | `/api/platform/orgs` | `platform:org:create` |
| `PUT` | `/api/platform/orgs/{id}` | `platform:org:update` |

Required rules:

- `org_code` must be unique under the same `tenant_id`.
- Build tree response by `parent_id`.
- Do not model manufacturing factory, workshop, warehouse, or location here. Those belong to manufacturing/WMS modules.
- Use `@RequiresPermission`.

Suggested package layout:

```text
backend/iaf-platform-org/src/main/java/com/company/iaf/platform/org/
  interfaces/controller/OrgController.java
  interfaces/dto/OrgCreateRequest.java
  interfaces/dto/OrgUpdateRequest.java
  interfaces/dto/OrgTreeNodeResponse.java
  application/OrgApplicationService.java
  domain/model/Org.java
  domain/model/OrgStatus.java
  domain/model/OrgType.java
  domain/repository/OrgRepository.java
  infrastructure/persistence/JdbcOrgRepository.java
```

### Step 3: Role and Permission Assignment Backend

Module: `iaf-platform-permission`.

Implement APIs:

| Method | Path | Permission |
|---|---|---|
| `GET` | `/api/platform/roles` | `platform:role:view` |
| `POST` | `/api/platform/roles` | `platform:role:create` |
| `PUT` | `/api/platform/roles/{id}` | `platform:role:update` |
| `PUT` | `/api/platform/roles/{id}/permissions` | `platform:role:assign-permission` |

Required rules:

- `role_code` must be unique under the same `tenant_id`.
- Assigned permission codes must exist in `sys_permission`.
- Role permission assignment should replace the role's current permission set atomically.
- Do not implement complex data permission or field permission yet; keep extension points only.
- Use `@RequiresPermission`.

Suggested package layout:

```text
backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/
  interfaces/controller/RoleController.java
  interfaces/dto/RoleCreateRequest.java
  interfaces/dto/RoleUpdateRequest.java
  interfaces/dto/RoleResponse.java
  interfaces/dto/AssignRolePermissionsRequest.java
  application/service/RoleApplicationService.java
  domain/model/Role.java
  domain/model/RoleStatus.java
  domain/model/Permission.java
  domain/repository/RoleRepository.java
  domain/repository/PermissionRepository.java
  infrastructure/persistence/JdbcRoleRepository.java
  infrastructure/persistence/JdbcPermissionRepository.java
```

## 5. Database Work

Do not edit `V0001__init_platform_auth_schema.sql`.

If existing tables are enough, do not add a migration.

If seed data must be extended, add:

```text
backend/iaf-app/src/main/resources/db/migration/V0002__seed_platform_management_permissions.sql
```

Seed these permission codes:

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

Also bind all of them to the seeded `platform_admin` role.

Use idempotent SQL where practical, for example `where not exists`, because local databases may already contain partial seed data during development.

## 6. Testing Requirements

Minimum tests:

- User creation succeeds.
- Duplicate username under same tenant fails.
- User disable succeeds.
- Disabled user cannot log in.
- Organization creation succeeds.
- Organization tree returns correct parent-child structure.
- Duplicate org code under same tenant fails.
- Role creation succeeds.
- Duplicate role code under same tenant fails.
- Role permission assignment succeeds.
- Unauthenticated sensitive API access returns HTTP 401.
- Authenticated user without permission returns HTTP 403.
- Authenticated user with permission succeeds.

Preferred test levels:

- Application service tests for business rules.
- Controller tests for permission and response envelope.
- Repository or migration checks for SQL behavior when JDBC repositories are added.

## 7. Code Map Updates

Update the code map in the same task.

Required updates:

- `docs/code-map/backend.md`
  - New controllers.
  - New DTOs.
  - New application services.
  - New domain models.
  - New repository interfaces and implementations.
  - New tests.
- `docs/code-map/api.md`
  - All new HTTP APIs.
  - Auth requirement.
  - Permission code.
  - Request and response shape.
  - Error behavior.
- `docs/code-map/database.md`
  - New migration, if any.
  - Seeded permission codes and role bindings, if any.
- `docs/code-map/frontend.md`
  - Keep as not initialized if no frontend work is done.

If the code map is intentionally not updated, the final report must explicitly explain why.

## 8. Quality Gate

Run before committing:

```bash
./scripts/check-quality.sh
```

If a migration is added, also run it against a clean PostgreSQL database.

Known local environment note:

- Port `5432` may be occupied by another project container.
- Do not stop or modify unrelated containers.
- Use a temporary PostgreSQL container without host port mapping for migration checks.

Example:

```bash
docker run --rm --name iaf-migration-check \
  -e POSTGRES_DB=iaf_migration_check \
  -e POSTGRES_USER=iaf \
  -e POSTGRES_PASSWORD=iaf \
  -d postgres:18

docker exec iaf-migration-check pg_isready -U iaf -d iaf_migration_check
docker exec -i iaf-migration-check psql -v ON_ERROR_STOP=1 -U iaf -d iaf_migration_check \
  < backend/iaf-app/src/main/resources/db/migration/V0001__init_platform_auth_schema.sql

# Run V0002 here if created.

docker rm -f iaf-migration-check
```

## 9. Commit Plan

Use small commits.

Recommended commits:

```text
feat: add platform user management
feat: add platform org management
feat: add platform role permission management
```

Before each commit:

```bash
git status --short --branch
./scripts/check-quality.sh
```

Push:

```bash
git push origin main
```

## 10. Final Report Requirements

The final report must include:

- Summary.
- Files changed.
- Architecture impact.
- Database migration impact.
- Permission impact.
- Code map impact.
- Tests run and results.
- Quality gate status.
- Known risks.
- Suggested next steps.
