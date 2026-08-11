# TASK-FE-0026 平台基础资料表单引擎迁移与测试

## 1. 任务目标

将平台用户和组织修改等场景的表单迁移或建立其表单引擎样板，在 `pc-admin` 中引入 `@iaf/form-engine` 模块，并配合 i18n 资源完成表单切换验证。

## 2. 必须先阅读

- AGENTS.md
- ai-coding/rules/03_frontend_rules.md
- ai-coding/tasks/frontend/TASK-FE-0025-form-engine-field-permission.md

## 3. 业务范围

本任务实现：

- 补充 i18n 语言包中关于专家模式 (`workspace.expertMode`) 的翻译词条。
- 将 `apps/pc-admin` 的 `package.json` 指向新的 `@iaf/form-engine`。
- 在 `packages/form-engine/` 编写单元测试和类型兼容检验，确保与 `pc-admin` 的组件整合。
- 在用户/组织创建的弹层表单或详情表单中迁移或测试该组件的集成效果。

## 4. 需要新增/修改的文件

前端：

```text
frontend/apps/pc-admin/package.json
frontend/packages/i18n/src/index.ts
```

## 5. 验收标准

- `apps/pc-admin` 能够引入 `ConfigurableFormPage` 并在测试中成功渲染。
- 构建打包完全成功。
