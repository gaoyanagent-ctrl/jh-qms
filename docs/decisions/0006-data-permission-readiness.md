# ADR-0006 Data Permission Readiness

## Status

Accepted

## Context

API permission codes answer whether a user may call an operation. Manufacturing systems also require data range restrictions such as tenant, company, plant, warehouse, organization, and self-owned records.

This task establishes the model and integration path, and ships a minimal user-list tracer bullet without adding incomplete authorization management tables.

## Decision

Data permission scope types:

- `TENANT`
- `COMPANY`
- `PLANT`
- `WAREHOUSE`
- `ORG`
- `SELF`
- `CUSTOM`

Planned binding tables:

- `sys_role_data_scope`: role-to-data-scope grants.
- `sys_user_data_scope`: user-specific data-scope overrides.

Application service pattern:

- API permission remains enforced by `@RequiresPermission`.
- Application services ask the permission module for effective data scopes for the current user and resource object.
- If a user has API permission but no data scope, list queries must return empty results rather than widening access.
- Write operations must validate that the target resource falls inside the effective scope.

Repository pattern:

- Repositories consume a structured data-scope condition object, not raw SQL fragments from callers.
- Tenant predicate is mandatory and is not considered a data-scope grant.
- Scope predicates must be built from typed fields such as `company_id`, `plant_id`, `warehouse_id`, `org_id`, or owner `user_id`.

Tracer bullet:

- The first implementation target is `GET /api/platform/users`.
- `UserApplicationService.listUsers` keeps `platform:user:view` API permission enforcement and derives the current minimal `ORG` scope from `SecurityContext.currentOrgId`.
- If a user has API permission but no current organization scope, the user list returns an empty page.
- `PlatformUserRepository` consumes a typed `UserDataScope`; `JdbcPlatformUserRepository` applies the scope through `sys_user_org` instead of accepting raw SQL fragments from callers.

## Consequences

- No new data permission migration is added in TASK-0218.
- TASK-0218 includes the production user-list tracer bullet and tests for empty/no-scope and organization-scope filtering.
- A later task must add the actual data-scope tables, management APIs, and effective role/user scope resolution.
