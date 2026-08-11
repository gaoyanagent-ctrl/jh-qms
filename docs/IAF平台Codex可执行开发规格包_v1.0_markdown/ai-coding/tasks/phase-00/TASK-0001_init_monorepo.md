# TASK-0001 初始化 IAF Monorepo

## 1. 任务目标

初始化 IAF 项目仓库结构，建立后端、前端、文档、AI coding 规范、脚本目录，为后续模块开发提供稳定工程骨架。

## 2. 必须先阅读

- AGENTS.md
- ai-coding/rules/01_project_rules.md

## 3. 业务范围

本任务实现：

- 根目录结构。
- README。
- .env.example。
- docker-compose 基础框架。
- scripts 目录。
- docs 目录。
- ai-coding 目录。

本任务不实现：

- 具体业务模块。
- 具体数据库表。
- 登录功能。

## 4. 目录结构

必须创建：

```text
iaf/
  AGENTS.md
  README.md
  docker-compose.yml
  .env.example
  docs/
    architecture/
    adr/
    module-specs/
    quality/
  ai-coding/
    rules/
    skills/
    prompts/
    tasks/
    reviews/
    examples/
  backend/
  frontend/
  scripts/
  tools/
```

## 5. 脚本

新增：

```text
scripts/check-quality.sh
scripts/run-backend-tests.sh
scripts/run-frontend-checks.sh
```

脚本可以先包含占位命令，但必须有清晰 TODO。

## 6. 验收标准

- 目录结构完整。
- README 说明项目定位和启动方式。
- AGENTS.md 存在。
- check-quality.sh 存在且可执行。
- 无业务代码。
