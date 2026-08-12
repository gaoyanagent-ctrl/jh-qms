# TASK-0404 Revision State And Parse Orchestration

## Objective

Move drawing revisions through controlled state transitions and create one durable,
claimable parse job after a source file is attached. Failed jobs can be retried without
creating duplicate active work.

## In Scope

- Generic platform state-machine transition validation contract.
- `DRAFT -> UPLOADED` on file attachment through `StateMachineService`.
- Durable `qms_drawing_parse_job` queue with tenant/org isolation and idempotency.
- Latest parse-job query and failed-job retry API.
- Parse job/status visibility in revision history.
- Permissions, audit, migration, tests, code maps and module specification.

## Out Of Scope

- PDF/DWG parser execution and Intermediate Model generation (TASK-0405+).
- Manual simulation of parser success/failure from the UI.
- Review, approval and release transitions.

## Acceptance

- Upload atomically changes a draft revision to `UPLOADED` and creates one `QUEUED` job.
- Repeated queue creation cannot create two active jobs for a revision.
- Retry is accepted only for a latest `FAILED` job and creates a new attempt.
- Every transition and retry is audited.
- UI shows the latest parse job state and a permission/state-aware retry action.
