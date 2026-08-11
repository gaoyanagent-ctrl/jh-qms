# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Project Is

IAF (Industrial Application Framework) is an AI-Coding-oriented monorepo for manufacturing enterprise systems (WMS, MES, SRM, QMS). It is a modular monolith built on Java 21 + Spring Boot 3.x with React + TypeScript frontend. The repo is **not** a generic CRUD admin or full low-code runtime — it targets enterprise engineering quality and manufacturing business extensibility.

## Required Reading Before Any Code Change

Per `AGENTS.md` (the highest-priority repo rule), read in order:

1. `AGENTS.md` — non-negotiable rules
2. `docs/architecture/*` — architecture blueprint
3. `docs/code-map/README.md` + the relevant `docs/code-map/*` (backend/api/database/frontend)
4. `ai-coding/rules/*` — executable numbered rule files, including project, backend, frontend, database, API, permission, workflow, state machine, rule engine, testing, code quality, code map, and agent parallel work rules
5. Relevant `docs/module-specs/*`
6. For frontend work, `docs/frontend/*`
7. The current task file under `ai-coding/tasks/`
8. Related DSL files under `dsl/` when present
9. `docs/quality/quality_gate.md`

`agent.md` is a symlink to `AGENTS.md` (compatibility for older tooling).

## Repository Layout

```text
iaf/
  AGENTS.md                 # top-priority AI rules
  README.md                 # doc layer index
  docs/                     # architecture, ADRs, code-map, module specs, frontend specs, quality
  ai-coding/                # rules, prompts, skills, tasks (AI governance)
  backend/                  # Java 21 Spring Boot modular monolith
  frontend/                 # React + TS + Ant Design Pro
  dsl/                      # business-object, document, state-machine, approval,
                            # permission, page, report, print, barcode, dashboard, integration
  tests/                    # E2E, API, contract, migration
  scripts/                  # check-quality, run-backend-tests, run-frontend-checks
  deploy/                   # deployment assets
  tools/                    # project tooling
```

## Common Commands

All scripts live at repo root. They tolerate missing build files while the monorepo bootstraps.

```bash
# Full quality gate (backend tests + frontend typecheck/lint/build)
./scripts/check-quality.sh

# Backend only — runs `mvn test` (uses ./mvnw if present, else mvn)
./scripts/run-backend-tests.sh

# Frontend only — pnpm typecheck/lint/build (falls back to npm)
./scripts/run-frontend-checks.sh

# Backend build, single module, single test
cd backend
mvn -pl iaf-platform-auth -am test
mvn -pl iaf-platform-auth -Dtest=AuthApplicationServiceTest test

# Local infra (postgres 18 on host 5123 per ADR-0002, redis 7, minio)
docker compose up -d postgres redis minio
# env overrides: copy .env.example -> .env first
```

Run the Spring Boot app from your IDE by launching `com.company.iaf.app.IafApplication` (the single `@SpringBootApplication` with `scanBasePackages = "com.company.iaf"`), or:

```bash
cd backend && mvn -pl iaf-app spring-boot:run
```

## Branch And PR Rules

- Do not develop directly on `main`; use a task branch such as `feature/TASK-xxxx-short-name` or `bugfix/TASK-xxxx-short-name`.
- Do not start new development work in the shared repository checkout. All subsequent agent development tasks must use a task-specific git worktree and branch.
- All changes must enter `main` through a Pull Request after review and the required quality gate.
- Agents must not directly merge, rebase, cherry-pick, or push task changes into `main`.
- Default PR merge strategy is Squash merge. Use a merge commit only when preserving multi-commit context is necessary and the PR explains why.
- Emergency bypasses require explicit repository-owner approval and must be documented with reason, risk, commit hash, validation result, and follow-up review plan.

Recommended worktree layout:

```bash
git fetch origin
git worktree add ../iaf-worktrees/TASK-xxxx-short-name -b feature/TASK-xxxx-short-name origin/main
```

## Backend Architecture

**Single deployable Spring Boot app, multiple Maven modules, no microservices.** See `docs/decisions/0001-backend-maven-multi-module.md`.

Module list (under `backend/`):

```text
iaf-app                   # Spring Boot bootstrap only — no business code
iaf-shared                # generic utilities only — must not depend on platform/manufacturing/WMS
iaf-platform-core
iaf-platform-auth
iaf-platform-org
iaf-platform-permission
iaf-platform-system
iaf-platform-workflow     # wraps Flowable, hidden from business modules
iaf-platform-statemachine
iaf-platform-rule         # JSON Logic; no Groovy/SpEL from business users
iaf-platform-integration
iaf-manufacturing-core
iaf-manufacturing-master
iaf-wms-core
iaf-wms-master
iaf-wms-inventory
iaf-wms-inbound
iaf-wms-strategy
```

**Dependency direction (enforced):**
- `iaf-app` depends on runtime modules and wires them up.
- `iaf-platform-*` may depend on `iaf-platform-core` and `iaf-shared` only. **No** manufacturing or WMS dependencies.
- `iaf-manufacturing-*` may not depend on WMS.
- WMS modules may depend on platform, manufacturing, wms-core/master/inventory.
- Business modules **must not** reach into another module's `infrastructure`, `entity`, or `mapper` packages. Cross-module calls go through application services, domain services, published APIs, or events.

**Per-module internal package layout** (lightweight DDD):

```text
interfaces/    # controllers, DTOs, assemblers
application/   # services, use-case orchestration, transactions, permission, events
domain/        # domain models, domain services, repository interfaces, enums
infrastructure/# persistence entities, mappers, repository impls, adapters, config
```

**Layer responsibilities:**
- Controllers: receive request, validate basic params, call application/query services, return result. **No business logic.**
- Application services: own transaction boundaries, use-case orchestration, permission checks, state machine, approval, event publishing, audit.
- Domain: business rules and cross-entity logic. Repository interfaces only here; implementations live in `infrastructure`.
- Infrastructure: persistence, integration adapters, Flowable adapters, technical config.

**Stack pinned in root `backend/pom.xml`:**
- Java 21, Spring Boot 3.3.5, MyBatis Plus 3.5.9, springdoc 2.6.0
- PostgreSQL (primary), MySQL compatibility reserved
- Flyway migrations, Spring Security + JWT, Redis, MinIO, Flowable

## Frontend Architecture

Frontend rules are governed by `ai-coding/rules/03_frontend_rules.md` and `docs/frontend/*`.

Required shape:

```text
frontend/
  apps/
    pc-admin/
    mobile-work/
    dashboard-view/      # reserved
    supplier-portal/     # reserved
  packages/
    api-client/
    auth/
    permissions/
    i18n/
    theme/
    domain-types/
    ui-core/
    ui-business/
    table-engine/
    form-engine/
    lowcode-engine/
    scan-runtime/
    offline-runtime/
    ai-assistant/
```

Rules:

- Use pnpm workspace, Vite, React, TypeScript.
- PC uses Ant Design + ProComponents; mobile uses Ant Design Mobile.
- Apps may depend on packages; packages must not depend on apps.
- All HTTP calls go through `packages/api-client`; pages call hooks, not raw APIs.
- Server state uses TanStack Query; global UI state may use Zustand.
- User-visible text uses i18n keys; colors and status colors use theme/semantic tokens.
- Permission and state driven UI must use shared permission/action/status components.
- PC routes use `TabWorkspace`; edit pages register dirty state.
- Mobile scan flows use `scan-runtime`; mobile offline flows use `offline-runtime`.
- Complex business pages expose `PageAIContext` and never pass hidden or unauthorized data to the AI assistant.

## Key Conventions

- **API paths**: `/api/{module}/{resources}`; business actions as `POST /api/{module}/{resources}/{id}/{action}` (e.g. `/api/wms/receipt-orders/{id}/submit`).
- **Response envelope**: unified `Result<T>` for single payloads, `PageResult<T>` for pagination. **No** raw Java stack traces in client-facing errors.
- **Error codes**: `{MODULE}_{DOMAIN}_{ERROR}` (e.g. `WMS_RECEIPT_INVALID_STATUS`).
- **Permission codes**: `module:object:action`. Backend permission checks are mandatory for write operations and sensitive reads; frontend hiding is UX only.
- **Business tables** must include: `id`, `tenant_id`, `created_by`, `created_at`, `updated_by`, `updated_at`, `deleted`, `version`, `ext_json`. Optional: `company_id`, `plant_id`, `department_id`, `warehouse_id`. Common-index fields: `tenant_id`, `company_id`, `plant_id`, `document_no`, `status`, `created_at`.
- **Migrations** live in `backend/<module>/src/main/resources/db/migration/Vxxxx__name.sql`. **Never** edit an existing migration — add a new one.
- **Money/quantity/weight/volume** use `decimal`/`numeric`, never `float`/`double`.
- **State transitions** in business logic must call `StateMachineService` — no inline transitions. **Inventory changes** must go through `InventoryPostingService` with idempotency keys. **Approvals** must go through `ApprovalApplicationService` — never call Flowable APIs directly. **External integration** must use application service + outbox/adapter, never call externally from core domain.
- **DTOs**: requests/responses must not be Entities. Prefer `record` for DTOs (Java 21). Use constructor injection.
- **Exceptions**: use unified `BusinessException(ErrorCode.X, "message")`; no raw `RuntimeException` from business code. Never expose internal paths/SQL errors in API responses.

## Code Map Discipline

`docs/code-map/` is the navigation source of truth for every agent. Update it **in the same task** whenever any of these change:

- Maven modules, Java packages, classes/interfaces/records/enums, public methods, cross-module contracts.
- Controllers, application services, domain services, repositories, mappers, adapters, config, filters, interceptors, scheduled jobs, event handlers.
- HTTP APIs, request/response bodies, status codes, error codes, permission codes, auth behavior.
- Flyway migrations, tables, columns, indexes, constraints, enums, table ownership.
- Frontend routes, pages, components, API clients, state stores, permission gates.
- DSL files or generated-code contracts.

Code map files: `backend.md`, `api.md`, `database.md`, `frontend.md`. If a task intentionally skips the code map, the final report must state why.

## Testing

- Backend: JUnit 5 + AssertJ + Mockito; Testcontainers for integration tests requiring real DB/services. Cover domain unit, application service, controller, repository/migration, and core flow integration tests. Focus on permission, state transitions, approval sync, rule hit, inventory posting, concurrency/idempotency.
- Frontend: TypeScript typecheck, ESLint, build, smoke tests for core pages.
- **Never** delete tests or relax validations to make CI pass. **Never** finish a task without running relevant tests or documenting why they could not run.

## Branch and Commit Conventions

- Branch: `feature/TASK-xxxx-short-name`
- Commit: `TASK-xxxx: short description`
- ADRs required for: tech stack changes, module boundary changes, DB strategy changes, workflow/rule engine core model changes, WMS inventory core algorithm changes. ADRs live in `docs/decisions/`.

## Final Report Per Task

Every task must end with: summary, files changed, architecture impact, DB migration impact, permission impact, code map impact, tests run + results, quality gate status, known risks, suggested next steps — plus the `docs/quality/quality_gate.md` checklist when code changed.
