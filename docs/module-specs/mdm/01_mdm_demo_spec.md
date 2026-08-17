# MDM Demo 模块规格

## 边界

MDM 负责数据域、模型、字段、UI Schema、动态主数据记录及不可变数据版本。首期以物料模型验证“同一模型定义驱动查询、表格和表单保存”。

## 核心约束

- 模型编码和字段编码在租户内唯一。
- 已发布字段不物理删除。
- 动态属性保存前按字段定义校验必填、类型和枚举。
- 每次创建或修改产生完整版本快照。
- 更新必须携带 `expectedVersion`，冲突不得覆盖。
- 所有查询和写入按 tenant 隔离；写操作强制后端权限。

## 首期 API

- `GET /api/mdm/models`
- `GET /api/mdm/models/{modelCode}/schema`
- `GET /api/mdm/models/{modelCode}/records`
- `POST /api/mdm/models/{modelCode}/records`
- `PUT /api/mdm/models/{modelCode}/records/{id}`

## 模型设计器

- 新建模型先进入 `DRAFT`，字段与 UI Schema 仅草稿态可修改。
- 校验字段编码、重复项、数据类型、枚举选项和长度后方可发布。
- 发布创建不可变 `mdm_model_version` 快照，已发布定义在设计器中只读。

## 同步 Excel 导入

- 模板按当前模型字段动态生成，包含字段说明、必填标识和枚举约束。
- `.xlsx`/`.xls` 上传先解析和逐行预检查，不直接写入数据库。
- 预检查全部通过后，通过统一批量写入管道生成草稿和不可变版本。
- 同步导入限制 1000 行；万行级异步任务、文件归档和结果文件属于后续阶段。
