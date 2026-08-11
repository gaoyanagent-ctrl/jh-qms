# Platform Foundation Troubleshooting

## Login Fails

Check:

- The login request includes `tenantCode`, `username`, and `password`.
- The tenant exists and is enabled.
- The user exists in the resolved tenant and is enabled.
- The local mock login does not use a hardcoded default tenant; provide a tenant code explicitly.

Relevant files:

- `frontend/apps/pc-admin/src/pages/LoginPage.tsx`
- `frontend/packages/mock-data/src/platform/auth.ts`
- `backend/iaf-platform-auth/src/main/java/com/company/iaf/platform/auth/application/AuthApplicationService.java`

## Menu Does Not Display

Check:

- The role has the menu code assigned.
- The menu is visible and enabled.
- The menu has required permissions and the user owns at least one of them.
- The route is registered in `frontend/apps/pc-admin/src/App.tsx`.

Relevant files:

- `ai-coding/templates/platform-foundation/platform-foundation-delivery-package.json`
- `frontend/packages/mock-data/src/platform/menus.ts`
- `backend/iaf-platform-permission/src/main/java/com/company/iaf/platform/permission/application/AuthMenuApplicationService.java`

## Button Does Not Display

Check:

- The page uses `PermissionButton` or `PermissionGate`.
- The action permission is listed in `PLATFORM_PERMISSIONS`.
- The current principal owns the permission.
- The business state does not disable the action.

Relevant file:

- `frontend/packages/permissions/src/index.tsx`

## API Returns 403

Check:

- Backend application service has the expected `@RequiresPermission`.
- The token principal contains the required permission code.
- Tenant context was loaded from the bearer token.
- The tenant is enabled for write operations.

Relevant files:

- `backend/iaf-platform-core/src/main/java/com/company/iaf/platform/core/security/RequiresPermission.java`
- `backend/iaf-platform-core/src/main/java/com/company/iaf/platform/core/security/RequiresPermissionAspect.java`

## Data Permission Filters Return Empty Lists

Check:

- The current user has a current organization context.
- The target users or records are assigned to the current organization scope.
- The caller has the API permission and the data permission predicate is not filtering everything.

Relevant API:

- `GET /api/platform/users`

## i18n Key Missing

Check:

- The key exists in `frontend/packages/i18n/src/index.ts`.
- Backend remote i18n resources do not override the key with an empty value.
- Menu title keys match the route/menu template.

## Theme Does Not Apply

Check:

- The app is wrapped by `IafThemeProvider`.
- Theme preference is saved through `/api/platform/preferences/me`.
- Page colors use Ant Design tokens or `@iaf/theme` design tokens.
- The page does not use raw hex colors or local status color logic.

## Platform Foundation Template Check Fails

Run:

```bash
node scripts/check-platform-foundation-templates.js
```

Common causes:

- A new permission was added to `PLATFORM_PERMISSIONS` but not to the delivery package.
- A new backend `platform:*` permission was seeded by Flyway but not added to the delivery package.
- A role references a permission code not present in the package.
- A permission recommends a role template that does not grant that permission.
- A production-backed route menu has no permission guard.
- A menu references a missing parent menu.
