# TASK-0410 LibreDWG Provider

## Objective

Provide a production DWG parser for the existing CAD parser SPI and create traceable
quality-characteristic candidates from native DWG dimension entities.

## Acceptance

- The AI service validates DWG input and invokes GNU LibreDWG `dwgread` in an isolated container.
- Native handles, entity types, layer names, geometry, text and bounding boxes are preserved.
- Dimensions, text, leaders and tolerance annotations are stored in the shared intermediate model.
- Every stored entity has source evidence identified as `DWG_ENTITY` and parser version `libredwg-0.14`.
- Native dimension measurements create `PENDING` quality-characteristic candidates even when no textual tolerance exists.
- Existing PDF parsing and the unavailable CAD fallback remain selectable through configuration.
- Parser and PostgreSQL integration regression tests pass.

## Deferred

- Browser rendering of full DWG geometry, associative dimension reconstruction, GD&T semantic
  interpretation, and PDF extraction improvements.

