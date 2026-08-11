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

This first slice does not attach files or change revision status. File attachment and
all later status changes must use the platform file and state-machine capabilities once
those contracts are implemented.

## 4. Commands And Idempotency

Create commands are protected by tenant-scoped natural keys and database unique
constraints. Repeating a command with the same business key returns a stable conflict
error and never creates a second record. A durable `Idempotency-Key` command ledger is
deferred to the file-upload slice, where transport retries become part of the flow.

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
```

Read and write permissions are enforced in the application layer. Frontend visibility
is not a security boundary.

## 7. Deferred Scope

- Object-storage upload, checksum calculation, and duplicate-file handling.
- Revision state transitions and transition audit.
- PDF/DWG preview.
- Parse jobs, SourceEvidence, and Drawing Intermediate Model.
- Updates, obsolescence, release, and physical deletion.

