# TASK-0405 Source Evidence And Drawing Intermediate Model

## Objective

Provide the durable, tenant-safe contract through which PDF and DWG parsers publish one
normalized Drawing Intermediate Model (DIM), queryable entities, and source evidence.

## In Scope

- Versioned DIM JSON Schema and parser-result validation.
- Drawing Intermediate Model, DrawingEntity, and SourceEvidence persistence.
- Parse lifecycle application port: start, complete, and fail.
- Controlled revision transitions `UPLOADED -> PARSING -> PARSED/FAILED`.
- Revision-scoped model/entity/evidence query APIs.
- Transactional audit, migration, tests, code maps, and module specification.

## Out Of Scope

- PDF/DWG parser algorithms and worker polling (TASK-0407/TASK-0408).
- PDF preview and evidence overlay UI (TASK-0406).
- Characteristic extraction/review (TASK-0409).

## Acceptance

- A queued job can start once and moves its revision to `PARSING`.
- A running job can atomically persist a schema-valid DIM, entities, and evidence and move
  its revision to `PARSED`.
- A running job can fail with a bounded error and move its revision to `FAILED`.
- Model, entity, and evidence queries cannot cross tenant, organization, or revision scope.
- Every automatically derived entity can reference one or more locatable evidence records.
