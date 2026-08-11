# IAF 前端工程与体验规范说明

本规范包用于指导 IAF 前端平台的产品体验设计、工程架构、组件体系、移动作业能力、低代码配置能力和 Codex 前端开发执行。

## 使用方式

本目录是前端规格说明目录，不是可执行规则目录，也不是正式任务目录。

权威关系：

- 前端可执行规则以 `ai-coding/rules/03_frontend_rules.md` 为准。
- 正式前端任务以 `ai-coding/tasks/frontend/` 为准。
- 本目录说明前端架构、体验、组件、交互、移动作业、主题、i18n、权限、状态、低代码和 AI 助手等规格。
- `14_Codex前端开发规则.md` 是规格包中的规则索引；如果与 `ai-coding/rules/03_frontend_rules.md` 冲突，以 `ai-coding/rules/03_frontend_rules.md` 为准。
- `15_前端任务拆分清单.md` 是路线图和拆分总览；具体执行必须使用 `ai-coding/tasks/frontend/` 中的任务文件。
- `16_平台管理页面交互规范.md`、`17_平台管理视觉设计规范.md`、`18_复杂视图与设计器规范.md`、`19_平台应用外壳与导航规范.md` 是 Platform Foundation 阶段的设计方案沉淀，执行 `TASK-0204`、`TASK-0207`、`TASK-0209`、`TASK-0211`、`TASK-0212` 前必须阅读。

建议项目结构：

```text
frontend/
  apps/
  packages/

docs/frontend/
  01_frontend_architecture.md
  ...

ai-coding/rules/
  03_frontend_rules.md

ai-coding/tasks/frontend/
  TASK-FE-0001-init-frontend-workspace.md
  ...
```

## 适用范围

- PC 管理端与业务作业端
- 移动 H5 / PDA 作业端
- 大屏看板端
- 供应商门户预留
- 多主题、多语言、权限、状态、低代码列表/表单、AI 助手
- Codex CLI / Cursor / Claude Code 生成前端代码

## 核心原则

1. 前端是 IAF 的核心子系统，不是普通页面集合。
2. PC 端和移动端分应用，公共能力放 packages。
3. 标准页面配置驱动，复杂页面源码开发但必须复用组件。
4. i18n、主题、权限、状态、字段权限从第一版内置。
5. Codex 只能按模板、组件、API hooks 和任务规格生成代码。
