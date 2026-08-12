# QMS Engineering Data Module Specification

## 1. Purpose

`iaf-qms-engineering` owns the controlled engineering-data hierarchy used by the QMS:

```text
Part -> Drawing -> DrawingRevision
```

This hierarchy is the upstream source for evidence, quality characteristics, inspection
standards, validation plans, and revision impact analysis.

## 2. Tenant And Organization Boundary

- Every query is tenant-qualified.
- Part ownership is scoped to the authenticated user's current organization.
- `part_no + org_id` is unique among active rows in a tenant.
- Child records inherit tenant and organization ownership from their parent; clients
  cannot supply a different tenant or organization id.

## 3. Aggregates

### Part

Required fields: `partNo`, `partName`. Optional business references include
`materialNo`, `customerId`, `vehicleModel`, `supplierId`, and `importanceLevel`.

New Parts start in `ACTIVE`. Physical deletion is not supported.

### Drawing

A Drawing belongs to one Part. `drawingNo` is unique within a Part. New Drawings start
in `ACTIVE`.

### DrawingRevision

A revision belongs to one Drawing. `revisionCode` is unique within a Drawing and
`revisionSeq` increases monotonically. A metadata-only revision starts in `DRAFT`, with
`parseStatus=PENDING` and `reviewStatus=PENDING`.

TASK-0403 attaches one controlled PDF/DWG source file to a revision through the
`QmsObjectStorage` port. Metadata is stored in `qms_file_object`; content is stored in
MinIO under an opaque tenant/revision key. TASK-0404 validates every transition through
`StateMachineService`: upload moves `DRAFT -> UPLOADED` and atomically enqueues parse
attempt 1; retry moves a failed revision `FAILED -> UPLOADED` and enqueues the next attempt.

TASK-0405 defines the parser-facing result contract. Starting a queued attempt moves the
revision `UPLOADED -> PARSING`; a schema-valid result atomically persists one versioned
Drawing Intermediate Model, normalized DrawingEntities, and locatable SourceEvidence before
moving `PARSING -> PARSED`. Failure moves `PARSING -> FAILED` and records a bounded diagnostic.
Every normalized entity must have evidence with source file, sheet/page, bounding box,
extractor version, and confidence. Parser algorithms and worker polling remain external to
this module contract.

TASK-0406 consumes that contract in the Review Workbench. PDF source content is fetched as
an authenticated Blob and rendered with PDF.js; selecting evidence changes to its source page
and scales the DIM bounding box onto the rendered Canvas. DWG files deliberately show the
deferred CAD-adapter state instead of attempting an inaccurate browser preview.

TASK-0409 consumes SourceEvidence independently of its PDF or CAD origin. Explicit linear
dimensions become pending `qms_quality_characteristic` records. A human must confirm or reject
each candidate; edits and decisions preserve evidence linkage and are audit recorded.

## 4. Commands And Idempotency

Create commands are protected by tenant-scoped natural keys and database unique
constraints. Repeating a command with the same business key returns a stable conflict
error and never creates a second record. File attachment is guarded by optimistic
locking and a single-file constraint. A durable `Idempotency-Key` command ledger remains
deferred to a later reliability slice.

## 5. Audit

Creating a Part, Drawing, or DrawingRevision writes a `qms_audit_log` entry in the same
transaction. The entry records actor, action, object type/id, JSON after-image, source,
trace id, and UTC timestamp.
Parse start, completion, failure, and result persistence write transition/result audit entries
in the same transaction as their state and data changes.

## 6. Permissions

```text
qms:part:view
qms:part:create
qms:drawing:view
qms:drawing:create
qms:drawing-revision:view
qms:drawing-revision:create
qms:drawing-revision:upload
qms:drawing-revision:retry-parse
qms:quality-characteristic:review
```

Read and write permissions are enforced in the application layer. Frontend visibility
is not a security boundary.

## 7. Deferred Scope

- Native DWG preview.
- Parse execution workers and PDF/DWG adapters.
- Updates, obsolescence, release, and physical deletion.
