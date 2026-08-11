# Codex 任务模板

## 1. 新功能任务模板

```markdown
# Task: <任务名称>

## Objective

<说明要完成什么业务能力，不超过 5 句话。>

## Business Context

- Module: <platform / manufacturing / wms / srm / mes / qms>
- Business Object: <对象名称>
- Scenario: <业务场景>

## Scope

Must include:
- <功能点 1>
- <功能点 2>

Must not include:
- <明确排除项 1>
- <明确排除项 2>

## Inputs

Read first:
- AGENTS.md
- docs/architecture/*
- ai-coding/rules/*
- dsl/<related>/*

## Required Changes

- DSL: <yes/no + 文件>
- Database migration: <yes/no>
- Backend: <yes/no + 模块>
- Frontend: <yes/no + 页面>
- Permission: <yes/no + 权限点>
- Tests: <yes/no + 测试类型>
- Docs: <yes/no>

## Acceptance Criteria

1. <验收条件 1>
2. <验收条件 2>
3. <验收条件 3>

## Test Requirements

- Unit tests:
- Integration tests:
- API tests:
- Frontend tests:
- E2E tests:

## Execution Rules

1. First inspect current implementation.
2. Produce an implementation plan before editing.
3. Make minimal coherent changes.
4. Run relevant tests.
5. Fix failures.
6. Final response must include changed files, tests run, risks, and follow-up items.
```

## 2. Bug 修复任务模板

```markdown
# Bugfix Task: <问题名称>

## Symptom

<现象>

## Expected Behavior

<期望行为>

## Reproduction Steps

1. ...
2. ...

## Suspected Area

- Module:
- API:
- Page:
- Database:

## Constraints

- Do not change public API unless necessary.
- Add regression tests.
- Keep fix minimal.

## Acceptance Criteria

1. Bug is reproduced by test before fix when possible.
2. Test passes after fix.
3. No unrelated refactor.
```

## 3. 重构任务模板

```markdown
# Refactor Task: <重构名称>

## Goal

<重构目标>

## Non-functional Constraints

- Behavior must remain unchanged.
- Public APIs must remain compatible unless explicitly approved.
- Existing tests must pass.

## Refactor Scope

Included:
- ...

Excluded:
- ...

## Safety Requirements

- Add characterization tests before refactor if coverage is insufficient.
- Keep commits small.
- Report any behavior change.
```
