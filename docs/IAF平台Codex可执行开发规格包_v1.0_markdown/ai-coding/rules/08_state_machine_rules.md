# 状态机开发规则

## 1. 状态变更入口

所有业务单据状态变更必须通过 StateMachineService。

## 2. 状态分层

- document_status。
- approval_status。
- execution_status。
- posting_status。
- settlement_status。

## 3. 状态迁移定义

每个迁移必须定义：

- from。
- action。
- to。
- guard condition。
- before action。
- after action。

## 4. 审计

每次状态迁移必须记录状态迁移日志。
