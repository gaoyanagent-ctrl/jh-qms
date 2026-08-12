# Jinheng QMS Phase-One Delivery Plan

## Objective

Implement the design as independently reviewable vertical slices in the standalone JH QMS repository while preserving
the chain from source engineering data to controlled quality documents.

## Framework Baseline Assessment

Imported from the IAF foundation baseline:

- Spring Boot modular monolith, React workspace, PostgreSQL/Flyway, unified API results.
- Authentication, tenant/current-organization context, RBAC permission enforcement.
- Platform menu/theme/i18n shell, Outbox baseline, local Docker Compose, quality scripts.

Required before dependent QMS flows:

- Production file/object-storage application contract.
- Executable state-machine, approval, and rule-engine application services.
- QMS frontend routes and API hooks.
- Independent Python AI service and CAD provider adapter.

## Delivery Sequence

| Task | Vertical outcome | Dependencies | Status |
|---|---|---|---|
| TASK-0401 | Part/ Drawing/ Revision metadata API, migration, permission, audit | IAF auth/org | Implemented |
| TASK-0402 | Part and Drawing management pages with Revision history | TASK-0401 | Implemented |
| TASK-0403 | File service contract, MinIO adapter, upload/checksum/deduplication | TASK-0401 | Planned |
| TASK-0404 | Revision state machine and upload-to-parse job orchestration | TASK-0403, platform state machine | Planned |
| TASK-0405 | SourceEvidence + Drawing Intermediate Model + evidence API | TASK-0404 | Planned |
| TASK-0406 | PDF preview, overlay, and Review Workbench shell | TASK-0402, TASK-0405 | Planned |
| TASK-0407 | Python AI service skeleton and PDF parser golden-sample pipeline | TASK-0405 | Planned |
| TASK-0408 | CAD parser SPI and DWG mock/provider contract | TASK-0405 | Planned |
| TASK-0409 | Quality Characteristic review/confirm/reject closed loop | TASK-0405, TASK-0407 | Planned |
| TASK-0410 | Inspection Standard draft generation and editable document | TASK-0409, platform rules | Planned |
| TASK-0411 | Coverage/consistency checker and release gate | TASK-0410 | Planned |
| TASK-0412 | Approval/release integration for controlled documents | TASK-0411, platform workflow/state | Planned |
| TASK-0413 | Validation Plan generation and controlled release | TASK-0412 | Planned |
| TASK-0414 | Gauge acceptance and validity enforcement | TASK-0410 | Planned |
| TASK-0415 | Revision diff, lineage, and change-impact workbench | TASK-0409, TASK-0413 | Planned |
| TASK-0416 | Knowledge ingestion and read-only Copilot | TASK-0415 | Planned |

## Stage Gates

Every task must use an independent JH QMS task branch and ship migration/domain/API/tests/code-map together. Changes must pass the repository quality
gate. AI outputs remain Draft, every extracted field must have evidence, and no controlled
object can be released without human review and approval.

The IAF repository is read-only upstream reference material. Do not create JH QMS branches,
commits, migrations, or pull requests in IAF.

Before TASK-0407, add the three field-level specifications requested by the source design:

1. Drawing Intermediate Model JSON Schema and parser rules.
2. Inspection Standard mapping matrix and Rule DSL.
3. AI tool contracts, prompt registry, and evaluation/golden-set protocol.
