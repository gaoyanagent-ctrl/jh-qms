# TASK-0412 DWG SVG Viewer

## Objective

Render parsed DWG revisions in the review workbench so reviewers can visually verify
quality-characteristic candidates against their native drawing positions.

## Acceptance

- LibreDWG converts model space to DXF and ezdxf produces a sanitized SVG during the existing parse job.
- Native dimensions, hatches, text and block geometry are rendered from DXF; no synthetic dimension graphics are added.
- The shared intermediate model records the preview content, viewBox and CAD coordinate system.
- The browser supports zoom, wheel zoom, pointer panning, reset and selected-evidence location.
- Selected evidence is transformed by the renderer's world-to-SVG matrix and highlighted.
- Existing PDF preview behavior remains unchanged.
- Parser tests, component tests, type checks and production builds pass.

## Known boundaries

- Proprietary proxy entities and unavailable CAD fonts can still differ from AutoCAD. A licensed
  ODA/AutoDWG provider remains the required path if pixel-identical AutoCAD output is mandated.
