# IAF

IAF, Industrial Application Framework, is an AI Coding oriented application framework for manufacturing enterprise systems.

This repository is organized as a monorepo for backend, frontend, DSL, deployment, tests, documentation, and AI development governance.

## Document Layers

| Layer | Path | Purpose |
|---|---|---|
| L1 | `docs/architecture/` | Architecture background, module boundaries, and core decisions |
| L2 | `AGENTS.md` + `ai-coding/rules/` | Mandatory AI coding rules |
| L3 | `docs/module-specs/` | Module-level executable specifications |
| L4 | `ai-coding/tasks/` | Structured development tasks |
| L5 | `docs/quality/` | Testing, quality gates, and acceptance standards |
| Frontend | `docs/frontend/` | Frontend architecture, UX, component, mobile, offline, i18n, theme, and AI assistant specifications |

## Key Paths

- `AGENTS.md`: highest-priority repository rules for AI coding tools.
- `agent.md`: compatibility link to `AGENTS.md`.
- `docs/architecture/`: architecture blueprint, module boundaries, and platform design.
- `docs/decisions/`: architecture decision records.
- `docs/codex_rules/`: original AI development governance documents.
- `docs/module-specs/`: detailed module specifications for platform and WMS work.
- `docs/frontend/`: normalized frontend engineering and experience specifications.
- `docs/iaf_frontend_specs/`: original frontend specification package.
- `docs/quality/quality_gate.md`: required quality gate and checklist.
- `ai-coding/rules/`: executable development rules used by Codex/Cursor/Claude Code.
- `ai-coding/prompts/`: master prompts for AI coding sessions.
- `ai-coding/skills/`: AI role and skill definitions.
- `ai-coding/tasks/`: task template and phased implementation tasks.
- `ai-coding/tasks/frontend/`: frontend task specifications.
- `dsl/`: business object, document, state machine, approval, permission, page, report, print, barcode, dashboard, and integration DSL.
- `backend/`: Java 21 + Spring Boot modular monolith modules.
- `frontend/`: React + TypeScript + Ant Design Pro application.
- `tests/`: E2E, API, contract, and migration tests.
- `scripts/`: local automation scripts.
- `tools/`: project tooling.

## Required Workflow

Before implementing any task, read `AGENTS.md`, `docs/architecture/*`, `ai-coding/rules/*`, relevant `docs/module-specs/*`, the current task file, and related DSL files.

Each task must produce an implementation plan, changed file list, migration impact, backend/frontend verification results, quality checklist, risks, and follow-up items.

## Development Roadmap

1. Initialize monorepo and base engineering structure.
2. Complete platform basics: users, organizations, roles, permissions, dictionaries, coding rules, attachments, and audit.
3. Complete state machine, approval workflow, rule engine, and Outbox.
4. Complete manufacturing common master data.
5. Complete WMS inbound sample flow: warehouse/location, inventory, receipt, putaway, and inventory posting.
6. Add designers, reporting, dashboard, and integration center capabilities.

## Quality Gate

Run the project quality gate before completing implementation tasks:

```bash
./scripts/check-quality.sh
```

The script is intentionally tolerant while the monorepo is being bootstrapped: it skips backend/frontend checks until the relevant build files exist.

## Local Infrastructure

Copy `.env.example` to `.env` when local overrides are needed, then start infrastructure services:

```bash
docker compose up -d postgres redis minio
```
