# AI Coding Tasks

`ai-coding/tasks/` is the only canonical executable task directory.

Agents should implement work from this tree. A task file in this directory is expected to define scope, required reading, implementation requirements, tests, permissions, database impact, code map updates, and acceptance criteria.

## Directory Use

- `TASK_TEMPLATE.md`: standard task template.
- `phase-*/*`: backend, platform, WMS, or cross-cutting milestone tasks.
- `frontend/*`: frontend task specifications.

## What Does Not Belong Here

- Handoff plans for another agent. Put those in `docs/operations/HANDOFF-*.md`.
- Runbooks or local environment notes. Put those in `docs/operations/RUNBOOK-*.md`.
- Architecture or UX specifications. Put those in `docs/architecture/`, `docs/module-specs/`, or `docs/frontend/`.

If a handoff plan creates new executable work, convert it into a task file here before assigning it as a canonical task.
