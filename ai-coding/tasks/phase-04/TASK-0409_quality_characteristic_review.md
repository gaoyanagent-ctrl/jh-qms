# TASK-0409 Quality Characteristic Review

## Objective

Convert explicit linear-dimension evidence from the shared DIM pipeline into traceable
quality-characteristic candidates and provide a human confirmation/rejection loop.

## Acceptance

- PDF and future CAD providers feed the same candidate generator through SourceEvidence.
- Only explicit `nominal ± tolerance` or `nominal +/- tolerance` text creates candidates.
- Candidates start `PENDING`; no parser output is automatically confirmed.
- Reviewers can edit name, nominal value, upper/lower tolerance and unit before confirmation.
- Confirmation and rejection use optimistic locking, retain source evidence, and write audit entries.
- Writes require `qms:quality-characteristic:review`; reads reuse revision-view permission.
- The Review Workbench locates a candidate's evidence on the drawing.

## Deferred

- Candidate merge/split, bulk review, GD&T parsing, OCR inference, and coverage statistics.
