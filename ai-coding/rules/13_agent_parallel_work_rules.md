# Agent 并行开发与合并规则

## 1. 目的

规范多个 Agent 同时开发时的工作区、分支、审核和合并流程，避免不同任务互相污染、未审核代码进入主干、质量门结果不可追踪。

## 2. 基本原则

- `main` 是集成分支，不是 Agent 日常开发分支。
- 每个正式任务必须使用独立分支。
- 所有后续 Agent 开发任务必须使用独立 git worktree；共享仓库 checkout 只用于集成、检查、创建 worktree 或用户明确指定的一次性操作。
- 多 Agent 并行开发时，每个 Agent 必须使用独立 git worktree。
- 所有任务分支进入 `main` 必须通过 Pull Request。
- 合并到 `main` 前必须通过代码审核和质量门。
- 只有集成负责人、仓库维护者或被明确授权的 Agent 可以在 Pull Request 中执行合并。
- Agent 禁止把任务分支直接 merge、rebase、cherry-pick 或 push 到 `main`，除非仓库负责人明确记录紧急例外。

## 3. 分支命名

正式任务分支：

```text
feature/TASK-xxxx-short-name
```

缺陷修复分支：

```text
bugfix/TASK-xxxx-short-name
```

实验性分支：

```text
experiment/TASK-xxxx-short-name
```

实验性分支不得直接合并 `main`，必须整理为正式 `feature/*` 或 `bugfix/*` 分支后再审核。

## 4. Worktree 规则

所有后续开发任务：

- 必须使用独立 worktree。
- 必须使用独立任务分支。
- 禁止直接在共享仓库 checkout 中开始新功能、缺陷修复、跨模块调整、数据库 migration、前后端联动或文档规则以外的代码修改。
- 共享仓库 checkout 可用于查看状态、创建 worktree、执行集成合并、处理用户明确要求的当前目录一次性操作；此类例外必须在最终报告中说明。

多 Agent 并行任务：

- 必须使用独立 worktree。
- 必须使用独立分支。
- 禁止多个 Agent 同时在同一工作区修改代码。
- 禁止多个 Agent 直接在 `main` 上并行开发。

推荐 worktree 目录：

```text
../iaf-worktrees/TASK-xxxx-short-name
```

创建示例：

```bash
git fetch origin
git worktree add ../iaf-worktrees/TASK-0205-menu-permission -b feature/TASK-0205-menu-permission origin/main
```

在已有分支上继续任务示例：

```bash
git fetch origin
git worktree add ../iaf-worktrees/TASK-0205-menu-permission feature/TASK-0205-menu-permission
```

清理示例：

```bash
git worktree remove ../iaf-worktrees/TASK-0205-menu-permission
git branch -d feature/TASK-0205-menu-permission
```

## 5. Agent 执行流程

每个 Agent 开始任务前：

1. 确认任务文件位于 `ai-coding/tasks/`。
2. 从最新 `origin/main` 创建任务分支。
3. 创建或进入该任务的独立 worktree。
4. 阅读 `AGENTS.md`、相关规则、任务文件、code map 和模块规格。
5. 输出简短计划后再修改代码。

开发中：

1. 只修改任务范围内文件。
2. 不回滚其他 Agent 或用户的无关变更。
3. 新增或修改 API、数据库、路由、模块、公共方法后同步更新 code map。
4. 任务内可做小提交，提交必须可审核。

完成时：

1. 运行相关测试。
2. 优先运行 `./scripts/check-quality.sh`。
3. 在最终报告中说明测试结果、code map、权限、数据库影响和风险。
4. 推送任务分支。
5. 创建或更新 Pull Request，等待审核；不得直接合并 `main`。

## 6. 审核流程

审核 Agent 必须：

- 从目标任务分支与 `main` 的 diff 开始审查。
- 按 `ai-coding/rules/11_code_quality_rules.md` 输出 Blocking/Major/Minor。
- 检查架构边界、权限、数据库 migration、测试、code map。
- 对前端任务检查 `ai-coding/rules/03_frontend_rules.md`。
- 对后端任务检查模块边界和权限注解。
- Blocking issue 未修复前禁止合并。

审核通过条件：

- 无 Blocking issue。
- Major issue 已修复，或有明确 ADR/任务说明接受风险。
- 质量门通过，或有合理且可追踪的不能运行说明。

## 7. Pull Request 合并流程

所有进入 `main` 的变更必须走 Pull Request。

PR 创建条件：

1. 任务分支已推送到远端。
2. 分支基于最新 `origin/main`，或 PR 中明确说明落后原因和处理计划。
3. PR 描述包含任务目标、变更范围、测试结果、质量门结果、code map 影响、权限影响、数据库 migration 影响和已知风险。
4. 试验性功能默认关闭，或有清晰 feature flag。

合并前：

1. 同步最新 `main`。
2. 处理冲突。
3. 重新运行质量门。
4. 确认 code map 与实际实现一致。
5. 确认没有生成产物、临时文件、无关修改。
6. 确认 PR 审核通过，且无未处理 Blocking issue。

合并方式：

- 默认使用 Squash merge，保持 `main` 历史按任务收敛。
- 需要保留多提交上下文时，可以使用普通 merge commit，但必须在 PR 中说明原因。
- 禁止本地直接 fast-forward 或直接 merge 到 `main` 后 push。
- 合并后删除任务分支和 worktree。

禁止：

- 未审核直接合并。
- 质量门失败仍合并。
- 绕过 Pull Request 合并到 `main`。
- 从 `experiment/*` 直接合并 `main`。
- 把多个无关任务混在同一个分支。

紧急例外：

- 只有仓库负责人可以批准绕过 PR。
- 例外必须在最终报告或运维记录中写明原因、风险、执行人、提交哈希、验证结果和后续补 PR/补审核计划。
- 紧急修复完成后必须补充 PR 或审核记录，保证审计链完整。

## 8. 当前仓库例外

如果用户明确要求当前 Agent 直接在当前工作区完成一次性操作，可以在当前工作区执行，但最终报告必须说明：

- 未使用独立 worktree 的原因。
- 是否影响其他正在运行的 Agent。
- 是否仍满足独立提交、审核和质量门要求。

新开发任务、大规模功能、跨模块任务、数据库 migration、前后端联动任务不得使用该例外。
