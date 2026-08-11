# API 与状态管理规范

## 1. API 调用原则

所有 HTTP 调用必须经过 `packages/api-client`。页面不得直接使用 axios、fetch 或第三方请求库。

## 2. api-client 结构

```text
packages/api-client/
  request.ts
  error-handler.ts
  auth-interceptor.ts
  retry-policy.ts
  types.ts
```

## 3. 模块 API 结构

```text
modules/wms/receipt/
  api.ts
  hooks.ts
  types.ts
```

页面调用 hooks，不直接调用 api：

```tsx
const { data, isLoading } = useReceiptOrderPage(query);
const submitMutation = useSubmitReceiptOrder();
```

## 4. 状态分类

| 状态类型 | 管理方式 |
|---|---|
| 服务端状态 | TanStack Query |
| 全局 UI 状态 | Zustand |
| 表单状态 | Ant Design Form |
| 临时页面状态 | useState |
| 权限状态 | auth / permissions package |
| 主题状态 | theme package |
| 多语言状态 | i18next |
| 离线队列状态 | offline-runtime |

## 5. 禁止事项

- 禁止把服务端列表数据放 Zustand。
- 禁止页面直接读写 localStorage。
- 禁止页面直接处理 token 刷新。
- 禁止多个页面共享临时表单状态。
- 禁止在业务组件中直接拼接 URL。

## 6. 错误处理

统一错误处理必须识别：

- 认证过期
- 权限不足
- 业务错误
- 参数校验错误
- 后端异常
- 网络异常
- 离线队列提交失败

业务错误应显示 messageKey 翻译结果和错误码。
