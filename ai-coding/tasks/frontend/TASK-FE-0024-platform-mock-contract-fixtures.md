# TASK-FE-0024 平台与业务 Mock 数据契约与集成

## 1. 任务目标

定义前端与后端的契约 Mock 数据，包括用户、组织、角色权限以及首期 WMS 收货单数据，并在应用启动时，依据环境变量 `VITE_IAF_MOCK_API=true` 将 Mock 数据源与 Mock API 适配器注入到全局 `apiClient` 实例中。

## 2. 必须先阅读

- AGENTS.md
- ai-coding/rules/03_frontend_rules.md
- docs/operations/HANDOFF-FE-CONTRACT-MOCK-FIRST_frontend_development_plan.md

## 3. 业务范围

本任务实现：

- 建立 `frontend/mock-data/` 目录：
  - `platform/users.ts`：实现用户增、删、改、查、密码重置、禁用的内存 Mock handlers。
  - `platform/orgs.ts`：实现组织架构树查询、增、改的内存 Mock handlers。
  - `platform/roles.ts`：实现角色列表查询、增、改、权限分配的内存 Mock handlers。
  - `wms/receiptOrders.ts`：实现收货单列表、明细和创建的 Mock 契约。
  - `register.ts`：集中将上述数据 Mock 注册到 `MockApiAdapter`。
- 修改 `frontend/apps/pc-admin/src/api/client.ts`：
  - 检查环境变量 `import.meta.env.VITE_IAF_MOCK_API === 'true'`。
  - 若为 `true`，实例化 `MockApiAdapter` 并作为 `mockAdapter` 注入 `createApiClient`。
  - 采用动态导入方式加载 `frontend/mock-data/register` 以完成初始化注册，避免在不需要 Mock 时（Mock=false）打入生产包。

本任务不实现：

- WMS 真正的后端接口逻辑和库存扣减业务逻辑。

## 4. 需要新增/修改的文件

前端：

```text
frontend/apps/pc-admin/src/api/client.ts
frontend/mock-data/register.ts [NEW]
frontend/mock-data/platform/users.ts [NEW]
frontend/mock-data/platform/orgs.ts [NEW]
frontend/mock-data/platform/roles.ts [NEW]
frontend/mock-data/wms/receiptOrders.ts [NEW]
```

## 5. 验收标准

- 在启用 `VITE_IAF_MOCK_API=true` 时，用户管理、组织管理、角色管理页面全部数据由 Mock 在内存中托管。
- 新增、修改和修改状态的操作能在当前会话（Session）的内存中真实生效（如新增用户能立刻在列表中刷新查出）。
- 离线/空数据/出错页面等异常模拟可以被 Mock 覆盖。
- 当 `VITE_IAF_MOCK_API=false` 时，系统依旧走 Proxy 的真实 HTTP 调用。
