# Platform Foundation Feedback Log

## Purpose

This is the Platform Foundation RC1 feedback intake and triage ledger. It keeps release feedback, UX review notes, Agent template feedback, smoke-test findings, and implementation runbook findings in one traceable place.

This file is not a feature backlog. Items that need implementation must point to a follow-up task or the next backlog file.

## Intake Sources

| Source | Evidence | Owner to triage | Notes |
|---|---|---|---|
| Manual release acceptance | Checklist run notes | Release owner | Use the RC gate names from `platform-foundation-release-checklist.md`. |
| Automated quality gate | `./scripts/check-quality.sh` output | Task owner | P0/P1 when quality gate blocks release. |
| Smoke test | `scripts/platform-foundation-smoke-test.sh` output | Platform owner | Record target env and credentials class, never secrets. |
| Frontend UX review | Page review table below | Frontend owner | Capture page, state, and exact interaction issue. |
| Implementation runbook replay | `platform-foundation-runbook-review.md` | Implementation owner | Capture inaccurate commands, missing prerequisites, and unclear steps. |
| Agent page template validation | `TASK-FE-0027` replay notes below | AI coding governance owner | Capture unclear template instructions and missing examples. |
| Permission/menu/template validation | `scripts/check-platform-foundation-templates.js` output | Platform permission owner | Delivery package drift is P1 unless explicitly downgraded. |

## Severity

| Severity | Definition | Stable-period policy |
|---|---|---|
| P0 | Security issue, tenant data leak, login unavailable, data corruption, empty migration failure | Hotfix immediately; blocks RC and business-domain start. |
| P1 | Permission bypass, core platform page unusable, initialization failure, quality gate failure, delivery package drift | Fix in the current stabilization window. |
| P2 | Major UX inconsistency, missing loading/error/empty/permission-denied state, missing i18n, accessibility problem, production API gap already visible to delivery | Triage with owner and target patch; may defer if not demo-critical. |
| P3 | Copy, minor layout, non-core convenience, documentation clarification | May defer to platform backlog. |

## Feedback Record Template

```text
ID:
Source:
Page/API/module:
Environment:
Steps:
Expected:
Actual:
Severity: P0 | P1 | P2 | P3
Blocks platform stability: yes | no
Owner:
Decision:
Follow-up task:
Verification:
```

## Current Feedback Ledger

| ID | Source | Page/API/module | Severity | Status | Owner | Decision | Follow-up |
|---|---|---|---|---|---|---|---|
| PF-FB-001 | Release governance review | `scripts/platform-foundation-smoke-test.sh` preferences step | P1 | FIXED | Platform system / release governance | Smoke script must read, merge, write, and restore current-user preferences because preference save is full replacement. | Completed in TASK-0222 PR #7 before merge. |
| PF-FB-002 | RC1 known issues | Dictionary and parameter pages | P2 | OPEN | `iaf-platform-system` / `pc-admin` | Mock-first page is accepted for RC1 governance but blocks production configuration acceptance. | Platform configuration governance backlog. |
| PF-FB-003 | RC1 known issues | Audit log page/API | P2 | OPEN | `iaf-platform-system` / `pc-admin` | Mock-first audit UI is accepted for RC1 governance but blocks production audit acceptance. | Platform security hardening backlog. |
| PF-FB-004 | RC1 known issues | Data/field permission configuration APIs | P2 | OPEN | `iaf-platform-permission` | Tracer-bullet behavior is accepted for RC1 governance; runtime configuration APIs are required before production pilot. | Platform security hardening backlog. |
| PF-FB-005 | RC1 known issues | Token/session store | P2 | OPEN | `iaf-platform-auth` | Development token store is accepted for RC1 governance; durable token/session strategy blocks production security hardening. | Platform security hardening backlog. |
| PF-FB-006 | Runbook replay | Local frontend dependency install | P3 | OPEN | Operations | Local `pnpm` wrapper may force a different working directory; runbook should mention using the project package manager directly when wrappers are present. | Track in runbook review. |
| PF-FB-007 | Runbook replay | `GET /api/platform/auth/menus` smoke step | P1 | OPEN | `iaf-platform-permission` / release owner | Local replay on 2026-07-15 reached health, login, tenant check, and current user, then failed current menus with HTTP 500. This blocks platform stability and business-domain entry until reproduced/fixed or explicitly reclassified. | PF-RR-003 / PF-RC1-006 |

## Core Page UX Review

Use this table during stabilization. `PASS` means the page has no blocking UX issue for RC1; `GAP` means a follow-up exists in the feedback ledger or backlog.

| Page | Primary goal clear | Main action clear | Dangerous action confirmed | Loading/error/empty covered | Field errors local | Icon-only a11y covered | Status | Follow-up |
|---|---|---|---|---|---|---|---|---|
| Login | Yes | Yes | N/A | Yes | Yes | N/A | PASS | None |
| Workbench | Yes | N/A | N/A | Basic | N/A | Basic | PASS | Future health/recent/favorites backlog |
| Users | Yes | Yes | Reset/disable require confirmation | Yes | Yes | Basic | PASS | Continue data-permission hardening |
| Organizations | Yes | Yes | Disable/delete not in RC1 | Yes | Yes | Basic | PASS | None |
| Roles | Yes | Yes | Assignment changes require explicit save | Yes | Yes | Basic | PASS | Large-permission search polish |
| Permissions | Yes | Read-focused | N/A | Basic | N/A | Basic | PASS | Runtime permission taxonomy polish |
| Menus | Yes | Yes | Cycle prevention exists; destructive ops limited | Yes | Yes | Basic | PASS | Menu-permission link editing deferred |
| Data permissions | Partly | Partly | N/A | Partial | Partial | Basic | GAP | PF-FB-004 |
| Field permissions | Partly | Partly | N/A | Partial | Partial | Basic | GAP | PF-FB-004 |
| Dictionary/parameters | Partly | Partly | Mock-first only | Partial | Partial | Basic | GAP | PF-FB-002 |
| Audit logs | Partly | Query goal clear | N/A | Partial | N/A | Basic | GAP | PF-FB-003 |
| Theme/brand/i18n | Yes | Yes | Preference reset/update guarded by UI | Yes | Yes | Basic | PASS | Snapshot API deferred by ADR-0008 |

## Agent Template Validation

Validation target:

```text
ai-coding/tasks/frontend/TASK-FE-0027-platform-page-generation-template.md
```

Replay scope for stabilization:

1. Select a low-risk platform configuration page, not a WMS/MES/SRM/QMS business page.
2. Verify the template forces `packages/api-client`, i18n, theme token usage, permission guards, table/form engine, loading/error/empty states, tests, and code map updates.
3. Record template defects here and update the template or create a follow-up task.

Current result:

| Check | Result | Notes |
|---|---|---|
| Uses `packages/api-client` | PASS | Template requires `api.ts` and hooks instead of direct HTTP. |
| i18n required | PASS | Template requires keys for visible copy. |
| Theme tokens required | PASS | Template points back to visual design governance. |
| Permission guard required | PASS | Template requires route/action permission mapping. |
| Table/form engine required | PASS | Template requires justification when bypassed. |
| Loading/error/empty states required | PASS | Template includes state coverage. |
| Tests required | PASS | Template requires frontend test and quality gate. |
| Code map update required | PASS | Template references `docs/code-map/frontend.md`. |
| Missing guidance | GAP | Add examples for production-backed dictionary/parameter pages once those APIs exist. |

Concrete replay record:

| Replay ID | Date | Agent | Selected page | Execution mode | Output | Defects found |
|---|---|---|---|---|---|---|
| PF-AGENT-001 | 2026-07-15 | Codex | `PlatformDictionaryParameterPage` under `frontend/apps/pc-admin/src/modules/platform/config/PlatformConfigPages.tsx` | Dry-run application of `TASK-FE-0027` to convert the mock-first dictionary/parameter page into a production-backed platform page | No runtime code generated in TASK-0223; output captured as backlog item PF-NEXT-004 because backend dictionary/parameter APIs are not production-complete | Template covers page structure, API client, i18n, theme, permissions, tests, and code map, but lacks a mock-first-to-production migration example and an API-readiness decision point. |

## Escalation Rules

- A new P0/P1 item must also be copied to `platform-foundation-known-issues.md`.
- A P2 item can remain in this feedback log when it has an owner and release decision.
- A deferred item must point to `platform-foundation-next-backlog.md` or a concrete `ai-coding/tasks/**` file.
- Do not close an item until verification evidence is recorded.
