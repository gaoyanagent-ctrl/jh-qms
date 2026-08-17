import { discoverLoginTenants, login, useAuthStore } from '@iaf/auth';
import type { LoginCredentials, LoginTenantOption } from '@iaf/domain-types';
import { iafLoginTemplates, useIafTheme } from '@iaf/theme';
import { BankOutlined } from '@ant-design/icons';
import { Alert, Button, List, Modal, Space, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Navigate, useNavigate } from 'react-router-dom';
import { apiClient } from '../api/client';
import { useRouteTabStore } from '../workspace/RouteTabStore';
import { loginTemplateRenderers } from './loginTemplates';

export const LoginPage = () => {
  const { t } = useTranslation();
  const { brandConfig, designTokens } = useIafTheme();
  const navigate = useNavigate();
  const token = useAuthStore((state) => state.token);
  const activeTabKey = useRouteTabStore((state) => state.activeTabKey);
  const [loading, setLoading] = useState(false);
  const [credentials, setCredentials] = useState<LoginCredentials | null>(null);
  const [tenantOptions, setTenantOptions] = useState<LoginTenantOption[]>([]);
  const [selectedTenantCode, setSelectedTenantCode] = useState<string | null>(null);
  const loginTarget = activeTabKey && activeTabKey !== '/login' ? activeTabKey : '/';

  useEffect(() => {
    if (token) {
      navigate(loginTarget, { replace: true });
    }
  }, [loginTarget, navigate, token]);

  if (token) {
    return <Navigate to={loginTarget} replace />;
  }

  const completeLogin = async (values: LoginCredentials, tenantCode: string) => {
    await login(apiClient, { ...values, tenantCode });
    setCredentials(null);
    setTenantOptions([]);
    navigate(loginTarget, { replace: true });
  };

  const submit = async (values: LoginCredentials) => {
    setLoading(true);
    try {
      const options = await discoverLoginTenants(apiClient, values);
      if (options.length === 1) {
        await completeLogin(values, options[0].tenantCode);
        return;
      }
      setCredentials(values);
      setTenantOptions(options);
      setSelectedTenantCode(options[0]?.tenantCode ?? null);
    } catch {
      message.error(t('auth.failed'));
    } finally {
      setLoading(false);
    }
  };

  const submitTenant = async () => {
    if (!credentials || !selectedTenantCode) return;
    setLoading(true);
    try {
      await completeLogin(credentials, selectedTenantCode);
    } catch {
      message.error(t('auth.failed'));
    } finally {
      setLoading(false);
    }
  };

  const closeTenantSelection = () => {
    setCredentials(null);
    setTenantOptions([]);
    setSelectedTenantCode(null);
  };

  const selectedTemplate = iafLoginTemplates.includes(brandConfig.loginTemplate)
    ? brandConfig.loginTemplate
    : 'standard-industrial';

  return (
    <>
      {loginTemplateRenderers[selectedTemplate]({
        brandConfig,
        designTokens,
        onSubmit: submit,
        loading,
        t
      })}
      <Modal
        open={tenantOptions.length > 1}
        title={t('auth.tenantSelection.title')}
        onCancel={closeTenantSelection}
        footer={[
          <Button key="cancel" onClick={closeTenantSelection}>{t('common.cancel')}</Button>,
          <Button key="confirm" type="primary" loading={loading} disabled={!selectedTenantCode} onClick={submitTenant}>
            {t('auth.tenantSelection.continue')}
          </Button>
        ]}
        destroyOnHidden
      >
        <Space direction="vertical" size="middle" style={{ width: '100%' }}>
          <Alert type="info" showIcon message={t('auth.tenantSelection.description')} />
          <List
            dataSource={tenantOptions}
            renderItem={(tenant) => {
              const selected = selectedTenantCode === tenant.tenantCode;
              return (
                <List.Item
                  onClick={() => setSelectedTenantCode(tenant.tenantCode)}
                  role="button"
                  tabIndex={0}
                  onKeyDown={(event) => {
                    if (event.key === 'Enter' || event.key === ' ') setSelectedTenantCode(tenant.tenantCode);
                  }}
                  style={{
                    cursor: 'pointer',
                    border: `1px solid ${selected ? designTokens.global.colorPrimary : '#d9d9d9'}`,
                    borderRadius: 8,
                    padding: '14px 16px',
                    marginBottom: 8,
                    background: selected ? `${designTokens.global.colorPrimary}0d` : undefined
                  }}
                >
                  <List.Item.Meta
                    avatar={<BankOutlined style={{ color: selected ? designTokens.global.colorPrimary : undefined, fontSize: 20 }} />}
                    title={<Typography.Text strong={selected}>{tenant.tenantName}</Typography.Text>}
                    description={tenant.tenantCode}
                  />
                </List.Item>
              );
            }}
          />
        </Space>
      </Modal>
    </>
  );
};
