import { AppPageContainer, IafMetricCard, IafStatusPill, IafSurface, IafToolbar, StatusTag } from '@iaf/ui-core';
import { AuditOutlined, ControlOutlined, DatabaseOutlined } from '@ant-design/icons';
import { Col, Row, Space, Table, Tabs, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useTranslation } from 'react-i18next';

interface MenuRecord {
  id: number;
  titleKey: string;
  routePath: string;
  permissionCode: string;
  status: 'ACTIVE' | 'DISABLED';
}

interface DictRecord {
  id: number;
  type: string;
  code: string;
  name: string;
  status: 'ACTIVE' | 'DISABLED';
}

interface ParameterRecord {
  id: number;
  group: string;
  key: string;
  value: string;
  scope: string;
}

interface AuditLogRecord {
  id: number;
  operator: string;
  module: string;
  action: string;
  result: 'SUCCESS' | 'FAILED';
  time: string;
}

const menuRecords: MenuRecord[] = [
  { id: 1, titleKey: 'menu.users', routePath: '/platform/users', permissionCode: 'platform:user:view', status: 'ACTIVE' },
  { id: 2, titleKey: 'menu.roles', routePath: '/platform/roles', permissionCode: 'platform:role:view', status: 'ACTIVE' },
  { id: 3, titleKey: 'menu.kanban', routePath: '/platform/kanban', permissionCode: 'platform:kanban:view', status: 'ACTIVE' }
];

const dictRecords: DictRecord[] = [
  { id: 1, type: 'platform_status', code: 'ACTIVE', name: 'Active', status: 'ACTIVE' },
  { id: 2, type: 'approval_result', code: 'APPROVED', name: 'Approved', status: 'ACTIVE' },
  { id: 3, type: 'priority', code: 'HIGH', name: 'High', status: 'ACTIVE' }
];

const parameterRecords: ParameterRecord[] = [
  { id: 1, group: 'theme', key: 'defaultTheme', value: 'light-industrial', scope: 'tenant' },
  { id: 2, group: 'approval', key: 'taskTimeoutHours', value: '24', scope: 'tenant' },
  { id: 3, group: 'integration', key: 'retryLimit', value: '3', scope: 'system' }
];

const auditLogRecords: AuditLogRecord[] = [
  { id: 1, operator: 'admin', module: 'Role', action: 'AssignPermission', result: 'SUCCESS', time: '2026-07-07 09:20' },
  { id: 2, operator: 'ops', module: 'Kanban', action: 'MoveCard', result: 'SUCCESS', time: '2026-07-07 09:44' },
  { id: 3, operator: 'system', module: 'Integration', action: 'SyncWeCom', result: 'FAILED', time: '2026-07-07 10:05' }
];

export const PlatformMenuConsolePage = () => {
  const { t } = useTranslation();
  const columns: ColumnsType<MenuRecord> = [
    { title: t('platformConfig.menuTitle'), dataIndex: 'titleKey', render: (key) => t(key) },
    { title: t('platformConfig.routePath'), dataIndex: 'routePath' },
    { title: t('platformConfig.permissionCode'), dataIndex: 'permissionCode', render: (code) => <Tag>{code}</Tag> },
    { title: t('common.fields.status'), dataIndex: 'status', render: (status: MenuRecord['status']) => <StatusTag status={status} label={t(`common.status.${status}`)} /> }
  ];

  return (
    <AppPageContainer title={t('platformConfig.menuConsole')}>
      <IafSurface
        title={t('platformConfig.menuConsole')}
        extra={<IafStatusPill tone="info">{t('platformConfig.mockFirst')}</IafStatusPill>}
      >
        <IafToolbar title={t('platformConfig.menuSummary')}>
          <Tag>{t('workspace.totalRecords', { total: menuRecords.length })}</Tag>
        </IafToolbar>
        <Table rowKey="id" size="small" bordered columns={columns} dataSource={menuRecords} pagination={false} />
      </IafSurface>
    </AppPageContainer>
  );
};

export const PlatformDictionaryParameterPage = () => {
  const { t } = useTranslation();
  const dictColumns: ColumnsType<DictRecord> = [
    { title: t('platformConfig.dictType'), dataIndex: 'type' },
    { title: t('platformConfig.dictCode'), dataIndex: 'code' },
    { title: t('platformConfig.dictName'), dataIndex: 'name' },
    { title: t('common.fields.status'), dataIndex: 'status', render: (status: DictRecord['status']) => <StatusTag status={status} label={t(`common.status.${status}`)} /> }
  ];
  const parameterColumns: ColumnsType<ParameterRecord> = [
    { title: t('platformConfig.parameterGroup'), dataIndex: 'group' },
    { title: t('platformConfig.parameterKey'), dataIndex: 'key' },
    { title: t('platformConfig.parameterValue'), dataIndex: 'value' },
    { title: t('platformConfig.parameterScope'), dataIndex: 'scope', render: (scope) => <Tag>{scope}</Tag> }
  ];

  return (
    <AppPageContainer title={t('platformConfig.dictionaryParameter')}>
      <IafSurface extra={<IafStatusPill tone="info">{t('platformConfig.mockFirst')}</IafStatusPill>}>
        <Tabs
          items={[
            {
              key: 'dict',
              label: (
                <Space>
                  <DatabaseOutlined />
                  {t('platformConfig.dictionary')}
                </Space>
              ),
              children: <Table rowKey="id" size="small" bordered columns={dictColumns} dataSource={dictRecords} pagination={false} />
            },
            {
              key: 'parameter',
              label: (
                <Space>
                  <ControlOutlined />
                  {t('platformConfig.parameter')}
                </Space>
              ),
              children: <Table rowKey="id" size="small" bordered columns={parameterColumns} dataSource={parameterRecords} pagination={false} />
            }
          ]}
        />
      </IafSurface>
    </AppPageContainer>
  );
};

export const PlatformAuditLogPage = () => {
  const { t } = useTranslation();
  const columns: ColumnsType<AuditLogRecord> = [
    { title: t('platformConfig.operator'), dataIndex: 'operator' },
    { title: t('platformConfig.module'), dataIndex: 'module' },
    { title: t('platformConfig.action'), dataIndex: 'action' },
    {
      title: t('platformConfig.result'),
      dataIndex: 'result',
      render: (result: AuditLogRecord['result']) => <StatusTag status={result} label={t(`platformConfig.results.${result}`)} />
    },
    { title: t('platformConfig.time'), dataIndex: 'time' }
  ];

  return (
    <AppPageContainer title={t('platformConfig.auditLog')}>
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={8}>
          <IafMetricCard
            title={t('platformConfig.auditSummary')}
            value={auditLogRecords.length}
            status="processing"
            icon={<AuditOutlined />}
            hint={t('platformConfig.mockFirst')}
          />
        </Col>
        <Col xs={24} lg={16}>
          <IafSurface compact>
            <Table rowKey="id" size="small" bordered columns={columns} dataSource={auditLogRecords} pagination={false} />
          </IafSurface>
        </Col>
      </Row>
    </AppPageContainer>
  );
};
