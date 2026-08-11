# Database Code Map

## Local Database Stack (ADR-0002)

- Image: `postgres:18` (full Debian-based image).
- Host port: `5123` (default in `docker-compose.yml`,
  `.env.example`, and `backend/iaf-app/src/main/resources/application.yml`).
  Host port `5432` is held by an unrelated project container and
  must not be reassigned to IAF.
- Internal port: `5432` (PostgreSQL default).
- Override: set `POSTGRES_PORT` in `.env` or `IAF_DATASOURCE_URL`
  in the environment.
- See `docs/decisions/0002-postgresql-18-upgrade.md` for context.

## Current Migrations

`iaf-app` has Flyway enabled by default in `application.yml`:

- Migration location: `classpath:db/migration`
- Enabled flag: `IAF_FLYWAY_ENABLED`, default `true`

### `V0002__seed_platform_management_permissions.sql`

- Owner module: `iaf-app` migration resources, platform auth/org/permission ownership.
- Purpose: extend seed data with the 12 permission codes required by the
  TASK-0101 user / org / role management APIs, and bind them to the
  seeded `platform_admin` role.
- Affected tables: `sys_permission` (inserts), `sys_role_permission` (inserts).
- New permission codes:
  - `platform:user:view`, `platform:user:create`, `platform:user:update`,
    `platform:user:disable`, `platform:user:reset-password`
  - `platform:org:view`, `platform:org:create`, `platform:org:update`
  - `platform:role:view`, `platform:role:create`, `platform:role:update`,
    `platform:role:assign-permission`
- Role bindings: all of the above are bound to role `platform_admin` (`id=1`).
- Idempotency: uses `ON CONFLICT DO NOTHING` against the primary key
  and the `uk_sys_role_permission` unique constraint; safe to re-run
  on partially-seeded local databases.

### `V0004__platform_system_configuration.sql`

- Owner module: `iaf-app` migration resources, platform system ownership.
- Purpose: add tenant-scoped platform theme, brand, remote i18n resource, and current-user experience preference persistence.
- Tables:
  - `sys_theme_config`: current tenant theme name, primary color, sidebar mode, and token JSON.
  - `sys_brand_config`: current tenant brand name, logo/favicon URLs, login hero copy, operations copy, and login background settings.
  - `sys_i18n_resource`: remote i18n key/value resources by tenant and locale.
  - `sys_user_experience_preference`: current-user UI preference JSON by tenant and user.
- Important constraints:
  - `sys_theme_config`: unique `(tenant_id, config_key)`.
  - `sys_brand_config`: unique `(tenant_id, config_key)`.
  - `sys_i18n_resource`: unique `(tenant_id, locale, resource_key)`.
  - `sys_user_experience_preference`: unique `(tenant_id, user_id)`.
- Seed data:
  - Default tenant current theme and brand rows.
  - Minimal `app.name` i18n resources for `zh-CN` and `en-US`.
  - Permission codes `platform:theme:*`, `platform:brand:*`, `platform:i18n:*`, and `platform:preference:me`, bound to `platform_admin`.
- Notes:
  - All tables include tenant, audit, soft delete, optimistic lock, and `ext_json` fields.
  - Preference and token payloads use PostgreSQL `jsonb`.

### `V0005__platform_menu_permission.sql`

- Owner module: `iaf-app` migration resources, platform permission ownership.
- Purpose: add backend-managed platform menus, menu-permission links, and role-menu bindings for dynamic navigation and role menu assignment.
- Tables:
  - `sys_menu`: tenant-scoped menu tree with menu code, type, i18n title key, route path, component key, icon, sort order, visibility, enabled flag, audit fields, soft delete, optimistic lock, and `ext_json`.
  - `sys_menu_permission`: many-to-many link from menu to permission points.
  - `sys_role_menu`: many-to-many link from role to menu.
- Important constraints:
  - `sys_menu`: unique `(tenant_id, menu_code)`.
  - `sys_menu_permission`: unique `(tenant_id, menu_id, permission_id)`.
  - `sys_role_menu`: unique `(tenant_id, role_id, menu_id)`.
- Seed data:
  - Platform root menu plus user, org, role, menu, dictionary, audit-log, approval-task, and Kanban menu entries.
  - Menu-permission links for user/org/role/menu management routes.
  - Permissions `platform:menu:*`, `platform:permission:view`, and `platform:role:assign-menu`, bound to `platform_admin`.
  - Role-menu bindings giving `platform_admin` access to all seeded menus.
- Notes:
  - All tables include tenant, audit, soft delete, optimistic lock, and `ext_json` fields.
  - Inserts are idempotent via `ON CONFLICT DO NOTHING` or conflict updates for seeded menu display metadata.

### `V0006__platform_user_org.sql`

- Owner module: `iaf-app` migration resources, platform auth/org ownership.
- Purpose: add tenant-scoped multi-organization assignment support for platform users.
- Tables:
  - `sys_user_org`: user-to-org assignments with primary flag, scope weight, reserved validity window, audit fields, soft delete, optimistic lock, and `ext_json`.
- Important constraints and indexes:
  - Partial unique index `uk_sys_user_org_active` on `(tenant_id, user_id, org_id)` where `deleted = false`.
  - Partial unique index `uk_sys_user_org_primary` on `(tenant_id, user_id)` where `is_primary = true and deleted = false`.
  - Lookup indexes on `(tenant_id, user_id)` and `(tenant_id, org_id)`.
- Seed data:
  - Backfills `sys_user_org` rows from existing non-null `sys_user.primary_org_id` values.
- Notes:
  - `sys_user_org.is_primary` is the source used by auth/user response snapshots for current organization.
  - `sys_user.primary_org_id` remains as the fast current/primary org snapshot and is synchronized together with `sys_user_org.is_primary` by the user organization assignment application service.
  - TASK-0218 user-list data-permission tracer bullet uses active `sys_user_org` rows as the organization scope predicate.

### `V0007__tenant_lifecycle_quota_outbox.sql`

- Owner module: migration resources in `iaf-app`; runtime ownership split across auth and integration.
- Purpose: TASK-0219 tenant lifecycle, quota, and Outbox baseline.
- Changes:
  - Extends `sys_tenant` with `disabled_at`, `initialization_status`, and `initialization_error`.
  - Creates `sys_tenant_quota`, owned by `iaf-platform-auth`.
  - Creates `platform_outbox_event`, owned by `iaf-platform-integration`.
- `sys_tenant_quota`:
  - Columns include tenant, `quota_key`, `quota_limit`, `quota_used`, audit fields, soft delete, optimistic lock, and `ext_json`.
  - Unique constraint `(tenant_id, quota_key)`.
  - Seed: default tenant `USER_COUNT` quota.
- `platform_outbox_event`:
  - Columns include tenant, `event_id`, aggregate type/id, event type, JSON payload, status, retry count, next retry time, audit fields, soft delete, optimistic lock, and `ext_json`.
  - Unique event id and indexes for tenant/status, event type/status, and aggregate lookup.
- New permission seed:
  - `platform:tenant:view`, `platform:tenant:create`, `platform:tenant:update`, `platform:tenant:disable`, `platform:tenant:enable`
  - `platform:tenant-quota:view`, `platform:tenant-quota:update`
  - `platform:outbox:view`, `platform:outbox:retry`
- Role binding: all new permissions are bound to seeded `platform_admin` for default tenant.

### `V0008__platform_foundation_permission_hardening.sql`

- Owner module: migration resources in `iaf-app`; runtime permission ownership remains platform auth/permission.
- Purpose: TASK-0220 platform foundation permission hardening.
- New permission seed:
  - `platform:data-permission:view`, `platform:data-permission:update`
  - `platform:field-permission:view`, `platform:field-permission:update`
  - `platform:dictionary:view`, `platform:dictionary:update`
  - `platform:parameter:view`, `platform:parameter:update`
  - `platform:audit:view`
- Role binding: all new permissions are bound to seeded `platform_admin` for default tenant.
- Menu binding: dictionary/parameter and audit menu rows are linked to their read permissions through `sys_menu_permission`.
- Notes:
  - Idempotent through `on conflict` clauses.
  - Does not add new business tables.
  - Existing non-default tenants are not backfilled by this migration; new tenants inherit these permissions through the TASK-0219 default-tenant initialization copy path.

## Platform Foundation Delivery Package

- Path: `ai-coding/templates/platform-foundation/platform-foundation-delivery-package.json`.
- Purpose: TASK-0221 machine-readable productization baseline for platform permissions, default role templates, menu delivery, design acceptance, and regression coverage.
- Runtime relationship:
  - It does not create database objects directly.
  - Flyway migrations remain the executable runtime seed source.
  - TASK-0219 tenant initialization continues to copy default tenant permissions, menus, roles, theme, and brand configuration for newly-created tenants.
- Validation: `scripts/check-platform-foundation-templates.js` checks permission drift against frontend permission constants and backend Flyway-seeded `platform:*` permissions, then validates role/menu/permission references.
- Snapshot API: deferred by ADR-0008 until the underlying configuration domains have production-backed APIs.

## Platform Foundation Release Governance

- Path: `docs/operations/platform-foundation-release-checklist.md`.
- Purpose: RC1 release gates and compatibility rules for platform APIs, permission codes, menu codes, configuration template schema, and migration history.
- Database impact:
  - This governance task adds no migration and no tables.
  - Empty database Flyway execution is a release gate.
  - Historical Flyway migrations remain immutable; breaking persistence changes require a new migration or compatibility script.

## Platform Foundation Feedback Stabilization

- Paths:
  - `docs/operations/platform-foundation-feedback-log.md`
  - `docs/operations/platform-foundation-stabilization-plan.md`
  - `docs/operations/platform-foundation-runbook-review.md`
  - `docs/operations/platform-foundation-next-backlog.md`
- Purpose: TASK-0223 records RC1 stabilization feedback, patch cadence, runbook replay, stability metrics, and platform-layer future backlog.
- Database impact:
  - No migration, table, column, index, seed data, or ownership change.
  - Feedback remains document-based; if runtime feedback tracking is later required, it must be implemented in a separate task with tenant, audit, soft-delete, optimistic-lock, and `ext_json` fields.

### `V0001__init_platform_auth_schema.sql`

- Owner module: `iaf-app` migration resources; table ownership is split by module per ADR-0005.
- Purpose: initialize the platform identity and permission baseline.
- Tables:
  - `sys_tenant`: tenant registry, owned by `iaf-platform-auth` in phase 1.
  - `sys_user`: platform login users, owned by `iaf-platform-auth`.
  - `sys_org`: management organization tree, owned by `iaf-platform-org`.
  - `sys_role`: platform roles, owned by `iaf-platform-permission`.
  - `sys_permission`: permission points, owned by `iaf-platform-permission`.
  - `sys_user_role`: user-role assignment, owned by `iaf-platform-permission`.
  - `sys_role_permission`: role-permission assignment, owned by `iaf-platform-permission`.
- Important constraints:
  - `sys_user`: unique `(tenant_id, username)`.
  - `sys_org`: unique `(tenant_id, org_code)`.
  - `sys_role`: unique `(tenant_id, role_code)`.
  - `sys_permission`: unique `(tenant_id, permission_code)`.
- Seed data:
  - Tenant: `default`.
  - Development user: `admin` / `admin123`.
  - Role: `platform_admin`.
  - Permission: `platform:auth:me`.
- Notes:
  - The seeded password uses Spring Security `{noop}` encoding for local development only.
  - All tables include tenant, audit, soft delete, optimistic lock, and `ext_json` fields.
  - Login tenant resolution is the only public request path that looks up `sys_tenant` by `tenant_code`; authenticated APIs derive tenant from token context.

## Database Rules To Reflect Here

When adding a migration, update this file with:

- Migration filename.
- Owning module.
- Tables created or changed.
- Table purpose.
- Important columns and indexes.
- Tenant and audit behavior.
- Permission or security impact.

Business tables must include:

- `tenant_id`
- audit fields
- `deleted`
- `version`
- `ext_json`

Existing Flyway migrations must not be edited after commit. Add a new migration instead.

### `V0401__qms_engineering_data_foundation.sql`

- Owner module: `iaf-qms-engineering`.
- Purpose: Jinheng QMS Part/Drawing/DrawingRevision metadata foundation and transactional
  module audit trail.
- Tables:
  - `qms_part`: organization-owned Part master; active-row unique
    `(tenant_id, org_id, part_no)`.
  - `qms_drawing`: Part child; active-row unique `(tenant_id, part_id, drawing_no)` and
    tenant-qualified foreign key to Part.
  - `qms_drawing_revision`: Drawing child with monotonic positive `revision_seq`, unique
    active revision code/sequence per Drawing, optional self-reference for supersession,
    and reserved file/release fields.
  - `qms_audit_log`: immutable actor/action/object before/after JSON audit records.
- Initial states: Part `ACTIVE`; Drawing `ACTIVE`; DrawingRevision `DRAFT`, parse status
  `PENDING`, review status `PENDING`.
- Common fields: every table includes tenant, audit timestamps/actors, soft delete,
  optimistic-lock version, and `ext_json`.
- Indexes: tenant/org/status, tenant/parent lookup, active natural keys, revision sequence,
  audit object/actor lookup, and created time.
- Permission seeds: six `qms:*` view/create permissions are created for every current
  tenant and assigned to that tenant's `platform_admin` role.
