# TASK-0416 Inspection Standard Approval And Release

## Objective

Turn an editable Inspection Standard draft into a controlled document through explicit submit,
human approval/rejection, and release actions without exposing workflow-engine internals.

## Acceptance

- Document status and approval status remain separate.
- Only fully reviewed items can be submitted.
- The submitter cannot approve their own request.
- Rejected documents become editable and may be resubmitted.
- Only approved documents can be released; released documents are read-only.
- Every action is tenant/org scoped, permission checked, state-machine validated, audited, and visible in history.
- Database migration, API/client types, bilingual UI, tests, and code maps ship together.

## Status

Implemented on `feature/TASK-0416-inspection-standard-approval`.
