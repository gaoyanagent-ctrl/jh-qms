# Platform Foundation Next Backlog

## Purpose

This backlog classifies platform-layer enhancements after RC1 stabilization. It prevents deferred platform work from being mixed into WMS, MES, SRM, QMS, or other business-domain tasks.

## Classification

| Category | Blocks business-domain start | Blocks production pilot | Notes |
|---|---|---|---|
| Security hardening | Only P0/P1 items | Yes | Durable auth, audit, data/field permission backends. |
| Operations hardening | No, unless release gate fails | Partly | Health, outbox worker, tenant initialization reports. |
| Configuration governance | No for first business slice | Yes for repeatable delivery package reuse | Snapshot import/export and config versioning. |
| UX stabilization | Only demo-critical P1/P2 | Partly | Accessibility, keyboard coverage, large permission assignment ergonomics. |
| AI Coding governance | No | No | Agent page-generation checks and template examples. |
| Platform designer | No | Scenario-dependent | Workflow/page/form designer hardening. |

## Backlog Items

| ID | Category | Priority | Blocks business-domain start | Summary | Trigger | Candidate task |
|---|---|---|---|---|---|---|
| PF-NEXT-001 | Security hardening | P1 | No, unless production pilot starts | Replace development in-memory token store with durable JWT/session strategy, expiration, revocation, and logout invalidation. | Production pilot, external users, or security review. | `TASK: Platform auth durable session hardening` |
| PF-NEXT-002 | Security hardening | P1 | No | Implement production audit persistence/query API and wire audit page to backend. | Audit acceptance or regulated pilot. | `TASK: Platform audit persistence and query` |
| PF-NEXT-003 | Security hardening | P1 | No | Complete runtime data-permission and field-permission configuration APIs beyond tracer-bullet behavior. | Production permission model, field masking, or AI context filtering. | `TASK: Platform data and field permission runtime APIs` |
| PF-NEXT-004 | Configuration governance | P2 | No | Implement dictionary and parameter backend APIs and replace mock-first page data. | Implementation package needs editable config. | `TASK: Platform dictionary parameter backend closure` |
| PF-NEXT-005 | Configuration governance | P2 | No | Implement configuration snapshot export/import with validation, idempotency, audit, and sensitive-field exclusion. | Multi-tenant rollout or environment promotion. | `TASK: Platform configuration snapshot API` |
| PF-NEXT-006 | Operations hardening | P2 | No | Add system health page and tenant initialization report. | Operations handoff needs runtime visibility. | `TASK: Platform operations health console` |
| PF-NEXT-007 | Operations hardening | P2 | No | Add outbox dispatch worker and frontend outbox operations page. | Integration scenario begins. | `TASK: Platform outbox operations hardening` |
| PF-NEXT-008 | UX stabilization | P2 | No | Improve role permission assignment for large permission sets: search, grouping, half-check explanation, assignment preview. | Permission count grows or implementation feedback reports confusion. | `TASK-FE: Role permission assignment UX polish` |
| PF-NEXT-009 | UX stabilization | P3 | No | Add favorites and recent visits to the shell/workbench. | Repeated implementation usage confirms value. | `TASK-FE: Platform shell recent and favorites` |
| PF-NEXT-010 | UX stabilization | P3 | No | Strengthen keyboard focus and icon-only aria/tooltip audit across platform pages. | Accessibility acceptance starts. | `TASK-FE: Platform accessibility pass` |
| PF-NEXT-011 | AI Coding governance | P3 | No | Add page-generation static checker for direct HTTP, hardcoded copy/color, permission-array checks, and missing code-map update hints. | More Agents start platform-page work. | `TASK: Platform page generation checker` |
| PF-NEXT-012 | AI Coding governance | P3 | No | Add production-backed examples to `TASK-FE-0027` after dictionary/parameter APIs exist. | PF-NEXT-004 completes. | `TASK-FE: Platform page template examples` |
| PF-NEXT-013 | Platform designer | P3 | No | Harden workflow/page/form designer entry pages and metadata governance. | Designer phase begins. | `TASK: Platform designer governance hardening` |
| PF-NEXT-014 | Operations hardening | P1 | Yes | Reproduce and fix or reclassify smoke-test failure where `GET /api/platform/auth/menus` returns HTTP 500 after login/current-user succeeds. | PF-RC1-006 remains open. | `TASK: Platform smoke current-menu failure stabilization` |

## Backlog Rules

- Do not combine unrelated backlog items into one large integration PR.
- Every backlog item that changes runtime behavior needs its own task file under `ai-coding/tasks/`.
- Security and permission backlog items must include negative tests.
- API, permission, menu, database, frontend route, or delivery package changes must update the corresponding code map.
- Business-domain tasks may reference this backlog but must not silently implement platform backlog work inside a business PR.
