# IAF 前端工程与体验规范说明

本规范包用于指导 IAF 前端平台的产品体验设计、工程架构、组件体系、移动作业能力、低代码配置能力和 Codex 前端开发执行。

## 使用方式

建议放入项目仓库：

```text
frontend/
  apps/
  packages/

docs/frontend/
  01_frontend_architecture.md
  ...

ai-coding/rules/
  frontend-rules.md

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
