# ADR-0004 Multi-Tenant Auth And Token Readiness

## Status

Accepted

## Context

IAF remains a modular monolith in phase 1, but it must support multiple tenants safely and keep a migration path toward multiple runtime instances and future services.

The previous login flow looked up users only by username. Because `sys_user` is tenant-scoped, this allowed ambiguous login semantics when different tenants had the same username.

The previous token store was in-memory. That is acceptable for local development, but it is not a production baseline because tokens disappear on restart, cannot be shared across instances, and cannot be centrally invalidated when tenant/user/permission state changes.

## Decision

- Login identifies the tenant from `LoginRequest.tenantCode`.
- Authenticated APIs derive tenant only from the token's `tenantId`; request headers cannot override tenant for normal business APIs.
- `sys_tenant` is the tenant registry for phase 1 and is owned by `iaf-platform-auth`.
- Login first resolves `tenantCode` to an enabled tenant, then resolves the user by `tenantId + username`.
- Login failures for unknown tenant, disabled tenant, missing user, disabled user, or bad password return the same unauthorized outcome.
- Tokens must carry `tenantId`, `userId`, `username`, `currentOrgId`, permissions, and expiry.
- Phase 1 selects Redis token store as the production target because centralized invalidation is more important than purely stateless JWT at this stage.
- The current in-memory token store remains only for local/dev/test. A prod profile must not start with the in-memory token store.
- JWT remains a future option behind the `AuthTokenStore` abstraction when permission and tenant versioning are implemented.

## Consequences

- Frontend login must send `tenantCode`; the page must not hardcode or prefill a default tenant. Local mock tenant validation may be configured through environment.
- `AuthUserRepository` queries are tenant-qualified.
- `BearerTokenAuthenticationFilter` is the standard point that restores `TenantContext` and `SecurityContext` from token authentication.
- Future Redis implementation must hash stored access tokens and must not persist raw bearer tokens.
- `permissionVersion` / `tokenVersion` is not implemented yet. Until it exists, permission changes rely on token expiry plus future centralized invalidation.

## Follow-Up

- Add Redis dependency and Redis-backed `AuthTokenStore`.
- Add token/user/permission version fields for forced invalidation.
- Add administrative token revocation when users, tenants, roles, or permissions are disabled.
