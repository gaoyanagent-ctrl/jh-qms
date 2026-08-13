# TASK-0412 DWG SVG Viewer

## Objective

Render parsed DWG revisions in the review workbench so reviewers can visually verify
quality-characteristic candidates against their native drawing positions.

## Acceptance

- LibreDWG produces a sanitized model-space SVG during the existing DWG parse job.
- Native dimension measurements omitted by `dwg2SVG` are added as a dedicated SVG overlay.
- The shared intermediate model records the preview content, viewBox and CAD coordinate system.
- The browser supports zoom, wheel zoom, pointer panning, reset and selected-evidence location.
- Selected evidence is transformed from CAD Y-up coordinates into the SVG viewBox and highlighted.
- Existing PDF preview behavior remains unchanged.
- Parser tests, component tests, type checks and production builds pass.

## Known boundaries

- LibreDWG does not render every HATCH, REGION or MTEXT construct. The viewer prioritizes
  complete model geometry plus native dimension verification; unsupported annotations remain
  available in the evidence list.
