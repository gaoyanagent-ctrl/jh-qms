import { loadCurrentUser, useAuthStore } from '@iaf/auth';
import { Spin } from 'antd';
import { useEffect, useState } from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { apiClient } from '../api/client';

export const ProtectedRoute = () => {
  const token = useAuthStore((state) => state.token);
  const principal = useAuthStore((state) => state.principal);
  const [loading, setLoading] = useState(Boolean(token && !principal));

  useEffect(() => {
    if (!token || principal) {
      setLoading(false);
      return;
    }

    setLoading(true);
    loadCurrentUser(apiClient).finally(() => setLoading(false));
  }, [principal, token]);

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  if (loading) {
    return <Spin fullscreen />;
  }

  return <Outlet />;
};
