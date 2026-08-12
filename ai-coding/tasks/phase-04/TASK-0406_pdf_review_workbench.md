# TASK-0406 PDF Review Workbench

## Objective

Provide an authenticated drawing-review surface that displays a revision source file beside
its parser evidence and lets a reviewer locate evidence on the corresponding PDF page.

## In Scope

- Revision-history entry point and permission-protected review route.
- Authenticated binary file download through the shared API client.
- PDF.js Canvas rendering with page navigation.
- Evidence confidence filters and evidence-to-page/BBox highlighting.
- Explicit deferred-adapter state for DWG files.
- Loading, empty, unavailable-model, and render-error states with Chinese/English copy.

## Out Of Scope

- PDF/DWG parsing algorithms and worker polling (TASK-0407/TASK-0408).
- Native CAD rendering.
- Characteristic correction, approval, and release workflow (TASK-0409+).

## Acceptance

- A user with revision-view permission can open an attached revision from Part details.
- PDF content is loaded with the current authenticated session and rendered without exposing
  the access token to application code.
- Selecting evidence navigates to its page and draws its normalized bounding box.
- Evidence can be filtered by high, medium, or low confidence.
- Missing parser output and DWG preview capability are communicated explicitly.
