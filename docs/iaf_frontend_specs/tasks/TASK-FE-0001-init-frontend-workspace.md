# TASK-FE-0001 初始化 frontend pnpm workspace

## 1. 任务目标

初始化 IAF 前端 monorepo，建立 apps 和 packages 目录，为 PC、移动端、共享包开发打基础。

## 2. 必须先阅读

- AGENTS.md
- docs/frontend/01_前端总体架构.md
- docs/frontend/14_Codex前端开发规则.md

## 3. 交付范围

新增：

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

## 4. 技术要求

- 使用 pnpm workspace。
- TypeScript strict 开启。
- 每个 package 必须有 package.json、src/index.ts、tsconfig.json。
- 不实现具体业务页面。

## 5. 验收标准

- `pnpm install` 成功。
- `pnpm typecheck` 成功。
- workspace 包引用能正常解析。
- apps 不依赖彼此。
- packages 不依赖 apps。
