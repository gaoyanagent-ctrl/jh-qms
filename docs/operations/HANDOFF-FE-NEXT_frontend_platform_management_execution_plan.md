# HANDOFF-FE-NEXT Frontend Platform Management Execution Plan

## 1. Purpose

This document is the handoff plan for the next agent.

The next recommended milestone is to initialize the frontend workspace and deliver the first usable PC management loop for the existing platform backend APIs:

- Login and current user bootstrap.
- PC admin layout.
- Platform user management.
- Platform organization tree management.
- Platform role and permission assignment.

This milestone should prove that the backend platform APIs, permission model, frontend engineering rules, i18n, theme, API client, and code map process can work together in one vertical slice.

## 2. Required Reading

Read these files before editing:

1. `AGENTS.md`
2. `CLAUDE.md`
3. `docs/frontend/00_README.md`
4. `docs/frontend/01_前端总体架构.md`
5. `docs/frontend/02_PC端布局与交互规范.md`
6. `docs/frontend/03_PC多标签页与工作台规范.md`
7. `docs/frontend/06_多主题设计规范.md`
8. `docs/frontend/07_多语言设计规范.md`
9. `docs/frontend/08_权限与状态驱动UI规范.md`
10. `docs/frontend/10_组件体系规范.md`
11. `docs/frontend/11_API与状态管理规范.md`
12. `docs/frontend/12_简洁模式与专家模式规范.md`
13. `docs/frontend/13_AI助手嵌入规范.md`
14. `docs/frontend/14_Codex前端开发规则.md`
15. `docs/code-map/README.md`
16. `docs/code-map/frontend.md`
17. `docs/code-map/api.md`
18. `ai-coding/rules/03_frontend_rules.md`
19. `ai-coding/rules/05_api_rules.md`
20. `ai-coding/rules/06_permission_rules.md`
21. `ai-coding/rules/10_testing_rules.md`
22. `ai-coding/rules/12_code_map_rules.md`
23. `ai-coding/tasks/frontend/TASK-FE-0001-init-frontend-workspace.md`
24. `ai-coding/tasks/phase-00/TASK-0003_init_frontend.md`

## 3. Current Baseline

Backend platform APIs already exist:

| Capability | APIs |
|---|---|
| Auth | `POST /api/platform/auth/login`, `GET /api/platform/auth/me` |
| Users | `GET /api/platform/users`, `POST /api/platform/users`, `PUT /api/platform/users/{id}`, `POST /api/platform/users/{id}/disable`, `POST /api/platform/users/{id}/reset-password` |
| Orgs | `GET /api/platform/orgs/tree`, `POST /api/platform/orgs`, `PUT /api/platform/orgs/{id}` |
| Roles | `GET /api/platform/roles`, `POST /api/platform/roles`, `PUT /api/platform/roles/{id}`, `PUT /api/platform/roles/{id}/permissions` |

Backend security expectations:

- All platform management APIs require Bearer auth.
- Sensitive APIs use `@RequiresPermission`.
- Permission codes follow `module:object:action`.
- Frontend permission hiding is UX only; backend authorization is mandatory.

Frontend state before this task:

- `frontend/` has not been initialized.
- Frontend rules and specs are normalized under `docs/frontend/`.
- Frontend executable rules are in `ai-coding/rules/03_frontend_rules.md`.
- Frontend task seeds are in `ai-coding/tasks/frontend/`.

## 4. Target Outcome

After this milestone, the repository should contain:

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
```

The PC app should support:

- Login with backend token.
- Load current user and permissions.
- Guard routes by authentication.
- Hide or disable actions by permission.
- Render platform user, organization, role, and permission assignment pages.
- Use unified API client, typed DTOs, i18n keys, and theme tokens.
- Keep page routes and component responsibilities documented in `docs/code-map/frontend.md`.

## 5. Recommended Execution Order

### Step 1: Initialize Frontend Workspace

Implement the workspace from `TASK-FE-0001`.

Required files:

```text
frontend/package.json
frontend/pnpm-workspace.yaml
frontend/tsconfig.base.json
frontend/apps/pc-admin/package.json
frontend/apps/pc-admin/tsconfig.json
frontend/apps/pc-admin/vite.config.ts
frontend/apps/pc-admin/src/main.tsx
frontend/apps/pc-admin/src/App.tsx
frontend/packages/*/package.json
frontend/packages/*/tsconfig.json
frontend/packages/*/src/index.ts
```

Recommended dependencies:

- `react`
- `react-dom`
- `react-router-dom`
- `antd`
- `@ant-design/pro-components`
- `@tanstack/react-query`
- `zustand`
- `i18next`
- `react-i18next`
- `vite`
- `typescript`
- `vitest`
- `@testing-library/react`
- `@testing-library/jest-dom`

Rules:

- Use `pnpm`, not npm or yarn.
- Enable TypeScript strict mode.
- Apps may depend on packages.
- Packages must not depend on apps.
- Do not add mobile app or WMS pages in this step.

### Step 2: Build Core Shared Packages

Implement the minimum package set needed by the PC platform pages.

#### `packages/domain-types`

Define shared frontend types for:

- `Result<T>`
- `PageResult<T>`
- `PlatformUser`
- `PlatformOrg`
- `PlatformRole`
- `PlatformPermission`
- Auth principal/current user.

Do not duplicate backend internals. Model only the HTTP contract consumed by the frontend.

#### `packages/api-client`

Implement:

- Base URL configuration via environment variable.
- Request wrapper around `fetch` or a small approved client.
- Bearer token injection.
- Unified `Result<T>` unwrap.
- Error normalization.
- 401 handling hook/callback.

Rules:

- Page code must not call `fetch` directly.
- Page code must not parse raw backend envelopes directly.
- Do not expose raw stack traces in UI.

#### `packages/auth`

Implement:

- Login API wrapper.
- Current user API wrapper.
- Token storage abstraction.
- Auth store or query hooks.
- Logout helper.

Token storage decision:

- For this milestone, local storage is acceptable for development.
- Document the limitation in code map and final report.
- Do not hardcode test credentials in production code.

#### `packages/permissions`

Implement:

- `hasPermission(permissionCode)`
- `hasAnyPermission(permissionCodes)`
- `PermissionGate`
- `PermissionButton` or equivalent wrapper.

Rules:

- Permission codes should be constants or typed string exports.
- Do not scatter literal permission strings across pages.

#### `packages/i18n`

Implement:

- `zh-CN`
- `en-US`
- i18next initialization.
- Namespaces for common, auth, platform user/org/role pages.

Rules:

- No hardcoded Chinese page labels in components.
- All visible text should go through i18n keys, except temporary test ids and technical constants.

#### `packages/theme`

Implement:

- Design token exports.
- Theme provider wrapper.
- Light theme as default.

Rules:

- Components should use Ant Design tokens or package tokens.
- Do not hardcode ad hoc colors in page components.

#### `packages/ui-core`

Implement the minimum reusable components:

- `AppPageContainer`
- `StatusTag`
- `ConfirmAction`
- `EmptyState`
- `ErrorState`

#### `packages/ui-business`

Implement only if needed by user/org/role pages:

- Organization tree selector.
- Permission checklist/tree component.
- Platform status display helpers.

Keep this package small in this milestone.

### Step 3: Build `pc-admin` Application Shell

Implement:

```text
frontend/apps/pc-admin/src/
  main.tsx
  App.tsx
  routes/
  layouts/
  pages/
    LoginPage.tsx
    WorkbenchPage.tsx
    platform/
      UserListPage.tsx
      OrgTreePage.tsx
      RoleListPage.tsx
  api/
  test/
```

Required routes:

| Route | Page | Auth |
|---|---|---|
| `/login` | Login page | Public |
| `/` | Workbench redirect/page | Auth required |
| `/platform/users` | User management | `platform:user:view` |
| `/platform/orgs` | Org management | `platform:org:view` |
| `/platform/roles` | Role management | `platform:role:view` |

Required shell behavior:

- Unauthenticated users are redirected to `/login`.
- Authenticated users load `/api/platform/auth/me`.
- Menu items are hidden when the user lacks the read permission.
- Write buttons are hidden or disabled when the user lacks write permissions.
- API errors render a stable error state.
- Logout clears token and query/cache state.

Do not implement multi-tab `TabWorkspace` in this milestone unless the basic shell is already green. Multi-tab can remain a follow-up task from `TASK-FE-0010`.

### Step 4: User Management Page

Implement a practical CRUD screen for:

- List users.
- Create user.
- Edit user.
- Disable user.
- Reset password.

Required permissions:

| Action | Permission |
|---|---|
| View list | `platform:user:view` |
| Create | `platform:user:create` |
| Edit | `platform:user:update` |
| Disable | `platform:user:disable` |
| Reset password | `platform:user:reset-password` |

Rules:

- Do not show `password_hash`.
- Reset password response should be handled carefully; do not log generated passwords.
- Page must use API client/hooks, not direct HTTP calls.
- Form labels and messages must use i18n.

### Step 5: Organization Management Page

Implement:

- Tree view from `GET /api/platform/orgs/tree`.
- Create child organization.
- Edit organization.

Required permissions:

| Action | Permission |
|---|---|
| View tree | `platform:org:view` |
| Create | `platform:org:create` |
| Edit | `platform:org:update` |

Rules:

- Do not model factory, workshop, warehouse, or WMS location here.
- Do not hardcode org type display text in the component.
- Keep org tree rendering stable for empty roots.

### Step 6: Role and Permission Assignment Page

Implement:

- List roles.
- Create role.
- Edit role.
- Assign permission codes to a role.

Required permissions:

| Action | Permission |
|---|---|
| View roles | `platform:role:view` |
| Create | `platform:role:create` |
| Edit | `platform:role:update` |
| Assign permissions | `platform:role:assign-permission` |

Important backend contract check:

- If the backend role list API does not expose available permissions, inspect current API/code map before adding frontend assumptions.
- If a permission list API is missing, either:
  - add a small backend API in the correct module with permission protection and tests, or
  - limit the frontend assignment UI to permissions already returned by existing role detail/list contract.

Do not hardcode the 13 seeded permission codes in UI as the long-term source of truth. Temporary seed constants are allowed only if documented as a short-term limitation in `docs/code-map/frontend.md` and final report.

### Step 7: Tests

Minimum frontend tests:

- Login success stores token and navigates to app shell.
- Login failure shows normalized error.
- Unauthenticated protected route redirects to login.
- Menu hides routes when permission is missing.
- User page hides create/edit/disable/reset actions without permissions.
- Org page renders tree data.
- Role page renders assignment control only with `platform:role:assign-permission`.

Recommended tools:

- Vitest.
- Testing Library.
- Mock Service Worker if practical; otherwise small local API mocks.

Do not weaken backend tests.

### Step 8: Code Map and Docs

Update in the same task:

- `docs/code-map/frontend.md`
  - Workspace packages.
  - PC routes.
  - Page ownership.
  - API client methods.
  - Permission gates.
  - Auth/token limitation.
- `docs/code-map/api.md`
  - Any backend API contract clarification or newly added API.
- `docs/code-map/backend.md`
  - Only if backend code is changed.
- `docs/code-map/database.md`
  - Only if migration/seed data changes.

If no backend/database change is made, explicitly state that in the final report.

## 6. Backend Change Policy

This milestone should prefer consuming existing backend APIs.

Backend changes are allowed only when needed to make the frontend contract correct and should follow these rules:

- Use the existing Maven module boundaries.
- Add a new Flyway migration; never edit old migrations.
- Add or update controller/application tests.
- Keep all APIs authenticated and permission-checked unless explicitly public.
- Update code map immediately.

Likely backend gap to verify:

- Whether role/permission assignment has a clean way for the frontend to fetch assignable permissions.

## 7. Out of Scope

Do not implement in this milestone:

- Mobile `mobile-work` app.
- Scan runtime.
- Offline runtime.
- WMS receipt/putaway pages.
- Table engine or form engine generalization beyond what the pages directly need.
- PC multi-tab workspace if the base shell is not complete.
- AI Assistant panel beyond reserving package structure and documenting future integration.
- State machine, approval workflow, or rule engine backend modules.

These are follow-up tasks after the first platform frontend vertical slice is green.

## 8. Quality Gate

Run these checks before final report:

```bash
./scripts/check-quality.sh
```

If frontend scripts are added, `check-quality.sh` should run or delegate to:

```bash
cd frontend
pnpm install
pnpm typecheck
pnpm test
pnpm lint
```

If `check-quality.sh` does not yet include frontend checks, update it in the same task or document why it cannot be updated safely.

Backend checks must remain green:

```bash
mvn -pl iaf-app -am test
```

## 9. Acceptance Criteria

The task is complete only when:

- `frontend/` workspace exists and installs successfully with pnpm.
- `pc-admin` starts with Vite.
- Login flow calls backend auth API through `packages/api-client`.
- Current user and permissions are loaded after login.
- Protected routes and menu items honor permissions.
- User, org, and role pages consume real API contracts or documented mocks for local frontend tests.
- No page directly uses raw `fetch`.
- No page hardcodes visible Chinese labels outside i18n resources.
- No page hardcodes ad hoc colors outside theme tokens.
- Permission strings are centralized.
- Frontend tests cover auth and permission gating.
- Backend tests still pass.
- `docs/code-map/frontend.md` is updated.
- Final report includes the quality gate checklist required by `docs/quality/quality_gate.md`.

## 10. Suggested Commit Breakdown

Use small commits that can be reviewed independently:

1. `chore(frontend): initialize pnpm workspace`
2. `feat(frontend): add shared api auth permission i18n theme packages`
3. `feat(frontend): add pc-admin shell and login flow`
4. `feat(frontend): add platform management pages`
5. `test(frontend): cover auth and permission gating`
6. `docs(frontend): update code map for platform management UI`

If backend API gaps are fixed, use a separate backend commit before the frontend consumer commit.

## 11. Final Report Requirements

The next agent must report:

- Summary.
- Files changed.
- Architecture impact.
- Database migration impact.
- Permission impact.
- Code map impact.
- Tests run and results.
- Quality gate status.
- Known risks.
- Suggested next steps.

Also include the task checklist from `docs/quality/quality_gate.md` because this task changes code.
