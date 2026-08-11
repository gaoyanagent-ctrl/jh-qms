# TASK-FE-0015 建立移动扫码运行时

## 1. 任务目标

建立统一扫码运行时，支持摄像头、PDA、蓝牙扫码枪、企业微信、钉钉、飞书扫码能力适配。

## 2. 交付范围

新增：

```text
packages/scan-runtime/src/
  ScanService.ts
  ScanDeviceAdapter.ts
  CameraScanAdapter.ts
  KeyboardScanAdapter.ts
  BluetoothScanAdapter.ts
  PdaNativeScanAdapter.ts
  WeComScanAdapter.ts
  DingTalkScanAdapter.ts
  FeishuScanAdapter.ts
  ScanParser.ts
  ScanActionRouter.ts
  types.ts
```

## 3. 功能要求

- 统一 ScanResult 模型。
- 支持键盘流扫码识别。
- 支持扫码类型解析。
- 支持按作业场景路由扫码动作。
- 支持手工输入兜底。

## 4. 验收标准

- mobile-work 可以通过测试输入模拟扫码。
- 同一个扫码结果可以被解析为物料、库位、任务或单据。
- 页面不直接调用企业微信 / 钉钉 / 飞书 SDK。
