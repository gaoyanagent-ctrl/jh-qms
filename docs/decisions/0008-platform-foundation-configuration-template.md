# ADR-0008 Platform Foundation Configuration Template

## Status

Accepted

## Context

TASK-0221 requires the platform foundation to move from usable pages and seed data to a repeatable delivery baseline. The current platform permissions, menus, roles, theme, brand, i18n, and page quality rules are spread across Flyway migrations, frontend mock data, i18n resources, and documentation.

We need a source that implementation teams and future Agents can inspect without reading every migration or page. We also need automated checks that prevent permission, menu, and role drift.

## Decision

IAF will maintain a versioned platform foundation delivery package under:

```text
ai-coding/templates/platform-foundation/platform-foundation-delivery-package.json
```

The package is the productization template for:

- Platform permission matrix.
- Default role templates.
- Default menu template.
- Platform page design acceptance matrix.
- Platform page regression test matrix.

The package is not a runtime configuration source yet. Runtime initialization remains owned by Flyway migrations and tenant initialization services until a dedicated configuration snapshot API is implemented.

Quality gate validation is performed by:

```text
scripts/check-platform-foundation-templates.js
scripts/check-quality.sh
```

The validator checks that:

- Template permission codes cover both frontend `PLATFORM_PERMISSIONS` and backend Flyway-seeded `platform:*` permissions.
- Template permission codes do not introduce values outside frontend constants or backend permission seeds.
- Role and menu references point to known permission codes.
- Every permission is assigned to at least one delivery role, and each recommended role actually grants that permission.
- Every route menu has at least one permission guard unless explicitly marked public or mock-first.

## Configuration Snapshot API

TASK-0221 mentions optional snapshot import/export APIs:

```text
GET  /api/platform/config-snapshots/export
POST /api/platform/config-snapshots/validate
POST /api/platform/config-snapshots/import
```

This task does not introduce those APIs because the current dictionary, parameter, audit, data-permission, and field-permission backend APIs are not all production-backed yet. Building import/export now would either export mock-only concepts or create an incomplete contract.

The snapshot API should be introduced by a later backend task after the persisted owner tables and audit semantics are complete. That task must define API contracts, permission codes, audit records, idempotent import behavior, and tenant isolation tests.

## Consequences

- Platform foundation delivery is now inspectable and versioned.
- Permission/menu/role drift is caught by the repository quality gate.
- Existing Flyway seed data remains the runtime baseline.
- New tenants continue to inherit baseline permissions through the TASK-0219 default-tenant initialization copy path.
- Existing non-default tenants are not backfilled by the delivery package; production rollout must handle them with an explicit migration or implementation runbook step.
