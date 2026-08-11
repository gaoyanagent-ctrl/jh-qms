# 前端开发规则

## 1. 技术栈

- React。
- TypeScript。
- Ant Design Pro / ProComponents。
- ECharts。
- 统一 request client。

## 2. 模块目录

```text
src/modules/{module}/{feature}/
  api.ts
  types.ts
  pages/
  components/
  hooks/
```

## 3. 页面规则

列表页必须支持：

- 查询。
- 分页。
- 状态筛选。
- 权限按钮。

表单页必须支持：

- 必填校验。
- 明细行编辑。
- 只读/隐藏字段。
- 提交前二次校验。

详情页必须显示：

- 基本信息。
- 明细。
- 状态。
- 操作记录。
- 审批信息，按需。

## 4. 权限规则

按钮显示由权限和状态共同决定。前端权限不能替代后端权限。

## 5. 类型规则

禁止大量使用 any。API 返回类型必须定义在 types.ts。

## 6. 错误处理

API 错误必须通过统一错误处理组件或 message 展示。
