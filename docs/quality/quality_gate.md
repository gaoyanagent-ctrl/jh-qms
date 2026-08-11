# 测试与质量门禁

## 1. 本地质量门禁

每次任务完成前必须执行：

```bash
./scripts/check-quality.sh
```

建议脚本包含：

```bash
cd backend && ./mvnw test
cd frontend && npm run typecheck && npm run lint && npm run build
```

## 2. 后端质量标准

| 类型 | 标准 |
|---|---|
| 编译 | 必须通过 |
| 单元测试 | 必须通过 |
| 集成测试 | 核心流程必须通过 |
| Migration | 空库可执行 |
| OpenAPI | 新接口可见 |
| 权限 | 写操作必须校验 |
| 状态 | 状态变更必须走状态机 |
| Code Map | 结构、类、API、数据库、前端路由变化必须更新 |

## 3. 前端质量标准

| 类型 | 标准 |
|---|---|
| TypeScript | typecheck 通过 |
| ESLint | 无 blocking error |
| Build | 通过 |
| 页面 | 主流程可访问 |
| 权限 | 按钮按权限和状态显示 |

## 4. 代码审查门禁

合并前必须通过 Code Reviewer Skill 审查：

- 架构分层。
- 模块边界。
- 权限安全。
- 状态机一致性。
- 审批集成。
- 库存事务一致性。
- 测试覆盖。
- Code map 是否同步。

## 5. 验收清单

每个任务必须在最终回复中逐项填写：

```text
[ ] 已读取 AGENTS.md
[ ] 已读取相关 rules
[ ] 已读取相关 code map
[ ] 已读取相关 module spec
[ ] 已完成代码修改
[ ] 已新增/更新 code map，或说明无需更新
[ ] 已新增/更新 migration
[ ] 已新增/更新测试
[ ] 后端测试通过
[ ] 前端构建通过
[ ] 权限点已处理
[ ] 菜单/路由已处理
[ ] 自检无 blocking issue
```
