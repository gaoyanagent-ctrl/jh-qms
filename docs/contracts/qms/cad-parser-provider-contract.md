# CAD Parser Provider Contract

A provider receives controlled DWG bytes, original name, document id, and revision. It must
return DIM `1.0.0` plus persistence entity/evidence lists.

- Preserve native CAD handles in `sourceEntityHandle` and `entityHandle`.
- Preserve entity type, layer, raw text, geometry, and sheet-space BBox whenever available.
- Use `DWG_ENTITY` evidence and record provider/extractor version.
- Coordinates must share one declared sheet coordinate system and fit sheet dimensions.
- Do not use OCR as the primary DWG path and do not invent evidence for unsupported entities.
- Provider absence is a durable `CAD_PROVIDER_UNAVAILABLE` failure, eligible for retry after
  configuration changes.
