# 代码质量审核规则

## 1. Blocking Issues

以下问题必须阻塞合并：

- 绕过状态机改状态。
- 绕过库存服务改库存。
- 后端未做权限校验。
- 数据库字段缺少 migration。
- 代码无法编译。
- 核心业务无测试。
- 跨模块访问 infrastructure。
- 修改模块、类、API、数据库或前端路由后未更新 `docs/code-map/*`，且最终报告未说明无需更新的理由。

## 2. Major Issues

- Service 过大。
- 业务规则散落多处。
- API 命名不一致。
- 前端 any 过多。
- 错误码不规范。
- Code map 描述与实际代码职责、API 或模块依赖不一致。

## 3. Minor Issues

- 命名可读性不足。
- 注释缺失。
- 页面布局不够统一。

## 4. Review 输出格式

```text
Blocking Issues:
Major Issues:
Minor Issues:
Positive Findings:
Recommendation:
```
