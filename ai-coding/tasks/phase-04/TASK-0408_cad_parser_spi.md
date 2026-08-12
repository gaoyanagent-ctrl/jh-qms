# TASK-0408 CAD Parser SPI And Provider Contract

## Objective

Keep DWG parsing independent from QMS business code while requiring all CAD providers to
emit the same DIM 1.0 and SourceEvidence contract as the PDF pipeline.

## Acceptance

- `CadParserPort` is the only dispatcher dependency for DWG parsing.
- Provider output preserves native entity handles, layers, entity types, geometry/BBoxes,
  raw text, and `DWG_ENTITY` evidence metadata.
- A contract-test Mock Provider emits deterministic DIM/evidence from a valid DWG signature.
- With no licensed production provider configured, queued DWG work fails explicitly with
  `CAD_PROVIDER_UNAVAILABLE` and never fabricates evidence.
- OCR is not used as the DWG primary path.

## Deferred

- Selection and licensing of an ODA/AutoDWG-compatible production adapter.
- Native browser CAD preview and raster/vector preview artifact generation.
