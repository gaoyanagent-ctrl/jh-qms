# TASK-FE-0018 建立 WMS 收货 PC 页面

## 1. 任务目标

基于 table-engine、form-engine、ui-business 和权限组件，实现 WMS 收货单 PC 列表、表单和详情页。

## 2. 页面范围

```text
apps/pc-admin/src/modules/wms/receipt/
  ReceiptOrderListPage.tsx
  ReceiptOrderFormPage.tsx
  ReceiptOrderDetailPage.tsx
  api.ts
  hooks.ts
  types.ts
  i18n.ts
```

## 3. 功能要求

- 列表支持查询、分页、状态筛选、列偏好保存。
- 表单支持主表字段、明细行、保存草稿、提交。
- 详情页支持状态摘要、业务动作、审批记录、操作日志、附件。
- 按钮受权限和状态共同控制。
- 页面提供 PageAIContext。

## 4. 权限点

```text
wms:receipt:view
wms:receipt:create
wms:receipt:update
wms:receipt:submit
wms:receipt:confirm
wms:receipt:cancel
wms:receipt:export
```

## 5. 验收标准

- 不直接 axios。
- 无硬编码中文。
- 标准列表走 table-engine。
- 标准表单走 form-engine。
- 状态标签使用 StatusTag。
- 权限按钮使用 PermissionButton。
- 构建、测试通过。
