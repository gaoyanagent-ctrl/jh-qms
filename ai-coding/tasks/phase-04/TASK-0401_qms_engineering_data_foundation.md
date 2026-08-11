# TASK-0401 QMS Engineering Data Foundation

## 1. Task Objective

Deliver the first runnable Jinheng QMS vertical slice on IAF: tenant- and
organization-scoped Part, Drawing, and DrawingRevision metadata registration and query,
with permissions, audit, Flyway migration, OpenAPI annotations, and automated tests.

## 2. Required Reading

- `AGENTS.md`
- `docs/decisions/0009-qms-engineering-module-boundary.md`
- `docs/module-specs/qms/01_engineering_data_spec.md`
- `docs/code-map/README.md`
- `docs/code-map/backend.md`
- `docs/code-map/api.md`
- `docs/code-map/database.md`
- `ai-coding/rules/01_project_rules.md`
- `ai-coding/rules/02_backend_rules.md`
- `ai-coding/rules/04_database_rules.md`
- `ai-coding/rules/05_api_rules.md`
- `ai-coding/rules/06_permission_rules.md`
- `ai-coding/rules/10_testing_rules.md`
- `ai-coding/rules/12_code_map_rules.md`
- `docs/quality/quality_gate.md`

## 3. In Scope

- Add and assemble `iaf-qms-engineering`.
- Create `qms_part`, `qms_drawing`, `qms_drawing_revision`, and `qms_audit_log`.
- Create/list/detail Part APIs.
- Create/list/detail Drawing APIs under a Part.
- Create/list/detail DrawingRevision metadata APIs under a Drawing.
- Enforce natural-key duplicate prevention, tenant isolation, current-org scope, and
  parent ownership.
- Seed QMS permissions for the default admin role.
- Add domain/application/controller/repository/migration tests.
- Update backend/API/database code maps.

## 4. Out Of Scope

- File upload, MinIO, checksum, parser, Viewer, SourceEvidence, AI service.
- Revision transitions beyond the initial `DRAFT` state.
- Frontend pages (scheduled in TASK-0402 after API contract validation).
- Update/delete/release operations.

## 5. Database Design

- `qms_part`: current-org master with active-row unique `(tenant_id, org_id, part_no)`.
- `qms_drawing`: child of Part with active-row unique `(tenant_id, part_id, drawing_no)`.
- `qms_drawing_revision`: child of Drawing with unique revision code and revision sequence.
- `qms_audit_log`: immutable module audit trail.
- All tables include tenant, audit, soft-delete, optimistic-lock, and `ext_json` fields.

## 6. API List

| Method | Path | Permission |
|---|---|---|
| GET | `/api/qms/parts` | `qms:part:view` |
| GET | `/api/qms/parts/{id}` | `qms:part:view` |
| POST | `/api/qms/parts` | `qms:part:create` |
| GET | `/api/qms/parts/{partId}/drawings` | `qms:drawing:view` |
| POST | `/api/qms/parts/{partId}/drawings` | `qms:drawing:create` |
| GET | `/api/qms/drawings/{id}` | `qms:drawing:view` |
| GET | `/api/qms/drawings/{drawingId}/revisions` | `qms:drawing-revision:view` |
| POST | `/api/qms/drawings/{drawingId}/revisions` | `qms:drawing-revision:create` |
| GET | `/api/qms/drawing-revisions/{id}` | `qms:drawing-revision:view` |

## 7. Test Requirements

- Domain validation and initial-state tests.
- Application tests for creation, duplicates, parent lookup, org scope, tenant scope,
  revision sequence, and audit calls.
- Controller tests for unified envelopes and validation.
- PostgreSQL Testcontainers migration/repository integration test.

## 8. Acceptance Criteria

- New module compiles inside the reactor and is assembled by `iaf-app`.
- All endpoints use unified results, OpenAPI descriptions, backend permissions, and
  authenticated tenant/current-org context.
- Migration succeeds on an empty PostgreSQL database.
- Relevant tests and repository quality gate pass.
- Backend, API, and database code maps match implementation.

