import type { ApiClient } from '@iaf/api-client';
import type { AuthPrincipal, LoginRequest, LoginResponse } from '@iaf/domain-types';
import { create } from 'zustand';

const TOKEN_KEY = 'iaf.pcAdmin.accessToken';

export interface TokenStorage {
  get(): string | null;
  set(token: string): void;
  clear(): void;
}

export const browserTokenStorage: TokenStorage = {
  get: () => window.localStorage.getItem(TOKEN_KEY),
  set: (token: string) => window.localStorage.setItem(TOKEN_KEY, token),
  clear: () => window.localStorage.removeItem(TOKEN_KEY)
};

interface AuthState {
  token: string | null;
  principal: AuthPrincipal | null;
  setToken: (token: string | null) => void;
  setPrincipal: (principal: AuthPrincipal | null) => void;
  clear: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  token: typeof window === 'undefined' ? null : browserTokenStorage.get(),
  principal: null,
  setToken: (token) => {
    if (token) {
      browserTokenStorage.set(token);
    } else {
      browserTokenStorage.clear();
    }
    set({ token });
  },
  setPrincipal: (principal) => set({ principal }),
  clear: () => {
    browserTokenStorage.clear();
    set({ token: null, principal: null });
  }
}));

export const login = async (apiClient: ApiClient, request: LoginRequest): Promise<LoginResponse> => {
  const response = await apiClient.post<LoginResponse>('/api/platform/auth/login', request);
  useAuthStore.getState().setToken(response.accessToken);
  useAuthStore.getState().setPrincipal(toPrincipal(response));
  return response;
};

export const loadCurrentUser = async (apiClient: ApiClient): Promise<AuthPrincipal> => {
  const principal = await apiClient.get<AuthPrincipal>('/api/platform/auth/me');
  useAuthStore.getState().setPrincipal(principal);
  return principal;
};

export const logout = () => {
  useAuthStore.getState().clear();
};

const toPrincipal = (response: LoginResponse): AuthPrincipal => ({
  tenantId: response.tenantId,
  userId: response.userId,
  username: response.username,
  displayName: response.displayName,
  currentOrgId: response.currentOrgId,
  organizations: response.organizations,
  permissions: response.permissions
});
