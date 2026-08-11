# TASK-0207 主题、多语言与品牌配置

## 1. 任务目标

实现平台级主题、品牌和多语言配置能力，使前端不再只有简单默认 Ant Design 样式，而具备可配置、可扩展的企业管理系统设计体系。

## 2. 必须先阅读

- `AGENTS.md`
- `ai-coding/rules/03_frontend_rules.md`
- `docs/frontend/06_多主题设计规范.md`
- `docs/frontend/07_多语言设计规范.md`
- `docs/code-map/frontend.md`

## 3. 业务范围

本任务实现：

- 主题 token 分层。
- 明暗主题。
- 紧凑/标准密度。
- 品牌配置。
- 登录页视觉升级。
- 工作台视觉升级。
- 多语言资源后台模型和前端加载机制。
- 主题配置页面。
- 多语言资源管理页面。

本任务不实现：

- 大屏主题编辑器。
- 租户商业化套餐。

## 4. 后端设计

新增配置模型建议：

- `sys_theme_config`
- `sys_brand_config`
- `sys_i18n_resource`

## 5. API 清单

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/platform/theme/current` | 当前主题配置 |
| PUT | `/api/platform/theme/current` | 更新主题配置 |
| GET | `/api/platform/brand/current` | 当前品牌配置 |
| PUT | `/api/platform/brand/current` | 更新品牌配置 |
| GET | `/api/platform/i18n/resources` | 多语言资源查询 |
| PUT | `/api/platform/i18n/resources` | 更新多语言资源 |

## 6. 权限点

```text
platform:theme:view
platform:theme:update
platform:brand:view
platform:brand:update
platform:i18n:view
platform:i18n:update
```

## 7. 前端设计

- `packages/theme` 支持语义 token、状态 token、图表 token。
- `packages/i18n` 支持远程资源合并和 fallback。
- 登录页、工作台、平台管理页升级为统一视觉样板。

## 7.1 当前实现边界

本任务第一版已落地平台系统配置后端闭环和个人偏好同步主链路：

- 后端 `iaf-platform-system` 提供主题、品牌、i18n 资源、当前用户偏好 API。
- Flyway `V0004__platform_system_configuration.sql` 创建主题、品牌、i18n、用户偏好表并种子化权限。
- 前端新增 `systemConfigApi`，登录后通过 `/api/platform/preferences/me` 加载个人偏好，保存偏好时优先写后端。
- 浏览器 localStorage 仍作为离线/开发 fallback。
- mock 模式支持主题、品牌、i18n、用户偏好接口。

后续任务继续完善：

- 主题配置页面和品牌配置页面。
- i18n 资源管理页面和运行时远程资源合并。
- 登录前公共品牌配置读取策略。
- 租户级默认偏好和用户级覆盖规则。

## 8. 测试要求

- 主题切换。
- 多语言切换。
- 缺失 key fallback。
- 权限控制。

## 9. 验收标准

- 平台可配置品牌名、Logo、主色、主题模式。
- 前端页面视觉明显区别于默认 Ant Design 拼装。
- `./scripts/check-quality.sh` 通过。
