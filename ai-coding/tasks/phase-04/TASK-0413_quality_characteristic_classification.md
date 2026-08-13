# TASK-0413 Quality Characteristic Classification And Bulk Review

## Objective

Complete the reviewed quality-characteristic data required by Inspection Standard generation,
including inspection semantics, manual creation, and safe bulk review actions.

## Required Reading

- `AGENTS.md`
- `docs/product/锦恒QMS系统总体架构与一期详细设计说明书_Codex开发版.md`, sections 9 and 11
- `docs/module-specs/qms/01_engineering_data_spec.md`
- `docs/contracts/qms/inspection-standard-mapping.md`
- `docs/quality/quality_gate.md`

## In Scope

- Add inspection/reference/ideal/fit/location/regulatory/mandatory flags to quality characteristics.
- Allow reviewers to edit classification, special-characteristic code, and inspection flags.
- Allow a reviewer to create a manual characteristic linked to a drawing revision.
- Bulk confirm or reject selected pending characteristics with optimistic-lock conflict protection.
- Add type/review filters and accessible selection actions to the Drawing Review Workbench.
- Preserve audit entries, tenant/org boundaries, and source evidence for parsed candidates.

## Out Of Scope

- Candidate merge/split and geometric duplicate resolution.
- Inspection Standard persistence or generation.
- Approval/release workflow.
- PDF inference improvements.

## Database And API

- Add a new Flyway migration; never change `V0406`.
- Extend `/api/qms/drawing-revisions/{revisionId}/characteristics` responses.
- Add manual-create and bulk-review commands under the same revision resource.
- Reuse `qms:quality-characteristic:review` for all writes.

## Acceptance

- Existing characteristics migrate with deterministic defaults.
- Single review and manual creation validate incompatible flag combinations.
- Bulk actions update only pending rows and fail atomically on stale versions.
- Every write is audit logged and tenant/org scoped.
- The workbench supports selection, filtering, manual creation, and bulk review without hiding the drawing.
- Backend, frontend, migration, and production smoke tests pass; code maps are updated.
