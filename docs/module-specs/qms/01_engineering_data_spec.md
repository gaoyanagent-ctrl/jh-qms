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
The parser worker and later `PARSING/PARSED` transitions remain deferred.

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
```

Read and write permissions are enforced in the application layer. Frontend visibility
is not a security boundary.

## 7. Deferred Scope

- PDF/DWG preview.
- Parse execution workers, SourceEvidence, and Drawing Intermediate Model.
- Updates, obsolescence, release, and physical deletion.
