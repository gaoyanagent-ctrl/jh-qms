# Backend Code Map

## Backend Shape

The backend is a Java 21, Spring Boot 3.x modular monolith under `backend/`.

Root Maven project:

- `backend/pom.xml`
- Group: `com.company.iaf`
- Version: `0.1.0-SNAPSHOT`
- Packaging: `pom`
- Main dependency management: Spring Boot BOM, Flyway, MyBatis Plus, springdoc.
- Plugin management: compiler release 21, Surefire JUnit Platform support, Spring Boot plugin.

All business modules must use these internal package layers when they contain code:

```text
interfaces
application
domain
infrastructure
```

Cross-module calls must not reach into another module's `infrastructure`, entity, mapper, or adapter packages.

## Maven Modules

### `iaf-mdm`

- `ModelDefinitionValidator` checks field codes, duplicates, data types, enum options and design warnings before publish.
- `MdmExcelImportService` generates model-version-aware Excel templates, persists workbook precheck tasks, revalidates on commit and uses atomic task claiming before the unified batch write (synchronous limit: 1,000 rows).
- `MdmImportObjectStorage` is the MDM-owned object-storage port; `MinioMdmImportObjectStorage` archives source workbooks under tenant-isolated keys without depending on QMS infrastructure.
- `MdmImportTaskDispatcher` claims queued tasks on a scheduled worker, parses archived workbooks and persists validation results for up to 10,000 rows.

- Owner module: cross-application master data authority.
- Layers: `interfaces`, `application`, `domain`, `infrastructure`.
- `MdmController`: model/schema and dynamic record REST entrypoint.
- `MdmApplicationService`: tenant-scoped unified validation, create/update, version snapshot and optimistic-lock transaction boundary.
- `DynamicRecordValidator`: metadata-driven required/type/enum validation.
- `MdmRepository` / `JdbcMdmRepository`: metadata, JSONB record and immutable version persistence.
- Depends on: `iaf-platform-core`, Spring JDBC, Jackson. It does not depend on QMS/WMS infrastructure.

### `iaf-app`

- Owner module: application bootstrap and HTTP entrypoint.
- Purpose: single deployable Spring Boot application that wires platform/manufacturing/WMS modules.
- Depends on: platform modules, manufacturing master, WMS inbound, WMS strategy, Spring Web, Validation, Security, Spring JDBC, MyBatis Plus, Flyway core, Flyway PostgreSQL database support, PostgreSQL driver, springdoc.
- Runtime notes:
  - `server.port` defaults to `8080` and can be overridden with `BACKEND_PORT`.
  - The default datasource points at PostgreSQL on host port `5123`.
  - Flyway is managed at version `10.22.0` with `flyway-database-postgresql`; this keeps Spring Boot 3.3 compatibility while allowing startup against the current PostgreSQL 18 development container. Flyway logs a warning because PG18 is newer than its tested support window.

Current classes:

`com.company.iaf.app.IafApplication`
- Layer: bootstrap.
- Purpose: Spring Boot application entrypoint.
- Key methods:
  - `main(String[] args)`: starts the IAF backend application.

`com.company.iaf.app.infrastructure.config.SecurityConfig`
- Layer: infrastructure/config.
- Purpose: HTTP security filter chain.
- Key methods:
  - `securityFilterChain(...)`: disables CSRF and HTTP Basic for current API baseline, permits `/api/health`, `/api/platform/auth/login`, and OpenAPI/Swagger paths, requires authentication for all other requests, and installs Bearer token authentication when the auth module is active.
  - `authenticationEntryPoint(ObjectMapper)`: returns unified HTTP 401 `Result`.
  - `accessDeniedHandler(ObjectMapper)`: returns unified HTTP 403 `Result`.
- Notes: Bearer tokens are currently in-memory development tokens. Replace with durable JWT/session strategy before production use.

`com.company.iaf.app.interfaces.controller.HealthCheckController`
- Layer: interfaces/controller.
- Purpose: public health check API.
- Key methods:
  - `health()`: returns `Result<HealthResponse>` with status `OK`.
  - `HealthResponse`: response record containing `status`.

`com.company.iaf.app.interfaces.controller.GlobalExceptionHandler`
- Layer: interfaces/controller advice.
- Purpose: converts exceptions into unified `Result` responses.
- Key methods:
  - `handleBusinessException(BusinessException)`: returns 400 with the business error code and message.
  - `handleValidationException(MethodArgumentNotValidException)`: returns 400 with `COMMON_VALIDATION_FAILED`.
  - `handleException(Exception)`: returns 500 with `COMMON_INTERNAL_ERROR`.

Current tests:

`com.company.iaf.app.IafApplicationTests`
- Purpose: verifies the application context loads without requiring a live datasource.

`com.company.iaf.app.interfaces.controller.HealthCheckControllerTest`
- Purpose: verifies `/api/health` returns unified success response and is public.

`com.company.iaf.app.interfaces.controller.GlobalExceptionHandlerTest`
- Purpose: verifies business and validation errors are converted to unified error responses.

### `iaf-shared`

- Owner module: shared kernel used by all backend modules.
- Purpose: small, stable primitives that are not owned by a business module.
- Notes: keep this module minimal. Do not place business rules here.

Current classes:

`com.company.iaf.shared.result.Result<T>`
- Purpose: unified non-page API response.
- Key methods:
  - `ok(T data)`: create successful response with code `OK`.
  - `fail(String code, String message)`: create failed response.

`com.company.iaf.shared.result.PageResult<T>`
- Purpose: unified paginated response payload.
- Key fields: `items`, `total`, `page`, `pageSize`.

`com.company.iaf.shared.exception.ErrorCode`
- Purpose: interface for typed error codes.
- Key methods:
  - `code()`: stable frontend/API error code.
  - `message()`: default human-readable message.

`com.company.iaf.shared.exception.CommonErrorCode`
- Purpose: common platform-independent error codes.
- Current values: `BAD_REQUEST`, `VALIDATION_FAILED`, `UNAUTHORIZED`, `FORBIDDEN`, `NOT_FOUND`, `CONFLICT`, `INTERNAL_ERROR`.

`com.company.iaf.shared.exception.BusinessException`
- Purpose: runtime exception for expected business failures.
- Key methods:
  - `errorCode()`: returns the typed error code.

`com.company.iaf.shared.persistence.BaseEntity`
- Purpose: base persistence fields required by IAF rules.
- Key fields: `id`, `tenantId`, `createdAt`, `createdBy`, `updatedAt`, `updatedBy`, `deleted`, `version`, `extJson`.
- Notes: business tables must keep these semantics unless an ADR explicitly allows otherwise.

`com.company.iaf.shared.tenant.TenantContext`
- Purpose: thread-local holder for current tenant id.
- Key methods:
  - `setTenantId(Long tenantId)`
  - `getTenantId()`
  - `clear()`

`com.company.iaf.shared.security.SecurityContext`
- Purpose: thread-local holder for current user id and permission codes.
- Key methods:
  - `setUserId(Long userId)`
  - `getUserId()`
  - `setCurrentOrgId(Long orgId)`
  - `getCurrentOrgId()`
  - `setPermissions(Set<String> permissions)`
  - `getPermissions()`
  - `hasPermission(String permission)`
  - `clear()`

`com.company.iaf.shared.context.ExecutionContext`
- Purpose: immutable snapshot for tenant/security context propagation across async tasks or message handlers.
- Key methods:
  - `capture()`: snapshots `TenantContext` and `SecurityContext`.
  - `openScope()`: restores the snapshot through `ContextScope`.

`com.company.iaf.shared.context.ContextScope`
- Purpose: scoped context restore/cleanup helper implementing `AutoCloseable`.
- Key methods:
  - `open(ExecutionContext)`: applies a snapshot and restores the previous thread state on close.

### Platform Modules

`iaf-platform-core`
- Purpose: shared platform abstractions and contracts; hosts the cross-platform security surface (declarative `@RequiresPermission` annotation, `PermissionChecker`, and the AOP aspect that drives both).
- Depends on: `iaf-shared`, Spring AOP, Spring context.
- Current classes:

`com.company.iaf.platform.core.security.RequiresPermission`
- Layer: cross-platform security annotation.
- Purpose: marks a class or method as requiring backend permission checks.
- Key members:
  - `value()`: required permission codes using `module:object:action`.
  - `mode()`: `ALL` requires every code, `ANY` requires at least one code.
- Notes: lives in `platform-core` (not `platform-permission`) so every platform module can apply permission gates without depending on the permission data layer (see ADR-0003).

`com.company.iaf.platform.core.security.PermissionChecker`
- Layer: cross-platform security helper.
- Purpose: central backend permission checker over `SecurityContext`.
- Key methods:
  - `requireAll(String... permissions)`: requires an authenticated user and all requested permissions; raises `COMMON_UNAUTHORIZED` or `COMMON_FORBIDDEN`.
  - `requireAny(String... permissions)`: requires an authenticated user and at least one requested permission; raises `COMMON_UNAUTHORIZED` or `COMMON_FORBIDDEN`.

`com.company.iaf.platform.core.security.RequiresPermissionAspect`
- Layer: cross-platform AOP aspect.
- Purpose: Spring AOP interceptor for `@RequiresPermission` on classes and methods.
- Key methods:
  - `checkPermission(ProceedingJoinPoint)`: evaluates class-level and method-level permission annotations before invoking the use case.

`com.company.iaf.platform.core.context.ExecutionContextTaskDecorator`
- Layer: cross-platform context propagation.
- Purpose: Spring `TaskDecorator` that captures `ExecutionContext` at submission and restores/cleans it around async task execution.

`com.company.iaf.platform.core.context.ExecutionContextConfiguration`
- Layer: cross-platform configuration.
- Purpose: exposes the context task decorator bean for future executor configuration.

Current tests:

`com.company.iaf.platform.core.security.PermissionCheckerTest`
- Purpose: verifies all-permission, any-permission, missing-permission, and unauthenticated-user behavior.

`com.company.iaf.platform.core.security.RequiresPermissionAspectTest`
- Purpose: verifies Spring AOP blocks annotated methods when the current user lacks the required permission.

`iaf-platform-auth`
- Purpose: authentication and identity.
- Current classes:

`com.company.iaf.platform.auth.interfaces.controller.AuthController`
- Layer: interfaces/controller.
- Purpose: authentication HTTP API.
- Key methods:
  - `login(LoginRequest)`: resolves `tenantCode`, verifies credentials within that tenant, and returns a Bearer token from the configured `AuthTokenStore`.
  - `me(AuthenticatedUser)`: returns current authenticated user.

`com.company.iaf.platform.auth.interfaces.dto.LoginRequest`
- Layer: interfaces/dto.
- Purpose: login request payload.
- Fields: `tenantCode`, `username`, `password`.

`com.company.iaf.platform.auth.interfaces.dto.LoginResponse`
- Layer: interfaces/dto.
- Purpose: login response payload.
- Fields: `tokenType`, `accessToken`, `expiresAt`, `tenantId`, `userId`, `username`, `displayName`, `currentOrgId`, `organizations`, `permissions`.

`com.company.iaf.platform.auth.interfaces.dto.CurrentUserResponse`
- Layer: interfaces/dto.
- Purpose: current user response payload.
- Notes: includes current organization id and active organization assignment snapshot.

`com.company.iaf.platform.auth.application.AuthApplicationService`
- Layer: application.
- Purpose: login and token authentication use cases.
- Key methods:
  - `login(String tenantCode, String username, String password)`: resolves enabled tenant, validates credentials within that tenant, and issues an auth token.
  - `authenticate(String token)`: resolves a token into an authenticated user or raises `COMMON_UNAUTHORIZED`.

`com.company.iaf.platform.auth.application.AuthTokenStore`
- Layer: application contract.
- Purpose: token issuing and lookup abstraction.

`com.company.iaf.platform.auth.domain.model.AuthenticatedUser`
- Layer: domain/model.
- Purpose: authenticated principal containing tenant, user, display name, current organization context, active organization assignments, and permission codes.

`com.company.iaf.platform.auth.domain.model.AuthToken`
- Layer: domain/model.
- Purpose: issued access token plus expiry and user snapshot.

`com.company.iaf.platform.auth.domain.model.LoginUser`
- Layer: domain/model.
- Purpose: login credential aggregate read from persistence.
- Key methods:
  - `toAuthenticatedUser(List<UserOrg>)`: converts persisted login user plus active user-org assignments into an authenticated principal.

`com.company.iaf.platform.auth.domain.repository.AuthUserRepository`
- Layer: domain/repository contract.
- Purpose: lookup login user by `tenantId + username`.

`com.company.iaf.platform.auth.domain.model.TenantInfo`
- Layer: domain/model.
- Purpose: lightweight tenant registry snapshot used during login tenant resolution.

`com.company.iaf.platform.auth.domain.repository.TenantRepository`
- Layer: domain/repository contract.
- Purpose: lookup tenant registry entries by `tenantCode`.

`com.company.iaf.platform.auth.infrastructure.persistence.JdbcAuthUserRepository`
- Layer: infrastructure/persistence.
- Purpose: JDBC implementation of login user lookup over `sys_user`, `sys_user_role`, `sys_role_permission`, and `sys_permission`.
- Wiring: unconditional Spring repository that requires an application-level `JdbcTemplate`.
- Notes: query is tenant-qualified by `u.tenant_id = ?`.

`com.company.iaf.platform.auth.infrastructure.persistence.JdbcTenantRepository`
- Layer: infrastructure/persistence.
- Purpose: tenant registry lookup by `tenant_code` for public login requests.
- Notes: intentionally does not use `TenantContext` because login has no token yet.

`com.company.iaf.platform.auth.infrastructure.security.InMemoryAuthTokenStore`
- Layer: infrastructure/security.
- Purpose: development token store backed by process memory.
- Notes: only enabled when `iaf.auth.token-store=memory` or unset; not production durable.

`com.company.iaf.platform.auth.infrastructure.security.AuthTokenStoreProductionGuard`
- Layer: infrastructure/security.
- Purpose: fails startup under `prod` profile if the configured token store is still in-memory.

`com.company.iaf.platform.auth.infrastructure.security.BearerTokenAuthenticationFilter`
- Layer: infrastructure/security.
- Purpose: clears residual context, reads `Authorization: Bearer ...`, sets Spring Security authentication, `TenantContext`, and `SecurityContext` user/organization/permission values, then clears all thread-local context after the request.
- Notes: authentication refresh rejects existing tokens whose tenant is no longer `ENABLED`.

`com.company.iaf.platform.auth.infrastructure.security.PasswordConfig`
- Layer: infrastructure/security.
- Purpose: provides Spring Security `PasswordEncoder`.

`com.company.iaf.platform.auth.application.UserApplicationService`
- Layer: application.
- Purpose: platform user management use cases.
- Key methods:
  - `listUsers(...)`: read flow guarded by `platform:user:view`; applies the TASK-0218 data-permission tracer bullet by deriving a current-organization `UserDataScope` from `SecurityContext.currentOrgId` and returning an empty page when no scope is present.
  - `getUser(...)`: read flow guarded by `platform:user:view`.
  - `getCurrentUser(...)`: current-user identity snapshot guarded by `platform:auth:me`.
  - `createUser(...)`: rejects duplicate usernames within the same tenant; requires `platform:user:create`.
  - `updateUser(...)`: optimistic-locked profile update; requires `platform:user:update`.
  - `disableUser(...)`: refuses to disable the current user; idempotent; requires `platform:user:disable`.
  - `resetPassword(...)`: replaces `password_hash`; requires `platform:user:reset-password`.
  - `getUserOrganizations(...)`: returns active user-organization assignments; requires `platform:user:view`.
  - `replaceUserOrganizations(...)`: replaces assignments, requires exactly one primary org when non-empty, and syncs `sys_user_org.is_primary` plus `sys_user.primary_org_id`; requires `platform:user:update`.
  - `switchCurrentOrgContext(...)`: validates that the current user belongs to the requested org and syncs `sys_user_org.is_primary` plus `sys_user.primary_org_id`; requires `platform:auth:me`.

`com.company.iaf.platform.auth.domain.model.UserOrg`
- Layer: domain/model.
- Purpose: active user-organization assignment snapshot joined with organization code/name/type.

`com.company.iaf.platform.auth.domain.model.UserOrgAssignment`
- Layer: domain/model.
- Purpose: replacement input for user organization assignments, including primary flag, scope weight, and reserved validity window.

`com.company.iaf.platform.auth.domain.model.UserDataScope`
- Layer: domain/model.
- Purpose: typed data-permission filter for the TASK-0218 user-list tracer bullet; currently carries allowed organization ids.

`com.company.iaf.platform.auth.domain.repository.UserOrgRepository`
- Layer: domain/repository contract.
- Purpose: persistence boundary for `sys_user_org` assignment lookup, replacement, org existence validation, and primary organization sync across `sys_user_org.is_primary` plus `sys_user.primary_org_id`.

`com.company.iaf.platform.auth.infrastructure.persistence.JdbcUserOrgRepository`
- Layer: infrastructure/persistence.
- Purpose: JDBC implementation for `sys_user_org`, using active validity-window filtering for assignment snapshots.
- Notes: joins `sys_org` to return display snapshots; it does not expose org module infrastructure classes.

`com.company.iaf.platform.auth.application.PlatformAuthErrorCode`
- Layer: application.
- Purpose: stable business error codes for the auth/user surface.

`com.company.iaf.platform.auth.domain.model.PlatformUser`
- Layer: domain/model.
- Purpose: tenant-scoped user aggregate without password material.

`com.company.iaf.platform.auth.domain.model.UserStatus`
- Layer: domain/model enum.
- Purpose: enabled/disabled lifecycle flag.

`com.company.iaf.platform.auth.domain.repository.PlatformUserRepository`
- Layer: domain/repository contract.
- Purpose: persistence boundary for `sys_user` (CRUD + status + password updates + paginated lookup).
- Data permission: list/count methods consume typed `UserDataScope` rather than raw SQL fragments.

`com.company.iaf.platform.auth.infrastructure.persistence.JdbcPlatformUserRepository`
- Layer: infrastructure/persistence.
- Purpose: JDBC implementation behind `@ConditionalOnBean(JdbcTemplate.class)`.
- Data permission: user list/count filters through `sys_user_org` with tenant and organization predicates for the TASK-0218 tracer bullet.

`com.company.iaf.platform.auth.interfaces.controller.UserController`
- Layer: interfaces/controller.
- Purpose: management HTTP API for `/api/platform/users`.
- Key methods: `list`, `get`, `me`, `create`, `update`, `getOrganizations`, `replaceOrganizations`, `switchOrgContext`, `disable`, `resetPassword`.

`com.company.iaf.platform.auth.interfaces.dto.UserCreateRequest`
- Layer: interfaces/dto.
- Purpose: `POST /api/platform/users` payload with Bean Validation.
- Notes: does not accept primary organization; organization assignment is owned by `PUT /api/platform/users/{id}/orgs`.

`com.company.iaf.platform.auth.interfaces.dto.UserUpdateRequest`
- Layer: interfaces/dto.
- Purpose: `PUT /api/platform/users/{id}` payload (no username/status/primary organization changes).

`com.company.iaf.platform.auth.interfaces.dto.UserResponse`
- Layer: interfaces/dto.
- Purpose: response record with `from(PlatformUser)` factory; never exposes `password_hash`.

`com.company.iaf.platform.auth.interfaces.dto.ResetPasswordRequest`
- Layer: interfaces/dto.
- Purpose: `POST /api/platform/users/{id}/reset-password` payload.

Current tests:

`com.company.iaf.platform.auth.application.AuthApplicationServiceTest`
- Purpose: verifies successful login/token lookup, wrong password rejection, and disabled user rejection.

`com.company.iaf.platform.auth.application.UserApplicationServiceTest`
- Purpose: verifies create, tenant-scoped duplicate detection, disable semantics (including self-disable rejection), update, password reset, and paginated lookup using an in-memory repository double.

`com.company.iaf.platform.auth.interfaces.controller.UserControllerTest`
- Purpose: verifies the unified `Result` envelope for list/get/create/update/disable/reset-password and BusinessException propagation using `MockMvcBuilders.standaloneSetup` with a local `@RestControllerAdvice` for business errors.

`iaf-platform-org`
- Purpose: organization tree management scoped per tenant.
- Depends on: `iaf-platform-core` (for `@RequiresPermission`), Spring JDBC, Spring Web.
- Current classes:

`com.company.iaf.platform.org.domain.model.Org`
- Layer: domain/model.
- Purpose: tenant-scoped organization node aggregate.

`com.company.iaf.platform.org.domain.model.OrgType`
- Layer: domain/model enum.
- Purpose: `COMPANY | DEPARTMENT | DIVISION | TEAM`. Manufacturing factory/workshop/warehouse/location live in manufacturing/WMS modules.

`com.company.iaf.platform.org.domain.model.OrgStatus`
- Layer: domain/model enum.
- Purpose: enabled/disabled lifecycle flag.

`com.company.iaf.platform.org.domain.repository.OrgRepository`
- Layer: domain/repository contract.
- Purpose: persistence boundary for `sys_org` (findById, existsByOrgCode, findAll, insert, update).

`com.company.iaf.platform.org.infrastructure.persistence.JdbcOrgRepository`
- Layer: infrastructure/persistence.
- Purpose: JDBC implementation behind `@ConditionalOnBean(JdbcTemplate.class)`.

`com.company.iaf.platform.org.application.OrgApplicationService`
- Layer: application.
- Purpose: organization management use cases.
- Key methods:
  - `getTree(...)`: builds the hierarchical response from a flat repository listing; requires `platform:org:view`.
  - `getOrg(...)`: single-node lookup; requires `platform:org:view`.
  - `createOrg(...)`: rejects duplicate `org_code`; validates parent existence; requires `platform:org:create`.
  - `updateOrg(...)`: rejects self-parent cycles and concurrent-version races; requires `platform:org:update`.

`com.company.iaf.platform.org.application.PlatformOrgErrorCode`
- Layer: application.
- Purpose: stable business error codes for the org surface.

`com.company.iaf.platform.org.interfaces.controller.OrgController`
- Layer: interfaces/controller.
- Purpose: management HTTP API for `/api/platform/orgs`.
- Key methods: `tree`, `get`, `create`, `update`.

`com.company.iaf.platform.org.interfaces.dto.OrgCreateRequest`
- Layer: interfaces/dto.
- Purpose: `POST /api/platform/orgs` payload.

`com.company.iaf.platform.org.interfaces.dto.OrgUpdateRequest`
- Layer: interfaces/dto.
- Purpose: `PUT /api/platform/orgs/{id}` payload.

`com.company.iaf.platform.org.interfaces.dto.OrgResponse`
- Layer: interfaces/dto.
- Purpose: flat organization response.

`com.company.iaf.platform.org.interfaces.dto.OrgTreeNodeResponse`
- Layer: interfaces/dto.
- Purpose: hierarchical response with mutable children list.

Current tests:

`com.company.iaf.platform.org.application.OrgApplicationServiceTest`
- Purpose: verifies create, duplicate-code rejection, parent-existence validation, tenant scoping, update with duplicate-code and self-parent rejection, and tree assembly.

`com.company.iaf.platform.org.interfaces.controller.OrgControllerTest`
- Purpose: verifies the unified envelope for tree/get/create/update and BusinessException propagation.

`iaf-platform-permission`
- Purpose: roles, permissions, menus, and assignment of permission/menu codes to roles.
- Depends on: `iaf-platform-core`, Spring JDBC, Spring Web.
- Notes: the declarative `@RequiresPermission` annotation, `PermissionChecker`, and the AOP aspect that drives both now live in `iaf-platform-core` (see ADR-0003) so every platform module can apply permission gates without depending on this module's data layer.
- Current classes:

`com.company.iaf.platform.permission.domain.model.Role`
- Layer: domain/model.
- Purpose: tenant-scoped role aggregate.

`com.company.iaf.platform.permission.domain.model.RoleStatus`
- Layer: domain/model enum.
- Purpose: enabled/disabled lifecycle flag.

`com.company.iaf.platform.permission.domain.model.Permission`
- Layer: domain/model.
- Purpose: platform permission aggregate (id, tenant, code, name, module/action codes).

`com.company.iaf.platform.permission.domain.model.Menu`
- Layer: domain/model.
- Purpose: tenant-scoped platform navigation menu aggregate with hierarchy, route, component key, visibility, enabled flag, sort order, and audit version fields.

`com.company.iaf.platform.permission.domain.repository.RoleRepository`
- Layer: domain/repository contract.
- Purpose: persistence boundary for `sys_role` plus `replacePermissions(tenantId, roleId, permissionIds)` for atomic role-permission rebinding.

`com.company.iaf.platform.permission.domain.repository.PermissionRepository`
- Layer: domain/repository contract.
- Purpose: lookup permissions by code (for assignment validation), by role (for current-binding reads), and list tenant permissions for the assignable permission console.

`com.company.iaf.platform.permission.domain.repository.MenuRepository`
- Layer: domain/repository contract.
- Purpose: persistence boundary for `sys_menu`, `sys_menu_permission`, and `sys_role_menu`; supports menu tree reads, current-user visible menus filtered by role-menu binding plus current permission codes, role menu binding reads, and atomic role-menu replacement.

`com.company.iaf.platform.permission.infrastructure.persistence.JdbcRoleRepository`
- Layer: infrastructure/persistence.
- Purpose: JDBC implementation of `RoleRepository`.

`com.company.iaf.platform.permission.infrastructure.persistence.JdbcPermissionRepository`
- Layer: infrastructure/persistence.
- Purpose: JDBC implementation of `PermissionRepository`.

`com.company.iaf.platform.permission.infrastructure.persistence.JdbcMenuRepository`
- Layer: infrastructure/persistence.
- Purpose: JDBC implementation of `MenuRepository`, including menu-permission joins and user-role-menu joins for `/api/platform/auth/menus`. Menus with no linked permission remain visible when assigned to the role; menus with linked permissions require the current user to have at least one linked permission code.

`com.company.iaf.platform.permission.application.RoleApplicationService`
- Layer: application.
- Purpose: role and permission assignment use cases.
- Key methods:
  - `listRoles(...)`, `getRole(...)`: paginated / single reads with current permission and menu codes; require `platform:role:view`.
  - `createRole(...)`: rejects duplicate `role_code`; requires `platform:role:create`.
  - `updateRole(...)`: optimistic-locked profile update; requires `platform:role:update`.
  - `assignPermissions(...)`: validates every requested code exists in tenant, then atomically replaces `sys_role_permission` rows; requires `platform:role:assign-permission`.
  - `assignMenus(...)`: validates every requested menu code exists in tenant, then atomically replaces `sys_role_menu` rows; requires `platform:role:assign-menu`.

`com.company.iaf.platform.permission.application.MenuApplicationService`
- Layer: application.
- Purpose: platform menu management and current-user menu discovery use cases.
- Key methods:
  - `listMenuTree(...)`: returns the tenant menu tree with linked permission codes; requires `platform:menu:view`.
  - `listCurrentUserMenus(...)`: returns visible/enabled menus reachable through the current user's roles and current permission codes; requires `platform:auth:me`.
  - `createMenu(...)`: rejects duplicate menu codes and validates parent existence; requires `platform:menu:create`.
  - `updateMenu(...)`: validates duplicate code, parent existence, self-parent rejection, and descendant-parent rejection so menu cycles cannot be created; requires `platform:menu:update`.

`com.company.iaf.platform.permission.application.PermissionApplicationService`
- Layer: application.
- Purpose: assignable permission lookup for the role-permission console.
- Key methods:
  - `listPermissions(...)`: lists tenant permissions; requires `platform:permission:view`.

`com.company.iaf.platform.permission.application.PlatformPermissionErrorCode`
- Layer: application.
- Purpose: stable business error codes for the role/permission surface.

`com.company.iaf.platform.permission.interfaces.controller.RoleController`
- Layer: interfaces/controller.
- Purpose: management HTTP API for `/api/platform/roles`.
- Key methods: `list`, `get`, `create`, `update`, `assignPermissions`, `assignMenus`.

`com.company.iaf.platform.permission.interfaces.controller.MenuController`
- Layer: interfaces/controller.
- Purpose: management HTTP API for `/api/platform/menus`.
- Key methods: `tree`, `create`, `update`.

`com.company.iaf.platform.permission.interfaces.controller.AuthMenuController`
- Layer: interfaces/controller.
- Purpose: current principal navigation API at `/api/platform/auth/menus`.
- Key methods: `currentUserMenus`.

`com.company.iaf.platform.permission.interfaces.controller.PermissionController`
- Layer: interfaces/controller.
- Purpose: assignable permission API at `/api/platform/permissions`.
- Key methods: `list`.

`com.company.iaf.platform.permission.interfaces.dto.RoleCreateRequest`
- Layer: interfaces/dto.
- Purpose: `POST /api/platform/roles` payload.

`com.company.iaf.platform.permission.interfaces.dto.RoleUpdateRequest`
- Layer: interfaces/dto.
- Purpose: `PUT /api/platform/roles/{id}` payload.

`com.company.iaf.platform.permission.interfaces.dto.RoleResponse`
- Layer: interfaces/dto.
- Purpose: response record including the currently-bound permission codes and menu codes.

`com.company.iaf.platform.permission.interfaces.dto.AssignRolePermissionsRequest`
- Layer: interfaces/dto.
- Purpose: `PUT /api/platform/roles/{id}/permissions` payload (the list replaces the role's bindings).

`com.company.iaf.platform.permission.interfaces.dto.AssignRoleMenusRequest`
- Layer: interfaces/dto.
- Purpose: `PUT /api/platform/roles/{id}/menus` payload (the list replaces the role's menu bindings).

`com.company.iaf.platform.permission.interfaces.dto.MenuCreateRequest`
- Layer: interfaces/dto.
- Purpose: `POST /api/platform/menus` payload.

`com.company.iaf.platform.permission.interfaces.dto.MenuUpdateRequest`
- Layer: interfaces/dto.
- Purpose: `PUT /api/platform/menus/{id}` payload.

`com.company.iaf.platform.permission.interfaces.dto.MenuResponse`
- Layer: interfaces/dto.
- Purpose: hierarchical menu response with linked permission codes and children.

`com.company.iaf.platform.permission.interfaces.dto.PermissionResponse`
- Layer: interfaces/dto.
- Purpose: tenant permission response for assignable permission lists.

Current tests:

`com.company.iaf.platform.permission.application.RoleApplicationServiceTest`
- Purpose: verifies create/duplicate detection, tenant scoping, update with duplicate-code rejection, atomic permission/menu replacement, unknown-code rejection, empty-list clear semantics, and paginated lookup.

`com.company.iaf.platform.permission.application.MenuApplicationServiceTest`
- Purpose: verifies menu tree assembly, current-user menu permission filtering, duplicate code rejection, missing-parent rejection, and descendant-parent cycle rejection.

`com.company.iaf.platform.permission.interfaces.controller.RoleControllerTest`
- Purpose: verifies the unified `Result` envelope for list/get/create/update/assignPermissions/assignMenus and BusinessException propagation.

`com.company.iaf.platform.permission.interfaces.controller.MenuPermissionControllerTest`
- Purpose: verifies the unified `Result` envelope for menu tree/current-user menus/create/update and assignable permission list endpoints.

`iaf-platform-system`
- Purpose: platform system configuration for theme, brand, i18n resources, current-user experience preferences, and future dictionaries/parameters/audit/attachments/jobs.
- Depends on: `iaf-platform-core`, Spring Web, Spring JDBC, Spring TX, Jackson, Jakarta Validation.
- Current classes:

`com.company.iaf.platform.system.interfaces.controller.SystemConfigurationController`
- Layer: interfaces/controller.
- Purpose: HTTP API for platform theme, brand, i18n resources, and current-user experience preferences.
- Key methods:
  - `getTheme()`, `saveTheme(...)`: `/api/platform/theme/current`.
  - `getBrand()`, `saveBrand(...)`: `/api/platform/brand/current`.
  - `listI18nResources(locale)`, `replaceI18nResources(...)`: `/api/platform/i18n/resources`.
  - `getMyPreference()`, `saveMyPreference(...)`: `/api/platform/preferences/me`.

`com.company.iaf.platform.system.application.SystemConfigurationApplicationService`
- Layer: application.
- Purpose: platform system configuration use cases, validation, permission gates, and transaction boundaries.
- Key methods:
  - `getTheme(...)`, `saveTheme(...)`: guarded by `platform:theme:view` / `platform:theme:update`.
  - `getBrand(...)`, `saveBrand(...)`: guarded by `platform:brand:view` / `platform:brand:update`.
  - `listI18nResources(...)`, `replaceI18nResources(...)`: guarded by `platform:i18n:view` / `platform:i18n:update`.
  - `getMyPreference(...)`, `saveMyPreference(...)`: guarded by `platform:preference:me`.

`com.company.iaf.platform.system.domain.repository.SystemConfigurationRepository`
- Layer: domain/repository contract.
- Purpose: persistence boundary for system configuration tables.

`com.company.iaf.platform.system.infrastructure.persistence.JdbcSystemConfigurationRepository`
- Layer: infrastructure/persistence.
- Purpose: JDBC implementation over `sys_theme_config`, `sys_brand_config`, `sys_i18n_resource`, and `sys_user_experience_preference`.
- Notes: returns code defaults when theme, brand, or user preference rows are not present; stores token and preference maps as PostgreSQL `jsonb`. User preference defaults include theme, form mode, density, font size, sidebar mode, sidebar collapsed state, sidebar width, motion, surface width, and workspace mode.

Current tests:

`com.company.iaf.platform.system.application.SystemConfigurationApplicationServiceTest`
- Purpose: verifies configuration validation and user preference save behavior.

`iaf-platform-workflow`
- Purpose: approval workflow facade over Flowable.
- `ApprovalApplicationService` is the engine-neutral submit/approve/reject/query contract.
- `JdbcApprovalApplicationService` persists the phase-one approval instance and immutable action history;
  QMS depends only on the application contract, allowing a later Flowable adapter replacement.
- Current state: module POM only.

`iaf-platform-statemachine`
- Purpose: state transition definitions and runtime service.
- Current state: module POM only.

`iaf-platform-rule`
- Purpose: business rule evaluation, JSON Logic execution, and rule lifecycle.
- Current state: module POM only.

`iaf-platform-integration`
- Purpose: outbox, integration adapters, callbacks, and external system connectivity.
- Current classes:

`com.company.iaf.platform.core.event.DomainEvent`
- Owner module: `iaf-platform-core`.
- Layer: cross-platform event contract.
- Purpose: typed domain event envelope with tenant id, aggregate identity, event type, and JSON payload.

`com.company.iaf.platform.core.event.DomainEventPublisher`
- Owner module: `iaf-platform-core`.
- Layer: cross-platform event contract.
- Purpose: application-service-facing event publication contract implemented by the platform integration Outbox baseline.

`com.company.iaf.platform.auth.application.TenantApplicationService`
- Owner module: `iaf-platform-auth`.
- Layer: application.
- Purpose: tenant lifecycle, idempotent initialization, enable/disable, and tenant quota use cases.
- Key methods: `listTenants`, `getTenant`, `createTenant`, `updateTenant`, `enableTenant`, `disableTenant`, `listQuotas`, `updateQuota`.
- Permissions: `platform:tenant:*`, `platform:tenant-quota:*`.
- Notes: requires platform tenant context (`tenant_id = 1`) in addition to permission codes; publishes `TenantCreatedEvent` through `DomainEventPublisher`.

`com.company.iaf.platform.auth.interfaces.controller.TenantController`
- Owner module: `iaf-platform-auth`.
- Layer: interfaces/controller.
- Purpose: HTTP API for `/api/platform/tenants`.

`com.company.iaf.platform.auth.domain.model.Tenant`, `TenantStatus`, `TenantQuota`
- Owner module: `iaf-platform-auth`.
- Layer: domain/model.
- Purpose: tenant registry and quota snapshots.

`com.company.iaf.platform.auth.infrastructure.persistence.JdbcTenantRepository`
- Owner module: `iaf-platform-auth`.
- Layer: infrastructure/persistence.
- Purpose: tenant lookup, lifecycle persistence, quota persistence, and phase-1 tenant initialization SQL.
- Notes: TASK-0219 initialization writes baseline rows into platform auth/org/permission/system tables inside the monolith and is documented as a future service-extraction point in ADR-0007.

`com.company.iaf.platform.integration.application.OutboxApplicationService`
- Owner module: `iaf-platform-integration`.
- Layer: application.
- Purpose: implements `DomainEventPublisher`, persists domain events to Outbox, lists outbox events for operations, dispatches pending events to handlers, and supports manual retry.
- Permissions: `platform:outbox:view`, `platform:outbox:retry` for operational API use.
- Notes: operational list/retry requires platform tenant context; the target event tenant is supplied explicitly and manual retry is tenant-qualified.

`com.company.iaf.platform.integration.domain.model.OutboxEvent`, `OutboxEventStatus`
- Owner module: `iaf-platform-integration`.
- Layer: domain/model.
- Purpose: durable outbox event snapshot and status enum (`PENDING`, `SENT`, `FAILED`).

`com.company.iaf.platform.integration.domain.repository.OutboxEventRepository`
- Owner module: `iaf-platform-integration`.
- Layer: domain/repository contract.
- Purpose: append, tenant-qualified query, mark sent/failed, and tenant-qualified mark pending.

`com.company.iaf.platform.integration.infrastructure.persistence.JdbcOutboxEventRepository`
- Owner module: `iaf-platform-integration`.
- Layer: infrastructure/persistence.
- Purpose: JDBC implementation over `platform_outbox_event`; retry updates use `where tenant_id = ? and id = ?`.

`com.company.iaf.platform.integration.interfaces.controller.OutboxEventController`
- Owner module: `iaf-platform-integration`.
- Layer: interfaces/controller.
- Purpose: operational API for `/api/platform/outbox-events`; list/retry accept explicit target `tenantId`.

`com.company.iaf.platform.core.architecture.ModuleBoundaryRulesTest`
- Owner module: `iaf-platform-core`.
- Layer: automated architecture test.
- Purpose: verifies module dependency constraints before future service extraction, including application-service isolation from JDBC/MyBatis persistence implementation details.

MDC / context notes:
- `BearerTokenAuthenticationFilter` writes and clears `traceId`, `tenantId`, `userId`, and `currentOrgId` MDC fields for requests.
- `ExecutionContext` captures `traceId`; `ContextScope` restores and clears the same MDC fields around async tasks.

### Platform Foundation Release Governance

`scripts/platform-foundation-smoke-test.sh`
- Owner module: repository operations.
- Layer: release validation script.
- Purpose: smoke-tests the Platform Foundation RC1 backend API path with an existing running backend.
- Covered endpoints: health, login, current user, current menus, users, org tree, roles, menu tree, permissions, theme, brand, i18n, current-user preference read/merged update/restore, missing-token denial, and optional outbox list.
- Notes: it is not part of `scripts/check-quality.sh` because it requires a running backend and database. The procedure is documented in `docs/operations/platform-foundation-smoke-test.md`.

Platform Foundation stabilization documents
- Owner module: repository operations.
- Layer: release and stabilization governance.
- Purpose: TASK-0223 records RC1 feedback intake, patch policy, stability metrics, runbook replay, and next platform backlog without adding backend runtime APIs.
- Files:
  - `docs/operations/platform-foundation-feedback-log.md`
  - `docs/operations/platform-foundation-stabilization-plan.md`
  - `docs/operations/platform-foundation-runbook-review.md`
  - `docs/operations/platform-foundation-next-backlog.md`
- Notes: no Java classes, controllers, repositories, permissions, or migrations were added by TASK-0223.

### QMS Modules

`iaf-qms-engineering`

- `DrawingFileApplicationService`: validates PDF/DWG uploads, computes SHA-256, stores
  content through `QmsObjectStorage`, atomically binds file metadata to a revision, and audits upload.
- `MinioQmsObjectStorage`: private S3-compatible MinIO adapter.
- `JdbcQmsFileObjectRepository`: tenant/org-qualified file metadata persistence.
- Purpose: first Jinheng QMS engineering-data boundary owning Part, Drawing, and
  DrawingRevision metadata plus its transactional audit trail.
- Depends on: `iaf-platform-core`, Spring Web/JDBC/TX/Validation, Jackson, and springdoc
  common contracts. It does not depend on another business module's infrastructure.
- Assembly: included by `iaf-app`; architectural rationale is ADR-0009.
- `InspectionStandardService` validates controlled-document transitions through
  `StateMachineService`, delegates human decisions to `ApprovalApplicationService`, blocks
  unresolved releases, and writes QMS audit records for submit/approve/reject/release.

`com.company.iaf.qms.engineering.application.PartApplicationService`
- Layer: application.
- Purpose: current-organization Part create/list/detail use cases, normalization,
  duplicate prevention, transaction ownership, permission checks, and audit writes.
- Key methods: `list(...)`, `get(...)`, `create(...)`.
- Permissions: `qms:part:view`, `qms:part:create`.

`com.company.iaf.qms.engineering.application.DrawingApplicationService`
- Layer: application.
- Purpose: Drawing and metadata-only DrawingRevision use cases within the selected
  Part/Drawing hierarchy.
- Key methods: `listDrawings(...)`, `getDrawing(...)`, `createDrawing(...)`,
  `listRevisions(...)`, `getRevision(...)`, `createRevision(...)`.
- Permissions: `qms:drawing:view/create`, `qms:drawing-revision:view/create`.
- Notes: new revisions are always `DRAFT/PENDING/PENDING`; no state transition is
  implemented before the platform state-machine contract exists.

`com.company.iaf.qms.engineering.domain.model.Part`, `Drawing`, `DrawingRevision`
- Layer: domain/model.
- Purpose: tenant- and organization-owned engineering data. `DrawingRevision.metadataDraft`
  is the safe initial-state factory.

`PartRepository`, `DrawingRepository`, `DrawingRevisionRepository`, `QmsAuditTrail`
- Layer: domain/repository contracts.
- Purpose: persistence boundaries for engineering aggregates and the immutable audit trail.

`JdbcPartRepository`, `JdbcDrawingRepository`, `JdbcDrawingRevisionRepository`,
`JdbcQmsAuditTrail`
- Layer: infrastructure/persistence.
- Purpose: tenant/current-org-qualified PostgreSQL access. Revision sequence reservation
  uses a PostgreSQL transaction-scoped advisory lock before `max(sequence)+1`.

`QmsPartController`, `QmsDrawingController`
- Layer: interfaces/controller.
- Purpose: authenticated `/api/qms/**` HTTP surface with validated DTOs, OpenAPI
  descriptions, and unified `Result`/`PageResult` envelopes.

Current tests:

- `DrawingRevisionTest`: safe initial state and invalid sequence.
- `PartApplicationServiceTest`: create, normalization, audit, duplicates, tenant/org scope,
  and search.
- `DrawingApplicationServiceTest`: parent validation, duplicates, revision ordering,
  supersession, and audit.
- `QmsPartControllerTest`: unified response and Bean Validation.
- `QmsEngineeringPostgresIntegrationTest`: empty PostgreSQL migration, JDBC repositories,
  tenant/org isolation, revision sequence, audit JSON, permission seeds, and QMS menu/role links.

TASK-0404 parse orchestration:

- `StateMachineService` / `DefaultStateMachineService`: shared explicit-transition validator.
- `DrawingFileApplicationService`: `DRAFT -> UPLOADED`, file attachment, parse-job enqueue,
  and transition audit in one database transaction.
- `DrawingParseJobApplicationService`: latest-job queries and permission-controlled retry
  from `FAILED -> UPLOADED` with an incremented attempt.
- `DrawingParseJobRepository` / `JdbcDrawingParseJobRepository`: durable, tenant/org-scoped
  parse queue and latest-attempt projections.

TASK-0405 parse-result contract:

- TASK-0407 adds the independent `ai-service` FastAPI application. Its `pdf-vector` adapter
  converts PDF pages and text coordinates into DIM 1.0 entities and SourceEvidence without
  invoking an external model. The service is internal-only in production Compose.
- `DrawingParseJobDispatcher` polls bounded queued-job batches, claims them through
  `DrawingParseLifecycleService`, reads the controlled object, invokes `PdfParserPort`, and
  completes or fails the durable job. `HttpPdfParserAdapter` is the internal HTTP adapter.
- TASK-0408 routes DWG jobs through `CadParserPort`. The default
  `UnavailableCadParserAdapter` records `CAD_PROVIDER_UNAVAILABLE`; the deterministic Mock
  Provider exists only under tests to enforce handle/layer/entity/evidence contract parity.

- `DrawingParseLifecycleService`: internal parser port for start/complete/fail with state
  machine validation, atomic parse-job/revision transitions, result validation, and audit.
- `DrawingParseResultRepository` / `JdbcDrawingParseResultRepository`: stores and queries one
  versioned DIM plus normalized entities and source evidence under tenant/org/revision scope.
- `DrawingParseResultQueryService`: permission-protected model/entity/evidence projections.
- `DrawingIntermediateModel`, `DrawingEntity`, and `SourceEvidence`: immutable domain records;
  parser adapters populate drafts while persistence supplies ownership and source identifiers.
- TASK-0413 extends `QualityCharacteristicService` and
  `JdbcQualityCharacteristicRepository` with validated inspection/reference/ideal/fit/location,
  regulatory and mandatory classifications, manual creation, and transactional bulk review.
  Every command remains tenant/org/revision scoped, permission checked, audited, and versioned.
- TASK-0421 automatically confirms parser-created inspection dimensions after legend
  classification. `JdbcQualityCharacteristicRepository.review` also permits a confirmed
  item to be corrected and reconfirmed while retaining optimistic locking and reviewer audit.
- TASK-0422 makes `InspectionStandardService.generate` synchronize an existing editable draft:
  eligible confirmed characteristics are added or refreshed, obsolete items are soft-deleted,
  and user-entered sampling, methods, and remarks are preserved. Approval and released states
  reject synchronization.
- TASK-0417 adds `ValidationPlanService` for rule-derived performance-item mapping, human review,
  approval, rejection, and controlled release through the shared state-machine and approval ports.

### Manufacturing Modules

`iaf-manufacturing-core`
- Purpose: manufacturing shared domain abstractions.
- Current state: module POM only.

`iaf-manufacturing-master`
- Purpose: manufacturing master data.
- Current state: module POM only.

### WMS Modules

`iaf-wms-core`
- Purpose: WMS shared domain abstractions.
- Current state: module POM only.

`iaf-wms-master`
- Purpose: warehouse, location, zone, owner, SKU, package, and related WMS master data.
- Current state: module POM only.

`iaf-wms-inventory`
- Purpose: inventory balance, transaction, lot, serial, freeze, and posting services.
- Current state: module POM only.

`iaf-wms-inbound`
- Purpose: receipt, putaway, inbound execution, and related workflows.
- Current state: module POM only.

`iaf-wms-strategy`
- Purpose: WMS strategy rules such as putaway, allocation, and picking strategies.
- Current state: module POM only.
# QMS drawing legend rules

- `DrawingLegendRuleService` and `JdbcDrawingLegendRuleRepository` manage tenant rules.
- Enabled rules classify parser-created pending candidates; configuration changes preserve
  reviewed and manually entered characteristics.
