import { login, useAuthStore } from '@iaf/auth';
import type { LoginRequest } from '@iaf/domain-types';
import { iafLoginTemplates, useIafTheme } from '@iaf/theme';
import { message } from 'antd';
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
  const loginTarget = activeTabKey && activeTabKey !== '/login' ? activeTabKey : '/';

  useEffect(() => {
    if (token) {
      navigate(loginTarget, { replace: true });
    }
  }, [loginTarget, navigate, token]);

  if (token) {
    return <Navigate to={loginTarget} replace />;
  }

  const submit = async (values: LoginRequest) => {
    setLoading(true);
    try {
      await login(apiClient, values);
      navigate(loginTarget, { replace: true });
    } catch {
      message.error(t('auth.failed'));
    } finally {
      setLoading(false);
    }
  };

  const selectedTemplate = iafLoginTemplates.includes(brandConfig.loginTemplate)
    ? brandConfig.loginTemplate
    : 'standard-industrial';

  return loginTemplateRenderers[selectedTemplate]({
    brandConfig,
    designTokens,
    onSubmit: submit,
    loading,
    t
  });
};
