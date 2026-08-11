# 锦恒 QMS

锦恒 QMS（JH QMS）是面向制造质量管理的一体化系统。本仓库从 IAF 工业应用基础框架初始化，但具有独立的代码、分支、版本与交付流程；IAF 仓库仅作为上游参考，不承载本项目的开发提交。

This repository is organized as a monorepo for backend, frontend, DSL, deployment, tests, documentation, and AI development governance.

一期总体架构和详细设计见 [`docs/product/锦恒QMS系统总体架构与一期详细设计说明书_Codex开发版.md`](docs/product/锦恒QMS系统总体架构与一期详细设计说明书_Codex开发版.md)，分阶段开发计划见 [`docs/operations/HANDOFF-jinheng-qms-phase-plan.md`](docs/operations/HANDOFF-jinheng-qms-phase-plan.md)。

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

## JH QMS 一期开发路线

1. 建立独立仓库、工程基线和 QMS 模块边界。
2. 建设零件、图纸、图纸版本及审计追踪等工程数据基础。
3. 建设检验标准、检验计划、来料/过程/成品检验闭环。
4. 建设不合格品、评审、处置、返工返修及 CAPA 闭环。
5. 建设质量追溯、统计分析、看板、报表和外部系统集成。

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
