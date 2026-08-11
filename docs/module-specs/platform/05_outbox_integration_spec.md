# Outbox 与集成中心详细设计

## 1. 目标

提供可靠事件和外部系统接口调用能力。第一版不引入 Kafka/NATS，使用数据库 Outbox + 调度器实现可靠异步处理。

## 2. Outbox 模型

### platform_outbox_message

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| tenant_id | bigint | 租户 |
| event_id | varchar(128) | 事件唯一 ID |
| event_type | varchar(128) | 事件类型 |
| aggregate_type | varchar(128) | 聚合类型 |
| aggregate_id | bigint | 聚合 ID |
| payload_json | jsonb | 事件内容 |
| status | varchar(32) | NEW/PROCESSING/SENT/FAILED |
| retry_count | int | 重试次数 |
| next_retry_at | timestamp | 下次重试 |
| created_at | timestamp | 创建时间 |

## 3. 集成中心模型

- IntegrationEndpoint。
- IntegrationAuthConfig。
- IntegrationMapping。
- IntegrationJob。
- IntegrationCallLog。

## 4. 第一版支持

- REST API。
- 数据库中间表。
- Excel 导入。
- 企微/钉钉/飞书消息通知。

## 5. API

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/platform/integration-endpoints | 接口列表 |
| POST | /api/platform/integration-endpoints | 新增接口 |
| POST | /api/platform/integration-endpoints/{id}/test | 测试接口 |
| GET | /api/platform/integration-call-logs | 调用日志 |
| POST | /api/platform/outbox/{id}/retry | 重试消息 |

## 6. 领域规则

- 业务事务内只写 outbox，不直接调用外部系统。
- Outbox 消息必须支持幂等。
- 调用失败必须可重试。
- 集成调用必须记录请求、响应、耗时和错误。
