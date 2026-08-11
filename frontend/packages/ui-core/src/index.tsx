import { useIafTheme, type IafDesignTokens } from '@iaf/theme';
import { App, Badge, Button, Card, Drawer, Empty, Modal, Popconfirm, Result, Space, Statistic, Tag, Typography, theme } from 'antd';
import type { CSSProperties, ReactNode } from 'react';

export const AppPageContainer = ({ title, extra, children }: { title: ReactNode; extra?: ReactNode; children: ReactNode }) => {
  const { token } = theme.useToken();

  return (
    <Space className="iaf-page-container" data-testid="iaf-page-container" direction="vertical" size={16} style={{ width: '100%' }}>
      <div
        className="iaf-page-header"
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: token.margin,
          width: '100%',
          minWidth: 0
        }}
      >
        <Typography.Title level={3} style={{ margin: 0, color: token.colorText }}>
          {title}
        </Typography.Title>
        {extra && <Space wrap>{extra}</Space>}
      </div>
      {children}
    </Space>
  );
};

export const IafSurface = ({
  title,
  extra,
  children,
  compact = false,
  style
}: {
  title?: ReactNode;
  extra?: ReactNode;
  children: ReactNode;
  compact?: boolean;
  style?: CSSProperties;
}) => {
  const { token } = theme.useToken();

  return (
    <Card
      className="iaf-surface"
      title={title}
      extra={extra}
      variant="borderless"
      styles={{
        body: {
          padding: compact ? token.padding : token.paddingLG
        }
      }}
      style={{
        border: `1px solid ${token.colorBorderSecondary}`,
        boxShadow: token.boxShadowTertiary,
        ...style
      }}
    >
      {children}
    </Card>
  );
};

export const IafMetricCard = ({
  title,
  value,
  suffix,
  status = 'default',
  icon,
  hint
}: {
  title: ReactNode;
  value: string | number;
  suffix?: ReactNode;
  status?: 'success' | 'processing' | 'warning' | 'error' | 'default';
  icon?: ReactNode;
  hint?: ReactNode;
}) => {
  const { token } = theme.useToken();

  return (
    <IafSurface compact>
      <Space align="start" style={{ justifyContent: 'space-between', width: '100%' }}>
        <Statistic title={title} value={value} suffix={suffix} valueStyle={{ color: token.colorText, fontWeight: 700 }} />
        <Badge status={status} />
      </Space>
      {hint && (
        <Typography.Text type="secondary">
          <Space size={6}>
            {icon}
            {hint}
          </Space>
        </Typography.Text>
      )}
    </IafSurface>
  );
};

export const IafToolbar = ({
  title,
  extra,
  children
}: {
  title?: ReactNode;
  extra?: ReactNode;
  children?: ReactNode;
}) => {
  const { token } = theme.useToken();

  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        gap: token.margin,
        padding: token.padding,
        background: token.colorFillAlter,
        border: `1px solid ${token.colorBorderSecondary}`,
        borderRadius: token.borderRadiusLG
      }}
    >
      <Space wrap>
        {title && <Typography.Text strong>{title}</Typography.Text>}
        {children}
      </Space>
      {extra && <Space wrap>{extra}</Space>}
    </div>
  );
};

export const IafSectionHeader = ({
  title,
  description,
  extra
}: {
  title: ReactNode;
  description?: ReactNode;
  extra?: ReactNode;
}) => (
  <Space align="start" style={{ justifyContent: 'space-between', width: '100%' }}>
    <Space direction="vertical" size={2}>
      <Typography.Text strong>{title}</Typography.Text>
      {description && <Typography.Text type="secondary">{description}</Typography.Text>}
    </Space>
    {extra}
  </Space>
);

export const IafStatusPill = ({
  tone = 'neutral',
  children
}: {
  tone?: 'success' | 'warning' | 'error' | 'info' | 'neutral';
  children: ReactNode;
}) => {
  const color = tone === 'neutral' ? 'default' : tone;
  return (
    <Tag bordered={false} color={color}>
      {children}
    </Tag>
  );
};

export type BusinessStatusTone = 'draft' | 'pending' | 'approved' | 'rejected' | 'processing' | 'closed' | 'available' | 'frozen' | 'urgent';

const normalizeStatus = (status: string) => status.trim().toUpperCase().replaceAll('-', '_');

export const resolveBusinessStatusTone = (status: string): BusinessStatusTone => {
  const normalized = normalizeStatus(status);
  if (['DRAFT', 'NEW', 'CREATED'].includes(normalized)) return 'draft';
  if (['PENDING', 'PENDING_APPROVAL', 'WAITING', 'SUBMITTED', 'FROZEN'].includes(normalized)) return 'pending';
  if (['APPROVED', 'COMPLETED', 'DONE', 'ACTIVE', 'ENABLED', 'AVAILABLE', 'SUCCESS'].includes(normalized)) return 'approved';
  if (['REJECTED', 'ERROR', 'FAILED', 'EXCEPTION', 'DISABLED', 'CANCELLED_BY_REJECT'].includes(normalized)) return 'rejected';
  if (['PROCESSING', 'RUNNING', 'IN_PROGRESS', 'EXECUTING'].includes(normalized)) return 'processing';
  if (['CLOSED', 'CANCELLED', 'ARCHIVED', 'INACTIVE'].includes(normalized)) return 'closed';
  return 'draft';
};

const toneToToken = (tokens: IafDesignTokens, tone: BusinessStatusTone) => {
  const semantic = tokens.semantic;
  switch (tone) {
    case 'pending':
      return { color: semantic.statusPendingColor, background: semantic.statusPendingBg };
    case 'approved':
      return { color: semantic.statusApprovedColor, background: semantic.statusApprovedBg };
    case 'rejected':
      return { color: semantic.statusRejectedColor, background: semantic.statusRejectedBg };
    case 'processing':
      return { color: semantic.statusProcessingColor, background: semantic.statusProcessingBg };
    case 'closed':
      return { color: semantic.statusClosedColor, background: semantic.statusClosedBg };
    case 'available':
      return { color: semantic.inventoryAvailableColor, background: semantic.inventoryAvailableBg };
    case 'frozen':
      return { color: semantic.inventoryFrozenColor, background: semantic.inventoryFrozenBg };
    case 'urgent':
      return { color: semantic.taskUrgentColor, background: semantic.taskUrgentBg };
    case 'draft':
    default:
      return { color: semantic.statusDraftColor, background: semantic.statusDraftBg };
  }
};

export const BusinessStatusBadge = ({
  tone,
  label
}: {
  tone: BusinessStatusTone;
  label: ReactNode;
}) => {
  const { designTokens } = useIafTheme();
  const colors = toneToToken(designTokens, tone);

  return (
    <Tag
      bordered
      style={{
        color: colors.color,
        background: colors.background,
        borderColor: colors.color,
        marginInlineEnd: 0
      }}
    >
      {label}
    </Tag>
  );
};

export const StatusTag = ({ status, label }: { status: string; label: ReactNode }) => (
  <BusinessStatusBadge tone={resolveBusinessStatusTone(status)} label={label} />
);

export const ApprovalStatusTag = ({ status, label }: { status: string; label: ReactNode }) => {
  const normalized = normalizeStatus(status);
  const tone: BusinessStatusTone =
    normalized === 'APPROVED'
      ? 'approved'
      : normalized === 'REJECTED'
        ? 'rejected'
        : normalized === 'PROCESSING'
          ? 'processing'
          : normalized === 'CLOSED'
            ? 'closed'
            : 'pending';
  return <BusinessStatusBadge tone={tone} label={label} />;
};

export const InventoryStatusTag = ({ status, label }: { status: string; label: ReactNode }) => {
  const normalized = normalizeStatus(status);
  const tone: BusinessStatusTone = normalized === 'AVAILABLE' ? 'available' : normalized === 'FROZEN' ? 'frozen' : resolveBusinessStatusTone(status);
  return <BusinessStatusBadge tone={tone} label={label} />;
};

export const DocumentStatusTag = ({ status, label }: { status: string; label: ReactNode }) => (
  <BusinessStatusBadge tone={resolveBusinessStatusTone(status)} label={label} />
);

export const ExecutionStatusTag = ({ status, label }: { status: string; label: ReactNode }) => (
  <BusinessStatusBadge tone={resolveBusinessStatusTone(status)} label={label} />
);

export const TaskStatusTag = ({ status, label, urgent = false }: { status: string; label: ReactNode; urgent?: boolean }) => (
  <BusinessStatusBadge tone={urgent ? 'urgent' : resolveBusinessStatusTone(status)} label={label} />
);

export const ConfirmAction = ({
  title,
  children,
  onConfirm
}: {
  title: ReactNode;
  children: ReactNode;
  onConfirm: () => void;
}) => (
  <Popconfirm title={title} onConfirm={onConfirm}>
    {children}
  </Popconfirm>
);

export const FormInteractionSurface = ({
  mode,
  open,
  title,
  confirmLoading,
  submitLabel,
  cancelLabel,
  children,
  onCancel,
  onSubmit,
  width
}: {
  mode: 'modal' | 'drawer' | 'page';
  open: boolean;
  title: ReactNode;
  confirmLoading?: boolean;
  submitLabel: ReactNode;
  cancelLabel: ReactNode;
  children: ReactNode;
  onCancel: () => void;
  onSubmit: () => void;
  width?: number | string;
}) => {
  const { token } = theme.useToken();
  const surfaceWidth = width ?? (mode === 'modal' ? 'min(90vw, 760px)' : 'min(92vw, 960px)');
  const actions = (
    <Space>
      <Button onClick={onCancel}>{cancelLabel}</Button>
      <Button type="primary" loading={confirmLoading} onClick={onSubmit}>
        {submitLabel}
      </Button>
    </Space>
  );

  if (!open) {
    return null;
  }

  if (mode === 'drawer') {
    return (
      <Drawer
        className="iaf-form-surface-drawer"
        open
        title={title}
        width={surfaceWidth}
        onClose={onCancel}
        maskClosable={false}
        destroyOnHidden
        styles={{
          wrapper: { maxWidth: '96vw' },
          body: {
            padding: token.paddingLG,
            background: token.colorBgLayout,
            overflowY: 'auto',
            maxHeight: 'calc(100vh - 114px)'
          },
          footer: {
            display: 'flex',
            justifyContent: 'flex-end',
            borderTop: `1px solid ${token.colorBorderSecondary}`
          }
        }}
        footer={actions}
      >
        <div style={{ maxWidth: 'min(100%, 960px)', marginInline: 'auto' }}>{children}</div>
      </Drawer>
    );
  }

  if (mode === 'page') {
    return (
      <Card
        className="iaf-form-surface-page"
        title={title}
        extra={actions}
        styles={{ body: { padding: token.paddingLG } }}
        style={{
          minHeight: 'calc(100vh - 160px)',
          borderColor: token.colorBorderSecondary,
          boxShadow: token.boxShadowTertiary
        }}
      >
        {children}
      </Card>
    );
  }

  return (
    <Modal
      className="iaf-form-surface-modal"
      open
      title={title}
      width={surfaceWidth}
      onCancel={onCancel}
      onOk={onSubmit}
      confirmLoading={confirmLoading}
      maskClosable={false}
      destroyOnHidden
      style={{ maxWidth: '92vw' }}
    >
      {children}
    </Modal>
  );
};

export const EmptyState = ({ description }: { description: ReactNode }) => <Empty description={description} />;

export const ErrorState = ({ title, subTitle, onRetry }: { title: ReactNode; subTitle?: ReactNode; onRetry?: () => void }) => (
  <Result
    status="error"
    title={title}
    subTitle={subTitle}
    extra={
      onRetry ? (
        <Button type="primary" onClick={onRetry}>
          Retry
        </Button>
      ) : undefined
    }
  />
);

export const useAppMessage = () => App.useApp().message;
