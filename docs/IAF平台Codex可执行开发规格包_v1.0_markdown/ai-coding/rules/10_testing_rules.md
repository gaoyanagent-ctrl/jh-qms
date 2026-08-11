# 测试规则

## 1. 后端测试

必须包含：

- Domain 单元测试。
- ApplicationService 测试。
- Controller 测试。
- Repository/Migration 测试。
- 核心流程集成测试。

## 2. 前端测试

至少保证：

- TypeScript typecheck 通过。
- ESLint 通过。
- build 通过。
- 核心页面 smoke test。

## 3. 覆盖重点

- 权限。
- 状态流转。
- 审批状态同步。
- 规则命中。
- 库存过账。
- 并发与幂等。

## 4. 禁止

禁止为了让测试通过删除业务校验。
