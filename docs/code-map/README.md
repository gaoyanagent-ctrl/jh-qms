# IAF Code Map

This directory is the canonical code map for the IAF repository.

Every agent must read this directory before changing code and must update it after changing architecture, modules, packages, classes, public methods, APIs, database migrations, frontend routes, or cross-module dependencies.

The code map is an index, not a duplicate of the source code. Keep entries concise and useful for navigation.

## Files

- `backend.md`: backend Maven modules, package boundaries, classes, and important methods.
- `api.md`: HTTP APIs, permission expectations, request/response conventions, and owning modules.
- `database.md`: database ownership, migration locations, table meaning, and important constraints.
- `frontend.md`: frontend routes, pages, components, state, and API clients.
- `../operations/RUNBOOK-platform-foundation-productization.md`: platform foundation delivery runbook.
- `../operations/RUNBOOK-platform-foundation-troubleshooting.md`: platform foundation troubleshooting runbook.
- `../operations/platform-foundation-document-index.md`: Platform Foundation RC1 document index and maintenance entrypoint.
- `../operations/platform-foundation-release-checklist.md`: Platform Foundation RC1 release gates, compatibility rules, and backlog classification.
- `../operations/platform-foundation-smoke-test.md`: Platform Foundation smoke test procedure.

## Update Rules

Update this code map in the same task when any of the following changes:

- A Maven module, frontend package, DSL folder, or deploy/runtime directory is added, renamed, removed, or repurposed.
- A Java package, application service, domain service, controller, repository, mapper, configuration class, or shared utility is added or meaningfully changed.
- A public method or method used across module boundaries is added, removed, renamed, or changes semantics.
- An HTTP API, permission code, request body, response shape, or error behavior changes.
- A Flyway migration, table, column, index, enum/status field, or ownership rule changes.
- A frontend route, page, component contract, API client, permission gate, or state store changes.
- A task intentionally does not update the code map because the change is too small; record that rationale in the final report.

## Entry Format

Prefer this format:

```text
Path or symbol
- Owner module:
- Layer:
- Purpose:
- Key methods or endpoints:
- Dependencies:
- Notes:
```

Do not document private implementation details unless they explain a boundary, business rule, or integration contract.
