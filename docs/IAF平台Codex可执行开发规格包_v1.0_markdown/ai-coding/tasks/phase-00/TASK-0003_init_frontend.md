# TASK-0003 初始化 React 前端工程

## 1. 任务目标

初始化 IAF 前端工程，采用 React + TypeScript + Ant Design Pro/ProComponents，建立统一布局、路由、request client、权限占位和模块目录。

## 2. 必须先阅读

- AGENTS.md
- ai-coding/rules/03_frontend_rules.md
- ai-coding/rules/05_api_rules.md

## 3. 目录结构

```text
frontend/src/
  app/
  layouts/
  routes/
  services/
  components/
  permissions/
  modules/
    platform/
    manufacturing/
    wms/
  designers/
  generated/
```

## 4. 必须实现

- 基础布局。
- 登录页占位。
- 首页 Dashboard 占位。
- request client。
- API 错误处理。
- 权限按钮组件 PermissionButton。
- 状态按钮组件 StatusActionButton。

## 5. 测试/检查

- npm run typecheck。
- npm run lint。
- npm run build。

## 6. 验收标准

- 前端可启动。
- 首页可访问。
- request client 存在。
- PermissionButton 存在。
- 构建通过。
