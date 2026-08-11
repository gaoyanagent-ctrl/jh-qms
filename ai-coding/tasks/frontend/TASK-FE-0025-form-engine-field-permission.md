# TASK-FE-0025 表单配置引擎与字段权限控制

## 1. 任务目标

设计并实现标准表单配置引擎 `@iaf/form-engine`，支持根据 Schema 配置渲染表单、隐藏字段、只读渲染，并提供四种层级的字段权限细粒度限制（VISIBLE_EDITABLE、VISIBLE_READONLY、HIDDEN、MASKED），同时支持 Simple（简洁）和 Expert（专家）模式切换展示。

## 2. 必须先阅读

- AGENTS.md
- ai-coding/rules/03_frontend_rules.md
- docs/operations/HANDOFF-FE-CONTRACT-MOCK-FIRST_frontend_development_plan.md

## 3. 业务范围

本任务实现：

- 建立新共享包 `@iaf/form-engine`。
- 定义表单配置协议 `FormDefinition`，包含字段类型、分栏、默认值、必填和专家模式标记。
- 实现 `FieldPermissionWrapper` 包装器，支持以下四种字段鉴权处理：
  - `VISIBLE_EDITABLE`：正常输入。
  - `VISIBLE_READONLY`：使用只读 plain text 展示该项，不渲染输入框。
  - `HIDDEN`：表单彻底隐藏该项。
  - `MASKED`：按掩码模板展示（如手机号 `138****0000`）。
- 实现 `ViewModeFieldResolver` 支持简单（隐藏带有 `isExpertOnly` 标记的字段）和专家（展示所有字段）模式的实时切换渲染。
- 提供核心表单页面容器 `ConfigurableFormPage` 作为高阶组件，组装上述表单逻辑。
- 编写 `FieldPermissionWrapper.test.tsx` 单元测试。

## 4. 需要新增/修改的文件

前端：

```text
frontend/packages/form-engine/package.json [NEW]
frontend/packages/form-engine/tsconfig.json [NEW]
frontend/packages/form-engine/src/index.ts [NEW]
frontend/packages/form-engine/src/FormDefinition.ts [NEW]
frontend/packages/form-engine/src/FieldPermissionWrapper.tsx [NEW]
frontend/packages/form-engine/src/FieldPermissionWrapper.test.tsx [NEW]
frontend/packages/form-engine/src/ViewModeFieldResolver.ts [NEW]
frontend/packages/form-engine/src/FormSectionRenderer.tsx [NEW]
frontend/packages/form-engine/src/ConfigurableFormPage.tsx [NEW]
```

## 5. 验收标准

- 配置为 `VISIBLE_READONLY` 的字段，渲染为普通的 plain text。
- 配置为 `HIDDEN` 的字段不在 DOM 树中渲染。
- 配置为 `MASKED` 的字段根据指定的模板脱敏显示。
- 单元测试运行完全通过。
