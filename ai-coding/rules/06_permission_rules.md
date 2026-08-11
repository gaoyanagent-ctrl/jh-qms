# 权限设计规则

## 1. 权限类型

必须支持：

- 菜单权限。
- 按钮权限。
- API 权限。
- 数据权限。
- 字段权限。
- 审批节点字段权限。
- 单据操作权限。
- 外部用户权限。
- 组织隔离权限。

## 2. 权限编码

```text
module:object:action
```

## 3. 数据权限范围

```text
ALL
TENANT
COMPANY
BUSINESS_UNIT
PLANT
DEPARTMENT
WAREHOUSE
SELF
CUSTOM
SUPPLIER_SELF
CUSTOMER_SELF
```

## 4. 字段权限

字段权限包括：

- visible。
- hidden。
- readonly。
- editable。
- required。

## 5. 后端强校验

所有敏感接口必须后端校验权限。前端隐藏按钮只是用户体验。
