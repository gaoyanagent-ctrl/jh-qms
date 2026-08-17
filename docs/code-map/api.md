# API Code Map

## MDM

- `GET /api/mdm/models` (`mdm:model:view`): list models with field/UI schema.
- `GET /api/mdm/models/{modelCode}/schema` (`mdm:model:view`): resolve one model definition.
- `GET /api/mdm/models/{modelCode}/records` (`mdm:record:view`): tenant-scoped dynamic record page.
- `POST /api/mdm/models/{modelCode}/records` (`mdm:record:create`): unified metadata validation and versioned create.
- `PUT /api/mdm/models/{modelCode}/records/{id}` (`mdm:record:update`): optimistic-lock update using `expectedVersion`.
- `POST /api/mdm/models` (`mdm:model:create`): create a model draft in an enabled data domain.
- `PUT /api/mdm/models/{modelCode}/draft` (`mdm:model:update`): validate and replace draft fields and UI Schema.
- `POST /api/mdm/models/{modelCode}/validate` (`mdm:model:update`): report definition errors and warnings.
- `POST /api/mdm/models/{modelCode}/publish` (`mdm:model:publish`): publish an immutable model-version snapshot.
- `POST /api/mdm/models/{modelCode}/records/batch-validate` (`mdm:record:create`): return row-level validation for pasted or imported records.
- `POST /api/mdm/models/{modelCode}/records/batch` (`mdm:record:create`): atomically create a validated batch through the normal record/version pipeline.
- `GET /api/mdm/models/{modelCode}/import-template` (`mdm:record:view`): download a dynamic `.xlsx` template with field descriptions and enum constraints.
- `POST /api/mdm/models/{modelCode}/imports` (`mdm:record:create`): upload `.xlsx`/`.xls`, parse at most 1,000 rows, persist the task snapshot and return a precheck preview without creating master records.
- `GET /api/mdm/models/{modelCode}/imports` (`mdm:record:view`): list the latest 100 persisted import tasks.
- `GET /api/mdm/models/{modelCode}/imports/{taskId}/errors` (`mdm:record:view`): return the stored row-level precheck result.
- `POST /api/mdm/models/{modelCode}/imports/{taskId}/commit` (`mdm:record:create`): revalidate, atomically claim and commit a ready task through the unified record pipeline.

All backend APIs must use:

- Path format: `/api/{module}/{resources}`
- Response envelope: `Result<T>` for object responses and `PageResult<T>` for paginated data.
- Error envelope: `Result.fail(code, message)`.
- Permission codes: `module:object:action`.

## Current APIs

### `POST /api/platform/auth/login`

- Owner module: `iaf-platform-auth`
- Controller: `com.company.iaf.platform.auth.interfaces.controller.AuthController`
- Method: `login(LoginRequest)`
- Authentication: public.
- Permission: none.
- Purpose: authenticate a platform user with tenant code, username, and password.
- Request:

```json
{
  "tenantCode": "acme",
  "username": "admin",
  "password": "admin123"
}
```

- Tenant behavior: login resolves `tenantCode` from the request body, then looks up the user by `tenantId + username`. Unknown tenant, disabled tenant, missing user, disabled user, or wrong password all return `COMMON_UNAUTHORIZED`.

- Response:

```json
{
  "success": true,
  "code": "OK",
  "message": "OK",
  "data": {
    "tokenType": "Bearer",
    "accessToken": "...",
    "expiresAt": "2026-07-03T16:00:00Z",
    "tenantId": 1,
    "userId": 1,
    "username": "admin",
    "displayName": "Platform Administrator",
    "currentOrgId": 1,
    "organizations": [
      {
        "orgId": 1,
        "orgCode": "CORP",
        "orgName": "IAF Industrial Corp",
        "primary": true
      }
    ],
    "permissions": ["platform:auth:me"]
  }
}
```

### `GET /api/platform/auth/me`

- Owner module: `iaf-platform-auth`
- Controller: `com.company.iaf.platform.auth.interfaces.controller.AuthController`
- Method: `me(AuthenticatedUser)`
- Authentication: Bearer token required.
- Permission: authenticated user baseline; seeded permission code is `platform:auth:me`.
- Purpose: return current authenticated user and permissions.
- Response includes `currentOrgId` and active organization assignment snapshot.
- Tenant behavior: authenticated APIs use `tenantId` from the bearer token. Normal business APIs do not accept tenant header override.

### `GET /api/platform/auth/menus`

- Owner module: `iaf-platform-permission`
- Controller: `com.company.iaf.platform.permission.interfaces.controller.AuthMenuController`
- Authentication: Bearer token required.
- Permission: `platform:auth:me`.
- Purpose: return visible and enabled navigation menus reachable through the current user's roles and current permission codes.
- Response: `Result<List<MenuResponse>>`; each menu node includes route/component metadata, linked permission codes, and children.
- Notes: a role-assigned menu with linked permission codes is returned only when the current user has at least one linked permission; role-assigned menus without linked permission codes remain visible.

### `GET /api/platform/users`

- Owner module: `iaf-platform-auth`
- Controller: `com.company.iaf.platform.auth.interfaces.controller.UserController`
- Method: `list(keyword, pageNo, pageSize)`
- Authentication: Bearer token required.
- Permission: `platform:user:view`.
- Purpose: paginated tenant-scoped user lookup with optional keyword search across username, display name, mobile, email.
- Data permission: TASK-0218 tracer bullet. The list is restricted to users assigned to the current organization scope from the authenticated context; callers with API permission but no current organization scope receive an empty page.
- Response: `Result<PageResult<UserResponse>>` with `records`, `total`, `pageNo`, `pageSize`.

### `GET /api/platform/users/{id}`

- Owner module: `iaf-platform-auth`
- Controller: `com.company.iaf.platform.auth.interfaces.controller.UserController`
- Authentication: Bearer token required.
- Permission: `platform:user:view`.
- Errors: `PLATFORM_AUTH_USER_NOT_FOUND` (HTTP 400 via the unified envelope).
- Response includes active `organizations` and `primaryOrgId`.

### `GET /api/platform/users/me`

- Owner module: `iaf-platform-auth`
- Controller: `com.company.iaf.platform.auth.interfaces.controller.UserController`
- Authentication: Bearer token required.
- Permission: `platform:auth:me`.
- Purpose: return current platform user profile with active organization assignments.

### `GET /api/platform/users/{id}/orgs`

- Owner module: `iaf-platform-auth`
- Controller: `com.company.iaf.platform.auth.interfaces.controller.UserController`
- Authentication: Bearer token required.
- Permission: `platform:user:view`.
- Purpose: query active organization assignments for a platform user.
- Response: `Result<UserOrganizationsResponse>` with `userId`, `primaryOrgId`, and `organizations`.

### `PUT /api/platform/users/{id}/orgs`

- Owner module: `iaf-platform-auth`
- Controller: `com.company.iaf.platform.auth.interfaces.controller.UserController`
- Authentication: Bearer token required.
- Permission: `platform:user:update`.
- Purpose: replace a user's organization assignments and synchronize `sys_user_org.is_primary` plus `sys_user.primary_org_id`.
- Request:

```json
{
  "organizations": [
    { "orgId": 1, "primary": true, "scopeWeight": 100 },
    { "orgId": 2, "primary": false, "scopeWeight": 0 }
  ]
}
```

- Validation: duplicate org ids are rejected; non-empty assignment sets must contain exactly one primary organization; org ids must exist in the current tenant.

### `PATCH /api/platform/users/{id}/org-context`

- Owner module: `iaf-platform-auth`
- Controller: `com.company.iaf.platform.auth.interfaces.controller.UserController`
- Authentication: Bearer token required.
- Permission: `platform:auth:me`; only the current user may switch their own organization context.
- Purpose: switch current user's organization context after validating active assignment membership; updates `sys_user_org.is_primary` and `sys_user.primary_org_id` together.
- Request: `{"orgId": 1}`.

### `POST /api/platform/users`

- Owner module: `iaf-platform-auth`
- Controller: `com.company.iaf.platform.auth.interfaces.controller.UserController`
- Authentication: Bearer token required.
- Permission: `platform:user:create`.
- Request:

```json
{
  "username": "alice",
  "password": "password123",
  "displayName": "Alice",
  "mobile": "13800000000",
  "email": "alice@example.com"
}
```

- Errors: `PLATFORM_AUTH_USERNAME_ALREADY_EXISTS`, `COMMON_VALIDATION_FAILED`.
- Password handling: plaintext is hashed via `PasswordEncoder` inside the application service.

### `PUT /api/platform/users/{id}`

- Owner module: `iaf-platform-auth`
- Controller: `com.company.iaf.platform.auth.interfaces.controller.UserController`
- Authentication: Bearer token required.
- Permission: `platform:user:update`.
- Request: `displayName`, `mobile`, `email` (no username / status / primary organization changes).
- Notes: primary organization is changed only through `PUT /api/platform/users/{id}/orgs` so `sys_user.primary_org_id` stays synchronized with `sys_user_org`.

### `POST /api/platform/users/{id}/disable`

- Owner module: `iaf-platform-auth`
- Controller: `com.company.iaf.platform.auth.interfaces.controller.UserController`
- Authentication: Bearer token required.
- Permission: `platform:user:disable`.
- Errors: `PLATFORM_AUTH_CANNOT_DISABLE_SELF`, `PLATFORM_AUTH_USER_NOT_FOUND`, `COMMON_CONFLICT`.

### `POST /api/platform/users/{id}/reset-password`

- Owner module: `iaf-platform-auth`
- Controller: `com.company.iaf.platform.auth.interfaces.controller.UserController`
- Authentication: Bearer token required.
- Permission: `platform:user:reset-password`.
- Request: `{"newPassword": "newSecret123"}` (min 8 chars).

### `GET /api/platform/orgs/tree`

- Owner module: `iaf-platform-org`
- Controller: `com.company.iaf.platform.org.interfaces.controller.OrgController`
- Authentication: Bearer token required.
- Permission: `platform:org:view`.
- Purpose: tenant-scoped hierarchical organization tree. Built server-side from a flat `sys_org` listing.
- Response: `Result<List<OrgTreeNodeResponse>>` where each node carries its children list.

### `GET /api/platform/orgs/{id}`

- Owner module: `iaf-platform-org`
- Controller: `com.company.iaf.platform.org.interfaces.controller.OrgController`
- Authentication: Bearer token required.
- Permission: `platform:org:view`.

### `POST /api/platform/orgs`

- Owner module: `iaf-platform-org`
- Controller: `com.company.iaf.platform.org.interfaces.controller.OrgController`
- Authentication: Bearer token required.
- Permission: `platform:org:create`.
- Request: `parentId?`, `orgCode`, `orgName`, `orgType`, `status?`, `sortNo?`.

### `PUT /api/platform/orgs/{id}`

- Owner module: `iaf-platform-org`
- Controller: `com.company.iaf.platform.org.interfaces.controller.OrgController`
- Authentication: Bearer token required.
- Permission: `platform:org:update`.
- Errors: `PLATFORM_ORG_CODE_ALREADY_EXISTS`, `PLATFORM_ORG_PARENT_NOT_FOUND`, `PLATFORM_ORG_NOT_FOUND`, `COMMON_CONFLICT`.

### `GET /api/platform/roles`

- Owner module: `iaf-platform-permission`
- Controller: `com.company.iaf.platform.permission.interfaces.controller.RoleController`
- Authentication: Bearer token required.
- Permission: `platform:role:view`.
- Response: `Result<PageResult<RoleResponse>>`; each `RoleResponse` includes the current permission code list.

### `GET /api/platform/roles/{id}`

- Owner module: `iaf-platform-permission`
- Controller: `com.company.iaf.platform.permission.interfaces.controller.RoleController`
- Authentication: Bearer token required.
- Permission: `platform:role:view`.

### `POST /api/platform/roles`

- Owner module: `iaf-platform-permission`
- Controller: `com.company.iaf.platform.permission.interfaces.controller.RoleController`
- Authentication: Bearer token required.
- Permission: `platform:role:create`.
- Request: `roleCode`, `roleName`, `roleType`, `status?`.

### `PUT /api/platform/roles/{id}`

- Owner module: `iaf-platform-permission`
- Controller: `com.company.iaf.platform.permission.interfaces.controller.RoleController`
- Authentication: Bearer token required.
- Permission: `platform:role:update`.
- Errors: `PLATFORM_PERMISSION_ROLE_CODE_ALREADY_EXISTS`, `PLATFORM_PERMISSION_ROLE_NOT_FOUND`, `COMMON_CONFLICT`.

### `PUT /api/platform/roles/{id}/permissions`

- Owner module: `iaf-platform-permission`
- Controller: `com.company.iaf.platform.permission.interfaces.controller.RoleController`
- Authentication: Bearer token required.
- Permission: `platform:role:assign-permission`.
- Request: `{"permissionCodes": ["platform:user:view", "platform:role:update"]}` — replaces the role's permission set atomically.
- Errors: `PLATFORM_PERMISSION_NOT_FOUND` (lists missing codes), `PLATFORM_PERMISSION_ROLE_NOT_FOUND`.

### `PUT /api/platform/roles/{id}/menus`

- Owner module: `iaf-platform-permission`
- Controller: `com.company.iaf.platform.permission.interfaces.controller.RoleController`
- Authentication: Bearer token required.
- Permission: `platform:role:assign-menu`.
- Request: `{"menuCodes": ["platform.users", "platform.roles"]}` — replaces the role's menu set atomically.
- Errors: `PLATFORM_PERMISSION_MENU_NOT_FOUND`, `PLATFORM_PERMISSION_ROLE_NOT_FOUND`.

### `GET /api/platform/menus/tree`

- Owner module: `iaf-platform-permission`
- Controller: `com.company.iaf.platform.permission.interfaces.controller.MenuController`
- Authentication: Bearer token required.
- Permission: `platform:menu:view`.
- Purpose: tenant-scoped platform menu tree management view including linked permission codes.
- Response: `Result<List<MenuResponse>>`.

### `POST /api/platform/menus`

- Owner module: `iaf-platform-permission`
- Controller: `com.company.iaf.platform.permission.interfaces.controller.MenuController`
- Authentication: Bearer token required.
- Permission: `platform:menu:create`.
- Request: `parentId?`, `menuCode`, `menuType`, `titleKey`, `routePath?`, `componentKey?`, `icon?`, `sortNo?`, `visible`, `enabled`.
- Errors: `PLATFORM_PERMISSION_MENU_CODE_ALREADY_EXISTS`, `PLATFORM_PERMISSION_MENU_NOT_FOUND`, `COMMON_VALIDATION_FAILED`.

### `PUT /api/platform/menus/{id}`

- Owner module: `iaf-platform-permission`
- Controller: `com.company.iaf.platform.permission.interfaces.controller.MenuController`
- Authentication: Bearer token required.
- Permission: `platform:menu:update`.
- Request: same fields as `POST /api/platform/menus`.
- Errors: `PLATFORM_PERMISSION_MENU_CODE_ALREADY_EXISTS`, `PLATFORM_PERMISSION_MENU_NOT_FOUND`, `COMMON_CONFLICT`.
- Notes: backend rejects self-parent and descendant-parent updates to prevent menu cycles.

### `GET /api/platform/permissions`

- Owner module: `iaf-platform-permission`
- Controller: `com.company.iaf.platform.permission.interfaces.controller.PermissionController`
- Authentication: Bearer token required.
- Permission: `platform:permission:view`.
- Purpose: list tenant permissions for role permission assignment.
- Response: `Result<List<PermissionResponse>>`.

### `GET /api/platform/theme/current`

- Owner module: `iaf-platform-system`
- Controller: `com.company.iaf.platform.system.interfaces.controller.SystemConfigurationController`
- Authentication: Bearer token required.
- Permission: `platform:theme:view`.
- Purpose: return the current tenant theme configuration.

### `PUT /api/platform/theme/current`

- Owner module: `iaf-platform-system`
- Authentication: Bearer token required.
- Permission: `platform:theme:update`.
- Request: `themeName`, `primaryColor`, `sidebarMode`, `tokens`.
- Errors: `PLATFORM_SYSTEM_INVALID_CONFIGURATION`.

### `GET /api/platform/brand/current`

- Owner module: `iaf-platform-system`
- Authentication: Bearer token required.
- Permission: `platform:brand:view`.
- Purpose: return the current tenant brand and login visual configuration.

### `PUT /api/platform/brand/current`

- Owner module: `iaf-platform-system`
- Authentication: Bearer token required.
- Permission: `platform:brand:update`.
- Request: brand name, logo/favicon URLs, login title/subtitle/operations copy, and login background settings.
- Errors: `PLATFORM_SYSTEM_INVALID_CONFIGURATION`.

### `GET /api/platform/i18n/resources`

- Owner module: `iaf-platform-system`
- Authentication: Bearer token required.
- Permission: `platform:i18n:view`.
- Query: `locale`.
- Purpose: list tenant-scoped remote i18n resources for a locale.

### `PUT /api/platform/i18n/resources`

- Owner module: `iaf-platform-system`
- Authentication: Bearer token required.
- Permission: `platform:i18n:update`.
- Request: `locale` plus `resources[]` containing `resourceKey` and `resourceValue`; replaces active rows for the locale.

### `GET /api/platform/preferences/me`

- Owner module: `iaf-platform-system`
- Authentication: Bearer token required.
- Permission: `platform:preference:me`.
- Purpose: return current user's tenant-scoped UI experience preferences.

### `PUT /api/platform/preferences/me`

- Owner module: `iaf-platform-system`
- Authentication: Bearer token required.
- Permission: `platform:preference:me`.
- Request: `{"settings": {...}}`; stores theme, density, sidebar, form interaction, surface width, motion, and workspace-mode preferences as JSON.

## Platform Foundation Permissions

TASK-0220 seeds these permission codes for current and upcoming platform foundation pages:

- `platform:data-permission:view`
- `platform:data-permission:update`
- `platform:field-permission:view`
- `platform:field-permission:update`
- `platform:dictionary:view`
- `platform:dictionary:update`
- `platform:parameter:view`
- `platform:parameter:update`
- `platform:audit:view`

Current frontend route usage:

- `/platform/dictionaries`: guarded by `platform:dictionary:view` or `platform:parameter:view`.
- `/platform/audit-logs`: guarded by `platform:audit:view`.

Dictionary, parameter, audit, data-permission, and field-permission backend APIs remain future work unless a task introduces them explicitly.

### Platform Foundation Configuration Snapshot API

- Status: intentionally deferred by ADR-0008.
- Current delivery mechanism: `ai-coding/templates/platform-foundation/platform-foundation-delivery-package.json` plus Flyway seed migrations and TASK-0219 tenant initialization copy path.
- Reason: platform dictionaries, parameters, audit, data-permission, and field-permission APIs are not all production-backed yet; adding a snapshot export/import API now would either expose incomplete runtime state or create a parallel persistence contract.
- Validation: `scripts/check-platform-foundation-templates.js` verifies the productization template and is part of `scripts/check-quality.sh`.

### `GET /api/health`

- Owner module: `iaf-app`
- Controller: `com.company.iaf.app.interfaces.controller.HealthCheckController`
- Method: `health()`
- Authentication: public.
- Permission: none.
- Purpose: basic application health probe.
- Response:

```json
{
  "success": true,
  "code": "OK",
  "message": "OK",
  "data": {
    "status": "OK"
  }
}
```

### `GET /api/platform/tenants`

- Owner module: `iaf-platform-auth`
- Controller: `TenantController`
- Authentication: Bearer token required.
- Permission: `platform:tenant:view`.
- Purpose: paginated tenant lifecycle list with optional keyword filter.

### `POST /api/platform/tenants`

- Owner module: `iaf-platform-auth`
- Authentication: Bearer token required.
- Permission: `platform:tenant:create`.
- Purpose: create a tenant and initialize root org, admin user, role, permissions, menus, system config, quota, and `TenantCreatedEvent` outbox record.
- Request: `tenantCode`, `tenantName`, `adminUsername`, `adminPassword`.
- Errors: `PLATFORM_AUTH_TENANT_CODE_ALREADY_EXISTS`.

### `GET /api/platform/tenants/{id}`

- Owner module: `iaf-platform-auth`
- Authentication: Bearer token required.
- Permission: `platform:tenant:view`.
- Purpose: tenant detail.

### `PUT /api/platform/tenants/{id}`

- Owner module: `iaf-platform-auth`
- Authentication: Bearer token required.
- Permission: `platform:tenant:update`.
- Purpose: update tenant display name with optimistic-lock persistence.

### `POST /api/platform/tenants/{id}/enable`

- Owner module: `iaf-platform-auth`
- Authentication: Bearer token required.
- Permission: `platform:tenant:enable`.
- Purpose: enable tenant login and write operations.

### `POST /api/platform/tenants/{id}/disable`

- Owner module: `iaf-platform-auth`
- Authentication: Bearer token required.
- Permission: `platform:tenant:disable`.
- Purpose: disable tenant. Tenant-aware login rejects disabled tenants; user write operations reject disabled tenants with `PLATFORM_AUTH_TENANT_DISABLED`.

### `GET /api/platform/tenants/{id}/quotas`

- Owner module: `iaf-platform-auth`
- Authentication: Bearer token required.
- Permission: `platform:tenant-quota:view`.
- Purpose: list tenant quota settings and usage snapshots.

### `PUT /api/platform/tenants/{id}/quotas`

- Owner module: `iaf-platform-auth`
- Authentication: Bearer token required.
- Permission: `platform:tenant-quota:update`.
- Purpose: upsert a tenant quota. TASK-0219 closes `USER_COUNT`; user creation fails with `PLATFORM_AUTH_TENANT_QUOTA_EXCEEDED` when exceeded.
- Request: `quotaKey`, `quotaLimit`.

### `GET /api/platform/outbox-events`

- Owner module: `iaf-platform-integration`
- Controller: `OutboxEventController`
- Authentication: Bearer token required.
- Permission: `platform:outbox:view`.
- Purpose: paginated operational list of target-tenant outbox events by optional status.
- Query: `tenantId`, optional `status`, `pageNo`, `pageSize`.

### `POST /api/platform/outbox-events/{id}/retry`

- Owner module: `iaf-platform-integration`
- Authentication: Bearer token required.
- Permission: `platform:outbox:retry`.
- Purpose: mark a failed outbox event as `PENDING` for redispatch.
- Query: `tenantId` target tenant owning the event.

## Error Handling

Global handler:

- Class: `com.company.iaf.app.interfaces.controller.GlobalExceptionHandler`
- `BusinessException`: HTTP 400, business error code.
- `MethodArgumentNotValidException`: HTTP 400, `COMMON_VALIDATION_FAILED`.
- Unhandled `Exception`: HTTP 500, `COMMON_INTERNAL_ERROR`.
- Unauthenticated request: HTTP 401, `COMMON_UNAUTHORIZED`.
- Access denied: HTTP 403, `COMMON_FORBIDDEN`.

## Release Governance

Platform Foundation RC1 release validation is documented in:

- `docs/operations/platform-foundation-release-checklist.md`
- `docs/operations/platform-foundation-smoke-test.md`
- `docs/operations/platform-foundation-known-issues.md`
- `docs/operations/platform-foundation-feedback-log.md`
- `docs/operations/platform-foundation-stabilization-plan.md`

`scripts/platform-foundation-smoke-test.sh` uses existing APIs only. It does not add an internal verification endpoint and does not change API compatibility rules. Published platform API paths, permission codes, menu codes, i18n keys, delivery package schema fields, and migration history are treated as compatibility-managed contracts after RC1.

TASK-0223 adds no API paths, request/response fields, error codes, authentication behavior, or permission codes. Stabilization feedback that requires an API change must be split into a new task and reflected in this file.

## QMS Engineering Data APIs

All endpoints require Bearer authentication, tenant context, and a current organization.
The current organization is applied as a mandatory data scope. API paths follow IAF's
`/api/{module}/{resources}` rule rather than the source design's `/api/v1` suggestion.

Frontend consumer: `frontend/apps/pc-admin/src/modules/qms/engineering/api.ts` provides the
typed TASK-0402 client; `hooks.ts` is the only business-page server-state entrypoint.
The frontend does not redefine, wrap, or version these endpoints.

### Parts

- `GET /api/qms/parts`
  - Permission: `qms:part:view`.
  - Query: optional `keyword`, `pageNo` (default 1), `pageSize` (default 20, max 200).
  - Response: `Result<PageResult<PartResponse>>`.
- `GET /api/qms/parts/{id}`
  - Permission: `qms:part:view`.
  - Error: `QMS_PART_NOT_FOUND`.
- `POST /api/qms/parts`
  - Permission: `qms:part:create`.
  - Request: `partNo`, optional `materialNo`, `partName`, optional `customerId`,
    `vehicleModel`, `supplierId`, `importanceLevel`.
  - Errors: `QMS_PART_NO_ALREADY_EXISTS`, `COMMON_VALIDATION_FAILED`.

### Drawings

- `GET /api/qms/parts/{partId}/drawings`
  - Permission: `qms:drawing:view`.
- `POST /api/qms/parts/{partId}/drawings`
  - Permission: `qms:drawing:create`.
  - Request: `drawingNo`, `drawingName`, `drawingType`, optional `sourceSystem`.
  - Errors: `QMS_PART_NOT_FOUND`, `QMS_DRAWING_NO_ALREADY_EXISTS`.
- `GET /api/qms/drawings/{id}`
  - Permission: `qms:drawing:view`.
  - Error: `QMS_DRAWING_NOT_FOUND`.

### Drawing Revisions

- `GET /api/qms/drawings/{drawingId}/revisions`
  - Permission: `qms:drawing-revision:view`.
  - Response is ordered by descending `revisionSeq`.
- `POST /api/qms/drawings/{drawingId}/revisions`
  - Permission: `qms:drawing-revision:create`.
  - Request: `revisionCode`, optional `effectiveDate`, optional `supersedesRevisionId`.
  - Creates metadata only in `DRAFT`, with parse/review status `PENDING`.
  - Errors: `QMS_DRAWING_NOT_FOUND`, `QMS_DRAWING_REVISION_CODE_ALREADY_EXISTS`,
    `QMS_DRAWING_REVISION_SUPERSEDES_INVALID`.
- `GET /api/qms/drawing-revisions/{id}`
  - Permission: `qms:drawing-revision:view`.
  - Error: `QMS_DRAWING_REVISION_NOT_FOUND`.

Create idempotency currently uses tenant-scoped natural-key constraints to reject replayed
business keys without creating duplicate rows. A transport `Idempotency-Key` ledger is
deferred to the upload workflow.
# QMS drawing revision files

- A revision owns two role-specific files: `DWG_SOURCE` for parsing and
  `PDF_REFERENCE` for human-readable visual comparison.
- `GET /api/qms/drawing-revisions/{revisionId}/files` — list both attachments.
- `POST /api/qms/drawing-revisions/{revisionId}/files/{role}` — upload the file for one role.
- `GET /api/qms/drawing-revisions/{revisionId}/files/{role}/content` — authenticated role content.
- `POST /api/qms/drawing-revisions/{revisionId}/file` — legacy primary PDF/DWG upload.
- `GET /api/qms/drawing-revisions/{revisionId}/file` — file metadata.
- `GET /api/qms/drawing-revisions/{revisionId}/file/content` — authenticated content download.

# QMS drawing parse orchestration

- `GET /api/qms/drawing-revisions/{revisionId}/parse-job` — latest attempt; revision-view permission.
- `GET /api/qms/drawings/{drawingId}/parse-jobs` — latest attempt for each drawing revision.
- `POST /api/qms/drawing-revisions/{revisionId}/parse-job/retry` — retry a failed latest
  attempt; `qms:drawing-revision:retry-parse` permission.

# QMS drawing parse results

All endpoints require `qms:drawing-revision:view` and enforce the current tenant and organization.

- `GET /api/qms/drawing-revisions/{revisionId}/intermediate-model` — versioned DIM JSON.
- `GET /api/qms/drawing-revisions/{revisionId}/entities` — normalized entity projection.
- `GET /api/qms/drawing-revisions/{revisionId}/evidence` — evidence list with viewer BBox.
- `GET /api/qms/drawing-revisions/{revisionId}/evidence/{evidenceId}` — scoped evidence detail.

The parser lifecycle is an internal application port, not an unauthenticated HTTP API.

# QMS quality characteristic review

- `GET /api/qms/drawing-revisions/{revisionId}/characteristics` — revision-scoped candidates;
  `qms:drawing-revision:view` permission.
- `POST /api/qms/drawing-revisions/{revisionId}/characteristics` — manually create a
  revision-linked characteristic; `qms:quality-characteristic:review` permission.
- `POST /api/qms/drawing-revisions/{revisionId}/characteristics/{id}/confirm|reject` — edit
  classification and decide one pending candidate with optimistic locking.
- `POST /api/qms/drawing-revisions/{revisionId}/characteristics/bulk-review` — atomically
  confirm or reject selected pending candidates using an id/version pair for every row.
# QMS drawing legend configuration

- `GET /api/qms/drawing-legend-rules` lists tenant legend mappings.
- `PUT /api/qms/drawing-legend-rules` applies versioned bulk edits and reclassifies
  evidence-backed pending candidates; both require `qms:drawing-legend:manage`.
# QMS inspection standard draft

- `GET /api/qms/drawing-revisions/{revisionId}/inspection-standard` reads the current draft.
- `POST .../generate` creates a rule-derived draft, or synchronizes an existing editable draft
  with eligible confirmed characteristics while preserving user-entered execution fields.
- `PUT .../{id}` saves execution fields and reaction plan with optimistic locking.
- `POST .../{id}/submit-approval` freezes a reviewed draft and starts an approval instance.
- `POST .../{id}/approve|reject` records a human decision; self-approval is forbidden.
- `POST .../{id}/release` releases an approved, fully reviewed standard as read-only.
- Permissions: `qms:inspection-standard:submit|approve|release` for their respective actions.
# QMS validation plan

- `GET /api/qms/inspection-standards/{standardId}/validation-plan` reads the current plan.
- `POST .../generate` creates a draft from released-standard performance items.
- `PUT .../{id}` confirms DV/PV, lab, quantity, dates, criteria, and equivalence fields.
- `POST .../{id}/submit-approval|approve|reject|release` controls publication.
