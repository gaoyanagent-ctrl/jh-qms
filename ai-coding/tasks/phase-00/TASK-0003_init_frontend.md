# TASK-0003 初始化 React 前端工程

## 1. 任务目标

初始化 IAF 前端 pnpm workspace，采用 React + TypeScript + Vite，建立 PC、移动端和共享 packages 基础结构。

本任务应优先参考更细的前端任务：

- `ai-coding/tasks/frontend/TASK-FE-0001-init-frontend-workspace.md`
- `docs/frontend/01_前端总体架构.md`
- `docs/frontend/14_Codex前端开发规则.md`

## 2. 必须先阅读

- AGENTS.md
- ai-coding/rules/03_frontend_rules.md
- ai-coding/rules/05_api_rules.md
- docs/frontend/00_README.md
- docs/frontend/01_前端总体架构.md
- docs/frontend/14_Codex前端开发规则.md

## 3. 目录结构

```text
frontend/
  package.json
  pnpm-workspace.yaml
  tsconfig.base.json
  apps/
    pc-admin/
    mobile-work/
  packages/
    api-client/
    auth/
    permissions/
    i18n/
    theme/
    domain-types/
    ui-core/
    ui-business/
    table-engine/
    form-engine/
    scan-runtime/
    offline-runtime/
    ai-assistant/
```

## 4. 必须实现

- pnpm workspace。
- TypeScript strict。
- `apps/pc-admin` Vite React app 占位。
- `apps/mobile-work` Vite React app 占位。
- 每个 package 必须有 `package.json`、`src/index.ts`、`tsconfig.json`。
- `packages/api-client` 占位 request client 和统一错误类型。
- `packages/permissions` 占位 `PermissionButton` / `PermissionGuard`。
- `packages/theme` 占位 ThemeProvider 和 token 类型。
- `packages/i18n` 占位中英文资源结构。
- 不实现具体业务页面。

## 5. 测试/检查

- pnpm install。
- pnpm typecheck。
- pnpm lint。
- pnpm build。
- ./scripts/check-quality.sh。

## 6. 验收标准

- workspace 包引用能正常解析。
- apps 不依赖彼此。
- packages 不依赖 apps。
- pc-admin 可启动。
- mobile-work 可启动。
- api-client、permissions、theme、i18n package 存在。
- 构建通过。
