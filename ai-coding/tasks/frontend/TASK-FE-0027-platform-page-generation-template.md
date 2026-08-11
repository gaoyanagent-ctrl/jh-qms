# TASK-FE-0027 Platform Page Generation Template

## Objective

Use this template when adding a new platform management page. The goal is consistent IAF platform behavior across list, tree/list, form drawer, detail drawer, permission assignment, menu tree editor, and audit list pages.

## Required Files

For a new page under `frontend/apps/pc-admin/src/modules/platform/<module>/` create or update:

```text
api.ts
hooks.ts
types.ts
<PageName>.tsx
<PageName>.test.tsx
```

Shared logic must go to `frontend/packages/*`, not the app page.

## Required Page Structure

- Use `AppPageContainer` for the page shell.
- Use `ConfigurableListPage` for standard lists.
- Use `FormInteractionSurface` for create/edit surfaces.
- Use `StatusTag` or another `@iaf/ui-core` status component for status fields.
- Use `PermissionRoute` at route level.
- Use `PermissionButton` or `PermissionGate` for actions.
- Use `FieldPermissionWrapper` for field visibility, readonly, hidden, or masked behavior.

## API Rules

- All HTTP calls go through `packages/api-client`.
- Page components call hooks, not URLs.
- Hooks use TanStack Query for server state.
- No page may read/write auth tokens or call `localStorage` for token handling.

## i18n and Theme Rules

- User-visible text must use i18n keys.
- Colors, status colors, spacing, radius, and elevation must come from Ant Design tokens or `@iaf/theme`.
- Do not hardcode Chinese copy, English business copy, raw hex colors, or permission checks in pages.

## Minimum Tests

Each new page must cover:

- Route or action permission behavior.
- Loading or empty state.
- Primary create/edit or view flow.
- Form validation for required fields.
- API hook wiring or mock response wiring.

## Code Map Updates

Update:

- `docs/code-map/frontend.md` for new routes, pages, hooks, API clients, or reusable components.
- `docs/code-map/api.md` for new backend APIs.
- `docs/code-map/database.md` for new migrations.

## Checklist

```text
[ ] api.ts uses packages/api-client
[ ] hooks.ts uses TanStack Query
[ ] types.ts owns page DTO/view types
[ ] route is protected by PermissionRoute
[ ] actions use PermissionButton or PermissionGate
[ ] fields use FieldPermissionWrapper where field permission applies
[ ] standard list uses table-engine
[ ] create/edit uses FormInteractionSurface
[ ] status uses StatusTag or domain status component
[ ] loading/error/empty states are visible
[ ] i18n keys exist in zh-CN and en-US
[ ] no raw fetch/axios
[ ] no raw localStorage token access
[ ] tests added or updated
[ ] code map updated
```
