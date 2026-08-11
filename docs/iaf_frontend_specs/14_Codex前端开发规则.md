# Codex 前端开发规则

## 1. 总规则

Codex 开发前端时，必须先读取：

```text
AGENTS.md
ai-coding/rules/frontend-rules.md
docs/frontend/*
当前 TASK-FE-xxxx.md
```

## 2. 技术栈锁定

- React + TypeScript
- pnpm workspace
- Vite
- PC 使用 Ant Design + ProComponents
- 移动使用 Ant Design Mobile
- i18next
- TanStack Query
- Zustand
- ECharts

未经架构任务批准，不得引入替代框架。

## 3. 目录规则

- PC 页面放 `apps/pc-admin/src/modules/**`
- 移动页面放 `apps/mobile-work/src/modules/**`
- 共享组件放 `packages/**`
- 业务类型放 `packages/domain-types`
- API 客户端放 `packages/api-client`

## 4. 禁止事项

- 禁止页面直接 axios / fetch。
- 禁止硬编码中文。
- 禁止硬编码颜色。
- 禁止硬编码权限判断。
- 禁止重复实现已有 selector / status / timeline 组件。
- 禁止把 PC 页面和移动页面写在同一个 app。
- 禁止绕过 table-engine / form-engine 写标准 CRUD 页面。
- 禁止把服务端状态放进 Zustand。
- 禁止将无权限字段传给 AI 助手上下文。

## 5. 每个页面的最低交付项

- 页面组件
- API 文件
- hooks 文件
- 类型定义
- i18n key
- 权限控制
- 状态控制
- loading / error / empty 状态
- 基础测试
- PageAIContext，复杂页面必须提供

## 6. 自检清单

提交前必须确认：

```text
pnpm lint 通过
pnpm typecheck 通过
pnpm test 通过
pnpm build 通过
页面无硬编码中文
页面无直接 axios
权限按钮使用 PermissionButton
状态标签使用 StatusTag
字段权限使用 FieldPermissionWrapper
标准列表支持列偏好和查询保存
```


## Codex 执行要求

- 开始实现前必须读取 `AGENTS.md`、`ai-coding/rules/frontend-rules.md` 和本规范。
- 不允许绕过 `packages/api-client` 直接调用 HTTP。
- 不允许在页面中硬编码中文、颜色、权限判断、状态判断。
- 新增页面必须同时补齐类型定义、API hooks、i18n key、权限点、基础测试和 Story/示例数据。
- 复杂页面必须输出 `PageAIContext`，供 AI 助手解释页面状态和下一步动作。
