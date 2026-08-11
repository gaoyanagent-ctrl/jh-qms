import { apiClient } from '../../../api/client';

export interface ThemeConfigResponse {
  themeName: string;
  primaryColor: string;
  sidebarMode: string;
  tokens: Record<string, unknown>;
}

export interface BrandConfigResponse {
  brandName: string;
  logoUrl?: string;
  faviconUrl?: string;
  loginHeroTitle: string;
  loginHeroSubtitle: string;
  loginOpsTitle: string;
  loginOpsDescription: string;
  loginBackgroundType: string;
  loginBackgroundImageUrl?: string;
  loginTemplate?: string;
}

export interface I18nResourceResponse {
  locale: string;
  resources: Array<{
    resourceKey: string;
    resourceValue: string;
  }>;
}

export interface UserPreferenceResponse {
  userId: number;
  settings: Record<string, unknown>;
}

export const systemConfigApi = {
  getTheme: () => apiClient.get<ThemeConfigResponse>('/api/platform/theme/current'),
  saveTheme: (payload: ThemeConfigResponse) => apiClient.put<ThemeConfigResponse>('/api/platform/theme/current', payload),
  getBrand: () => apiClient.get<BrandConfigResponse>('/api/platform/brand/current'),
  saveBrand: (payload: BrandConfigResponse) => apiClient.put<BrandConfigResponse>('/api/platform/brand/current', payload),
  listI18nResources: (locale: string) => apiClient.get<I18nResourceResponse>('/api/platform/i18n/resources', { query: { locale } }),
  replaceI18nResources: (payload: I18nResourceResponse) => apiClient.put<void>('/api/platform/i18n/resources', payload),
  getMyPreference: () => apiClient.get<UserPreferenceResponse>('/api/platform/preferences/me'),
  saveMyPreference: (settings: Record<string, unknown>) => apiClient.put<UserPreferenceResponse>('/api/platform/preferences/me', { settings })
};
