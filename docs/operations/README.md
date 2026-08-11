# Operations, Handoffs, and Runbooks

`docs/operations/` is not a canonical task directory.

Use this directory for operational records and temporary guidance:

- `HANDOFF-*.md`: handoff plan for another agent.
- `RUNBOOK-*.md`: environment, startup, deployment, or maintenance procedure.
- `INCIDENT-*.md`: troubleshooting or incident record.
- `NOTE-*.md`: local environment note.

Canonical executable tasks must live under `ai-coding/tasks/`.

Current platform foundation runbooks:

- `RUNBOOK-platform-foundation-productization.md`: implementation delivery checklist for the platform foundation permission/menu/role package, design acceptance, regression checks, and release handoff.
- `RUNBOOK-platform-foundation-troubleshooting.md`: diagnosis paths for login, menu visibility, permission, configuration, theme, and template validation problems.
- `platform-foundation-document-index.md`: single entrypoint for RC1 release, implementation, validation, and maintenance documents.
- `platform-foundation-release-checklist.md`: RC1 scope freeze, release gates, defect severity, compatibility rules, design governance, and backlog categories.
- `platform-foundation-known-issues.md`: P0/P1/P2/P3 issue ledger and deferral record.
- `platform-foundation-smoke-test.md`: manual and scripted smoke test procedure for existing platform APIs.
- `platform-foundation-feedback-log.md`: RC1 stabilization feedback intake, triage ledger, UX review, and Agent template replay record.
- `platform-foundation-stabilization-plan.md`: stabilization patch policy, metrics, compatibility guardrails, and business-domain entry checklist.
- `platform-foundation-runbook-review.md`: runbook replay notes, local startup findings, and reproducibility criteria.
- `platform-foundation-next-backlog.md`: classified platform-layer enhancement backlog after RC1 stabilization.

If a file in this directory describes work to implement, treat it as a handoff or plan. Before assigning it as a formal task, convert the actionable scope into `ai-coding/tasks/**/*.md`.

Do not create `TASK-*.md` files in this directory. The `TASK-*` prefix is reserved for `ai-coding/tasks/`.
