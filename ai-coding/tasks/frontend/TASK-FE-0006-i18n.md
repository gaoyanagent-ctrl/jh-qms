# TASK-FE-0006 建立 i18n 中英文体系

## 1. 任务目标

建立前端中英文国际化体系，确保 PC 和移动端都能通过统一 i18n 包显示菜单、按钮、字段、枚举和错误信息。

## 2. 交付范围

新增：

```text
packages/i18n/src/
  index.ts
  i18n.ts
  locales/zh-CN/common.json
  locales/zh-CN/platform.json
  locales/zh-CN/wms.json
  locales/zh-CN/errors.json
  locales/en-US/common.json
  locales/en-US/platform.json
  locales/en-US/wms.json
  locales/en-US/errors.json
```

## 3. 必须包含 key

```text
common.action.create
common.action.update
common.action.delete
common.action.saveDraft
common.action.submit
common.action.cancel
common.action.confirm
common.action.export
common.message.unsavedChanges
menu.workbench
menu.wms.receipt
enum.documentStatus.DRAFT
enum.documentStatus.EFFECTIVE
error.common.unknown
```

## 4. 验收标准

- pc-admin 能切换中文 / 英文。
- mobile-work 能切换中文 / 英文。
- 示例按钮无硬编码中文。
- 缺失 key 时有 fallback。
