# Frontend Code Map

## Current State

The frontend workspace has been initialized as a pnpm monorepo.

Frontend executable rules and specifications now live in:

- `ai-coding/rules/03_frontend_rules.md`
- `docs/frontend/`
- `ai-coding/tasks/frontend/`

`docs/iaf_frontend_specs/` keeps the original frontend specification package.

Current workspace:

```text
frontend/
  package.json
  pnpm-workspace.yaml
  tsconfig.base.json
  apps/
    pc-admin/
  packages/
    api-client/
    auth/
    permissions/
    i18n/
    theme/
    domain-types/
    ui-core/
    ui-business/
    mock-data/
    table-engine/
    form-engine/
```

Legacy placeholder folders still exist under `frontend/src/**` from the earlier repository scaffold. They are not part of the active executable frontend workspace.

## Applications

### `frontend/apps/pc-admin`

- Owner module: Frontend PC admin app.
- Framework: Vite + React + TypeScript.
- UI: Ant Design.
- Purpose: PC administration shell and platform management pages.
- Important files:
  - `src/main.tsx`: initializes i18n, optionally loads the remote brand configuration seam, then mounts the theme provider, Ant Design app provider, global CSS, and React root.
  - `src/config/brandConfig.ts`: current PC admin brand entrypoint. `VITE_IAF_LOGIN_TEMPLATE` selects one of the five login templates for `/login`; `VITE_IAF_BRAND_CONFIG_API=true` enables loading `/api/platform/brand/current` for logo, favicon, login background, and login template overrides.
  - `src/global.css`: app-level CSS overrides that cannot be expressed through Ant Design tokens. Currently stabilizes opened right-side Drawer wrapper positioning, removes browser default body margin, scopes login autofill/dark-form fixes, provides login-template mobile responsiveness, and defines scoped TASK-0217 visual baseline constraints for page containers, page headers, surfaces, drawer/modal form surfaces, content min-width, mobile drawer fallback, and modal body scrolling.
  - `src/App.tsx`: React Query provider and route tree. Platform user/org/role/menu routes are wrapped with `PermissionRoute` so direct URL access is blocked when the current principal lacks the page read permission.
  - `src/routes/ProtectedRoute.tsx`: redirects unauthenticated users to `/login` and loads current user through auth API.
  - `src/layouts/MainLayout.tsx`: authenticated platform shell with backend-driven navigation from `/api/platform/auth/menus`, permission-filtered fallback navigation, menu search, empty search state, industrial sidebar with light/dark sidebar mode, explicit sidebar menu color tokens, fixed-height viewport shell, preference-driven collapsible/resizable sidebar, independently scrolling menu region, fixed bottom sidebar profile entry, 44px topbar icon actions, command palette, notification drawer, language toggle, simple/expert workspace mode toggle, user profile dropdown, personal preference drawer, compact topbar theme toggle, and logout. After login it loads `/api/platform/preferences/me` and uses local persisted preferences as fallback. Direct shell sidebar collapse and resize interactions also save through `/api/platform/preferences/me` first, falling back to local storage on failure.
  - `src/pages/LoginPage.tsx`: login logic and configurable template selector driven by `IafBrandConfig.loginTemplate`; successful login and existing-token redirects target the last active workspace tab when available. Login submits `tenantCode`, `username`, and `password`.
  - `src/pages/loginTemplates.tsx`: five token-driven login templates converted from `docs/iaf-login-designs.zip`: standard industrial, cyber AI, immersive glass, minimal technical, and bento dashboard. Standard and bento templates use optimized local WebP background assets; dark concept templates read `designTokens.loginTemplates` palette mappings to preserve the reference designs independently of the active app theme. The shared form includes a required tenant-code field and does not prefill a default tenant.
  - `src/assets/login-standard-industrial.webp` and `src/assets/login-bento-industrial.webp`: optimized local login background assets generated from the retained JPG source files.
  - `src/workspace/`: Tab workspace layout container (`TabWorkspace.tsx`), Zustand-based persistent store (`RouteTabStore.ts`), and dirty forms watcher (`DirtyStateRegistry.ts`). Route tabs update labels when known route-title mappings are added.
  - `src/api/client.ts`: app-level API client instance.
    - Uses real HTTP by default.
    - When `VITE_IAF_MOCK_API=true`, synchronously registers `@iaf/mock-data` handlers before the API client is used.
  - `src/modules/platform/`: modularized platform pages. Users, orgs, roles, and system configuration API clients contain code-split files. Kanban, approval, and config pages currently use mock-first in-page data until their backend APIs are implemented.
  - `src/modules/qms/engineering/`: TASK-0402 production-backed Part list/detail, Drawing hierarchy, metadata-only Revision history, typed API clients, TanStack Query hooks, and permission-filtered page/AI context.
  - `vite.config.ts`: development server config. `/api` is proxied to `VITE_IAF_API_PROXY_TARGET`, defaulting to `http://localhost:8080`.
  - `../../vitest.config.ts`: frontend-root Vitest config covering `apps/**/*.test.*` and `packages/**/*.test.*`.
- Local access:
  - Default Vite port: `5173`.
  - To use the current IAF backend when local port `8080` is occupied, start with `VITE_IAF_API_PROXY_TARGET=http://127.0.0.1:18080`.
  - To use frontend-only Mock mode, start with `VITE_IAF_MOCK_API=true`.
- Tests:
  - `src/routes/ProtectedRoute.test.tsx`: unauthenticated redirect.
  - `src/layouts/MainLayout.test.tsx`: menu permission hiding, preference reset persistence, preference drawer sidebar save behavior, and direct shell sidebar collapse/resize backend persistence.
  - `src/workspace/RouteTabStore.test.ts`: workspace tab state unit tests.
  - `src/modules/platform/users/UserListPage.test.tsx`: user page create action permission visibility and user page dependency wiring.
  - `src/modules/qms/engineering/*.test.tsx`: QMS list/error/permission and Part/Drawing/Revision creation/hierarchy coverage; `api.test.ts` pins TASK-0401 request paths and bodies.

## Quality Scripts

- `scripts/run-frontend-checks.sh`: repository-level frontend quality gate.
  - `typecheck`: runs TypeScript checks for all active frontend packages/apps.
  - `lint`: currently aliases TypeScript checks because no ESLint config has been introduced.
  - `test`: runs frontend-root Vitest through `frontend/vitest.config.ts`.
  - `build`: builds `apps/pc-admin`.
  - Static guardrails run before dependency checks and fail when app/package code bypasses `packages/api-client` with direct HTTP calls outside the API client package, or when app code directly reads/writes auth tokens through local storage.
- `frontend/package.json` delegates `typecheck`, `lint`, `test`, and `build` to `scripts/run-frontend-checks.sh`.
- `frontend/package.json` includes `@playwright/test` as a dev dependency for local UI inspection and screenshot-driven checks.
- `frontend/package.json` exposes `pnpm e2e`, backed by `frontend/playwright.config.ts`, for mock-first PC admin shell smoke/visual checks.
- `frontend/e2e/platform-shell.spec.ts`: Playwright coverage for login, platform page navigation, sidebar profile viewport stability, screenshot nonblank checks, and light/dark shell behavior.
- `frontend/e2e/login-templates.spec.ts`: Playwright coverage for the five login templates across desktop and mobile viewports, including no-horizontal-overflow checks and mock-auth submit coverage.
- `frontend/e2e/platform-pages-visual.spec.ts`: TASK-0217 Playwright visual baseline coverage for the current login page, workbench, users, orgs, roles, menus, dictionaries, audit logs, approval tasks, and Kanban in mock mode. It checks route access after login, key page headings, horizontal overflow, nonblank screenshots, and basic heading contrast across desktop and mobile viewports. It uses SPA navigation after login because the current mock auth token registry is process-memory backed.
- `frontend/e2e/qms-engineering.spec.ts`: mock-backed browser flow for Part -> Drawing -> Revision creation plus mobile viewport overflow/nonblank validation.

## Platform Foundation Productization

- `ai-coding/templates/platform-foundation/platform-foundation-delivery-package.json`: machine-readable delivery package for the platform foundation permission matrix, default role templates, menu baseline, design acceptance matrix, and regression matrix.
- `scripts/check-platform-foundation-templates.js`: validates the delivery package against `frontend/packages/permissions/src/index.tsx` and backend Flyway-seeded `platform:*` permissions, checks role/menu/permission references, and is executed by `scripts/check-quality.sh`.
- `ai-coding/tasks/frontend/TASK-FE-0027-platform-page-generation-template.md`: canonical frontend task template for adding production platform pages that follow the PC admin shell, API client, permission, i18n, theme, test, and code-map rules.
- `docs/operations/RUNBOOK-platform-foundation-productization.md`: runbook for installing dependencies, applying migrations, validating the package, testing platform pages, and preparing release handoff.
- `docs/operations/RUNBOOK-platform-foundation-troubleshooting.md`: troubleshooting guide for login, menu, permission, theme, i18n, and package drift issues.
- `docs/operations/platform-foundation-release-checklist.md`: RC1 release gates, defect severity, compatibility governance, design governance, and next-backlog categories.
- `docs/operations/platform-foundation-smoke-test.md`: manual and scripted smoke test procedure for login, menus, core platform APIs, config APIs, and backend denial behavior.
- `docs/operations/platform-foundation-known-issues.md`: RC1 known issue ledger for mock-first and deferred platform surfaces.
- `docs/operations/platform-foundation-document-index.md`: single navigation entrypoint for Platform Foundation RC1 documentation.
- `docs/operations/platform-foundation-feedback-log.md`: RC1 feedback intake, core page UX review, and Agent page-template replay record.
- `docs/operations/platform-foundation-stabilization-plan.md`: stabilization patch policy, metrics, and business-domain entry checklist.
- `docs/operations/platform-foundation-runbook-review.md`: runbook replay notes for backend/frontend local startup and smoke reproducibility.
- `docs/operations/platform-foundation-next-backlog.md`: post-RC platform-layer frontend and shared platform backlog.
- TASK-0223 runtime impact: no frontend route, component, API client, state store, permission gate, or i18n key changed.

## Frontend Module Resolution

- `frontend/tsconfig.base.json`: central TypeScript path mapping for all active `@iaf/*` workspace packages, including `api-client`, `auth`, `domain-types`, `form-engine`, `i18n`, `mock-data`, `permissions`, `table-engine`, `theme`, `ui-business`, and `ui-core`.
- `frontend/package.json`: root dependency manifest for the active frontend workspace. It explicitly declares Ant Design, ProComponents, `@ant-design/icons`, React, TanStack Query, i18next, React Router, Zustand, Vite, Vitest, Playwright, and Testing Library dependencies used by the packages and `apps/pc-admin`.
- `frontend/apps/pc-admin/vite.config.ts`: mirrors the same aliases for local app development, builds, and Playwright webServer runs.
- `frontend/vitest.config.ts`: mirrors the same aliases for repository-level Vitest runs.
- Notes:
  - `@iaf/mock-data` maps to `packages/mock-data/src/register.ts` because that package exports `registerMocks` from `register.ts` rather than `src/index.ts`.

## Routes

| Route | Page | Auth | Permission |
|---|---|---|---|
| `/login` | `LoginPage` | Public | none |
| `/` | `WorkbenchPage` | Bearer token | authenticated |
| `/platform/users` | `UserListPage` | Bearer token | `platform:user:view` for menu visibility |
| `/platform/orgs` | `OrgTreePage` | Bearer token | `platform:org:view` for menu visibility |
| `/platform/roles` | `RoleListPage` | Bearer token | `platform:role:view` for menu visibility |
| `/platform/menus` | `PlatformMenuConsolePage` | Bearer token | `platform:menu:view` for menu visibility |
| `/platform/dictionaries` | `PlatformDictionaryParameterPage` | Bearer token | `platform:dictionary:view` or `platform:parameter:view` |
| `/platform/audit-logs` | `PlatformAuditLogPage` | Bearer token | `platform:audit:view` |
| `/platform/approval/tasks` | `ApprovalTaskCenterPage` | Bearer token | frontend mock-first |
| `/platform/kanban` | `PlatformKanbanPage` | Bearer token | frontend mock-first |
| `/qms/engineering/parts` | `QmsPartListPage` | Bearer token | `qms:part:view` |
| `/qms/engineering/parts/:partId` | `QmsPartDetailPage` | Bearer token | `qms:part:view`; child actions use Drawing/Revision permissions |

Route-level permission guards prevent direct URL access to protected platform pages. Backend APIs remain the final authority for access control.

### `QmsPartListPage` and `QmsPartDetailPage`

- Path: `frontend/apps/pc-admin/src/modules/qms/engineering/`.
- Purpose: current-organization Part search/pagination/create and Part -> Drawing -> DrawingRevision navigation.
- API client: `qmsEngineeringApi` in `api.ts`; all calls use the app `@iaf/api-client` instance.
- Server state: `hooks.ts` owns QMS Query keys, reads, mutations, and parent-scoped invalidation.
- UI: `ConfigurableListPage` for Parts; token-driven descriptions/tables and `FormInteractionSurface` for Part, Drawing, and metadata-only Revision creation.
- Permissions: centralized `QMS_PERMISSIONS`; route uses `qms:part:view`, while each create control uses its corresponding create permission.
- Context: `pageContext.tsx` registers `PageContext` and produces permission-filtered `PageAIContext`; hidden permissions/actions are not exported to the context.
- Modes: simple mode shows business identifiers/status; expert mode adds organization/version/source/sequence/timestamp fields.
- Mock seam: `frontend/packages/mock-data/src/qms/engineering.ts` supplies stateful sample records when `VITE_IAF_MOCK_API=true`.
- Out of scope: file upload/preview/parsing, revision transitions, release, evidence, and AI extraction.

## Pages

### `LoginPage`

- Path: `frontend/apps/pc-admin/src/pages/LoginPage.tsx`
- Purpose: login form for `POST /api/platform/auth/login`.
- Dependencies:
  - `@iaf/auth` login helper and token store.
  - `@iaf/i18n` for visible text.
  - `@iaf/theme` for brand config, selected login template, and design tokens.
- Template file:
  - `frontend/apps/pc-admin/src/pages/loginTemplates.tsx`
  - Exports `loginTemplateRenderers` for the five supported templates.
- Current configuration entry:
  - `frontend/apps/pc-admin/src/config/brandConfig.ts`
  - `VITE_IAF_LOGIN_TEMPLATE` selects the active template for local/static configuration.
  - `/login?loginTemplate=...` overrides the selected template at runtime for visual inspection.
  - `VITE_IAF_BRAND_CONFIG_API=true` enables the remote brand config seam. The first wired fields are logo, favicon, background, and template; user-visible text still uses i18n keys.
- Notes:
  - Submits `tenantCode`, `username`, and `password`; tenant code is required and intentionally not defaulted in the page.
  - Stores access token in browser local storage for this development milestone.
  - This storage choice is temporary and should be revisited before production hardening.
  - Uses `IafBrandConfig.loginTemplate` to select `standard-industrial`, `cyber-ai`, `immersive-glass`, `minimal-technical`, or `bento-dashboard`.
  - Uses the shared Ant Design tokens and `@iaf/theme` design tokens for layout, surfaces, brand treatment, login-template palettes, and industrial visual direction.
  - Supports runtime template switching with `/login?loginTemplate=...`; the query value is resolved by `src/config/brandConfig.ts`.
  - Redirects authenticated users to the last active workspace tab when present, avoiding an intermediate workbench flash after login.
  - User-visible copy is provided by `@iaf/i18n`.

### `UserListPage`

- Path: `frontend/apps/pc-admin/src/modules/platform/users/UserListPage.tsx`
- Purpose: platform user list, create, edit, disable, reset password, and organization assignment management.
- API methods:
  - `usersApi.listUsers` (in `src/modules/platform/users/api.ts`)
  - `usersApi.createUser`
  - `usersApi.updateUser`
  - `usersApi.getUserOrganizations`
  - `usersApi.assignUserOrganizations`
  - `usersApi.switchUserOrgContext`
  - `usersApi.disableUser`
  - `usersApi.resetPassword`
- Custom hooks:
  - `useUsersQuery`, `useCreateUserMutation`, `useUpdateUserMutation`, `useUserOrganizationsQuery`, `useAssignUserOrganizationsMutation`, `useDisableUserMutation`, `useResetPasswordMutation` (in `src/modules/platform/users/hooks.ts`)
- Permission gates:
  - `platform:user:create`: create button (`PermissionButton`).
  - `platform:user:update`: edit and assign-organizations row actions.
  - `platform:user:disable`: disable action.
  - `platform:user:reset-password`: reset password action.
- Notes:
  - Does not display password hash.
  - Reset password input is submitted to backend and not logged.
  - Organization assignment uses the platform organization tree as the selectable source, requires a primary organization when any organization is assigned, and renders assigned organizations in the user list.
  - Create/edit uses `FormInteractionSurface` and `@iaf/theme` form interaction and surface-width preferences.
  - When the preference is `page`, the list body is hidden and the form renders as an independent work surface; modal and drawer modes keep the list context visible.

### `OrgTreePage`

- Path: `frontend/apps/pc-admin/src/modules/platform/orgs/OrgTreePage.tsx`
- Purpose: organization tree display, create child org, edit org.
- API methods:
  - `orgsApi.listOrgTree` (in `src/modules/platform/orgs/api.ts`)
  - `orgsApi.createOrg`
  - `orgsApi.updateOrg`
- Custom hooks:
  - `useOrgTreeQuery`, `useCreateOrgMutation`, `useUpdateOrgMutation` (in `src/modules/platform/orgs/hooks.ts`)
- Permission gates:
  - `platform:org:create`: create button (`PermissionButton`).
  - `platform:org:update`: edit action (`useHasPermission`).
- Notes:
  - Uses only platform organization types: `COMPANY`, `DEPARTMENT`, `TEAM`.
  - Does not model factory, workshop, warehouse, or WMS location.
  - Create/edit uses `FormInteractionSurface` and `@iaf/theme` form interaction and surface-width preferences.
  - When the preference is `page`, the tree/list body is hidden and the form renders as an independent work surface; modal and drawer modes keep the tree/list context visible.

### `RoleListPage`

- Path: `frontend/apps/pc-admin/src/modules/platform/roles/RoleListPage.tsx`
- Purpose: role list, create, edit, and permission assignment.
- API methods:
  - `rolesApi.listRoles` (in `src/modules/platform/roles/api.ts`)
  - `rolesApi.createRole`
  - `rolesApi.updateRole`
  - `rolesApi.assignRolePermissions`
  - `rolesApi.assignRoleMenus`
  - `rolesApi.listPermissions`
- Custom hooks:
  - `useRolesQuery`, `useCreateRoleMutation`, `useUpdateRoleMutation`, `useAssignPermissionsMutation`, `useAssignMenusMutation`, `usePermissionsQuery` (in `src/modules/platform/roles/hooks.ts`)
- Permission gates:
  - `platform:role:create`: create button (`PermissionButton`).
  - `platform:role:update`: edit action (`useHasPermission`).
  - `platform:role:assign-permission`: permission assignment action (`useHasPermission`).
  - `platform:role:assign-menu`: menu assignment action (`useHasPermission`).
- Notes:
  - Role responses include each role's current permission codes and menu codes.
  - Assignable permission options are loaded from `/api/platform/permissions`, with frontend constants used as i18n metadata when the backend code matches a known seeded permission.
  - Menu assignment options are loaded from `/api/platform/menus/tree`.
  - Create/edit uses `FormInteractionSurface` and `@iaf/theme` form interaction and surface-width preferences.
  - When the preference is `page`, the list body is hidden and the form renders as an independent work surface; modal and drawer modes keep the list context visible.

### `PlatformKanbanPage`

- Path: `frontend/apps/pc-admin/src/modules/platform/kanban/PlatformKanbanPage.tsx`
- Purpose: frontend-only Kanban implementation for validating Platform Foundation complex-view layout, industrial theme behavior, state-driven card UI, WIP display, filters, drag-and-drop movement, and card detail drawer.
- Data:
  - Static in-page sample data only.
- Notes:
  - No backend API or platform permission has been introduced yet.
  - Drag-and-drop currently uses browser-native drag events and local React state.
  - Formal Kanban APIs, permissions, persistence, ordering rules, and backend state validation remain part of the backend `TASK-0211` implementation.

### `PlatformMenuConsolePage`

- Path: `frontend/apps/pc-admin/src/modules/platform/menus/PlatformMenuConsolePage.tsx`
- Purpose: platform menu management console for menu tree browsing, create, and edit.
- API methods:
  - `menusApi.listMenusTree` (in `src/modules/platform/menus/api.ts`)
  - `menusApi.createMenu`
  - `menusApi.updateMenu`
- Custom hooks:
  - `useMenusTreeQuery`, `useCreateMenuMutation`, `useUpdateMenuMutation` (in `src/modules/platform/menus/hooks.ts`)
- Permission gates:
  - `platform:menu:create`: create button (`PermissionButton`).
  - `platform:menu:update`: edit action (`PermissionButton`).
- Notes:
  - Uses backend menu tree data and mock handlers for frontend-only development mode.
  - Does not currently edit `sys_menu_permission` links; those are seeded and shown read-only on menu rows.
  - Parent menu options exclude the current menu and all descendants so the UI cannot submit a menu cycle.
  - Create/edit uses `FormInteractionSurface` and `@iaf/theme` form interaction and surface-width preferences.

### `PlatformDictionaryParameterPage`

- Path: `frontend/apps/pc-admin/src/modules/platform/config/PlatformConfigPages.tsx`
- Purpose: mock-first dictionary and system parameter UI for `TASK-0206` UI validation.
- Data:
  - Static in-page sample dictionary and parameter records.
- Notes:
  - Backend dictionary, parameter, and audit APIs still need implementation before this page becomes production-backed.
  - Route and menu visibility are guarded by `platform:dictionary:view` or `platform:parameter:view`.
  - Uses `IafSurface` as the standard panel wrapper for dictionary and parameter tabs.

### `PlatformAuditLogPage`

- Path: `frontend/apps/pc-admin/src/modules/platform/config/PlatformConfigPages.tsx`
- Purpose: mock-first operation log query surface for `TASK-0206` UI validation.
- Data:
  - Static in-page sample operation logs.
  - Uses `IafMetricCard` for audit summary and `IafSurface` for the log table.
- Notes:
  - Route and menu visibility are guarded by `platform:audit:view`.

### `ApprovalTaskCenterPage`

- Path: `frontend/apps/pc-admin/src/modules/platform/approval/ApprovalTaskCenterPage.tsx`
- Purpose: mock-first approval task center for `TASK-0208` UI validation.
- Features:
  - Todo, done, and started tabs.
  - Task detail drawer.
  - Approval timeline.
  - Approve, reject, and return actions with local feedback.
  - Uses `IafSurface`, `IafSectionHeader`, and `IafStatusPill` for approval list and detail sections.
- Notes:
  - Uses static data until the approval task APIs are implemented.

### `systemConfigApi`

- Path: `frontend/apps/pc-admin/src/modules/platform/systemConfig/api.ts`
- Purpose: frontend API client for platform theme, brand, i18n resource, and current-user preference endpoints.
- Methods:
  - `getTheme`, `saveTheme`
  - `getBrand`, `saveBrand`
  - `listI18nResources`, `replaceI18nResources`
  - `getMyPreference`, `saveMyPreference`
- Notes:
  - `MainLayout` currently consumes `getMyPreference` and `saveMyPreference` so personal UI preferences are backend-first after login, with browser local storage fallback.
  - Theme, brand, and i18n methods are available for upcoming configuration pages.

## Packages

### `frontend/packages/domain-types`

- Owner package: `@iaf/domain-types`
- Purpose: typed frontend representation of backend HTTP contracts.
- Key exports:
  - `Result<T>`
  - `PageResult<T>`
  - `LoginRequest`
  - `LoginResponse`
  - `AuthPrincipal`
  - `PlatformUser`
  - `PlatformOrg`
  - `PlatformRole`
  - `PlatformMenu`
  - `PlatformPermission`
  - `QmsPart`, `QmsDrawing`, `QmsDrawingRevision` and their create request/status types.
  - request DTO interfaces for user, org, role, role permission assignment, role menu assignment, and platform menu create/update.

### `frontend/packages/api-client`

- Owner package: `@iaf/api-client`
- Purpose: unified request client.
- Key exports:
  - `ApiClient`
  - `ApiError`
  - `createApiClient`
- Behavior:
  - Injects Bearer token from configured callback.
  - Unwraps backend `Result<T>`.
  - Normalizes failed backend or HTTP responses into `ApiError`.
  - Calls `onUnauthorized` when HTTP 401 is returned.
  - Supports optional `MockApiAdapter`; mock requests receive method, path params, query params, request body, and normalized headers.
  - If no mock route matches, requests pass through to real HTTP.

### `frontend/packages/auth`

- Owner package: `@iaf/auth`
- Purpose: frontend auth state and platform auth API helpers.
- Key exports:
  - `browserTokenStorage`
  - `useAuthStore`
  - `login`
  - `loadCurrentUser`
  - `logout`
- State:
  - Zustand store with `token` and `principal`.
- Known limitation:
  - Token is stored in `localStorage` for the first development milestone.

### `frontend/packages/permissions`

- Owner package: `@iaf/permissions`
- Purpose: centralized permission constants, permission rendering components, and context-aware Hooks.
- Key exports:
  - `PLATFORM_PERMISSIONS`
  - `QMS_PERMISSIONS`
  - `PLATFORM_PERMISSION_OPTIONS`
  - `hasPermission`
  - `hasAnyPermission`
  - `useUserPermissions`
  - `useHasPermission`
  - `useHasAnyPermission`
  - `PermissionGate` (context-aware if no permissions are passed)
  - `PermissionButton`
  - `PermissionRoute`
- Dependencies:
  - `@iaf/auth`
  - `@iaf/domain-types`
  - `antd`
  - `react-router-dom`
- Notes:
  - `PLATFORM_PERMISSION_OPTIONS` mirrors seeded backend platform permissions for local i18n metadata; role assignment now loads assignable permissions from `/api/platform/permissions`.
  - `PermissionRoute` is used by the app route tree for direct URL permission interception; menu hiding is not the only frontend guard.

### `frontend/packages/i18n`

- Owner package: `@iaf/i18n`
- Purpose: i18next initialization and `zh-CN` / `en-US` resources.
- Key exports:
  - `resources`
  - `initIafI18n`
  - `i18n`

### `frontend/packages/theme`

- Owner package: `@iaf/theme`
- Purpose: IAF frontend theme provider and persisted user-scoped experience preferences.
- Key exports:
  - `IafThemeProvider`: wraps Ant Design `ConfigProvider`, loads persisted experience settings, and applies runtime Ant Design tokens.
  - `useIafTheme`: reads and updates personal experience settings and switches the preference storage scope after login.
  - `iafThemes`: theme config registry.
  - `iafShellTokens`: theme-owned shell token registry used by `MainLayout` for the industrial sidebar and topbar visual system.
  - `iafLightShellTokens`: theme-owned light sidebar token registry.
  - `iafSurfaceWidths`: standard, wide, and extra-wide form surface width registry.
  - `iafDesignTokens`: IAF Design Tokens v2 registry with Global Token, Semantic Token, Component Token, surfaces, status colors, data visualization palettes, elevation, focus rings, Kanban, dashboard, and designer canvas styling.
  - `iafDefaultLoginTemplateTokens`: default visual token pack for the five login template concepts.
  - `iafThemeNames`: supported theme registry.
  - `iafLoginTemplates`: supported login template registry.
  - `IafThemeName`: `light-industrial`, `dark-industrial`, `compact-industrial`, `dashboard-industrial`, `mobile-work`, `high-contrast`, or `customer-brand`.
  - `IafFormInteractionMode`: `modal`, `drawer`, or `page`.
  - `IafDensity`: `compact`, `standard`, or `comfortable`.
  - `IafFontSize`: `small`, `default`, or `large`.
  - `IafSidebarMode`: `dark` or `light`.
  - `IafMotionLevel`: `none`, `subtle`, or `standard`.
  - `IafSurfaceWidth`: `standard`, `wide`, or `extra-wide`.
  - `IafWorkspaceMode`: `simple` or `expert`.
  - `IafDesignTokens`: typed Design Tokens v2 structure, including `loginTemplates` for standard, terminal, glass, brutalist, and bento login visuals.
  - `IafLoginTemplateName`: `standard-industrial`, `cyber-ai`, `immersive-glass`, `minimal-technical`, or `bento-dashboard`.
  - `IafBrandConfig`: default brand and login visual configuration, including `loginTemplate`, until the backend theme configuration API is available.
- Behavior:
  - Persists preferences under `iaf.experience.settings.{tenantId,userId scope}` in browser local storage after `MainLayout` receives the authenticated principal; the global key remains a pre-login fallback.
  - Personal preferences currently include theme, form interaction mode, density, font size, sidebar mode, sidebar collapsed state, sidebar width, motion level, form surface width, and workspace mode.
  - Density and font size are mapped into Ant Design runtime tokens so lists, forms, controls, drawers, and modals update immediately after saving.
  - `light-industrial` is the default platform management theme.
  - `dark-industrial` provides the first industrial dark theme for Kanban, monitoring, designers, and dashboard previews.
  - `compact-industrial` provides a denser professional admin workspace.
  - `dashboard-industrial` provides a dark data-screen-oriented workspace.
  - `mobile-work` reserves larger type, spacing, and action heights for mobile/field work.
  - `high-contrast` provides stronger contrast for accessibility validation.
  - `customer-brand` reserves a configurable customer-brand theme entry.
  - Theme tokens use a coordinated industrial slate/cyan/green direction for light mode and paired dark industrial tokens for dark mode. The light sidebar token set uses explicit dark text and stronger muted text for default-state readability.
  - Ant Design Menu and Button component tokens are overridden for sidebar hover/selected states and consistent default borders.
  - Shell-specific colors are centralized in `iafShellTokens` and `iafLightShellTokens`; layout components should not hardcode shell palettes.
  - Complex-view colors must come from `iafDesignTokens` instead of local ad hoc colors. Current token groups cover Global Token, Semantic Token, Component Token, status, data visualization, Kanban, dashboard, and future designer/canvas surfaces.
  - Business status colors are defined as semantic tokens for draft, pending, approved, rejected, processing, closed, available inventory, frozen inventory, and urgent tasks.
  - Create/edit interaction mode and surface width are captured as cross-page preferences; individual pages must consume this capability through shared engines instead of hardcoding their own mode or width.

### `frontend/packages/ui-core`

- Owner package: `@iaf/ui-core`
- Purpose: reusable low-level UI components.
- Key exports:
  - `AppPageContainer`
  - `IafSurface`
  - `IafMetricCard`
  - `IafToolbar`
  - `IafSectionHeader`
  - `IafStatusPill`
  - `StatusTag`
  - `BusinessStatusBadge`
  - `DocumentStatusTag`
  - `ApprovalStatusTag`
  - `ExecutionStatusTag`
  - `InventoryStatusTag`
  - `TaskStatusTag`
  - `resolveBusinessStatusTone`
  - `ConfirmAction`
  - `FormInteractionSurface`
  - `EmptyState`
  - `ErrorState`
  - `useAppMessage`
- Notes:
  - `AppPageContainer`, `IafSurface`, and `FormInteractionSurface` expose scoped CSS classes/test anchors used by TASK-0217 visual baseline checks.
  - `IafSurface` is the standard token-driven panel wrapper for dashboards, workbench sections, config pages, and future detail surfaces.
  - `IafMetricCard` is the standard compact KPI/health metric component for workbench and dashboard-like pages.
  - `IafToolbar` is the standard inline page/list toolbar surface for filters, summaries, and local actions.
  - `IafSectionHeader` is the standard title/description/extra composition for detail sections and complex panels.
  - `IafStatusPill` is the standard semantic status marker for mock-first, preview, sync, and process states.
  - `StatusTag` and the business-specific status tag components use `@iaf/theme` semantic tokens. Business pages should use these components instead of defining local status color maps.
  - `InventoryStatusTag` maps available/frozen inventory states to the inventory semantic tokens.
  - `TaskStatusTag` supports urgent task highlighting through the task urgent semantic token.
  - `FormInteractionSurface` renders standard create/edit content as a modal, drawer, or page panel based on the shared experience preference.
  - Default modal width is `min(90vw, 760px)` with `92vw` max width; default drawer width is `min(92vw, 960px)` with `96vw` max wrapper width. Platform pages pass `@iaf/theme` surface width preferences when personal preferences are available.
  - Drawer mode uses a fixed footer action area and scrollable body.
  - Page mode renders an independent work surface; caller pages are responsible for hiding their list/tree body while the surface is open.

### `frontend/packages/ui-business`

- Owner package: `@iaf/ui-business`
- Purpose: reusable business UI helpers for the first platform pages.
- Key exports:
  - `toOrgTreeData`
  - `OrgTreeView`
  - `PermissionChecklist`

### `frontend/packages/mock-data`

- Owner package: `@iaf/mock-data`
- Purpose: in-memory stateful mock handlers for platform services and WMS templates.
- Key exports:
  - `registerMocks`
- Mock files:
  - `src/platform/auth.ts`: Mock handlers for login and current-principal lookup.
  - `src/platform/menus.ts`: Mock handlers for menu tree, current-user menus, menu create, and menu update.
  - `src/platform/users.ts`: Mock handlers for users CRUD, resets, and disable.
  - `src/platform/orgs.ts`: Mock handlers for org trees and edits.
  - `src/platform/roles.ts`: Mock handlers for roles lists, permission mapping, assignable permission list, and role menu assignment.
  - `src/platform/systemConfig.ts`: Mock handlers for theme, brand, i18n resources, and current-user experience preferences, including sidebar collapsed state and sidebar width defaults.
  - `src/wms/receiptOrders.ts`: Prototype WMS receipt order mock handlers (lists, details, creations).
  - `src/qms/engineering.ts`: stateful Part/Drawing/Revision list and create handlers matching TASK-0401 paths.
- Mock login:
  - Mock tenant validation accepts any non-empty tenant code unless `VITE_IAF_MOCK_TENANT_CODE` is configured, in which case it must match that configured value.
  - `admin` with any non-empty password receives all seeded platform permissions.
  - `operator` with any non-empty password receives read-only platform permissions.

### `frontend/packages/table-engine`

- Owner package: `@iaf/table-engine`
- Purpose: metadata-driven customizable list configuration engine.
- Key exports:
  - `ConfigurableListPage`
  - `useListViewPreference`
- Files:
  - `src/ListViewDefinition.ts`: types for table columns, search fields, row actions.
  - `src/ColumnSettings.tsx`: dialog to toggle and reorder columns.
  - `src/SearchPanelRenderer.tsx`: token-driven filters renderer with bordered filter surface.
  - `src/useListViewPreference.ts`: browser-storage backed list preferences with non-browser storage guard.
  - Row actions preserve confirmation dialogs even when permission gates are applied.
- Behavior:
  - `ConfigurableListPage` renders a professional list work surface with active filter count, visible column count, selected row count, row selection, sticky table header, localized empty state, horizontal overflow handling, refresh hook, and density-aware Ant Design table size through `@iaf/theme`.

### `frontend/packages/form-engine`

- Owner package: `@iaf/form-engine`
- Purpose: metadata-driven customizable form configuration engine with security mapping.
- Key exports:
  - `ConfigurableFormPage`
  - `FieldPermissionWrapper`
- Files:
  - `src/FormDefinition.ts`: types for fields schema and sections.
  - `src/FieldPermissionWrapper.tsx`: dynamic wrapper rendering inputs, read-only values, masked fields or hidden components.
  - `src/ViewModeFieldResolver.ts`: filter handling Simple/Expert modes.
  - `src/FormSectionRenderer.tsx`: layouts grids rendering.

## Expected Future Structure

Planned additions:

```text
frontend/
  apps/
    mobile-work/
    dashboard-view/
    supplier-portal/
  packages/
    lowcode-engine/
    scan-runtime/
    offline-runtime/
    ai-assistant/
```

Planned stack:

- pnpm workspace
- Vite
- React + TypeScript
- PC: Ant Design + ProComponents
- Mobile: Ant Design Mobile
- React Router
- TanStack Query
- Zustand for global UI state
- ECharts
- i18next
- IndexedDB + Dexie for mobile offline runtime
- Workbox / Vite PWA when offline capability is implemented
- Unified request client via `packages/api-client`
# QMS revision upload

- The Part detail revision table uploads PDF/DWG source files with the dedicated
  `qms:drawing-revision:upload` permission and displays file type/checksum after attachment.
- The same table loads the latest parse attempt per revision in one drawing-scoped query,
  displays its queue status/attempt number, and exposes retry only for failed jobs to users
  with `qms:drawing-revision:retry-parse`.
