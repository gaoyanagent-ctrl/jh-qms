# Platform Foundation Productization Runbook

## Purpose

This runbook gives implementation teams and Agents a repeatable path for validating the IAF platform foundation before WMS, MES, SRM, QMS, or other business modules are added.

## Inputs

- Repository branch containing the latest platform foundation changes.
- PostgreSQL database configured through `backend/iaf-app/src/main/resources/application.yml` or environment variables.
- Frontend dependencies installed when frontend checks are required.
- Platform foundation delivery package:

```text
ai-coding/templates/platform-foundation/platform-foundation-delivery-package.json
```

## Environment Preparation

1. Start from a clean task worktree.
2. Confirm Java 21 and Maven are available:

```bash
cd backend
mvn -version
```

3. Confirm Node, pnpm, and ripgrep are available:

```bash
node --version
pnpm --version
rg --version
```

4. Install frontend dependencies when frontend checks are required:

```bash
cd frontend
pnpm install
```

## Database Migration

Run backend tests or start the app with Flyway enabled. Flyway migrations live in:

```text
backend/iaf-app/src/main/resources/db/migration/
```

Important baseline migrations:

- `V0001__init_platform_auth_schema.sql`
- `V0002__seed_platform_management_permissions.sql`
- `V0004__platform_system_configuration.sql`
- `V0005__platform_menu_permission.sql`
- `V0006__platform_user_org.sql`
- `V0007__tenant_lifecycle_quota_outbox.sql`
- `V0008__platform_foundation_permission_hardening.sql`

Do not edit historical migrations. Add a new migration for changes.

## Default Configuration Validation

Run the productization package validator:

```bash
node scripts/check-platform-foundation-templates.js
```

Expected result:

```text
Platform foundation template check passed: ...
```

The validator checks frontend permission constants, backend Flyway-seeded permissions, role templates, menu templates, and productization matrices.

## Tenant and User Verification

1. Create or select a tenant through the platform tenant lifecycle API when available.
2. Confirm the tenant has platform permissions copied from the default tenant.
3. Confirm at least one administrator user has:

```text
platform:auth:me
platform:user:view
platform:org:view
platform:role:view
platform:menu:view
```

4. Confirm the user has at least one active organization assignment and current organization context.

## Platform Page Verification

Use the delivery package `designAcceptanceMatrix` and `regressionMatrix` as the checklist.

Core pages:

- `/login`
- `/`
- `/platform/users`
- `/platform/orgs`
- `/platform/roles`
- `/platform/menus`
- `/platform/dictionaries`
- `/platform/audit-logs`
- `/platform/approval/tasks`
- `/platform/kanban`

Each protected platform page must be validated for:

- Route access with the required permission.
- Route denial without the required permission.
- Loading, empty, and error state behavior.
- i18n text rendering.
- Theme token usage.
- Permission-driven actions.

## Quality Gate

Run:

```bash
./scripts/check-quality.sh
```

The gate includes:

- Platform foundation template validation.
- Backend tests.
- Frontend static guardrails, typecheck, tests, and build when dependencies are installed.

If frontend dependencies are intentionally absent, `run-frontend-checks.sh` still runs static guardrails and then reports the dependency skip.

## RC1 Release Governance

Before calling a Platform Foundation build an RC, complete:

```text
docs/operations/platform-foundation-release-checklist.md
docs/operations/platform-foundation-smoke-test.md
docs/operations/platform-foundation-known-issues.md
```

The release checklist freezes RC1 scope, defines blocking gates, records compatibility rules, and links known P0/P1/P2/P3 issues to release decisions.

## Troubleshooting

See:

```text
docs/operations/RUNBOOK-platform-foundation-troubleshooting.md
```
