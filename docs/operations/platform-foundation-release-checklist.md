# Platform Foundation RC1 Release Checklist

## Purpose

This checklist freezes the Platform Foundation RC1 scope and defines the gates that must pass before a release candidate can be accepted. It is a release governance document, not a new feature backlog.

## RC1 Scope

| Capability | Status | Release expectation | Notes |
|---|---|---|---|
| Login and tenant context | READY | Tenant code login, current user, current organization snapshot, disabled tenant rejection | Public login remains the only tenant-code lookup path. |
| Application shell, navigation, and tabs | READY | Protected shell, backend-driven menus, permission-filtered routes, tab workspace | Frontend token storage remains a pre-production hardening item. |
| Organization management | READY | Tree list, create, update, route permission, tenant isolation | Platform org only; manufacturing org dimensions are outside RC1. |
| User management | READY | List, create, update, disable, reset password, org assignment, current-org switch | User list has the TASK-0218 data-permission tracer bullet. |
| Role, permission, and menu management | READY | Role CRUD, permission assignment, menu tree, menu assignment | Permission matrix is governed by the TASK-0221 delivery package. |
| Data permission and field permission | NEEDS_FIX | Seeded permissions and frontend/page-level tracer bullet exist | Production backend configuration APIs remain future work. |
| Dictionary and parameter | NEEDS_FIX | Seeded permissions and frontend mock-first page exist | Production backend APIs are not complete. |
| Audit log | NEEDS_FIX | Seeded permission and frontend mock-first page exist | Production audit persistence/query API is not complete. |
| Theme, brand, i18n, and user preference | READY | Current config APIs and preference persistence exist | Snapshot import/export is deferred by ADR-0008. |
| Tenant lifecycle, quota, and outbox | READY | Backend APIs, permissions, tenant initialization, and outbox retry exist | Frontend operational pages are not in RC1. |
| Platform configuration delivery package | READY | Versioned template, permission/menu/role matrix, validation script | `scripts/check-platform-foundation-templates.js` is in the quality gate. |
| Productization and troubleshooting runbooks | READY | Implementation and troubleshooting paths are documented | See `RUNBOOK-platform-foundation-productization.md`. |
| Platform page generation template | READY | Future platform pages have a task template | See `ai-coding/tasks/frontend/TASK-FE-0027-platform-page-generation-template.md`. |
| Configuration snapshot import/export API | DEFERRED | No runtime API in RC1 | Deferred by ADR-0008 until backing config domains are production-backed. |

## Out Of Scope

- WMS, MES, SRM, QMS, purchasing, production, quality, inventory, or other business-domain flows.
- Full workflow designer enhancement.
- Full low-code runtime.
- Commercial billing.
- Microservice split.
- New UI framework, ORM, workflow engine, or rule engine.

## Release Gates

All gates are blocking unless the release owner records an explicit downgrade in `platform-foundation-known-issues.md`.

| Gate | Required command or evidence | Blocks RC |
|---|---|---|
| Repository quality gate | `./scripts/check-quality.sh` exits 0 | Yes |
| Backend tests | Maven reactor tests pass through `scripts/run-backend-tests.sh` | Yes |
| Frontend checks | `scripts/run-frontend-checks.sh` typecheck/test/build pass when dependencies are installed | Yes for release build |
| Frontend static guardrails | Direct HTTP/token access guardrails pass | Yes |
| Template drift | `node scripts/check-platform-foundation-templates.js` passes | Yes |
| Empty database migration | Start backend against an empty PostgreSQL database with Flyway enabled | Yes |
| Default admin login | Smoke test login succeeds for configured tenant/user | Yes |
| Current user and menus | Smoke test loads `/api/platform/auth/me` and `/api/platform/auth/menus` | Yes |
| Core platform reads | Smoke test reads users, orgs, roles, menus, permissions | Yes |
| Backend denial | Smoke test confirms protected API rejects missing token | Yes |
| Non-sensitive config update | Smoke test reads, merges, writes, and restores `/api/platform/preferences/me` | Yes |
| Theme, brand, i18n reads | Smoke test reads current theme, brand, and i18n resources | Yes |
| Audit query | Production audit API exists and is smoke-tested | No for RC1; tracked as NEEDS_FIX |
| Snapshot API | Available or explicitly deferred by ADR | No for RC1; deferred by ADR-0008 |

## Defect Severity

| Level | Definition | RC policy |
|---|---|---|
| P0 | Security break, tenant data leak, login outage, data corruption, migration failure | Must be fixed before RC |
| P1 | Permission bypass, core page unusable, default initialization failure, quality gate failure | Must be fixed before RC |
| P2 | Major visual inconsistency, missing error/empty state, missing i18n, accessibility issue | Must be triaged before RC; may defer with owner |
| P3 | Copy improvement, minor layout issue, non-core convenience issue | May defer to backlog |

Known issues and deferrals are tracked in:

```text
docs/operations/platform-foundation-known-issues.md
```

## Compatibility Rules

After RC1, these contracts are treated as published platform foundation contracts:

- API paths listed in `docs/code-map/api.md`.
- Permission codes seeded by Flyway and listed in `ai-coding/templates/platform-foundation/platform-foundation-delivery-package.json`.
- Menu codes listed in the delivery package.
- i18n keys used by platform shell and platform pages.
- Delivery package schema fields.
- Database migration history and table ownership recorded in `docs/code-map/database.md`.

Rules:

- Do not rename or remove a published API path, permission code, menu code, or i18n key without an ADR.
- Do not edit historical Flyway migrations; add a new migration or compatibility script.
- Additive API fields are allowed when backward compatible.
- Breaking API/request/response changes need a compatibility period or migration path.
- Delivery package `schemaVersion` must increase when consumers need different parsing logic.
- Any permission/menu/config template change must update the delivery package, code map, and release checklist when release semantics change.

## Design System Governance

Release acceptance uses `docs/frontend/17_平台管理视觉设计规范.md` and `docs/frontend/19_平台应用外壳与导航规范.md` as the long-term visual contract.

Blocking design regressions:

- Hardcoded colors in platform pages.
- Hardcoded user-visible Chinese/English copy in pages.
- Direct permission-array checks in pages.
- Standard list/form pages bypassing table-engine/form-engine without documented reason.
- New duplicate components for existing status, table, form, shell, or permission primitives.
- Missing loading, error, empty, or permission-denied states on a platform page.

## RC1 Smoke Test

Run:

```bash
IAF_BASE_URL=http://localhost:8080 \
IAF_TENANT_CODE=default \
IAF_USERNAME=admin \
IAF_PASSWORD=admin123 \
./scripts/platform-foundation-smoke-test.sh
```

The detailed procedure is in:

```text
docs/operations/platform-foundation-smoke-test.md
```

## Production Frontend Deployment

Use only `./scripts/deploy-production-frontend.sh`. Do not run production
`docker compose build/up` directly from a task worktree. The script serializes
deployments across worktrees, requires a clean revision already contained in
`origin/main` by default, runs navigation regression tests, builds an immutable
SHA-tagged image, and verifies the running revision label after replacement.

## Post-RC Backlog Categories

| Category | Example items | Blocks business modules |
|---|---|---|
| Platform security hardening | Durable token/JWT, token revocation, audit persistence, field permission backend APIs | Yes for production pilot |
| Platform experience optimization | Page-level visual polish, accessibility, keyboard coverage, visual baselines | No, unless demo-critical |
| Platform configuration governance | Snapshot export/import APIs, dictionary/parameter backend APIs, config versioning | Yes for implementation package reuse |
| Platform operations | Health dashboard, outbox dispatch worker, tenant initialization reports | No for first business module development |
| Platform designer | Workflow designer hardening, page/form metadata designer | No for CRUD-first modules |
| Platform integration | Retry scheduler, integration credentials, adapter registry | No unless integration scenario starts |
| AI coding assistance | Page generation lint, task templates, code-map validators | No |

Business modules may start only when:

- P0/P1 issues are closed.
- User/org/role/menu/permission platform flows are usable.
- Tenant isolation and backend permission checks remain green.
- A module-specific task defines its own API, DB, permission, frontend, and test scope.

## RC1 Stabilization Flow

After RC1 release governance is accepted, feedback is handled through:

- `docs/operations/platform-foundation-feedback-log.md`: feedback intake, UX review, Agent template replay, and triage.
- `docs/operations/platform-foundation-stabilization-plan.md`: hotfix/patch/polish/deferred policy, stability metrics, and business-domain entry checklist.
- `docs/operations/platform-foundation-runbook-review.md`: reproducibility replay and runbook correction notes.
- `docs/operations/platform-foundation-next-backlog.md`: classified platform-layer backlog after stabilization.

P0/P1 feedback still blocks platform stability. P2 feedback requires owner and release decision. P3 feedback can be deferred when it does not affect core platform setup, security, or implementation reproducibility.
