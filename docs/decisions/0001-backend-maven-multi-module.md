# ADR-0001: Backend Maven Multi-Module Structure

## Status

Accepted

## Context

IAF is an industrial application framework, platform foundation, WMS scenario template, and AI Coding engineering sample. It needs clear module boundaries for platform capabilities, manufacturing common models, and WMS scenario modules.

The project considered two backend structures:

- Single Maven project with package-level modules.
- Maven multi-module project with a modular monolith deployment model.

`docs/Maven_mulitple_moudles.md` recommends Maven multi-module with a flat module layout for phase 1.

## Decision

The backend must use a Maven multi-module structure with a modular monolith runtime:

- One deployable Spring Boot application.
- Multiple Maven modules.
- Clear module boundaries.
- No Spring Cloud microservices in phase 1.

The first version uses a flat Maven multi-module layout:

```text
backend/
  pom.xml
  iaf-app/
  iaf-shared/
  iaf-platform-core/
  iaf-platform-auth/
  iaf-platform-org/
  iaf-platform-permission/
  iaf-platform-system/
  iaf-platform-workflow/
  iaf-platform-statemachine/
  iaf-platform-rule/
  iaf-platform-integration/
  iaf-manufacturing-core/
  iaf-manufacturing-master/
  iaf-wms-core/
  iaf-wms-master/
  iaf-wms-inventory/
  iaf-wms-inbound/
  iaf-wms-strategy/
```

Each module must keep the internal lightweight DDD package structure:

```text
interfaces/
application/
domain/
infrastructure/
```

## Dependency Rules

- `iaf-app` depends on runtime modules and performs application assembly.
- `iaf-shared` must not depend on business-specific modules.
- `iaf-platform-*` modules may depend on `iaf-platform-core` and `iaf-shared`.
- `iaf-platform-*` modules must not depend on manufacturing or WMS modules.
- `iaf-manufacturing-*` modules must not depend on WMS modules.
- WMS modules may depend on platform, manufacturing, WMS core/master, and inventory modules as needed.
- Business modules must not directly access another module's `infrastructure`, `entity`, or `mapper` packages.

Cross-module calls must go through application services, domain services, published APIs, or events.

## Consequences

This structure is slightly more complex than a single Maven project, but it gives the project enforceable module boundaries, clearer AI Coding task scopes, better long-term productization, and a cleaner path to future service extraction if needed.
