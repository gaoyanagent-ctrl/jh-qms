# AI Coding Rules

This directory is the executable rule path for Codex, Cursor, Claude Code, and other AI coding tools.

The original governance documents remain in `docs/codex_rules/`. Files here are normalized task execution rules split by concern:

- project engineering
- backend
- frontend
- database
- API
- permission
- workflow
- state machine
- rule engine
- testing
- code quality
- code map maintenance
- agent parallel work, worktree, review, and merge flow

Always read `AGENTS.md` before these rules.

## Directory Boundary

This directory is the rule source, not a task source and not a specification archive.

- Put executable Agent rules here.
- Put formal tasks in `ai-coding/tasks/`.
- Put architecture, frontend, and module specifications in `docs/architecture/`, `docs/frontend/`, and `docs/module-specs/`.
- Put handoff plans and runbooks in `docs/operations/`.

If a `docs/*` specification conflicts with a rule here, follow the rule here unless an ADR states otherwise.
