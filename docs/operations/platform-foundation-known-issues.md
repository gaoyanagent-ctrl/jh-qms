# Platform Foundation Known Issues

## Purpose

This file is the RC1 defect ledger for Platform Foundation. It prevents release decisions from being hidden in review comments or informal chat.

## Release Policy

- P0 and P1 issues block RC release.
- P2 issues require explicit owner and release-risk decision.
- P3 issues may move to backlog.
- A deferred item must reference a later task or backlog category.

## Current Issue Summary

| ID | Severity | Status | Owner module | Summary | Release decision | Follow-up |
|---|---|---|---|---|---|---|
| PF-RC1-001 | P2 | OPEN | iaf-platform-system / frontend pc-admin | Dictionary and parameter pages are mock-first; production backend APIs are not complete. | Does not block RC1 governance because permissions/menu/template are seeded and the gap is visible. | Platform configuration governance backlog |
| PF-RC1-002 | P2 | OPEN | iaf-platform-system / frontend pc-admin | Audit log page is mock-first; production audit persistence/query API is not complete. | Does not block RC1 governance; blocks production audit acceptance. | Platform security hardening backlog |
| PF-RC1-003 | P2 | OPEN | iaf-platform-permission | Data-permission and field-permission runtime configuration APIs are not complete beyond tracer-bullet behavior. | Does not block RC1 governance; must be completed before production pilot. | Platform security hardening backlog |
| PF-RC1-004 | P2 | DEFERRED | iaf-platform-system | Config snapshot import/export API is deferred. | Accepted for RC1 by ADR-0008. | Platform configuration governance backlog |
| PF-RC1-005 | P2 | OPEN | iaf-platform-auth / iaf-app | Current token store remains development-oriented; durable JWT/session revocation strategy is not complete. | Does not block RC1 governance; blocks production security hardening. | Platform security hardening backlog |
| PF-RC1-006 | P1 | OPEN | iaf-platform-permission / release validation | Local smoke replay on 2026-07-15 failed at `GET /api/platform/auth/menus` with HTTP 500 after health, login, tenant check, and current user passed. | Blocks platform stability and business-domain entry until fixed, reproduced cleanly, or explicitly reclassified by review. Does not require runtime code in TASK-0223 because this task records the stabilization mechanism. | Platform stabilization hotfix |

## P0/P1 Status

At the time this governance task was first created, there were no known open P0 or P1 issues recorded in this ledger. The 2026-07-15 runbook replay added PF-RC1-006 as an open P1 stabilization blocker.

Any new P0/P1 found during release validation must be added above and must block release until fixed or explicitly reclassified through review.

## Stabilization Feedback Link

RC1 stabilization feedback is collected in:

```text
docs/operations/platform-foundation-feedback-log.md
```

Only issues that affect release decision, P0/P1 blocking status, or accepted P2 deferrals need to be mirrored in this known-issues ledger. Routine P3 polish can stay in the feedback log or next backlog.

## Issue Record Template

```text
ID:
Severity: P0 | P1 | P2 | P3
Status: OPEN | FIXING | FIXED | DEFERRED | WON'T_FIX
Owner module:
Problem:
Impact:
Reproduction:
Expected:
Actual:
Release decision:
Follow-up task:
Verification:
```

## Severity Guide

P0:

- Security exploit.
- Tenant data leak.
- Login unavailable.
- Data corruption.
- Empty database migration failure.

P1:

- Backend permission bypass.
- Core platform page unusable.
- Default tenant/user/menu initialization failure.
- Quality gate failure.
- Delivery package drift from backend or frontend permission source.

P2:

- Mock-first page blocks production usage but not governance.
- Noticeable UI inconsistency.
- Missing loading, empty, error, or permission-denied state.
- Missing i18n key.
- Accessibility or keyboard issue.

P3:

- Copy improvement.
- Minor layout adjustment.
- Non-core workflow convenience.
