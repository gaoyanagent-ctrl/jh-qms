# Code Map Rules

`docs/code-map/` is the project-wide source of navigation truth for agents.

Every coding task must keep the code map aligned with the repository.

## 1. Required Reading

Before changing code, read:

1. `docs/code-map/README.md`
2. The relevant code map file:
   - backend changes: `docs/code-map/backend.md`
   - API changes: `docs/code-map/api.md`
   - database changes: `docs/code-map/database.md`
   - frontend changes: `docs/code-map/frontend.md`

## 2. Required Updates

Update the code map in the same task when adding, deleting, renaming, or materially changing:

- Maven modules or frontend packages.
- Java packages, classes, interfaces, records, enums, public methods, or cross-module contracts.
- Controllers, application services, domain services, repositories, mappers, adapters, configuration classes, filters, interceptors, scheduled jobs, or event handlers.
- HTTP APIs, request/response bodies, status codes, error codes, permission codes, or authentication behavior.
- Flyway migrations, tables, columns, indexes, constraints, enum/status values, or table ownership.
- Frontend routes, pages, components, API clients, state stores, permission gates, and visible workflow or state behavior.
- DSL files or generated-code contracts.

## 3. What To Record

Record only durable navigation and contract information:

- File path or symbol.
- Owning module.
- Layer.
- Purpose.
- Key public methods or endpoints.
- Dependencies and cross-module calls.
- Permission, migration, or API impact when relevant.

Do not copy large code snippets. Do not document private line-by-line implementation details.

## 4. Quality Gate

A task is incomplete if it changes code or API/database/frontend structure without either:

- updating the relevant `docs/code-map/*` file; or
- explicitly explaining in the final report why no code map update was needed.

