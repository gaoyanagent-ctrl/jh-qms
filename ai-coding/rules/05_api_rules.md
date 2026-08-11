# API 设计规则

## 1. 路径

```text
/api/{module}/{resources}
```

## 2. HTTP 方法

| 方法 | 用途 |
|---|---|
| GET | 查询 |
| POST | 新增或动作 |
| PUT | 全量/主要修改 |
| PATCH | 局部修改，谨慎使用 |
| DELETE | 删除/作废，按业务语义决定 |

## 3. 动作接口

业务动作使用：

```text
POST /api/{module}/{resources}/{id}/{action}
```

示例：

```text
POST /api/wms/receipt-orders/{id}/submit
POST /api/wms/receipt-orders/{id}/confirm-receipt
```

## 4. 返回结构

统一返回：

```json
{
  "success": true,
  "code": "OK",
  "message": "",
  "data": {}
}
```

分页返回：

```json
{
  "records": [],
  "total": 0,
  "pageNo": 1,
  "pageSize": 20
}
```

## 5. 错误码

错误码格式：

```text
{MODULE}_{DOMAIN}_{ERROR}
```

示例：

```text
WMS_RECEIPT_INVALID_STATUS
PLATFORM_PERMISSION_DENIED
```
