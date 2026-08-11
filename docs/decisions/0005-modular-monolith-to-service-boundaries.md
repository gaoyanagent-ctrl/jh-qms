# ADR-0005 Modular Monolith To Service Boundaries

## Status

Accepted

## Context

IAF phase 1 is a Maven multi-module modular monolith. Future deployments may split modules into services, but phase 1 must not introduce Spring Cloud, gateways, service discovery, or distributed transaction infrastructure.

The codebase still needs stable data ownership rules now, otherwise future service extraction will require broad SQL and module-boundary rewrites.

## Decision

Table ownership is:

- `sys_tenant`: `iaf-platform-auth` in phase 1; future tenant service boundary.
- `sys_user`, `sys_user_org`: `iaf-platform-auth`.
- `sys_org`: `iaf-platform-org`.
- `sys_role`, `sys_permission`, `sys_menu`, `sys_user_role`, `sys_role_permission`, `sys_menu_permission`, `sys_role_menu`: `iaf-platform-permission`.
- `sys_theme_config`, `sys_brand_config`, `sys_i18n_resource`, `sys_user_experience_preference`: `iaf-platform-system`.
- Future WMS inventory tables: `iaf-wms-inventory`.
- Future WMS inbound document tables: `iaf-wms-inbound`.

Current monolith rules:

- Modules may share one physical database.
- Cross-module reads may exist only behind application services, domain contracts, published APIs, or documented repository contracts.
- Modules must not inject or import another module's infrastructure classes.
- SQL must remain tenant-qualified except for explicitly named tenant registry lookups.

Future service rules:

- Services must not access another service's tables directly.
- Services must not perform cross-service database joins.
- Cross-service writes must use APIs, domain events, or Outbox.
- Cross-service query aggregation must use read models or application-layer aggregation.
- Direct SQL joins that are tolerated in the monolith must be replaced before extracting a service.

## Consequences

- `JdbcAuthUserRepository` may join auth-owned user/role binding and permission-owned permission tables during monolith phase, but this is a known extraction point.
- `JdbcUserOrgRepository` may join `sys_org` for display snapshots during monolith phase, but future extraction needs an organization read model or org API.
- Code map must record table owner and cross-module persistence joins.
