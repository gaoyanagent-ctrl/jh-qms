# TASK-FE-0023 契约优先与 Mock API 运行时

## 1. 任务目标

在 `@iaf/api-client` 共享包中，建立不依赖外部包（如 MSW）的纯前端轻量级 Mock Interceptor 运行时，支持对特定 Path Pattern 进行拦截并解析路径参数与查询参数，从而使前端能在后端未完成时进行契约开发。

## 2. 必须先阅读

- AGENTS.md
- ai-coding/rules/03_frontend_rules.md
- docs/frontend/11_API与状态管理规范.md

## 3. 业务范围

本任务实现：

- 建立 `MockResponse` 模型，定义请求成功与失败结构，使之符合后端通用返回 `Result<T>` 的格式。
- 建立 `MockRouteRegistry` 路由匹配表，支持将如 `/api/platform/users/:id` 的路由模式解析为匹配的正则，并提取其 URL path params。
- 建立 `MockApiAdapter` 负责匹配请求并分发到具体 handler，解析入参 body 和 query params。
- 扩展 `ApiClient` 类，使其在构造时能够可选接受 `mockAdapter`；并在 `request()` 动作发起前进行拦截。若匹配，则跳过 `fetch` 并直接返回 Mock 数据，支持模拟延时、空数据及 HTTP 401 触发 logout 等行为；若不匹配，则仍执行真实网络调用（即 Passthrough 穿透能力）。
- 编写 `MockRouteRegistry.test.ts` 验证路由解析与匹配逻辑是否准确。

本任务不实现：

- 具体的 mock 数据集或将 mocks 注入应用。

## 4. 需要新增/修改的文件

前端：

```text
frontend/packages/api-client/src/index.ts
frontend/packages/api-client/src/mock/MockResponse.ts [NEW]
frontend/packages/api-client/src/mock/MockRouteRegistry.ts [NEW]
frontend/packages/api-client/src/mock/MockRouteRegistry.test.ts [NEW]
frontend/packages/api-client/src/mock/MockApiAdapter.ts [NEW]
```

## 5. 验收标准

- `MockRouteRegistry` 能精确匹配 `/api/users/:id` 类型的模式，并能正确提取 `{ id: "123" }` 等路径参数。
- 当 `mockAdapter` 存在且匹配某路由时，`ApiClient` 不发送实际网络请求，但通过相同的 interface 交付数据。
- 单元测试运行完全通过。
