# AGENTS.md - JH QMS AI Development Rules

## Project Positioning

JH QMS is JinHeng's quality management system. This repository uses IAF (Industrial Application Framework) as its imported engineering foundation and develops QMS product capabilities independently.

The IAF repository and local `/iaf` checkout are upstream references only. All JH QMS branches, commits, worktrees, pull requests, migrations, and product documentation must be created in this repository. Never modify or push to IAF while implementing JH QMS tasks.

The inherited IAF platform provides a unified engineering structure, platform capabilities, manufacturing common models, business document patterns, approval/state/rule capabilities, lightweight DSL, source generation, and AI coding governance.

This is not a generic CRUD admin system and not a full low-code runtime. Development must satisfy enterprise engineering quality, manufacturing business extensibility, AI coding maintainability, and project delivery practicality.

## Required Reading Order

Before changing code for any task, read and follow:

1. `AGENTS.md`
2. `docs/architecture/06_项目文档与规则目录治理.md`
3. `docs/architecture/*`
4. `docs/code-map/README.md` and relevant `docs/code-map/*`
5. `ai-coding/rules/*`
6. Relevant module specs under `docs/module-specs/*`
7. For frontend work, relevant `docs/frontend/*`
8. The current task file under `ai-coding/tasks/`
9. Related handoff or runbook files under `docs/operations/`, when explicitly referenced by the user or task
10. Related DSL files under `dsl/`, when present
11. `docs/quality/quality_gate.md`

`docs/codex_rules/` keeps the original governance documents. `ai-coding/rules/` is the executable rule directory for Codex, Cursor, Claude Code, and other AI coding tools.
`docs/iaf_frontend_specs/` keeps the original frontend specification package. `docs/frontend/` is the normalized frontend specification directory used by agents.

## Directory Authority

Use the repository directories by authority level:

- `AGENTS.md`: global entrypoint and highest-priority project rules.
- `ai-coding/rules/`: only executable rule directory. If a rule conflicts with a specification document, this directory wins unless an ADR changes the decision.
- `ai-coding/tasks/`: only canonical executable task directory. Agents should implement tasks from this tree, not from `docs/`.
- `docs/architecture/`, `docs/frontend/`, and `docs/module-specs/`: specification and design directories. They explain intent, architecture, UX, and domain contracts; they are not task queues.
- `docs/code-map/`: current implementation map. Update it in the same task when code, APIs, database objects, routes, public methods, or cross-module dependencies change.
- `docs/operations/`: handoff plans, runbooks, environment notes, and troubleshooting records. Files here may guide an agent, but they are not canonical task specifications.
- `docs/codex_rules/` and `docs/iaf_frontend_specs/`: original source packages retained for traceability. Do not treat them as executable rules unless a current rule explicitly references them.

Detailed directory governance lives in `docs/architecture/06_项目文档与规则目录治理.md`.

## Technology Stack

Backend:

- Java 21
- Spring Boot 3.x
- Modular monolith
- Lightweight DDD layering
- MyBatis Plus + Mapper XML
- Flyway migration
- PostgreSQL first, with MySQL compatibility reserved
- Spring Security + JWT
- Redis
- MinIO
- Flowable as the approval workflow kernel, hidden behind platform approval services

Frontend:

- Frontend executable rules: `ai-coding/rules/03_frontend_rules.md`
- Frontend specifications: `docs/frontend/00_README.md` and related `docs/frontend/*`
- Frontend task source: `ai-coding/tasks/frontend/`

Do not introduce a new core framework, ORM, UI framework, workflow engine, or rule engine without explicit approval and ADR.

## Frontend Project Structure Decision

The frontend must be treated as a first-class subsystem, not a collection of pages.

The frontend workspace layout, dependency direction, application/package boundaries, and delivery rules are defined in `ai-coding/rules/03_frontend_rules.md`. Architecture and UX details are defined in `docs/frontend/`.

## Architecture Rules

- Use modular monolith boundaries. A single deployment unit does not permit direct cross-module infrastructure access.
- Follow the module package layers: `interfaces`, `application`, `domain`, `infrastructure`.
- Controllers only receive requests, validate basic parameters, call application/query services, and return results.
- Application services own transaction boundaries, use-case orchestration, permission checks, state machine calls, approval calls, event publishing, and audit recording.
- Domain services and domain models contain manufacturing and business rules.
- Infrastructure contains persistence entities, mappers, repository implementations, integration adapters, Flowable adapters, and technical configuration.
- Do not inject Mapper/Repository into Controller.
- Do not place business logic in Controller, Entity, Mapper XML, or frontend code.
- Do not access another module's `infrastructure`, `entity`, or `mapper` directly.

## Backend Project Structure Decision

The backend must use a Maven multi-module structure as accepted in `docs/decisions/0001-backend-maven-multi-module.md`.

The system is a modular monolith:

- One deployable Spring Boot application.
- Multiple Maven modules.
- Clear module boundaries.
- No Spring Cloud microservices in phase 1.

Do not implement the backend as a single Maven project with only package-level modules.

The first version must use a flat Maven multi-module layout:

- `iaf-app`
- `iaf-shared`
- `iaf-platform-core`
- `iaf-platform-auth`
- `iaf-platform-org`
- `iaf-platform-permission`
- `iaf-platform-system`
- `iaf-platform-workflow`
- `iaf-platform-statemachine`
- `iaf-platform-rule`
- `iaf-platform-integration`
- `iaf-manufacturing-core`
- `iaf-manufacturing-master`
- `iaf-wms-core`
- `iaf-wms-master`
- `iaf-wms-inventory`
- `iaf-wms-inbound`
- `iaf-wms-strategy`
- `iaf-qms-engineering`

Each module must follow the internal package structure: `interfaces`, `application`, `domain`, `infrastructure`.

Business modules must not directly access another module's infrastructure package. Cross-module calls must go through application services, domain services, published APIs, or events.

## Non-negotiable Rules

1. Do not bypass architecture boundaries.
2. Do not create APIs without authentication and permission checks unless explicitly marked public.
3. Do not create business tables without `tenant_id`, audit fields, `deleted`, `version`, and `ext_json`.
4. Do not change existing Flyway migrations. Add a new migration instead.
5. Do not expose Flowable internals to business modules.
6. Do not hardcode organization, role, user, permission, dictionary, status, or tenant values in business logic.
7. Do not implement business state transitions without `StateMachineService`.
8. Do not update inventory balance without writing inventory transaction records through the inventory posting service.
9. Do not perform external integration calls directly inside core domain logic. Use application service plus outbox/integration adapter.
10. Do not delete tests or reduce validation standards to make CI pass.
11. Do not finish a task without running relevant tests or documenting why they could not be run.
12. Do not bypass `packages/api-client` for frontend HTTP calls.
13. Do not hardcode frontend Chinese copy, colors, permission checks, status colors, or token handling in pages.
14. Do not merge task branches into `main` directly. All changes must enter `main` through a pull request that has passed review and the required quality gate, unless the repository owner explicitly documents an emergency exception.
15. Do not start new development tasks in the shared repository checkout. All subsequent Agent work must use an independent git worktree and task branch, following `ai-coding/rules/13_agent_parallel_work_rules.md`.

## Platform Capability Rules

- API paths must follow `/api/{module}/{resources}`.
- Responses must use unified `Result<T>` and `PageResult<T>` structures.
- All business errors must use unified error codes; never expose raw Java stack traces to the frontend.
- Permission codes follow `module:object:action`.
- Backend permission checks are mandatory for write operations and sensitive reads. Frontend permission hiding is only UX.
- Business documents separate `document_status`, `approval_status`, `execution_status`, `posting_status`, and `settlement_status` where applicable.
- Business modules must call `ApprovalApplicationService`; they must not call Flowable APIs directly.
- Business-configurable rules should use `RuleEngineService` and JSON Logic. Do not allow arbitrary Java, Groovy, or SpEL scripts from business users.
- WMS inventory changes must go through `InventoryPostingService`, use idempotency keys, update balances consistently, and create inventory transaction records.

## Frontend Capability Rules

`ai-coding/rules/03_frontend_rules.md` is the only executable frontend rule source. `docs/frontend/` provides detailed specifications and examples. If `AGENTS.md`, `docs/frontend/*`, and `ai-coding/rules/03_frontend_rules.md` appear to conflict, follow `ai-coding/rules/03_frontend_rules.md` unless an ADR states otherwise.

## AI Execution Flow

For every task:

1. Summarize the task objective.
2. Identify impacted modules, file types, and risks.
3. Check whether DSL, database migration, backend, frontend, permission, tests, docs, and module specs need changes.
4. Inspect existing code and module structure before editing.
5. Create or confirm the task-specific git worktree and branch before code changes.
6. Give a short implementation plan before code changes.
7. Make the minimum necessary changes and avoid unrelated refactors.
8. Create or update tests.
9. Update `docs/code-map/*` when modules, classes, methods, APIs, database objects, frontend routes, DSL contracts, or cross-module dependencies changed.
10. Run formatting, linting, backend tests, frontend tests, and relevant integration or migration checks.
11. If tests fail, analyze and fix the cause instead of weakening checks.
12. Push the task branch and prepare or update a pull request for review when the task is ready to enter `main`; do not merge to `main` directly.
13. Provide the required final report.

## Required Final Report

At the end of each task, report:

- Summary
- Files changed
- Architecture impact
- Database migration impact
- Permission impact
- Code map impact
- Tests run and results
- Quality gate status
- Known risks
- Suggested next steps

Also include the task checklist from `docs/quality/quality_gate.md` when the task changes code.

## Rule Index

- Project rules: `ai-coding/rules/01_project_rules.md`
- Backend rules: `ai-coding/rules/02_backend_rules.md`
- Frontend rules: `ai-coding/rules/03_frontend_rules.md`
- Database rules: `ai-coding/rules/04_database_rules.md`
- API rules: `ai-coding/rules/05_api_rules.md`
- Permission rules: `ai-coding/rules/06_permission_rules.md`
- Workflow rules: `ai-coding/rules/07_workflow_rules.md`
- State machine rules: `ai-coding/rules/08_state_machine_rules.md`
- Rule engine rules: `ai-coding/rules/09_rule_engine_rules.md`
- Testing rules: `ai-coding/rules/10_testing_rules.md`
- Code quality rules: `ai-coding/rules/11_code_quality_rules.md`
- Code map rules: `ai-coding/rules/12_code_map_rules.md`
- Agent parallel work rules: `ai-coding/rules/13_agent_parallel_work_rules.md`
- Frontend specifications: `docs/frontend/`
- Frontend task list: `ai-coding/tasks/frontend/`
- Directory governance: `docs/architecture/06_项目文档与规则目录治理.md`
- Operations and handoffs: `docs/operations/README.md`
- Agent skills: `ai-coding/skills/agent_skills.md`
- Master prompt: `ai-coding/prompts/codex_master_prompt.md`
- Task template: `ai-coding/tasks/TASK_TEMPLATE.md`
- Quality gate: `docs/quality/quality_gate.md`
