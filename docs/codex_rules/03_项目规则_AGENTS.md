# AGENTS.md 项目规则

本文件建议放在仓库根目录，作为 Codex/Cursor/Claude Code 的最高优先级项目规则。

```markdown
# AGENTS.md

## Project

This repository implements IAF: Industrial Application Framework, an AI Coding oriented application framework for manufacturing enterprise systems.

## Non-negotiable Rules

1. Do not bypass architecture boundaries.
2. Do not create APIs without authentication and permission checks unless explicitly marked public.
3. Do not create business tables without tenant_id and audit fields.
4. Do not change existing Flyway migrations. Add a new migration instead.
5. Do not expose Flowable internals to business modules.
6. Do not hardcode organization, role, user, permission, dictionary, status, or tenant values in business logic.
7. Do not implement business state transitions without using the state machine service.
8. Do not update inventory balance without writing inventory transaction records.
9. Do not perform external integration calls directly inside core domain logic. Use application service + outbox/integration adapter.
10. Do not finish a task without running relevant tests or documenting why they could not be run.

## Development Flow

For every task:

1. Read docs/architecture and ai-coding/rules.
2. Summarize the task objective.
3. Identify impacted modules.
4. Check whether DSL, database, backend, frontend, tests, permissions, and docs need changes.
5. Create or update tests.
6. Run formatting, linting, backend tests, frontend tests, and relevant integration tests.
7. Provide a final summary including changed files, test results, risks, and follow-up items.

## Backend Rules

- Use Java 21 and Spring Boot 3.x.
- Use modular monolith boundaries.
- Follow package layers: interfaces, application, domain, infrastructure.
- Controllers only call application services.
- Application services orchestrate use cases and transactions.
- Domain services contain business rules.
- Infrastructure contains persistence, integration, Flowable adapters, and technical implementations.
- Do not inject Mapper/Repository into Controller.
- Do not place business logic in Controller, Entity, Mapper XML, or frontend code.

## Frontend Rules

- Use React + TypeScript + Ant Design Pro/ProComponents.
- Do not hardcode backend URLs.
- Do not hardcode permission decisions. Use permission hooks/components.
- Keep generated pages under src/generated or module-specific generated folders when applicable.
- Shared components must be reusable and documented.
- API types should come from OpenAPI generation or shared service typings.

## Database Rules

- Use Flyway migrations.
- Every business table must include id, tenant_id, created_by, created_at, updated_by, updated_at, deleted, version, ext_json.
- Business document tables must include document_no, document_status, approval_status when applicable.
- Inventory-related changes must be transactionally auditable.
- Add indexes for tenant_id, organization scope fields, document_no, status fields, and major foreign keys.

## Testing Rules

- Unit tests for domain logic.
- Integration tests for application services and persistence.
- Contract tests for APIs when interfaces change.
- E2E tests for critical flows.
- No critical module is considered done without tests.

## Output Format

At the end of each task, report:

- Summary
- Files changed
- Architecture impact
- Database migration impact
- Permission impact
- Tests run and results
- Known risks
- Suggested next steps
```
