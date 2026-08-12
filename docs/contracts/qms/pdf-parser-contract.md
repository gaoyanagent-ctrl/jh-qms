# PDF Parser Contract

The internal parser accepts one source PDF plus `document_id` and `revision`. It emits DIM
schema `1.0.0`, a flat persistence-oriented entity list, and SourceEvidence records.

- Coordinates use PDF points in a top-left coordinate system and are bounded by the sheet's
  `width` and `height`.
- Page and `sheetNo` are one-based strings/numbers.
- Vector text blocks become `TEXT` entities; whitespace is collapsed only in
  `normalizedText`, while `rawText` retains source line breaks.
- Every entity has exactly one parser evidence reference in this baseline.
- Identifiers derive from the source checksum, page, and block ordinal, making identical
  inputs deterministic.
- `PDF_VECTOR` confidence is `1.0` because it represents source extraction confidence, not
  engineering-semantic correctness.
- Invalid structures return HTTP 422. OCR fallback is deferred and must never invent source
  coordinates.
