# Platform Foundation Runbook Review

## Purpose

This document records RC1 runbook replay results. It verifies that a new environment, tenant, and implementer can reproduce the Platform Foundation baseline without relying on personal memory.

Primary runbooks:

- `docs/operations/RUNBOOK-platform-foundation-productization.md`
- `docs/operations/platform-foundation-release-checklist.md`
- `docs/operations/platform-foundation-smoke-test.md`
- `docs/operations/RUNBOOK-platform-foundation-troubleshooting.md`

## Replay Scope

| Area | Replay step | Expected result | Status | Notes |
|---|---|---|---|---|
| Environment | Confirm Java 21 and Maven | Versions available | PASS | Java 21 and Maven are required for backend checks. |
| Infrastructure | Start or reuse PostgreSQL | Backend can connect to configured datasource | PASS | Local default uses PostgreSQL on host port `5123`. |
| Database migration | Start backend or run tests with Flyway enabled | Migrations apply through latest version | PASS | Current local replay moved schema to `V0008` during startup. |
| Frontend dependencies | Install frontend dependencies | Dependency-based checks can run | GAP | Local `pnpm` wrapper may force another directory; use direct project package-manager entry when wrappers are customized. |
| Delivery package validation | `node scripts/check-platform-foundation-templates.js` | Permission/menu/role package passes drift checks | PASS | Covered by quality gate. |
| Quality gate | `./scripts/check-quality.sh` | Backend and frontend gates pass or dependency skip is explicit | PASS | Existing script reports frontend dependency skip when dependencies are absent. |
| Runtime backend | Start `iaf-app` | `/api/health` returns success | PASS | If `8080` is occupied, use `BACKEND_PORT=8081`. |
| Runtime frontend | Start Vite app | `/` returns `200`; `/api` proxy reaches backend | PASS | Set `VITE_IAF_API_PROXY_TARGET` when backend is not on `8080`. |
| Smoke test | Run `platform-foundation-smoke-test.sh` | Existing API baseline passes | FAIL | Local replay on 2026-07-15 reached current user, then `GET /api/platform/auth/menus` returned HTTP 500. |

## Replay Evidence

Local replay command:

```bash
IAF_BASE_URL=http://localhost:8081 \
IAF_TENANT_CODE=default \
IAF_USERNAME=admin \
IAF_PASSWORD=admin123 \
IAF_EXPECTED_TENANT_ID=1 \
IAF_SMOKE_TARGET_TENANT_ID=1 \
./scripts/platform-foundation-smoke-test.sh
```

Observed result:

```text
PASS health
PASS login
PASS login tenant id
PASS current user
FAIL current menus: expected HTTP 200, got 500
{"success":false,"code":"COMMON_INTERNAL_ERROR","message":"Internal server error","data":null}
```

This is an incomplete runbook replay. It is intentionally recorded as a stabilization blocker instead of being treated as a completed run.

## Replay Notes

### Backend startup

The backend root POM is not the Spring Boot app. If `mvn -pl iaf-app -am spring-boot:run` executes the parent project in the local Maven setup, use one of these safer approaches:

```bash
cd backend
mvn -pl iaf-app -am package -DskipTests
mvn -pl iaf-app package spring-boot:repackage -DskipTests
BACKEND_PORT=8081 java -jar iaf-app/target/iaf-app-0.1.0-SNAPSHOT.jar
```

Use `8081` or another free port when `8080` is occupied.

### Frontend startup

Default Vite proxy target is `http://localhost:8080`. If the backend is on another port:

```bash
cd frontend/apps/pc-admin
VITE_IAF_API_PROXY_TARGET=http://localhost:8081 ../../node_modules/.bin/vite --host 0.0.0.0 --port 5173
```

If a local `pnpm` wrapper changes directories before executing, use the project package manager binary directly or install dependencies from a shell where `pnpm` respects the current working directory.

### Preference smoke safety

The smoke test must preserve current-user preferences:

1. `GET /api/platform/preferences/me`.
2. Save the original `settings`.
3. Merge a temporary smoke marker.
4. `PUT /api/platform/preferences/me`.
5. Restore the original settings with another `PUT`.

Do not send a partial settings object unless the API explicitly supports merge semantics.

## Findings

| ID | Severity | Status | Area | Finding | Action |
|---|---|---|---|---|---|
| PF-RR-001 | P3 | OPEN | Frontend environment | Some local shells may resolve `pnpm` to a wrapper that changes directories. | Document direct binary fallback in future local environment notes if this affects more agents. |
| PF-RR-002 | P3 | OPEN | Backend startup | Parent-POM `spring-boot:run` can fail because the parent has no main class. | Prefer app module repackage + `java -jar` in local startup notes. |
| PF-RR-003 | P1 | OPEN | Smoke test | `GET /api/platform/auth/menus` returned HTTP 500 during local replay on 2026-07-15 after login/current-user succeeded. | Reproduce against a clean database or target release environment, fix or reclassify, then rerun the full smoke test. |

## Completion Criteria

The runbook is considered stable enough for business-domain entry when:

```text
[x] Empty-database migration replay succeeds.
[x] Default tenant/admin login is smoke-tested.
[ ] Current user, menus, users, orgs, roles, menus, permissions, theme, brand, i18n, and preferences are smoke-tested.
[ ] At least one implementer can follow the runbook without private context.
[ ] All runbook P0/P1 findings are closed.
```
