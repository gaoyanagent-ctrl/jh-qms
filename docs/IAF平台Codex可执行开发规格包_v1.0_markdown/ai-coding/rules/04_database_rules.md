# 数据库设计规则

## 1. Migration

所有数据库变更必须使用 Flyway。

命名：

```text
V{版本号}__{说明}.sql
```

示例：

```text
V0001__init_platform_schema.sql
```

## 2. 通用字段

业务表必须包含：

```sql
id bigint primary key,
tenant_id bigint not null,
created_by bigint,
created_at timestamp not null,
updated_by bigint,
updated_at timestamp not null,
deleted boolean not null default false,
version int not null default 0,
ext_json jsonb
```

按需包含：

```sql
company_id bigint,
plant_id bigint,
department_id bigint,
warehouse_id bigint
```

## 3. 命名规则

- 表名小写下划线。
- 字段名小写下划线。
- 主表：模块_对象。
- 明细表：模块_对象_line。

## 4. 索引规则

常用查询字段必须建索引：

- tenant_id。
- company_id。
- plant_id。
- document_no。
- status。
- created_at。

## 5. 金额数量

金额、数量、重量、体积必须使用 decimal/numeric，不得使用 float/double。

## 6. JSON 扩展字段

ext_json 只能存低频扩展字段。核心查询字段必须是真实列。
