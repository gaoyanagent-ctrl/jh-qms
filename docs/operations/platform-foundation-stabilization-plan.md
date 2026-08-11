# Platform Foundation Stabilization Plan

## Purpose

This plan defines the Platform Foundation RC1 stabilization window after release governance. It controls how feedback becomes patches, how stability is measured, and when the platform foundation is stable enough for business-domain work to begin.

## Stabilization Window

| Window | Allowed changes | Gate |
|---|---|---|
| RC1 stabilization | P0/P1 hotfixes, P2 patches with owner, documentation/runbook corrections, non-breaking UX polish | Quality gate and review required |
| Post-stabilization | P0/P1 hotfix only unless a new task explicitly opens platform enhancement work | Release owner approval |
| Business-domain start | No open P0/P1; accepted P2 list has owner and target; core user/org/role/menu/permission flows usable | Entry checklist below |

## Patch Types

| Type | Scope | Compatibility rule | Required evidence |
|---|---|---|---|
| `hotfix` | P0/P1 security, migration, login, permission, tenant isolation, core page outage | May change contracts only with ADR or compatibility migration | Failing reproduction, fix test, quality gate |
| `patch` | P2 production/API/UX gap that affects implementation or demo | Must be additive or backward compatible | Linked feedback item, focused tests or documented manual check |
| `polish` | P3 copy, layout, accessibility, documentation improvement | Must not rename API, permission, menu, i18n, or config fields | Review checklist and relevant static checks |
| `deferred` | Valuable but not current stabilization scope | Must be recorded in next backlog | Owner and trigger condition |

## Patch Merge Requirements

Every stabilization patch must:

- Use an independent task branch and worktree.
- Reference a feedback ID from `platform-foundation-feedback-log.md`.
- Avoid WMS/MES/SRM/QMS business-domain scope.
- Preserve published API paths, permission codes, menu codes, i18n keys, delivery package schema, and migration history unless an ADR and compatibility path are provided.
- Update tests, delivery package, docs, and code map when contracts or implementation maps change.
- Run `./scripts/check-quality.sh` or record an equivalent command set and the reason the full gate could not run.

## Stability Metrics

| Metric | Collection method | Stable threshold | Owner |
|---|---|---|---|
| P0/P1 open count | `platform-foundation-known-issues.md` and feedback log | `0` | Release owner |
| Smoke test pass rate | `scripts/platform-foundation-smoke-test.sh` on target environment | Two consecutive passes after last P1 fix | Release owner |
| Backend test pass rate | `scripts/run-backend-tests.sh` or Maven reactor | Pass | Backend owner |
| Frontend typecheck/lint/test/build | `scripts/run-frontend-checks.sh` with dependencies installed | Pass for release build | Frontend owner |
| Template drift | `node scripts/check-platform-foundation-templates.js` | Pass | Platform permission owner |
| Permission-denial false positives | Manual/automated release notes | No core-page blocker | Platform permission owner |
| Unreachable menu/page count | Smoke/manual navigation | `0` for RC1 core pages | Frontend owner |
| i18n missing count | Frontend checks and manual review | `0` blocking visible platform shell/page labels | Frontend owner |
| Theme token violations | Static guardrails and review | `0` blocking platform page violations | Frontend owner |
| Direct HTTP/token access violations | `scripts/run-frontend-checks.sh` guardrails | `0` | Frontend owner |
| Code map unsynced count | Review against diff | `0` for changed contracts | Task owner |
| Runbook replay gaps | `platform-foundation-runbook-review.md` | No P0/P1 replay gap | Operations owner |

## Business-Domain Entry Checklist

Business-domain module implementation may start only when:

```text
[ ] No open P0/P1 in platform foundation known issues. Current blocker: PF-RC1-006.
[ ] No open P0/P1 in platform foundation feedback log. Current blocker: PF-FB-007.
[ ] Smoke test passed twice on the target baseline after the latest stabilization patch. Current replay is incomplete at current menus.
[ ] User/org/role/menu/permission flows are usable for implementation setup.
[ ] Tenant isolation and backend permission tests remain green.
[ ] Frontend shell, login, protected routes, and core platform page navigation are stable.
[ ] Delivery package validation passes.
[ ] Accepted P2 items have owner, release decision, and backlog destination.
[ ] Runbook replay has no blocker that depends on personal knowledge.
[ ] New business-domain task defines its own API, DB, permission, frontend, and test scope.
```

## Compatibility Guardrails

During stabilization:

- Published API paths may only receive additive fields or compatible validation tightening.
- Permission and menu code renames are breaking changes and require ADR plus migration/config-template update.
- i18n key deletion is breaking when the key is used by shell, menu, or published platform pages.
- Historical Flyway migrations remain immutable.
- Preference writes must preserve unrelated settings unless the API explicitly documents replacement semantics and the caller intentionally replaces the full object.
- Delivery package `schemaVersion` changes only when consumers need new parsing behavior.

## Reporting Cadence

At the end of each stabilization patch:

1. Update the feedback item status.
2. Update `platform-foundation-known-issues.md` if severity is P0/P1 or release decision changes.
3. Update the next backlog when a deferred item is created.
4. Record quality gate results in the PR.
5. Keep this plan unchanged unless the stabilization policy changes.
