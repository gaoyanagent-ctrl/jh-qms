# Frontend Visual Audit

Task: `TASK-0217_platform_page_visual_baseline`

Baseline branch: `feature/TASK-0217-platform-page-visual-baseline` at `bc9f3c9`.

## Scope

Reviewed and covered by automated visual smoke checks:

- Current login page at `/login`.
- Platform shell after login.
- `/`
- `/platform/users`
- `/platform/orgs`
- `/platform/roles`
- `/platform/menus`
- `/platform/dictionaries`
- `/platform/audit-logs`
- `/platform/approval/tasks`
- `/platform/kanban`

Viewports covered:

- `1366x768`
- `1440x900` and `1920x1080` for the current login page.
- `390x844`

Themes covered:

- `light-industrial` for all platform routes above.
- `dark-industrial` for the shell and key platform pages.

## Findings

### Blocking

- Workspace package resolution was incomplete for `@iaf/table-engine`, `@iaf/form-engine`, and `@iaf/mock-data`.
  - Status: fixed.
  - Fix: added TypeScript paths plus Vite/Vitest aliases so checks do not depend on package-manager symlink behavior.

- Existing visual smoke tests could not run under npm-installed dependencies because Vite could not resolve local `@iaf/*` packages.
  - Status: fixed.
  - Fix: same alias baseline as above.

### Major

- Platform page visual regression coverage only checked shell stability and did not verify key page titles, no horizontal overflow, nonblank screenshots, or basic foreground/background contrast.
  - Status: fixed.
  - Fix: added `frontend/e2e/platform-pages-visual.spec.ts`.

- Page containers and form surfaces lacked stable visual-test anchors and some responsive surface constraints.
  - Status: fixed.
  - Fix: added stable `iaf-page-container`, `iaf-page-header`, `iaf-surface`, and form surface classes, plus scoped CSS constraints for page headers, drawers, modals, and mobile drawer fallback.

### Minor

- The route menu label for dictionaries differs from the page title: menu is `字典参数`, page title is `字典与参数`.
  - Status: accepted for this task.
  - Note: visual baseline follows the current page title.

## Deferred

- Five login template URLs from the task (`standard-industrial`, `cyber-ai`, `immersive-glass`, `minimal-technical`, `bento-dashboard`) are not present on this baseline branch.
  - Reason: `feature/TASK-0214-theme-status-login` is not an ancestor of this task branch.
  - Current coverage validates the existing `/login` page across required desktop and mobile viewports.
  - Follow-up: extend `frontend/e2e/login-templates.spec.ts` after the login-template branch is integrated into the baseline.

## Automated Checks

Added visual baseline checks:

- Login page root content is visible.
- Platform route root shell is visible.
- Current page title is visible.
- Horizontal overflow is at most 1 px.
- Screenshot buffer is above a nonblank threshold.
- Key heading contrast ratio is at least 3:1.
- Light and dark themes are covered in mock mode.
