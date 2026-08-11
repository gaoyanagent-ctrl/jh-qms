# Platform Foundation Document Index

## Purpose

This is the single entrypoint for Platform Foundation RC1 release, implementation, validation, and maintenance documents.

## Authority

| Topic | Authoritative source |
|---|---|
| Global agent rules | `AGENTS.md` |
| Executable engineering rules | `ai-coding/rules/` |
| Canonical tasks | `ai-coding/tasks/` |
| Architecture intent | `docs/architecture/` |
| ADRs | `docs/decisions/` |
| Current implementation map | `docs/code-map/` |
| Frontend specifications | `docs/frontend/` |
| Runbooks and release records | `docs/operations/` |

Lower-authority documents must not override higher-authority rules.

## RC1 Release Governance

- `docs/operations/platform-foundation-release-checklist.md`
  - RC1 scope freeze.
  - Release gates.
  - Defect severity.
  - Compatibility rules.
  - Backlog categories.
- `docs/operations/platform-foundation-known-issues.md`
  - P0/P1/P2/P3 issue ledger.
  - Current deferrals.
  - Issue record template.
- `docs/operations/platform-foundation-smoke-test.md`
  - Manual and scripted smoke test procedure.
- `scripts/platform-foundation-smoke-test.sh`
  - Existing API smoke test helper.
- `docs/operations/platform-foundation-feedback-log.md`
  - RC1 stabilization feedback intake.
  - UX review record.
  - Agent page-template validation feedback.
- `docs/operations/platform-foundation-stabilization-plan.md`
  - Stabilization patch policy.
  - Stability metrics.
  - Business-domain entry checklist.
- `docs/operations/platform-foundation-runbook-review.md`
  - Implementation runbook replay results.
  - Reproducibility gaps and local startup notes.
- `docs/operations/platform-foundation-next-backlog.md`
  - Classified platform-layer enhancement backlog after RC1 stabilization.

## Productization Assets

- `ai-coding/templates/platform-foundation/platform-foundation-delivery-package.json`
  - Permission matrix.
  - Role templates.
  - Menu templates.
  - Design acceptance matrix.
  - Regression matrix.
- `scripts/check-platform-foundation-templates.js`
  - Validates the delivery package against frontend permission constants and backend Flyway seed permissions.
- `docs/decisions/0008-platform-foundation-configuration-template.md`
  - Records why the delivery package is not yet a runtime config snapshot API.
- `docs/operations/RUNBOOK-platform-foundation-productization.md`
  - Implementation and validation runbook.
- `docs/operations/RUNBOOK-platform-foundation-troubleshooting.md`
  - Troubleshooting runbook.
- `ai-coding/tasks/frontend/TASK-FE-0027-platform-page-generation-template.md`
  - Template for future platform management pages.

## Current Implementation Maps

- `docs/code-map/backend.md`
  - Maven modules, backend classes, and important service/controller methods.
- `docs/code-map/api.md`
  - API contracts, permission expectations, and request/response notes.
- `docs/code-map/database.md`
  - Flyway migrations, tables, ownership, and seed behavior.
- `docs/code-map/frontend.md`
  - Frontend workspace, routes, pages, quality scripts, and platform productization assets.

## Frontend UX and Design Contracts

- `docs/frontend/16_平台管理页面交互规范.md`
  - Platform management page interactions.
- `docs/frontend/17_平台管理视觉设计规范.md`
  - Visual tokens, density, component appearance, and design governance.
- `docs/frontend/19_平台应用外壳与导航规范.md`
  - App shell, sidebar, topbar, user profile, and preference governance.

## Maintenance Flow

1. Start from `AGENTS.md`.
2. Read the current task under `ai-coding/tasks/`.
3. Read this document index.
4. Read the specific code map and frontend/backend rules for the affected area.
5. Make changes in an independent worktree and task branch.
6. Update the delivery package, code map, release checklist, or known-issue ledger when contracts change.
7. During stabilization, record feedback in `platform-foundation-feedback-log.md` before creating a patch task.
8. Run the quality gate and record results in the PR.

## Avoiding Duplicate Truth

- Do not copy API details into runbooks when `docs/code-map/api.md` is the authoritative current API map.
- Do not copy full permission matrices into prose documents; link to the delivery package.
- Do not create task files under `docs/operations/`.
- Do not create release exceptions outside `platform-foundation-known-issues.md`.
