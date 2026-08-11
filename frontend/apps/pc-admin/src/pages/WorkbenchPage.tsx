import { AppPageContainer, IafMetricCard, IafSurface } from '@iaf/ui-core';
import {
  AlertOutlined,
  ApiOutlined,
  AuditOutlined,
  CheckCircleOutlined,
  ClusterOutlined,
  DashboardOutlined,
  DeploymentUnitOutlined,
  FieldTimeOutlined,
  SafetyCertificateOutlined,
  ThunderboltOutlined
} from '@ant-design/icons';
import { Button, Col, List, Progress, Row, Space, Tag, Typography, theme } from 'antd';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';

const metricItems = [
  { key: 'pendingApprovals', value: 18, suffix: '', status: 'processing' as const, icon: <AuditOutlined /> },
  { key: 'workflowHealth', value: 96, suffix: '%', status: 'success' as const, icon: <CheckCircleOutlined /> },
  { key: 'integrationAlerts', value: 3, suffix: '', status: 'warning' as const, icon: <ApiOutlined /> },
  { key: 'permissionChanges', value: 12, suffix: '', status: 'default' as const, icon: <SafetyCertificateOutlined /> }
];

const quickLinks = [
  { key: 'users', route: '/platform/users', icon: <ClusterOutlined /> },
  { key: 'roles', route: '/platform/roles', icon: <SafetyCertificateOutlined /> },
  { key: 'orgs', route: '/platform/orgs', icon: <DeploymentUnitOutlined /> },
  { key: 'kanban', route: '/platform/kanban', icon: <DashboardOutlined /> }
];

const operations = [
  { key: 'approval', level: 'HIGH' },
  { key: 'exception', level: 'MEDIUM' },
  { key: 'sync', level: 'LOW' }
];

const systemHealth = [
  { key: 'auth', value: 99 },
  { key: 'workflow', value: 96 },
  { key: 'integration', value: 91 }
];

const levelColor = (level: string) => {
  if (level === 'HIGH') return 'error';
  if (level === 'MEDIUM') return 'warning';
  return 'success';
};

export const WorkbenchPage = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { token } = theme.useToken();

  return (
    <AppPageContainer
      title={t('workbench.title')}
      extra={
        <Space>
          <Button icon={<FieldTimeOutlined />}>{t('workbench.today')}</Button>
          <Button type="primary" icon={<DashboardOutlined />} onClick={() => navigate('/platform/kanban')}>
            {t('workbench.openKanban')}
          </Button>
        </Space>
      }
    >
      <Space direction="vertical" size={16} style={{ width: '100%' }}>
        <IafSurface>
          <Row gutter={[16, 16]}>
            {metricItems.map((item) => (
              <Col key={item.key} xs={24} sm={12} xl={6}>
                <IafMetricCard
                  title={t(`workbench.metrics.${item.key}`)}
                  value={item.value}
                  suffix={item.suffix}
                  status={item.status}
                  icon={item.icon}
                  hint={t(`workbench.metricHints.${item.key}`)}
                />
              </Col>
            ))}
          </Row>
        </IafSurface>

        <Row gutter={[16, 16]}>
          <Col xs={24} xl={14}>
            <IafSurface
              title={
                <Space>
                  <AlertOutlined />
                  {t('workbench.operationFocus')}
                </Space>
              }
              extra={<Button type="link">{t('common.actions.refresh')}</Button>}
              style={{ height: '100%', borderColor: token.colorBorderSecondary }}
            >
              <List
                dataSource={operations}
                renderItem={(item) => (
                  <List.Item
                    actions={[
                      <Tag key="level" color={levelColor(item.level)}>
                        {t(`kanban.priority.${item.level}`)}
                      </Tag>,
                      <Button key="open" type="link" onClick={() => navigate('/platform/kanban')}>
                        {t('workbench.open')}
                      </Button>
                    ]}
                  >
                    <List.Item.Meta
                      avatar={<ThunderboltOutlined style={{ color: token.colorPrimary }} />}
                      title={t(`workbench.operations.${item.key}.title`)}
                      description={t(`workbench.operations.${item.key}.description`)}
                    />
                  </List.Item>
                )}
              />
            </IafSurface>
          </Col>
          <Col xs={24} xl={10}>
            <IafSurface
              title={t('workbench.systemHealth')}
              style={{ height: '100%', borderColor: token.colorBorderSecondary }}
            >
              <Space direction="vertical" size={16} style={{ width: '100%' }}>
                {systemHealth.map((item) => (
                  <div key={item.key}>
                    <Space style={{ justifyContent: 'space-between', width: '100%' }}>
                      <Typography.Text strong>{t(`workbench.health.${item.key}`)}</Typography.Text>
                      <Typography.Text type="secondary">{item.value}%</Typography.Text>
                    </Space>
                    <Progress percent={item.value} showInfo={false} strokeColor={item.value > 95 ? token.colorSuccess : token.colorWarning} />
                  </div>
                ))}
              </Space>
            </IafSurface>
          </Col>
        </Row>

        <IafSurface title={t('workbench.quickAccess')} style={{ borderColor: token.colorBorderSecondary }}>
          <Row gutter={[12, 12]}>
            {quickLinks.map((item) => (
              <Col key={item.key} xs={24} sm={12} lg={6}>
                <Button
                  block
                  size="large"
                  icon={item.icon}
                  onClick={() => navigate(item.route)}
                  style={{ height: 56, justifyContent: 'flex-start' }}
                >
                  {t(`workbench.quickLinks.${item.key}`)}
                </Button>
              </Col>
            ))}
          </Row>
        </IafSurface>
      </Space>
    </AppPageContainer>
  );
};
