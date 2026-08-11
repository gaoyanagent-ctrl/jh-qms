# TASK-FE-0021 修复前端质量脚本

## 1. 任务目标

修复并简化 `frontend/package.json` 中的根脚本，消除在不同终端环境下可能由于 `$PWD` 变量导致的任务执行异常，确保质量门可在项目任意目录被正确调用。

## 2. 必须先阅读

- AGENTS.md
- ai-coding/rules/03_frontend_rules.md
- docs/quality/quality_gate.md

## 3. 业务范围

本任务实现：

- 简化 `frontend/package.json` 中 `typecheck`、`lint`、`test`、`build` 的指令，改用标准 `pnpm` 递归执行。
- 确保 `./scripts/run-frontend-checks.sh` 脚本和根质量门 `./scripts/check-quality.sh` 能够完美通过。

本任务不实现：

- 任何具体的业务页面或组件的逻辑变更。

## 4. 需要新增/修改的文件

前端：

```text
frontend/package.json
```

## 5. 验收标准

- 简化后的 package.json 脚本符合 pnpm workspace 标准指令。
- 运行 `./scripts/run-frontend-checks.sh` 顺利通过，无报错。
