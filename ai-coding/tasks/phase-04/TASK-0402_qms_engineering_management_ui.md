# TASK-0402 QMS Engineering Management UI

## 1. Task Objective

Deliver the first operator-facing JH QMS frontend vertical slice: authenticated,
permission-aware Part management, Drawing hierarchy navigation, and DrawingRevision
history on top of the validated TASK-0401 API contract.

## 2. Required Reading

- `AGENTS.md`
- `docs/architecture/06_项目文档与规则目录治理.md`
- `docs/decisions/0009-qms-engineering-module-boundary.md`
- `docs/module-specs/qms/01_engineering_data_spec.md`
- `docs/code-map/README.md`
- `docs/code-map/frontend.md`
- `docs/code-map/api.md`
- `ai-coding/rules/01_project_rules.md`
- `ai-coding/rules/03_frontend_rules.md`
- `ai-coding/rules/05_api_rules.md`
- `ai-coding/rules/06_permission_rules.md`
- `ai-coding/rules/10_testing_rules.md`
- `ai-coding/rules/12_code_map_rules.md`
- Relevant specifications under `docs/frontend/`
- `docs/quality/quality_gate.md`

## 3. In Scope

- Add QMS routes and navigation entries to the PC admin application.
- Add typed QMS API clients and TanStack Query hooks through `packages/api-client`.
- Add a configurable Part list with keyword search, pagination, empty/error/loading states,
  column preferences, and permission-aware create action.
- Add a Part creation form with validation and submission feedback.
- Add a Part detail page that shows engineering metadata, child Drawings, and each
  Drawing's Revision history.
- Add Drawing and metadata-only Revision creation flows using the TASK-0401 APIs.
- Add `zh-CN` and `en-US` translations, PageContext/PageAIContext, and automated tests.
- Update the frontend and API code maps and the phase handoff status.

## 4. Out Of Scope

- File upload, MinIO, checksum, preview, parsing, evidence overlays, and AI extraction.
- Revision state transitions, approval, release, update, delete, or obsolescence.
- SourceEvidence, Drawing Intermediate Model, quality characteristics, and inspection
  standard pages.
- Backend API or database changes unless a contract defect blocks the frontend slice.

The authenticated shell consumes backend-managed menus whenever available. A small,
idempotent QMS menu seed is therefore included as the navigation contract needed to
make the new route discoverable; it does not change the TASK-0401 business API.

## 5. Affected Files

```text
frontend/apps/pc-admin/src/modules/qms/engineering/**
frontend/apps/pc-admin/src/App.tsx
frontend/packages/domain-types/src/index.ts
frontend/packages/i18n/src/index.ts
frontend/packages/permissions/src/index.tsx
frontend/packages/mock-data/src/**
frontend/packages/table-engine/src/ConfigurableListPage.tsx
backend/iaf-qms-engineering/src/main/resources/db/migration/**
backend/iaf-qms-engineering/src/test/**
docs/code-map/frontend.md
docs/code-map/api.md
docs/operations/HANDOFF-jinheng-qms-phase-plan.md
```

## 6. API Contract

TASK-0402 consumes, but does not redefine, the TASK-0401 endpoints under
`/api/qms/parts`, `/api/qms/drawings`, and `/api/qms/drawing-revisions`.

## 7. Frontend Design

- `/qms/engineering/parts`: `ConfigurableListPage` for current-organization Parts.
- `/qms/engineering/parts/:partId`: `BusinessDetailPage`-style hierarchy view.
- Create flows use accessible modal forms with visible labels, inline validation,
  loading state, and recovery-oriented errors.
- The hierarchy is Part -> Drawing -> Revision; URLs remain deep-linkable and browser
  back navigation preserves list filters where the existing shell supports it.
- Simple mode presents business identifiers and current states; expert mode adds IDs,
  source metadata, sequence values, and timestamps without duplicating the page.

## 8. Permissions

```text
qms:part:view
qms:part:create
qms:drawing:view
qms:drawing:create
qms:drawing-revision:view
qms:drawing-revision:create
```

Frontend permission controls are UX only; TASK-0401 remains the authorization boundary.

## 9. Test Requirements

- API client path/query/body contract tests where supported by the current harness.
- Page tests for list success, empty, error, search, pagination, and permission hiding.
- Interaction tests for Part, Drawing, and Revision creation flows.
- Route/navigation smoke test and production build.
- `pnpm lint`, `pnpm typecheck`, `pnpm test`, and relevant E2E/quality checks.

## 10. Acceptance Criteria

- An authenticated permitted user can list and create Parts from the PC application.
- A Part detail route displays Drawings and Revision history and supports permitted
  metadata creation actions.
- No page directly calls `fetch`/`axios`, reads tokens, hardcodes permission checks,
  business colors, or user-visible copy.
- Loading, error, empty, disabled, and submission states are visible and accessible.
- QMS routes integrate with the existing shell and TabWorkspace conventions.
- Frontend/API code maps and the phase plan match the implementation.
- Frontend quality checks and production build pass.

## 11. Execution Note

TASK-0401 was deployed and validated on `ops/TASK-0401-qms-domain-deployment` but had not
yet entered `main` when this task started. This task branch therefore explicitly uses
that validated commit as its dependency base; its Pull Request must declare the stacked
dependency or be retargeted after TASK-0401 is merged.

## 12. Implementation Evidence

- `./scripts/check-quality.sh`: passed; all backend Maven modules, PostgreSQL migration,
  44 frontend tests, TypeScript checks, static guardrails, and production build passed.
- `node node_modules/@playwright/test/cli.js test e2e/qms-engineering.spec.ts --workers=1`:
  passed 2/2 browser tests, including the Part -> Drawing -> Revision create flow and
  mobile viewport overflow/nonblank validation.
- UI/UX review: token-only styling, localized visible copy/validation/empty state,
  keyboard-selectable Drawing rows, responsive horizontal table scrolling, labelled
  form controls, permission-specific child queries, and recoverable error states checked.
