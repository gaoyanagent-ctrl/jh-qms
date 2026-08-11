import { ConfigProvider, theme as antdTheme, type ThemeConfig } from 'antd';
import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';

export const iafThemeNames = [
  'light-industrial',
  'dark-industrial',
  'compact-industrial',
  'dashboard-industrial',
  'mobile-work',
  'high-contrast',
  'customer-brand'
] as const;

export type IafThemeName = typeof iafThemeNames[number];
export type IafFormInteractionMode = 'modal' | 'drawer' | 'page';
export type IafDensity = 'compact' | 'standard' | 'comfortable';
export type IafFontSize = 'small' | 'default' | 'large';
export type IafSidebarMode = 'dark' | 'light';
export type IafMotionLevel = 'none' | 'subtle' | 'standard';
export type IafSurfaceWidth = 'standard' | 'wide' | 'extra-wide';
export type IafWorkspaceMode = 'simple' | 'expert';
export const iafLoginTemplates = [
  'standard-industrial',
  'cyber-ai',
  'immersive-glass',
  'minimal-technical',
  'bento-dashboard'
] as const;

export type IafLoginTemplateName = typeof iafLoginTemplates[number];
export type IafLoginBackground =
  | { type: 'token' }
  | { type: 'image'; imageUrl: string };

export interface IafBrandConfig {
  brandNameKey: string;
  logoUrl?: string;
  faviconUrl?: string;
  loginHeroTitleKey: string;
  loginHeroSubtitleKey: string;
  loginOpsTitleKey: string;
  loginOpsDescriptionKey: string;
  loginTelemetryKeys: readonly string[];
  loginBackground: IafLoginBackground;
  loginTemplate: IafLoginTemplateName;
}

export interface IafShellTokens {
  sidebarBg: string;
  sidebarPanel: string;
  sidebarBorder: string;
  sidebarText: string;
  sidebarMuted: string;
  sidebarAccent: string;
  sidebarAvatarText: string;
  sidebarAvatarGlow: string;
  sidebarShadow: string;
  sidebarActiveBg: string;
  topbarBg: string;
  topbarAccent: string;
  topbarShadow?: string;
}

export interface IafLoginTemplatePalette {
  accent: string;
  accentBg: string;
  accentBorder: string;
  controlBg: string;
  controlText: string;
  text: string;
  muted: string;
  subtle: string;
  border: string;
  panel: string;
  pageBg: string;
  gridLine: string;
  glow: string;
}

export interface IafLoginTemplateTokens {
  terminal: IafLoginTemplatePalette;
  glass: IafLoginTemplatePalette;
  brutalist: IafLoginTemplatePalette;
  standard: {
    heroOverlay: string;
    heroBg: string;
    heroLogo: string;
    heroText: string;
    heroMuted: string;
    formBg: string;
    formBorder: string;
    formShadow: string;
  };
  bento: {
    imageOverlay: string;
    featureBg: string;
    featureBorder: string;
    formShadow: string;
  };
}

export interface IafDesignTokens {
  global: {
    colorPrimary: string;
    colorSuccess: string;
    colorWarning: string;
    colorError: string;
    colorInfo: string;
    fontSizeBase: number;
    borderRadius: number;
    spacing: number;
  };
  semantic: {
    statusDraftColor: string;
    statusDraftBg: string;
    statusPendingColor: string;
    statusPendingBg: string;
    statusApprovedColor: string;
    statusApprovedBg: string;
    statusRejectedColor: string;
    statusRejectedBg: string;
    statusProcessingColor: string;
    statusProcessingBg: string;
    statusClosedColor: string;
    statusClosedBg: string;
    inventoryAvailableColor: string;
    inventoryAvailableBg: string;
    inventoryFrozenColor: string;
    inventoryFrozenBg: string;
    taskUrgentColor: string;
    taskUrgentBg: string;
  };
  component: {
    tableHeaderBg: string;
    formSectionBg: string;
    pageHeaderHeight: number;
    sidebarWidth: number;
    mobileActionButtonHeight: number;
    dashboardCardBg: string;
  };
  surface: {
    panelBg: string;
    panelBorder: string;
    panelMutedBg: string;
    toolbarBg: string;
    insetBg: string;
  };
  status: {
    success: string;
    successBg: string;
    warning: string;
    warningBg: string;
    error: string;
    errorBg: string;
    info: string;
    infoBg: string;
    neutral: string;
    neutralBg: string;
  };
  dataViz: {
    categorical: readonly string[];
    sequential: readonly string[];
    exception: string;
    forecast: string;
  };
  elevation: {
    level1: string;
    level2: string;
    level3: string;
  };
  focusRing: string;
  kanban: {
    columnBg: string;
    cardBg: string;
    cardDraggingBorder: string;
    wipWarningBg: string;
  };
  dashboard: {
    metricBg: string;
    metricAccent: string;
    gridLine: string;
  };
  designer: {
    canvasBg: string;
    nodeBg: string;
    nodeBorder: string;
    connector: string;
  };
  loginTemplates: IafLoginTemplateTokens;
}

export interface IafExperienceSettings {
  themeName: IafThemeName;
  formInteractionMode: IafFormInteractionMode;
  density: IafDensity;
  fontSize: IafFontSize;
  sidebarMode: IafSidebarMode;
  sidebarCollapsed: boolean;
  sidebarWidth: number;
  motionLevel: IafMotionLevel;
  surfaceWidth: IafSurfaceWidth;
  workspaceMode: IafWorkspaceMode;
}

interface IafThemeContextValue extends IafExperienceSettings {
  settings: IafExperienceSettings;
  preferenceScope?: string;
  brandConfig: IafBrandConfig;
  designTokens: IafDesignTokens;
  updateExperienceSettings: (settings: Partial<IafExperienceSettings>) => void;
  resetExperienceSettings: () => void;
  setPreferenceScope: (scope?: string) => void;
  setThemeName: (themeName: IafThemeName) => void;
  setFormInteractionMode: (mode: IafFormInteractionMode) => void;
}

const STORAGE_KEY = 'iaf.experience.settings';

export const iafDefaultExperienceSettings: IafExperienceSettings = {
  themeName: 'light-industrial',
  formInteractionMode: 'drawer',
  density: 'standard',
  fontSize: 'default',
  sidebarMode: 'dark',
  sidebarCollapsed: false,
  sidebarWidth: 248,
  motionLevel: 'subtle',
  surfaceWidth: 'wide',
  workspaceMode: 'simple'
};

export const iafDefaultBrandConfig: IafBrandConfig = {
  brandNameKey: 'app.name',
  loginHeroTitleKey: 'auth.brandTitle',
  loginHeroSubtitleKey: 'auth.brandSubtitle',
  loginOpsTitleKey: 'auth.opsTitle',
  loginOpsDescriptionKey: 'auth.opsDescription',
  loginTelemetryKeys: ['availability', 'throughput', 'exceptions'],
  loginBackground: { type: 'token' },
  loginTemplate: 'standard-industrial'
};

export const iafShellTokens: Record<IafThemeName, IafShellTokens> = {
  'light-industrial': {
    sidebarBg: '#0f172a',
    sidebarPanel: '#162033',
    sidebarBorder: '#2b3a52',
    sidebarText: '#f1f7fb',
    sidebarMuted: '#b8c7d4',
    sidebarAccent: '#22d3ee',
    sidebarAvatarText: '#082f3b',
    sidebarAvatarGlow: 'rgba(34, 211, 238, 0.18)',
    sidebarShadow: '8px 0 24px rgba(15, 23, 42, 0.16)',
    sidebarActiveBg: '#1d4252',
    topbarBg: '#ffffff',
    topbarAccent: '#059669',
    topbarShadow: '0 1px 0 rgba(15, 23, 42, 0.04)'
  },
  'dark-industrial': {
    sidebarBg: '#07131c',
    sidebarPanel: '#0b1823',
    sidebarBorder: '#223746',
    sidebarText: '#e5edf3',
    sidebarMuted: '#8ba1b2',
    sidebarAccent: '#34b6d8',
    sidebarAvatarText: '#082f3b',
    sidebarAvatarGlow: 'rgba(52, 182, 216, 0.18)',
    sidebarShadow: '8px 0 24px rgba(0, 0, 0, 0.24)',
    sidebarActiveBg: '#123747',
    topbarBg: '#111f2a',
    topbarAccent: '#34b6d8'
  },
  'compact-industrial': {
    sidebarBg: '#101827',
    sidebarPanel: '#162033',
    sidebarBorder: '#2b3a52',
    sidebarText: '#f1f7fb',
    sidebarMuted: '#b8c7d4',
    sidebarAccent: '#0ea5a4',
    sidebarAvatarText: '#ffffff',
    sidebarAvatarGlow: 'rgba(14, 165, 164, 0.18)',
    sidebarShadow: '8px 0 20px rgba(15, 23, 42, 0.14)',
    sidebarActiveBg: '#173a46',
    topbarBg: '#ffffff',
    topbarAccent: '#0f766e',
    topbarShadow: '0 1px 0 rgba(15, 23, 42, 0.05)'
  },
  'dashboard-industrial': {
    sidebarBg: '#06111a',
    sidebarPanel: '#0a1823',
    sidebarBorder: '#1d3a49',
    sidebarText: '#e5f5fa',
    sidebarMuted: '#93aebd',
    sidebarAccent: '#22d3ee',
    sidebarAvatarText: '#062533',
    sidebarAvatarGlow: 'rgba(34, 211, 238, 0.2)',
    sidebarShadow: '10px 0 30px rgba(0, 0, 0, 0.28)',
    sidebarActiveBg: '#123747',
    topbarBg: '#0a1823',
    topbarAccent: '#22d3ee'
  },
  'mobile-work': {
    sidebarBg: '#123029',
    sidebarPanel: '#174238',
    sidebarBorder: '#2c5b4f',
    sidebarText: '#f1fbf8',
    sidebarMuted: '#c1d8d1',
    sidebarAccent: '#8dd7bf',
    sidebarAvatarText: '#123029',
    sidebarAvatarGlow: 'rgba(141, 215, 191, 0.2)',
    sidebarShadow: '8px 0 24px rgba(18, 48, 41, 0.18)',
    sidebarActiveBg: '#1f584b',
    topbarBg: '#ffffff',
    topbarAccent: '#0f766e',
    topbarShadow: '0 1px 0 rgba(15, 23, 42, 0.06)'
  },
  'high-contrast': {
    sidebarBg: '#000000',
    sidebarPanel: '#111111',
    sidebarBorder: '#ffffff',
    sidebarText: '#ffffff',
    sidebarMuted: '#d9d9d9',
    sidebarAccent: '#ffd400',
    sidebarAvatarText: '#000000',
    sidebarAvatarGlow: 'rgba(255, 212, 0, 0.3)',
    sidebarShadow: '8px 0 0 rgba(255, 255, 255, 0.16)',
    sidebarActiveBg: '#262626',
    topbarBg: '#000000',
    topbarAccent: '#ffd400'
  },
  'customer-brand': {
    sidebarBg: '#102a43',
    sidebarPanel: '#183b56',
    sidebarBorder: '#2f5f82',
    sidebarText: '#f0f7fb',
    sidebarMuted: '#bcccdc',
    sidebarAccent: '#38bdf8',
    sidebarAvatarText: '#082f49',
    sidebarAvatarGlow: 'rgba(56, 189, 248, 0.18)',
    sidebarShadow: '8px 0 24px rgba(16, 42, 67, 0.18)',
    sidebarActiveBg: '#24506d',
    topbarBg: '#ffffff',
    topbarAccent: '#0ea5e9',
    topbarShadow: '0 1px 0 rgba(15, 23, 42, 0.06)'
  }
};

export const iafLightShellTokens: Record<IafThemeName, IafShellTokens> = {
  'light-industrial': {
    sidebarBg: '#f8fafc',
    sidebarPanel: '#ffffff',
    sidebarBorder: '#cbd8e5',
    sidebarText: '#0f172a',
    sidebarMuted: '#475569',
    sidebarAccent: '#0f766e',
    sidebarAvatarText: '#ffffff',
    sidebarAvatarGlow: 'rgba(15, 118, 110, 0.18)',
    sidebarShadow: '8px 0 24px rgba(15, 23, 42, 0.08)',
    sidebarActiveBg: '#d7efe9',
    topbarBg: '#ffffff',
    topbarAccent: '#0f766e',
    topbarShadow: '0 1px 0 rgba(15, 23, 42, 0.06)'
  },
  'dark-industrial': {
    sidebarBg: '#10202c',
    sidebarPanel: '#142838',
    sidebarBorder: '#2f4555',
    sidebarText: '#edf6fa',
    sidebarMuted: '#a9bac8',
    sidebarAccent: '#5fcce8',
    sidebarAvatarText: '#082f3b',
    sidebarAvatarGlow: 'rgba(95, 204, 232, 0.18)',
    sidebarShadow: '8px 0 24px rgba(0, 0, 0, 0.24)',
    sidebarActiveBg: '#1e4457',
    topbarBg: '#111f2a',
    topbarAccent: '#5fcce8'
  },
  'compact-industrial': {
    sidebarBg: '#f8fafc',
    sidebarPanel: '#ffffff',
    sidebarBorder: '#cbd8e5',
    sidebarText: '#0f172a',
    sidebarMuted: '#475569',
    sidebarAccent: '#0f766e',
    sidebarAvatarText: '#ffffff',
    sidebarAvatarGlow: 'rgba(15, 118, 110, 0.18)',
    sidebarShadow: '8px 0 18px rgba(15, 23, 42, 0.07)',
    sidebarActiveBg: '#d7efe9',
    topbarBg: '#ffffff',
    topbarAccent: '#0f766e',
    topbarShadow: '0 1px 0 rgba(15, 23, 42, 0.06)'
  },
  'dashboard-industrial': {
    sidebarBg: '#10202c',
    sidebarPanel: '#142838',
    sidebarBorder: '#2f4555',
    sidebarText: '#edf6fa',
    sidebarMuted: '#a9bac8',
    sidebarAccent: '#5fcce8',
    sidebarAvatarText: '#082f3b',
    sidebarAvatarGlow: 'rgba(95, 204, 232, 0.18)',
    sidebarShadow: '8px 0 24px rgba(0, 0, 0, 0.24)',
    sidebarActiveBg: '#1e4457',
    topbarBg: '#111f2a',
    topbarAccent: '#5fcce8'
  },
  'mobile-work': {
    sidebarBg: '#f5fbf8',
    sidebarPanel: '#ffffff',
    sidebarBorder: '#bfd8cf',
    sidebarText: '#123029',
    sidebarMuted: '#44665e',
    sidebarAccent: '#0f766e',
    sidebarAvatarText: '#ffffff',
    sidebarAvatarGlow: 'rgba(15, 118, 110, 0.18)',
    sidebarShadow: '8px 0 24px rgba(18, 48, 41, 0.08)',
    sidebarActiveBg: '#d9f0e8',
    topbarBg: '#ffffff',
    topbarAccent: '#0f766e',
    topbarShadow: '0 1px 0 rgba(15, 23, 42, 0.06)'
  },
  'high-contrast': {
    sidebarBg: '#ffffff',
    sidebarPanel: '#ffffff',
    sidebarBorder: '#000000',
    sidebarText: '#000000',
    sidebarMuted: '#1f1f1f',
    sidebarAccent: '#005fcc',
    sidebarAvatarText: '#ffffff',
    sidebarAvatarGlow: 'rgba(0, 95, 204, 0.25)',
    sidebarShadow: '8px 0 0 rgba(0, 0, 0, 0.16)',
    sidebarActiveBg: '#e6f0ff',
    topbarBg: '#ffffff',
    topbarAccent: '#005fcc',
    topbarShadow: '0 1px 0 #000000'
  },
  'customer-brand': {
    sidebarBg: '#f5f9fc',
    sidebarPanel: '#ffffff',
    sidebarBorder: '#cbd8e5',
    sidebarText: '#102a43',
    sidebarMuted: '#486581',
    sidebarAccent: '#0ea5e9',
    sidebarAvatarText: '#ffffff',
    sidebarAvatarGlow: 'rgba(14, 165, 233, 0.18)',
    sidebarShadow: '8px 0 24px rgba(16, 42, 67, 0.08)',
    sidebarActiveBg: '#e0f2fe',
    topbarBg: '#ffffff',
    topbarAccent: '#0ea5e9',
    topbarShadow: '0 1px 0 rgba(15, 23, 42, 0.06)'
  }
};

export const iafSurfaceWidths: Record<IafSurfaceWidth, string> = {
  standard: 'min(90vw, 760px)',
  wide: 'min(92vw, 960px)',
  'extra-wide': 'min(96vw, 1120px)'
};

export const iafDefaultLoginTemplateTokens: IafLoginTemplateTokens = {
  terminal: {
    accent: '#34d399',
    accentBg: 'rgba(16, 185, 129, 0.1)',
    accentBorder: 'rgba(52, 211, 153, 0.5)',
    controlBg: '#020617',
    controlText: '#34d399',
    text: '#e2e8f0',
    muted: '#94a3b8',
    subtle: '#64748b',
    border: '#1e293b',
    panel: 'rgba(15, 23, 42, 0.82)',
    pageBg: '#020617',
    gridLine: 'rgba(148, 163, 184, 0.18)',
    glow: 'rgba(16, 185, 129, 0.14)'
  },
  glass: {
    accent: '#22d3ee',
    accentBg: '#0891b2',
    accentBorder: 'rgba(34, 211, 238, 0.42)',
    controlBg: 'rgba(15, 23, 42, 0.62)',
    controlText: '#f8fafc',
    text: '#f8fafc',
    muted: '#94a3b8',
    subtle: '#64748b',
    border: 'rgba(30, 41, 59, 0.95)',
    panel: 'rgba(2, 6, 23, 0.45)',
    pageBg: '#020617',
    gridLine: '#0f172a',
    glow: 'rgba(34, 211, 238, 0.18)'
  },
  brutalist: {
    accent: '#f59e0b',
    accentBg: 'rgba(245, 158, 11, 0.16)',
    accentBorder: 'rgba(245, 158, 11, 0.42)',
    controlBg: '#111111',
    controlText: '#f59e0b',
    text: '#eeeeee',
    muted: '#94a3b8',
    subtle: '#64748b',
    border: 'rgba(245, 158, 11, 0.34)',
    panel: '#000000',
    pageBg: '#111111',
    gridLine: 'rgba(245, 158, 11, 0.24)',
    glow: 'rgba(245, 158, 11, 0.16)'
  },
  standard: {
    heroOverlay: 'linear-gradient(90deg, rgba(2, 6, 23, 0.94) 0%, rgba(15, 23, 42, 0.78) 46%, rgba(15, 23, 42, 0.46) 100%), linear-gradient(180deg, rgba(8, 47, 73, 0.22), rgba(2, 6, 23, 0.52))',
    heroBg: '#07111f',
    heroLogo: '#e0f2fe',
    heroText: '#ffffff',
    heroMuted: '#dbeafe',
    formBg: '#ffffff',
    formBorder: '#cbd5e1',
    formShadow: '0 28px 72px rgba(15, 23, 42, 0.2)'
  },
  bento: {
    imageOverlay: 'linear-gradient(rgba(15, 23, 42, 0.82), rgba(15, 23, 42, 0.86))',
    featureBg: 'rgba(15, 23, 42, 0.82)',
    featureBorder: 'rgba(148, 163, 184, 0.24)',
    formShadow: '0 24px 56px rgba(15, 23, 42, 0.12)'
  }
};

const createDesignTokens = ({
  primary,
  success,
  successBg,
  warning,
  warningBg,
  error,
  errorBg,
  info,
  infoBg,
  neutral,
  neutralBg,
  panelBg,
  panelBorder,
  panelMutedBg,
  toolbarBg,
  insetBg,
  radius = 6,
  spacing = 16,
  fontSize = 14,
  pageHeaderHeight = 56,
  sidebarWidth = 248,
  mobileActionButtonHeight = 48,
  categorical,
  sequential,
  elevation,
  focusRing,
  kanban,
  dashboard,
  designer,
  loginTemplates = iafDefaultLoginTemplateTokens
}: {
  primary: string;
  success: string;
  successBg: string;
  warning: string;
  warningBg: string;
  error: string;
  errorBg: string;
  info: string;
  infoBg: string;
  neutral: string;
  neutralBg: string;
  panelBg: string;
  panelBorder: string;
  panelMutedBg: string;
  toolbarBg: string;
  insetBg: string;
  radius?: number;
  spacing?: number;
  fontSize?: number;
  pageHeaderHeight?: number;
  sidebarWidth?: number;
  mobileActionButtonHeight?: number;
  categorical: readonly string[];
  sequential: readonly string[];
  elevation: IafDesignTokens['elevation'];
  focusRing: string;
  kanban: IafDesignTokens['kanban'];
  dashboard: IafDesignTokens['dashboard'];
  designer: IafDesignTokens['designer'];
  loginTemplates?: IafLoginTemplateTokens;
}): IafDesignTokens => ({
  global: {
    colorPrimary: primary,
    colorSuccess: success,
    colorWarning: warning,
    colorError: error,
    colorInfo: info,
    fontSizeBase: fontSize,
    borderRadius: radius,
    spacing
  },
  semantic: {
    statusDraftColor: neutral,
    statusDraftBg: neutralBg,
    statusPendingColor: warning,
    statusPendingBg: warningBg,
    statusApprovedColor: success,
    statusApprovedBg: successBg,
    statusRejectedColor: error,
    statusRejectedBg: errorBg,
    statusProcessingColor: info,
    statusProcessingBg: infoBg,
    statusClosedColor: neutral,
    statusClosedBg: neutralBg,
    inventoryAvailableColor: success,
    inventoryAvailableBg: successBg,
    inventoryFrozenColor: warning,
    inventoryFrozenBg: warningBg,
    taskUrgentColor: error,
    taskUrgentBg: errorBg
  },
  component: {
    tableHeaderBg: toolbarBg,
    formSectionBg: panelMutedBg,
    pageHeaderHeight,
    sidebarWidth,
    mobileActionButtonHeight,
    dashboardCardBg: dashboard.metricBg
  },
  surface: {
    panelBg,
    panelBorder,
    panelMutedBg,
    toolbarBg,
    insetBg
  },
  status: {
    success,
    successBg,
    warning,
    warningBg,
    error,
    errorBg,
    info,
    infoBg,
    neutral,
    neutralBg
  },
  dataViz: {
    categorical,
    sequential,
    exception: error,
    forecast: categorical[4] ?? info
  },
  elevation,
  focusRing,
  kanban,
  dashboard,
  designer,
  loginTemplates
});

export const iafDesignTokens: Record<IafThemeName, IafDesignTokens> = {
  'light-industrial': createDesignTokens({
    primary: '#334155',
    success: '#047857',
    successBg: '#dff3ec',
    warning: '#b88416',
    warningBg: '#fff4d6',
    error: '#dc2626',
    errorBg: '#fee2e2',
    info: '#0284c7',
    infoBg: '#dbeafe',
    neutral: '#475569',
    neutralBg: '#f1f5f9',
    panelBg: '#ffffff',
    panelBorder: '#e2e8f0',
    panelMutedBg: '#f8fafc',
    toolbarBg: '#f1f5f9',
    insetBg: '#eef4f6',
    categorical: ['#0284c7', '#059669', '#d8a83f', '#dc2626', '#7c3aed', '#0f766e', '#475569'],
    sequential: ['#e0f2fe', '#7dd3fc', '#0284c7', '#075985'],
    elevation: {
      level1: '0 1px 2px rgba(15, 23, 42, 0.04)',
      level2: '0 8px 24px rgba(15, 23, 42, 0.08)',
      level3: '0 18px 42px rgba(15, 23, 42, 0.12)'
    },
    focusRing: '0 0 0 3px rgba(2, 132, 199, 0.18)',
    kanban: {
      columnBg: '#f1f5f9',
      cardBg: '#ffffff',
      cardDraggingBorder: '#0284c7',
      wipWarningBg: '#fff4d6'
    },
    dashboard: {
      metricBg: '#ffffff',
      metricAccent: '#059669',
      gridLine: '#e2e8f0'
    },
    designer: {
      canvasBg: '#f8fafc',
      nodeBg: '#ffffff',
      nodeBorder: '#cbd5e1',
      connector: '#64748b'
    }
  }),
  'dark-industrial': createDesignTokens({
    primary: '#34b6d8',
    success: '#4ade80',
    successBg: '#123525',
    warning: '#d8a83f',
    warningBg: '#3a2d13',
    error: '#ff7272',
    errorBg: '#3a1d20',
    info: '#5dade2',
    infoBg: '#123747',
    neutral: '#a9bac8',
    neutralBg: '#142838',
    panelBg: '#111f2a',
    panelBorder: '#2a3f4f',
    panelMutedBg: '#0b1823',
    toolbarBg: '#142838',
    insetBg: '#10202c',
    categorical: ['#5dade2', '#4ade80', '#d8a83f', '#ff7272', '#a78bfa', '#5eead4', '#a9bac8'],
    sequential: ['#0b1823', '#123747', '#34b6d8', '#a5f3fc'],
    elevation: {
      level1: '0 1px 0 rgba(255, 255, 255, 0.04)',
      level2: '0 12px 28px rgba(0, 0, 0, 0.24)',
      level3: '0 24px 56px rgba(0, 0, 0, 0.34)'
    },
    focusRing: '0 0 0 3px rgba(52, 182, 216, 0.24)',
    kanban: {
      columnBg: '#0b1823',
      cardBg: '#111f2a',
      cardDraggingBorder: '#34b6d8',
      wipWarningBg: '#3a2d13'
    },
    dashboard: {
      metricBg: '#111f2a',
      metricAccent: '#34b6d8',
      gridLine: '#223746'
    },
    designer: {
      canvasBg: '#07131c',
      nodeBg: '#111f2a',
      nodeBorder: '#2a3f4f',
      connector: '#8ba1b2'
    }
  }),
  'compact-industrial': createDesignTokens({
    primary: '#334155',
    success: '#047857',
    successBg: '#dff3ec',
    warning: '#a96f00',
    warningBg: '#fff4d6',
    error: '#c81e1e',
    errorBg: '#fee2e2',
    info: '#0369a1',
    infoBg: '#dbeafe',
    neutral: '#475569',
    neutralBg: '#f1f5f9',
    panelBg: '#ffffff',
    panelBorder: '#d8e2ea',
    panelMutedBg: '#f8fafc',
    toolbarBg: '#eef4f6',
    insetBg: '#edf4f7',
    spacing: 12,
    pageHeaderHeight: 48,
    sidebarWidth: 232,
    mobileActionButtonHeight: 44,
    categorical: ['#0369a1', '#047857', '#a96f00', '#c81e1e', '#6d28d9', '#0f766e', '#475569'],
    sequential: ['#e0f2fe', '#7dd3fc', '#0369a1', '#075985'],
    elevation: {
      level1: '0 1px 2px rgba(15, 23, 42, 0.04)',
      level2: '0 6px 18px rgba(15, 23, 42, 0.07)',
      level3: '0 14px 34px rgba(15, 23, 42, 0.1)'
    },
    focusRing: '0 0 0 3px rgba(3, 105, 161, 0.18)',
    kanban: {
      columnBg: '#f1f5f9',
      cardBg: '#ffffff',
      cardDraggingBorder: '#0369a1',
      wipWarningBg: '#fff4d6'
    },
    dashboard: {
      metricBg: '#ffffff',
      metricAccent: '#047857',
      gridLine: '#d8e2ea'
    },
    designer: {
      canvasBg: '#f8fafc',
      nodeBg: '#ffffff',
      nodeBorder: '#cbd5e1',
      connector: '#64748b'
    }
  }),
  'dashboard-industrial': createDesignTokens({
    primary: '#22d3ee',
    success: '#4ade80',
    successBg: '#123525',
    warning: '#f4c95d',
    warningBg: '#3a2d13',
    error: '#ff7272',
    errorBg: '#3a1d20',
    info: '#67e8f9',
    infoBg: '#123747',
    neutral: '#b7c6d2',
    neutralBg: '#142838',
    panelBg: '#0b1823',
    panelBorder: '#1d3a49',
    panelMutedBg: '#07131c',
    toolbarBg: '#102838',
    insetBg: '#06111a',
    pageHeaderHeight: 60,
    categorical: ['#67e8f9', '#4ade80', '#f4c95d', '#ff7272', '#a78bfa', '#5eead4', '#b7c6d2'],
    sequential: ['#06111a', '#123747', '#22d3ee', '#a5f3fc'],
    elevation: {
      level1: '0 1px 0 rgba(255, 255, 255, 0.04)',
      level2: '0 14px 32px rgba(0, 0, 0, 0.28)',
      level3: '0 28px 64px rgba(0, 0, 0, 0.38)'
    },
    focusRing: '0 0 0 3px rgba(34, 211, 238, 0.26)',
    kanban: {
      columnBg: '#07131c',
      cardBg: '#0b1823',
      cardDraggingBorder: '#22d3ee',
      wipWarningBg: '#3a2d13'
    },
    dashboard: {
      metricBg: '#0b1823',
      metricAccent: '#22d3ee',
      gridLine: '#1d3a49'
    },
    designer: {
      canvasBg: '#06111a',
      nodeBg: '#0b1823',
      nodeBorder: '#1d3a49',
      connector: '#93aebd'
    }
  }),
  'mobile-work': createDesignTokens({
    primary: '#0f766e',
    success: '#047857',
    successBg: '#dff3ec',
    warning: '#b88416',
    warningBg: '#fff4d6',
    error: '#dc2626',
    errorBg: '#fee2e2',
    info: '#0284c7',
    infoBg: '#dbeafe',
    neutral: '#44665e',
    neutralBg: '#eef8f4',
    panelBg: '#ffffff',
    panelBorder: '#d5e7df',
    panelMutedBg: '#f5fbf8',
    toolbarBg: '#eef8f4',
    insetBg: '#e6f4ee',
    spacing: 20,
    fontSize: 16,
    pageHeaderHeight: 64,
    sidebarWidth: 268,
    mobileActionButtonHeight: 52,
    categorical: ['#0284c7', '#047857', '#b88416', '#dc2626', '#7c3aed', '#0f766e', '#44665e'],
    sequential: ['#dff3ec', '#8dd7bf', '#0f766e', '#134e4a'],
    elevation: {
      level1: '0 1px 2px rgba(18, 48, 41, 0.04)',
      level2: '0 10px 26px rgba(18, 48, 41, 0.08)',
      level3: '0 20px 48px rgba(18, 48, 41, 0.12)'
    },
    focusRing: '0 0 0 3px rgba(15, 118, 110, 0.2)',
    kanban: {
      columnBg: '#eef8f4',
      cardBg: '#ffffff',
      cardDraggingBorder: '#0f766e',
      wipWarningBg: '#fff4d6'
    },
    dashboard: {
      metricBg: '#ffffff',
      metricAccent: '#0f766e',
      gridLine: '#d5e7df'
    },
    designer: {
      canvasBg: '#f5fbf8',
      nodeBg: '#ffffff',
      nodeBorder: '#bfd8cf',
      connector: '#44665e'
    }
  }),
  'high-contrast': createDesignTokens({
    primary: '#ffd400',
    success: '#00d084',
    successBg: '#002d1f',
    warning: '#ffd400',
    warningBg: '#3d3100',
    error: '#ff6b6b',
    errorBg: '#3d0000',
    info: '#7cc7ff',
    infoBg: '#002b4d',
    neutral: '#ffffff',
    neutralBg: '#1f1f1f',
    panelBg: '#000000',
    panelBorder: '#ffffff',
    panelMutedBg: '#111111',
    toolbarBg: '#1f1f1f',
    insetBg: '#000000',
    radius: 4,
    spacing: 18,
    fontSize: 16,
    pageHeaderHeight: 60,
    categorical: ['#7cc7ff', '#00d084', '#ffd400', '#ff6b6b', '#c084fc', '#5eead4', '#ffffff'],
    sequential: ['#000000', '#002b4d', '#0070cc', '#7cc7ff'],
    elevation: {
      level1: '0 0 0 1px rgba(255, 255, 255, 0.8)',
      level2: '0 0 0 2px rgba(255, 255, 255, 0.9)',
      level3: '0 0 0 3px rgba(255, 212, 0, 0.9)'
    },
    focusRing: '0 0 0 3px rgba(255, 212, 0, 0.7)',
    kanban: {
      columnBg: '#111111',
      cardBg: '#000000',
      cardDraggingBorder: '#ffd400',
      wipWarningBg: '#3d3100'
    },
    dashboard: {
      metricBg: '#000000',
      metricAccent: '#ffd400',
      gridLine: '#ffffff'
    },
    designer: {
      canvasBg: '#000000',
      nodeBg: '#111111',
      nodeBorder: '#ffffff',
      connector: '#ffd400'
    }
  }),
  'customer-brand': createDesignTokens({
    primary: '#0ea5e9',
    success: '#047857',
    successBg: '#dff3ec',
    warning: '#b88416',
    warningBg: '#fff4d6',
    error: '#dc2626',
    errorBg: '#fee2e2',
    info: '#0ea5e9',
    infoBg: '#e0f2fe',
    neutral: '#486581',
    neutralBg: '#f0f4f8',
    panelBg: '#ffffff',
    panelBorder: '#d9e2ec',
    panelMutedBg: '#f5f9fc',
    toolbarBg: '#f0f4f8',
    insetBg: '#eaf3f9',
    categorical: ['#0ea5e9', '#047857', '#b88416', '#dc2626', '#7c3aed', '#0f766e', '#486581'],
    sequential: ['#e0f2fe', '#7dd3fc', '#0ea5e9', '#075985'],
    elevation: {
      level1: '0 1px 2px rgba(16, 42, 67, 0.04)',
      level2: '0 8px 24px rgba(16, 42, 67, 0.08)',
      level3: '0 18px 42px rgba(16, 42, 67, 0.12)'
    },
    focusRing: '0 0 0 3px rgba(14, 165, 233, 0.18)',
    kanban: {
      columnBg: '#f0f4f8',
      cardBg: '#ffffff',
      cardDraggingBorder: '#0ea5e9',
      wipWarningBg: '#fff4d6'
    },
    dashboard: {
      metricBg: '#ffffff',
      metricAccent: '#0ea5e9',
      gridLine: '#d9e2ec'
    },
    designer: {
      canvasBg: '#f5f9fc',
      nodeBg: '#ffffff',
      nodeBorder: '#bcccdc',
      connector: '#486581'
    }
  })
};

const fontSizeTokens: Record<IafFontSize, number> = {
  small: 13,
  default: 14,
  large: 16
};

const densityTokens: Record<
  IafDensity,
  {
    controlHeight: number;
    padding: number;
    paddingLG: number;
    tableCellPaddingBlock: number;
    tableCellPaddingInline: number;
    formItemMarginBottom: number;
  }
> = {
  compact: {
    controlHeight: 30,
    padding: 12,
    paddingLG: 16,
    tableCellPaddingBlock: 8,
    tableCellPaddingInline: 10,
    formItemMarginBottom: 14
  },
  standard: {
    controlHeight: 32,
    padding: 16,
    paddingLG: 24,
    tableCellPaddingBlock: 12,
    tableCellPaddingInline: 12,
    formItemMarginBottom: 20
  },
  comfortable: {
    controlHeight: 36,
    padding: 20,
    paddingLG: 28,
    tableCellPaddingBlock: 14,
    tableCellPaddingInline: 16,
    formItemMarginBottom: 24
  }
};

const motionTokens: Record<IafMotionLevel, { motionDurationFast: string; motionDurationMid: string; motionDurationSlow: string }> = {
  none: {
    motionDurationFast: '0s',
    motionDurationMid: '0s',
    motionDurationSlow: '0s'
  },
  subtle: {
    motionDurationFast: '0.08s',
    motionDurationMid: '0.14s',
    motionDurationSlow: '0.2s'
  },
  standard: {
    motionDurationFast: '0.1s',
    motionDurationMid: '0.2s',
    motionDurationSlow: '0.3s'
  }
};

const themeConfigs: Record<IafThemeName, ThemeConfig> = {
  'light-industrial': {
    token: {
      colorPrimary: '#334155',
      colorSuccess: '#059669',
      colorWarning: '#b88416',
      colorError: '#dc2626',
      colorInfo: '#0284c7',
      colorText: '#0f172a',
      colorTextSecondary: '#475569',
      colorTextTertiary: '#64748b',
      colorBgLayout: '#f8fafc',
      colorBgContainer: '#ffffff',
      colorBgElevated: '#ffffff',
      colorBorder: '#e2e8f0',
      colorSplit: '#e5edf3',
      borderRadius: 6,
      fontSize: 14
    },
    components: {
      Layout: {
        headerBg: '#ffffff',
        siderBg: '#f8fafc'
      },
      Table: {
        headerBg: '#f1f5f9',
        rowHoverBg: '#f8fbfd',
        borderColor: '#e2e8f0'
      },
      Menu: {
        itemSelectedBg: '#e6f3f1',
        itemSelectedColor: '#047857',
        itemHoverBg: '#eef4f6',
        itemHoverColor: '#334155'
      },
      Button: {
        defaultBorderColor: '#cbd5e1'
      },
      Card: {
        headerBg: '#ffffff',
        colorBorderSecondary: '#e2e8f0',
        boxShadowTertiary: '0 1px 2px rgba(15, 23, 42, 0.04)'
      },
      Tabs: {
        itemSelectedColor: '#047857',
        inkBarColor: '#059669'
      },
      Drawer: {
        footerPaddingBlock: 14,
        footerPaddingInline: 20
      },
      Modal: {
        headerBg: '#ffffff',
        footerBg: '#ffffff'
      }
    }
  },
  'dark-industrial': {
    algorithm: antdTheme.darkAlgorithm,
    token: {
      colorPrimary: '#34b6d8',
      colorSuccess: '#4ade80',
      colorWarning: '#d8a83f',
      colorError: '#ff7272',
      colorInfo: '#5dade2',
      colorText: '#e5edf3',
      colorTextSecondary: '#a9bac8',
      colorTextTertiary: '#8296a6',
      colorBgLayout: '#07131c',
      colorBgContainer: '#111f2a',
      colorBgElevated: '#142838',
      colorBorder: '#2a3f4f',
      colorSplit: '#223746',
      borderRadius: 6,
      fontSize: 14
    },
    components: {
      Layout: {
        headerBg: '#111f2a',
        siderBg: '#0b1823'
      },
      Table: {
        headerBg: '#182c3a',
        rowHoverBg: '#152b3a',
        borderColor: '#2a3f4f'
      },
      Menu: {
        itemSelectedBg: '#123747',
        itemSelectedColor: '#5fcce8',
        itemHoverBg: '#142838',
        itemHoverColor: '#d5edf5'
      },
      Button: {
        defaultBorderColor: '#345064'
      },
      Card: {
        headerBg: '#111f2a',
        colorBorderSecondary: '#2a3f4f',
        boxShadowTertiary: '0 1px 0 rgba(255, 255, 255, 0.04)'
      },
      Tabs: {
        itemSelectedColor: '#5fcce8',
        inkBarColor: '#34b6d8'
      },
      Drawer: {
        footerPaddingBlock: 14,
        footerPaddingInline: 20
      },
      Modal: {
        headerBg: '#111f2a',
        footerBg: '#111f2a'
      }
    }
  },
  'compact-industrial': {
    token: {
      colorPrimary: '#334155',
      colorSuccess: '#047857',
      colorWarning: '#a96f00',
      colorError: '#c81e1e',
      colorInfo: '#0369a1',
      colorText: '#0f172a',
      colorTextSecondary: '#475569',
      colorTextTertiary: '#64748b',
      colorBgLayout: '#f8fafc',
      colorBgContainer: '#ffffff',
      colorBgElevated: '#ffffff',
      colorBorder: '#d8e2ea',
      colorSplit: '#e5edf3',
      borderRadius: 5,
      fontSize: 13
    },
    components: {
      Layout: {
        headerBg: '#ffffff',
        siderBg: '#f8fafc'
      },
      Table: {
        headerBg: '#eef4f6',
        rowHoverBg: '#f8fbfd',
        borderColor: '#d8e2ea'
      },
      Menu: {
        itemSelectedBg: '#d7efe9',
        itemSelectedColor: '#047857',
        itemHoverBg: '#eef4f6',
        itemHoverColor: '#334155'
      },
      Button: {
        defaultBorderColor: '#cbd5e1'
      },
      Card: {
        headerBg: '#ffffff',
        colorBorderSecondary: '#d8e2ea',
        boxShadowTertiary: '0 1px 2px rgba(15, 23, 42, 0.04)'
      },
      Tabs: {
        itemSelectedColor: '#047857',
        inkBarColor: '#0f766e'
      },
      Drawer: {
        footerPaddingBlock: 12,
        footerPaddingInline: 18
      },
      Modal: {
        headerBg: '#ffffff',
        footerBg: '#ffffff'
      }
    }
  },
  'dashboard-industrial': {
    algorithm: antdTheme.darkAlgorithm,
    token: {
      colorPrimary: '#22d3ee',
      colorSuccess: '#4ade80',
      colorWarning: '#f4c95d',
      colorError: '#ff7272',
      colorInfo: '#67e8f9',
      colorText: '#e5f5fa',
      colorTextSecondary: '#b7c6d2',
      colorTextTertiary: '#93aebd',
      colorBgLayout: '#06111a',
      colorBgContainer: '#0b1823',
      colorBgElevated: '#102838',
      colorBorder: '#1d3a49',
      colorSplit: '#1d3a49',
      borderRadius: 6,
      fontSize: 14
    },
    components: {
      Layout: {
        headerBg: '#0a1823',
        siderBg: '#06111a'
      },
      Table: {
        headerBg: '#102838',
        rowHoverBg: '#123747',
        borderColor: '#1d3a49'
      },
      Menu: {
        itemSelectedBg: '#123747',
        itemSelectedColor: '#67e8f9',
        itemHoverBg: '#102838',
        itemHoverColor: '#e5f5fa'
      },
      Button: {
        defaultBorderColor: '#2f5f82'
      },
      Card: {
        headerBg: '#0b1823',
        colorBorderSecondary: '#1d3a49',
        boxShadowTertiary: '0 1px 0 rgba(255, 255, 255, 0.04)'
      },
      Tabs: {
        itemSelectedColor: '#67e8f9',
        inkBarColor: '#22d3ee'
      },
      Drawer: {
        footerPaddingBlock: 14,
        footerPaddingInline: 20
      },
      Modal: {
        headerBg: '#0b1823',
        footerBg: '#0b1823'
      }
    }
  },
  'mobile-work': {
    token: {
      colorPrimary: '#0f766e',
      colorSuccess: '#047857',
      colorWarning: '#b88416',
      colorError: '#dc2626',
      colorInfo: '#0284c7',
      colorText: '#123029',
      colorTextSecondary: '#44665e',
      colorTextTertiary: '#5f7f77',
      colorBgLayout: '#f5fbf8',
      colorBgContainer: '#ffffff',
      colorBgElevated: '#ffffff',
      colorBorder: '#d5e7df',
      colorSplit: '#d5e7df',
      borderRadius: 8,
      fontSize: 16
    },
    components: {
      Layout: {
        headerBg: '#ffffff',
        siderBg: '#f5fbf8'
      },
      Table: {
        headerBg: '#eef8f4',
        rowHoverBg: '#f5fbf8',
        borderColor: '#d5e7df'
      },
      Menu: {
        itemSelectedBg: '#d9f0e8',
        itemSelectedColor: '#0f766e',
        itemHoverBg: '#eef8f4',
        itemHoverColor: '#123029'
      },
      Button: {
        defaultBorderColor: '#bfd8cf'
      },
      Card: {
        headerBg: '#ffffff',
        colorBorderSecondary: '#d5e7df',
        boxShadowTertiary: '0 1px 2px rgba(18, 48, 41, 0.04)'
      },
      Tabs: {
        itemSelectedColor: '#0f766e',
        inkBarColor: '#0f766e'
      },
      Drawer: {
        footerPaddingBlock: 16,
        footerPaddingInline: 22
      },
      Modal: {
        headerBg: '#ffffff',
        footerBg: '#ffffff'
      }
    }
  },
  'high-contrast': {
    algorithm: antdTheme.darkAlgorithm,
    token: {
      colorPrimary: '#ffd400',
      colorSuccess: '#00d084',
      colorWarning: '#ffd400',
      colorError: '#ff6b6b',
      colorInfo: '#7cc7ff',
      colorText: '#ffffff',
      colorTextSecondary: '#d9d9d9',
      colorTextTertiary: '#c7c7c7',
      colorBgLayout: '#000000',
      colorBgContainer: '#000000',
      colorBgElevated: '#111111',
      colorBorder: '#ffffff',
      colorSplit: '#ffffff',
      borderRadius: 4,
      fontSize: 16
    },
    components: {
      Layout: {
        headerBg: '#000000',
        siderBg: '#000000'
      },
      Table: {
        headerBg: '#1f1f1f',
        rowHoverBg: '#262626',
        borderColor: '#ffffff'
      },
      Menu: {
        itemSelectedBg: '#262626',
        itemSelectedColor: '#ffd400',
        itemHoverBg: '#1f1f1f',
        itemHoverColor: '#ffffff'
      },
      Button: {
        defaultBorderColor: '#ffffff'
      },
      Card: {
        headerBg: '#000000',
        colorBorderSecondary: '#ffffff',
        boxShadowTertiary: '0 0 0 1px rgba(255, 255, 255, 0.8)'
      },
      Tabs: {
        itemSelectedColor: '#ffd400',
        inkBarColor: '#ffd400'
      },
      Drawer: {
        footerPaddingBlock: 16,
        footerPaddingInline: 22
      },
      Modal: {
        headerBg: '#000000',
        footerBg: '#000000'
      }
    }
  },
  'customer-brand': {
    token: {
      colorPrimary: '#0ea5e9',
      colorSuccess: '#047857',
      colorWarning: '#b88416',
      colorError: '#dc2626',
      colorInfo: '#0ea5e9',
      colorText: '#102a43',
      colorTextSecondary: '#486581',
      colorTextTertiary: '#627d98',
      colorBgLayout: '#f5f9fc',
      colorBgContainer: '#ffffff',
      colorBgElevated: '#ffffff',
      colorBorder: '#d9e2ec',
      colorSplit: '#d9e2ec',
      borderRadius: 6,
      fontSize: 14
    },
    components: {
      Layout: {
        headerBg: '#ffffff',
        siderBg: '#f5f9fc'
      },
      Table: {
        headerBg: '#f0f4f8',
        rowHoverBg: '#f5f9fc',
        borderColor: '#d9e2ec'
      },
      Menu: {
        itemSelectedBg: '#e0f2fe',
        itemSelectedColor: '#0369a1',
        itemHoverBg: '#eaf3f9',
        itemHoverColor: '#102a43'
      },
      Button: {
        defaultBorderColor: '#bcccdc'
      },
      Card: {
        headerBg: '#ffffff',
        colorBorderSecondary: '#d9e2ec',
        boxShadowTertiary: '0 1px 2px rgba(16, 42, 67, 0.04)'
      },
      Tabs: {
        itemSelectedColor: '#0369a1',
        inkBarColor: '#0ea5e9'
      },
      Drawer: {
        footerPaddingBlock: 14,
        footerPaddingInline: 20
      },
      Modal: {
        headerBg: '#ffffff',
        footerBg: '#ffffff'
      }
    }
  }
};

const isThemeName = (value: unknown): value is IafThemeName =>
  typeof value === 'string' && iafThemeNames.includes(value as IafThemeName);

const isFormInteractionMode = (value: unknown): value is IafFormInteractionMode =>
  value === 'modal' || value === 'drawer' || value === 'page';

const isDensity = (value: unknown): value is IafDensity => value === 'compact' || value === 'standard' || value === 'comfortable';

const isFontSize = (value: unknown): value is IafFontSize => value === 'small' || value === 'default' || value === 'large';

const isSidebarMode = (value: unknown): value is IafSidebarMode => value === 'dark' || value === 'light';

const isMotionLevel = (value: unknown): value is IafMotionLevel => value === 'none' || value === 'subtle' || value === 'standard';

const isSurfaceWidth = (value: unknown): value is IafSurfaceWidth => value === 'standard' || value === 'wide' || value === 'extra-wide';

const isWorkspaceMode = (value: unknown): value is IafWorkspaceMode => value === 'simple' || value === 'expert';

const isSidebarWidth = (value: unknown): value is number =>
  typeof value === 'number' && Number.isFinite(value) && value >= 220 && value <= 360;

const canUseStorage = () => typeof window !== 'undefined' && typeof window.localStorage !== 'undefined';

const createStorageKey = (scope?: string) => (scope ? `${STORAGE_KEY}.${scope}` : STORAGE_KEY);

const loadSettings = (scope?: string): IafExperienceSettings => {
  if (!canUseStorage()) {
    return iafDefaultExperienceSettings;
  }

  const raw = window.localStorage.getItem(createStorageKey(scope)) ?? window.localStorage.getItem(STORAGE_KEY);
  if (!raw) {
    return iafDefaultExperienceSettings;
  }

  try {
    const parsed = JSON.parse(raw) as Partial<IafExperienceSettings>;
    return {
      themeName: isThemeName(parsed.themeName) ? parsed.themeName : iafDefaultExperienceSettings.themeName,
      formInteractionMode: isFormInteractionMode(parsed.formInteractionMode)
        ? parsed.formInteractionMode
        : iafDefaultExperienceSettings.formInteractionMode,
      density: isDensity(parsed.density) ? parsed.density : iafDefaultExperienceSettings.density,
      fontSize: isFontSize(parsed.fontSize) ? parsed.fontSize : iafDefaultExperienceSettings.fontSize,
      sidebarMode: isSidebarMode(parsed.sidebarMode) ? parsed.sidebarMode : iafDefaultExperienceSettings.sidebarMode,
      sidebarCollapsed: typeof parsed.sidebarCollapsed === 'boolean' ? parsed.sidebarCollapsed : iafDefaultExperienceSettings.sidebarCollapsed,
      sidebarWidth: isSidebarWidth(parsed.sidebarWidth) ? parsed.sidebarWidth : iafDefaultExperienceSettings.sidebarWidth,
      motionLevel: isMotionLevel(parsed.motionLevel) ? parsed.motionLevel : iafDefaultExperienceSettings.motionLevel,
      surfaceWidth: isSurfaceWidth(parsed.surfaceWidth) ? parsed.surfaceWidth : iafDefaultExperienceSettings.surfaceWidth,
      workspaceMode: isWorkspaceMode(parsed.workspaceMode) ? parsed.workspaceMode : iafDefaultExperienceSettings.workspaceMode
    };
  } catch {
    return iafDefaultExperienceSettings;
  }
};

const persistSettings = (settings: IafExperienceSettings, scope?: string) => {
  if (canUseStorage()) {
    window.localStorage.setItem(createStorageKey(scope), JSON.stringify(settings));
  }
};

const IafThemeContext = createContext<IafThemeContextValue>({
  ...iafDefaultExperienceSettings,
  settings: iafDefaultExperienceSettings,
  brandConfig: iafDefaultBrandConfig,
  designTokens: iafDesignTokens[iafDefaultExperienceSettings.themeName],
  updateExperienceSettings: () => undefined,
  resetExperienceSettings: () => undefined,
  setPreferenceScope: () => undefined,
  setThemeName: () => undefined,
  setFormInteractionMode: () => undefined
});

export const iafThemes = themeConfigs;

const createRuntimeTheme = (baseTheme: ThemeConfig, settings: IafExperienceSettings): ThemeConfig => {
  const density = densityTokens[settings.density];
  const motion = motionTokens[settings.motionLevel];

  return {
    ...baseTheme,
    token: {
      ...baseTheme.token,
      fontSize: fontSizeTokens[settings.fontSize],
      controlHeight: density.controlHeight,
      padding: density.padding,
      paddingLG: density.paddingLG,
      ...motion
    },
    components: {
      ...baseTheme.components,
      Table: {
        ...baseTheme.components?.Table,
        cellPaddingBlock: density.tableCellPaddingBlock,
        cellPaddingInline: density.tableCellPaddingInline,
        cellPaddingBlockSM: Math.max(6, density.tableCellPaddingBlock - 2),
        cellPaddingInlineSM: Math.max(8, density.tableCellPaddingInline - 2)
      },
      Form: {
        ...baseTheme.components?.Form,
        itemMarginBottom: density.formItemMarginBottom
      },
      Drawer: {
        ...baseTheme.components?.Drawer,
        paddingLG: density.paddingLG
      },
      Modal: {
        ...baseTheme.components?.Modal,
        contentBg: baseTheme.token?.colorBgContainer
      },
      Segmented: {
        ...baseTheme.components?.Segmented,
        trackBg: baseTheme.token?.colorFillAlter,
        itemColor: baseTheme.token?.colorTextSecondary,
        itemHoverColor: baseTheme.token?.colorText,
        itemHoverBg: baseTheme.token?.colorBgElevated,
        itemSelectedBg: baseTheme.token?.colorPrimary,
        itemSelectedColor: settings.themeName === 'high-contrast' ? '#000000' : baseTheme.token?.colorBgContainer,
        itemActiveBg: baseTheme.token?.colorPrimaryActive ?? baseTheme.token?.colorPrimary
      }
    }
  };
};

export const IafThemeProvider = ({ children, brandConfig = iafDefaultBrandConfig }: { children: ReactNode; brandConfig?: IafBrandConfig }) => {
  const [settings, setSettings] = useState<IafExperienceSettings>(() => loadSettings());
  const [preferenceScope, setPreferenceScopeState] = useState<string | undefined>();

  const updateExperienceSettings = useCallback((patch: Partial<IafExperienceSettings>) => {
    setSettings((current) => {
      const next = { ...current, ...patch };
      persistSettings(next, preferenceScope);
      return next;
    });
  }, [preferenceScope]);

  const setPreferenceScope = useCallback((scope?: string) => {
    setPreferenceScopeState(scope);
    setSettings(loadSettings(scope));
  }, []);

  const value = useMemo<IafThemeContextValue>(
    () => ({
      ...settings,
      settings,
      preferenceScope,
      brandConfig,
      designTokens: iafDesignTokens[settings.themeName],
      updateExperienceSettings,
      resetExperienceSettings: () => {
        setSettings(iafDefaultExperienceSettings);
        persistSettings(iafDefaultExperienceSettings, preferenceScope);
      },
      setPreferenceScope,
      setThemeName: (themeName) => {
        updateExperienceSettings({ themeName });
      },
      setFormInteractionMode: (formInteractionMode) => {
        updateExperienceSettings({ formInteractionMode });
      }
    }),
    [brandConfig, preferenceScope, setPreferenceScope, settings, updateExperienceSettings]
  );
  const runtimeTheme = useMemo(() => createRuntimeTheme(themeConfigs[settings.themeName], settings), [settings]);

  return (
    <IafThemeContext.Provider value={value}>
      <ConfigProvider theme={runtimeTheme}>{children}</ConfigProvider>
    </IafThemeContext.Provider>
  );
};

export const useIafTheme = () => useContext(IafThemeContext);
