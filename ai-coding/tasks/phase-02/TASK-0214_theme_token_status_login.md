# TASK-0214 Theme Token System and Configurable Login Templates

## Objective

Implement the first executable theme token system for the IAF frontend, standardize business status visual components, and convert the five referenced login-page concepts from `docs/iaf-login-designs.zip` into configurable IAF login templates.

## Scope

- Extend `@iaf/theme` from two hardcoded themes to the first supported theme set:
  - `light-industrial`
  - `dark-industrial`
  - `compact-industrial`
  - `dashboard-industrial`
  - `mobile-work`
  - `high-contrast`
  - `customer-brand`
- Add three token layers:
  - Global Token
  - Semantic Token
  - Component Token
- Add semantic business state colors for:
  - draft
  - pending
  - approved
  - rejected
  - processing
  - closed
  - inventory available
  - inventory frozen
  - urgent task
- Add unified status components in `@iaf/ui-core`:
  - `StatusTag`
  - `BusinessStatusBadge`
  - `DocumentStatusTag`
  - `ApprovalStatusTag`
  - `ExecutionStatusTag`
  - `InventoryStatusTag`
  - `TaskStatusTag`
- Convert the login design package into five selectable templates:
  - `standard-industrial`
  - `cyber-ai`
  - `immersive-glass`
  - `minimal-technical`
  - `bento-dashboard`

## Rules

- Do not introduce Tailwind, motion libraries, lucide, or another UI framework.
- Login page visible text must use i18n keys.
- Login page brand name, logo, background, and selected template must come from `IafBrandConfig`.
- Business status colors must come from semantic tokens.
- Business pages must not define their own status colors.
- Keep backend API and database unchanged in this task.
- Update `docs/code-map/frontend.md` when implementation changes are complete.

## Verification

- Frontend typecheck.
- Frontend tests.
- Frontend build.
- Full `./scripts/check-quality.sh`.

## Handoff Notes

The referenced zip is an input artifact only. Do not commit the generated design app or introduce its dependencies into the IAF frontend workspace.
