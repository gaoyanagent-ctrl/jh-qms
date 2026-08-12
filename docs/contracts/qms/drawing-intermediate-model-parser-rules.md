# Drawing Intermediate Model Parser Rules 1.0.0

- Coordinates use the source sheet coordinate system. `bbox` is `(x, y, width, height)`;
  width and height cannot be negative. The viewer adapter owns coordinate conversion.
- `sheetNo` and `entityId` are stable within one revision. DWG handles are retained in
  `sourceEntityHandle`; PDF parsers generate deterministic entity IDs.
- Text is retained verbatim in `rawText`; normalization is additive and never overwrites
  source text.
- Every automatically derived publishable field must carry at least one stable `evidenceKey` reference.
  Evidence stores source file, sheet/page, bounding box, extractor identity, and confidence.
- Confidence is a decimal from 0 through 1. It indicates extraction confidence, not approval.
- Parser output is immutable after successful completion. A retry creates a new parse attempt;
  only the first successful result for a revision is accepted in phase one.
- PDF and DWG adapters must both emit schema version `1.0.0`. Unsupported schema versions are
  rejected before any result rows or state changes are committed.
