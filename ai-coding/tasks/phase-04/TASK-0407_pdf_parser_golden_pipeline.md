# TASK-0407 PDF Parser Golden-Sample Pipeline

## Objective

Establish the independent Python AI service and a deterministic vector-PDF parser that emits
the versioned DIM and locatable SourceEvidence contract from TASK-0405.

## Acceptance

- The service has a container health endpoint and no host/public port.
- A PDF parse request returns sheet geometry, normalized text entities, and page/BBox evidence.
- Entity evidence references are complete and deterministic for identical input.
- Invalid PDF content fails with a bounded client error.
- Golden-sample tests verify text, page, coordinates, and evidence linkage.

## Deferred

- Scanned PDF OCR/VLM fallback and dimension semantics.
- Spring parse-job dispatcher integration.
- DWG provider adapter (TASK-0408).
