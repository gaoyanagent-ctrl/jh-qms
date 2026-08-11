import { iafDefaultBrandConfig, iafLoginTemplates, type IafBrandConfig, type IafLoginTemplateName } from '@iaf/theme';
import { systemConfigApi, type BrandConfigResponse } from '../modules/platform/systemConfig/api';

export const iafPcAdminLoginTemplateOptions = iafLoginTemplates;

const coerceLoginTemplate = (value: unknown): IafLoginTemplateName | undefined => {
  if (typeof value === 'string' && iafLoginTemplates.includes(value as IafLoginTemplateName)) {
    return value as IafLoginTemplateName;
  }
  return undefined;
};

const resolveLoginTemplate = (value: unknown): IafLoginTemplateName => {
  if (typeof window !== 'undefined') {
    const fromQuery = coerceLoginTemplate(new URLSearchParams(window.location.search).get('loginTemplate'));
    if (fromQuery) {
      return fromQuery;
    }
  }

  return coerceLoginTemplate(value) ?? iafDefaultBrandConfig.loginTemplate;
};

export const iafPcAdminBrandConfig: IafBrandConfig = {
  ...iafDefaultBrandConfig,
  loginTemplate: resolveLoginTemplate(import.meta.env.VITE_IAF_LOGIN_TEMPLATE)
};

const shouldLoadRemoteBrandConfig = () => import.meta.env.VITE_IAF_BRAND_CONFIG_API === 'true';

const mapRemoteBrandConfig = (response: BrandConfigResponse): Partial<IafBrandConfig> => {
  const loginTemplate = resolveLoginTemplate(response.loginTemplate);
  return {
    logoUrl: response.logoUrl,
    faviconUrl: response.faviconUrl,
    loginBackground: response.loginBackgroundType === 'image' && response.loginBackgroundImageUrl
      ? { type: 'image', imageUrl: response.loginBackgroundImageUrl }
      : { type: 'token' },
    loginTemplate
  };
};

export const loadIafPcAdminBrandConfig = async (): Promise<IafBrandConfig> => {
  if (!shouldLoadRemoteBrandConfig()) {
    return iafPcAdminBrandConfig;
  }

  try {
    const remote = await systemConfigApi.getBrand();
    return {
      ...iafPcAdminBrandConfig,
      ...mapRemoteBrandConfig(remote),
      loginTemplate: resolveLoginTemplate(new URLSearchParams(window.location.search).get('loginTemplate') ?? remote.loginTemplate)
    };
  } catch {
    return iafPcAdminBrandConfig;
  }
};
