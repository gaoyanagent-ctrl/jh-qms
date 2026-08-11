# ADR-0007 Tenant Lifecycle Quota And Outbox Baseline

## Status

Accepted

## Context

TASK-0219 follows the TASK-0218 multi-tenant baseline. IAF still runs as a modular monolith, but platform operations now need tenant lifecycle APIs, idempotent tenant initialization, basic quota enforcement, durable event recording, and tenant-aware observability.

## Decision

- `iaf-platform-auth` owns `sys_tenant` and the tenant lifecycle API in phase 1.
- Tenant creation initializes the tenant inside the same application transaction:
  - root organization,
  - administrator user,
  - `platform_admin` role,
  - permissions copied from the platform seed tenant,
  - user-role and user-org bindings,
  - menu/menu-permission/role-menu baseline,
  - theme, brand, and i18n defaults,
  - `USER_COUNT` quota.
- Initialization uses idempotent SQL upserts/conflict handling so the same tenant can be retried without duplicating baseline rows.
- Tenant status is enforced for login through TASK-0218 tenant-aware login, for existing token authentication through `AuthApplicationService.authenticate`, and for platform user write operations through `UserApplicationService`.
- Tenant lifecycle, tenant quota, and Outbox operational APIs are platform-operations APIs. They require the platform tenant context (`tenant_id = 1`) in addition to the permission code so copied tenant permissions cannot grant tenant administrators cross-tenant administration power.
- The first quota closure is `USER_COUNT`; creating a user fails with `PLATFORM_AUTH_TENANT_QUOTA_EXCEEDED` when enabled users would exceed the configured quota.
- Domain events use `DomainEventPublisher` from `iaf-platform-core`; the `iaf-platform-integration` module implements it with `platform_outbox_event`.
- `TenantCreatedEvent` is the TASK-0219 Outbox tracer bullet.
- The Outbox dispatcher is a minimal application service loop that marks events `SENT` on success and `FAILED` with retry metadata on handler failure. Outbox operations require platform tenant context, while list/retry take an explicit target `tenantId`; manual retry moves an event back to `PENDING` only when both target `tenant_id` and event id match.
- MDC keys are standardized as `traceId`, `tenantId`, `userId`, and `currentOrgId`; request authentication and async context scopes restore and clear them.
- Module boundary checks are implemented as automated tests in `iaf-platform-core`; `iaf-app` is treated as the assembly module and is excluded from the cross-module infrastructure import rule. Application services are also checked so they do not depend directly on JDBC/MyBatis persistence implementation details.

## Consequences

- No Spring Cloud, service discovery, broker, or distributed transaction framework is introduced.
- Tenant initialization still performs monolith-local SQL against several platform-owned tables. This is acceptable for phase 1 and is a documented extraction point before service splitting.
- Outbox records are durable, but no scheduled dispatcher is enabled yet; dispatching is an application service operation for this baseline.
- WMS/manufacturing tenant-aware tracer bullet remains a follow-up task because TASK-0219 is implemented as the first vertical platform operations slice.
