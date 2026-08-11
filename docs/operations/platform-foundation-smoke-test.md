# Platform Foundation Smoke Test

## Purpose

This smoke test validates the minimum Platform Foundation runtime path for RC1. It uses existing APIs only and does not introduce a verification API.

## Prerequisites

- Backend is running.
- PostgreSQL is reachable and Flyway has applied migrations.
- Default platform tenant and admin user exist.
- `curl` is installed.
- `jq` is installed.

Default local values:

```text
IAF_BASE_URL=http://localhost:8080
IAF_TENANT_CODE=default
IAF_USERNAME=admin
IAF_PASSWORD=admin123
```

## Scripted Run

```bash
IAF_BASE_URL=http://localhost:8080 \
IAF_TENANT_CODE=default \
IAF_USERNAME=admin \
IAF_PASSWORD=admin123 \
./scripts/platform-foundation-smoke-test.sh
```

Optional:

```bash
IAF_EXPECTED_TENANT_ID=1
IAF_SMOKE_TARGET_TENANT_ID=1
```

`IAF_EXPECTED_TENANT_ID` verifies the login response tenant id. `IAF_SMOKE_TARGET_TENANT_ID` enables tenant-scoped outbox list smoke checks.

## Covered Checks

| Step | API | Expected |
|---|---|---|
| Health | `GET /api/health` | success |
| Login | `POST /api/platform/auth/login` | Bearer token returned |
| Current user | `GET /api/platform/auth/me` | user id, tenant id, permissions |
| Current menus | `GET /api/platform/auth/menus` | menu payload returned |
| Users | `GET /api/platform/users` | page payload returned |
| Organizations | `GET /api/platform/orgs/tree` | tree payload returned |
| Roles | `GET /api/platform/roles` | page payload returned |
| Menu tree | `GET /api/platform/menus/tree` | tree payload returned |
| Permissions | `GET /api/platform/permissions` | permission payload returned |
| Theme | `GET /api/platform/theme/current` | config payload returned |
| Brand | `GET /api/platform/brand/current` | config payload returned |
| i18n | `GET /api/platform/i18n/resources?locale=zh-CN` | resource payload returned |
| Preference update | `GET` then merged `PUT /api/platform/preferences/me` then restore original settings | config payload returned without destroying user preferences |
| Backend denial | `GET /api/platform/users` without token | HTTP 401 or 403 |
| Outbox | `GET /api/platform/outbox-events?tenantId=...` | optional when target tenant id is set |

## Manual Checks

If the script is not available, verify:

1. Login with tenant code, username, and password.
2. Load current user.
3. Load visible menus.
4. Open users, orgs, roles, and menu management.
5. Confirm a protected API rejects a missing token.
6. Read theme, brand, and i18n resources.
7. Read current-user preferences, merge a temporary smoke marker, write it, then restore the original settings.
8. Record any audit-log gap in `platform-foundation-known-issues.md`.

## Known RC1 Gaps

Audit query, dictionary/parameter production APIs, and data/field permission production configuration APIs are not fully smoke-tested in RC1 because their production backend surfaces are not complete. They are tracked in:

```text
docs/operations/platform-foundation-known-issues.md
```

## Failure Handling

- Any P0 or P1 failure blocks RC release.
- P2 failures require owner and release-risk decision.
- Record failures in `platform-foundation-known-issues.md`.
- Do not weaken the script or quality gate to hide a real failure.
