import type { LoginRequest } from '@iaf/domain-types';
import type { IafBrandConfig, IafDesignTokens, IafLoginTemplateName } from '@iaf/theme';
import {
  ApartmentOutlined,
  ApiOutlined,
  ArrowRightOutlined,
  BuildOutlined,
  CodeOutlined,
  DatabaseOutlined,
  DeploymentUnitOutlined,
  FieldTimeOutlined,
  GoogleOutlined,
  LoginOutlined,
  MessageOutlined,
  SafetyCertificateOutlined,
  ThunderboltOutlined,
  BankOutlined,
  UserOutlined
} from '@ant-design/icons';
import { Button, Checkbox, Form, Input, Space, Typography, theme } from 'antd';
import type { CSSProperties, ReactNode } from 'react';
import type { TFunction } from 'i18next';
import bentoIndustrialImage from '../assets/login-bento-industrial.webp';
import standardIndustrialImage from '../assets/login-standard-industrial.webp';

export interface LoginTemplateProps {
  brandConfig: IafBrandConfig;
  designTokens: IafDesignTokens;
  loading: boolean;
  onSubmit: (values: LoginRequest) => void;
  t: TFunction;
}

const pageBase: CSSProperties = {
  minHeight: '100vh',
  overflow: 'hidden'
};

const brandName = ({ brandConfig, t }: LoginTemplateProps) => t(brandConfig.brandNameKey);

const brandLogo = (props: LoginTemplateProps, style?: CSSProperties) => {
  const { brandConfig, t } = props;
  if (brandConfig.logoUrl) {
    return <img src={brandConfig.logoUrl} alt={brandName(props)} style={{ maxHeight: 32, ...style }} />;
  }
  return (
    <span style={{ display: 'inline-flex', alignItems: 'center', gap: 10, fontWeight: 800, ...style }}>
      <BuildOutlined />
      <span>{t('auth.loginTemplates.brandShort')}</span>
    </span>
  );
};

const resolveHeroBackground = (props: LoginTemplateProps, fallbackImage: string): CSSProperties['backgroundImage'] => {
  const imageUrl = props.brandConfig.loginBackground.type === 'image' ? props.brandConfig.loginBackground.imageUrl : fallbackImage;
  return `${props.designTokens.loginTemplates.standard.heroOverlay}, url(${imageUrl})`;
};

const getDarkPalette = (props: LoginTemplateProps, variant: 'light' | 'terminal' | 'glass' | 'brutalist' | 'bento') =>
  variant === 'terminal' || variant === 'glass' || variant === 'brutalist' ? props.designTokens.loginTemplates[variant] : undefined;

const LoginForm = ({
  props,
  variant,
  submitLabel,
  showRemember = false,
  showForgot = true,
  ssoMode = 'text'
}: {
  props: LoginTemplateProps;
  variant: 'light' | 'terminal' | 'glass' | 'brutalist' | 'bento';
  submitLabel: string;
  showRemember?: boolean;
  showForgot?: boolean;
  ssoMode?: 'text' | 'icon';
}) => {
  const { loading, onSubmit, t } = props;
  const { token } = theme.useToken();
  const loginTokens = props.designTokens.loginTemplates;
  const darkPalette = getDarkPalette(props, variant);
  const isStandard = variant === 'light';
  const accent = darkPalette?.accent ?? (isStandard ? '#1e293b' : token.colorPrimary);
  const controlBg = darkPalette?.controlBg ?? (isStandard ? '#f8fafc' : token.colorFillAlter);
  const controlText = darkPalette?.controlText ?? (isStandard ? '#0f172a' : token.colorText);
  const borderColor = darkPalette?.border ?? (isStandard ? '#cbd5e1' : token.colorBorderSecondary);
  const radius = variant === 'brutalist' ? 0 : variant === 'terminal' ? 4 : 8;
  const buttonBg = variant === 'terminal' ? loginTokens.terminal.accentBg : variant === 'brutalist' ? loginTokens.brutalist.accent : variant === 'glass' ? loginTokens.glass.accentBg : variant === 'bento' ? token.colorText : '#1e293b';
  const buttonColor = variant === 'terminal' ? loginTokens.terminal.accent : variant === 'brutalist' || variant === 'glass' ? loginTokens.terminal.pageBg : isStandard ? '#ffffff' : token.colorBgContainer;
  const inputStyle: CSSProperties = {
    minHeight: variant === 'brutalist' ? 44 : 46,
    borderRadius: radius,
    borderColor,
    background: controlBg,
    color: controlText
  };

  return (
    <Form<LoginRequest> layout="vertical" onFinish={onSubmit} requiredMark={false}>
      <Form.Item name="tenantCode" label={t('auth.tenantCode')} rules={[{ required: true, message: t('auth.required') }]}>
        <Input prefix={variant === 'light' ? <BankOutlined /> : undefined} autoComplete="organization" placeholder={t('auth.tenantCodePlaceholder')} style={inputStyle} />
      </Form.Item>
      <Form.Item name="username" label={t(variant === 'terminal' ? 'auth.loginTemplates.cyberAi.username' : variant === 'glass' ? 'auth.loginTemplates.immersiveGlass.username' : variant === 'brutalist' ? 'auth.loginTemplates.minimalTechnical.username' : variant === 'bento' ? 'auth.loginTemplates.bento.username' : 'auth.username')} rules={[{ required: true, message: t('auth.required') }]}>
        <Input prefix={variant === 'light' ? <UserOutlined /> : undefined} autoComplete="username" placeholder={t(variant === 'terminal' ? 'auth.loginTemplates.cyberAi.usernamePlaceholder' : variant === 'glass' ? 'auth.loginTemplates.immersiveGlass.usernamePlaceholder' : variant === 'brutalist' ? 'auth.loginTemplates.minimalTechnical.usernamePlaceholder' : variant === 'bento' ? 'auth.loginTemplates.bento.usernamePlaceholder' : 'auth.loginTemplates.standardIndustrial.usernamePlaceholder')} style={inputStyle} />
      </Form.Item>
      <Form.Item name="password" label={t(variant === 'terminal' ? 'auth.loginTemplates.cyberAi.password' : variant === 'glass' ? 'auth.loginTemplates.immersiveGlass.password' : variant === 'brutalist' ? 'auth.loginTemplates.minimalTechnical.password' : 'auth.password')} rules={[{ required: true, message: t('auth.required') }]}>
        <Input.Password autoComplete="current-password" placeholder={t('auth.loginTemplates.passwordPlaceholder')} style={inputStyle} />
      </Form.Item>
      {(showRemember || showForgot) && (
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBlockEnd: token.margin }}>
          {showRemember ? <Checkbox style={{ color: darkPalette?.muted ?? token.colorTextSecondary }}>{t('auth.loginTemplates.rememberMe')}</Checkbox> : <span />}
          {showForgot && (
            <Typography.Link style={{ color: accent, fontSize: isStandard ? 13 : 12, fontWeight: isStandard ? 700 : undefined }}>
              {t(variant === 'brutalist' ? 'auth.loginTemplates.minimalTechnical.forgot' : 'auth.loginTemplates.forgotPassword')}
            </Typography.Link>
          )}
        </div>
      )}
      <Button
        block
        className={isStandard ? 'iaf-login-standard-submit' : undefined}
        htmlType="submit"
        loading={loading}
        icon={variant === 'terminal' ? <ThunderboltOutlined /> : variant === 'glass' ? <LoginOutlined /> : undefined}
        style={{
          minHeight: variant === 'bento' ? 52 : 46,
          borderRadius: radius,
          borderColor: variant === 'terminal' ? loginTokens.terminal.accentBorder : buttonBg,
          background: buttonBg,
          color: buttonColor,
          fontWeight: 800,
          fontSize: isStandard ? 15 : undefined,
          textTransform: variant === 'terminal' || variant === 'brutalist' ? 'uppercase' : undefined,
          letterSpacing: variant === 'terminal' || variant === 'brutalist' ? 1 : 0,
          boxShadow: isStandard ? '0 14px 28px rgba(15, 23, 42, 0.18)' : undefined,
          textRendering: isStandard ? 'geometricPrecision' : undefined
        }}
      >
        <Space size={8}>
          <span>{submitLabel}</span>
          {(variant === 'light' || variant === 'bento' || variant === 'brutalist') && <ArrowRightOutlined />}
        </Space>
      </Button>
      <SsoBlock props={props} variant={variant} mode={ssoMode} />
    </Form>
  );
};

const SsoBlock = ({ props, variant, mode }: { props: LoginTemplateProps; variant: 'light' | 'terminal' | 'glass' | 'brutalist' | 'bento'; mode: 'text' | 'icon' }) => {
  const { t } = props;
  const { token } = theme.useToken();
  const darkPalette = getDarkPalette(props, variant);
  const accent = darkPalette?.accent ?? token.colorPrimary;
  const borderColor = darkPalette?.border ?? token.colorBorderSecondary;
  const itemStyle: CSSProperties = {
    minHeight: 42,
    borderRadius: variant === 'brutalist' ? 0 : 8,
    borderColor,
    background: darkPalette?.controlBg ?? token.colorBgContainer,
    color: darkPalette?.muted ?? token.colorTextSecondary,
    flex: 1
  };

  return (
    <div style={{ marginTop: token.marginLG, paddingTop: token.padding, borderTop: `1px solid ${borderColor}` }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: token.marginSM, marginBottom: token.margin }}>
        {variant === 'bento' && <div style={{ height: 1, background: borderColor, flex: 1 }} />}
        <Typography.Text style={{ color: darkPalette?.muted ?? token.colorTextSecondary, fontSize: 12, textTransform: variant === 'terminal' || variant === 'brutalist' ? 'uppercase' : undefined }}>
          {t(variant === 'terminal' ? 'auth.loginTemplates.cyberAi.ssoTitle' : variant === 'brutalist' ? 'auth.loginTemplates.minimalTechnical.ssoTitle' : variant === 'glass' ? 'auth.loginTemplates.immersiveGlass.ssoTitle' : variant === 'bento' ? 'auth.loginTemplates.bento.ssoTitle' : 'auth.loginTemplates.ssoTitle')}
        </Typography.Text>
        {variant === 'bento' && <div style={{ height: 1, background: borderColor, flex: 1 }} />}
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, minmax(0, 1fr))', gap: token.marginSM }}>
        {[
          [<MessageOutlined key="ding" style={{ color: token.colorInfo }} />, mode === 'icon' ? '' : t('auth.sso.dingtalk')],
          [<ApartmentOutlined key="wecom" style={{ color: token.colorSuccess }} />, mode === 'icon' ? '' : t('auth.sso.wecom')],
          [<GoogleOutlined key="google" style={{ color: token.colorError }} />, mode === 'icon' ? '' : t('auth.sso.google')]
        ].map(([icon, label], index) => (
          <Button key={index} disabled icon={icon} style={{ ...itemStyle, borderColor: variant === 'terminal' || variant === 'brutalist' ? accent : itemStyle.borderColor }}>
            {label}
          </Button>
        ))}
      </div>
    </div>
  );
};

export const StandardIndustrialLogin = (props: LoginTemplateProps) => {
  const { brandConfig, t } = props;
  const { token } = theme.useToken();
  const loginTokens = props.designTokens.loginTemplates.standard;

  return (
    <main className="iaf-login-page iaf-login-standard" style={{ ...pageBase, display: 'flex', background: token.colorBgContainer }}>
      <section
        style={{
          width: '50%',
          minWidth: 520,
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'space-between',
          padding: 48,
          color: token.colorTextLightSolid,
          backgroundColor: loginTokens.heroBg,
          backgroundImage: resolveHeroBackground(props, standardIndustrialImage),
          backgroundSize: 'cover',
          backgroundPosition: 'center',
          position: 'relative'
        }}
      >
        <div className="iaf-login-standard-hero-copy" style={{ maxWidth: 620, padding: 32, borderRadius: 28, background: 'rgba(2, 6, 23, 0.42)', border: '1px solid rgba(226, 232, 240, 0.18)', boxShadow: '0 24px 60px rgba(2, 6, 23, 0.28)', backdropFilter: 'blur(8px)' }}>
          <div style={{ fontSize: 24, marginBottom: 44 }}>{brandLogo(props, { color: loginTokens.heroLogo, letterSpacing: 2, textShadow: '0 2px 14px rgba(0, 0, 0, 0.42)' })}</div>
          <Typography.Title style={{ margin: 0, color: loginTokens.heroText, fontSize: 52, lineHeight: 1.04, fontWeight: 850, letterSpacing: -1.2, maxWidth: 560, textShadow: '0 3px 18px rgba(0, 0, 0, 0.48)' }}>
            {t(brandConfig.loginHeroTitleKey)}
          </Typography.Title>
          <Typography.Paragraph style={{ color: loginTokens.heroMuted, fontSize: 22, lineHeight: 1.55, marginTop: 22, marginBottom: 0, fontWeight: 500, maxWidth: 560, textShadow: '0 2px 12px rgba(0, 0, 0, 0.46)' }}>
            {t(brandConfig.loginHeroSubtitleKey)}
          </Typography.Paragraph>
        </div>
        <Typography.Text style={{ color: loginTokens.heroLogo, fontSize: 12, fontWeight: 600, textShadow: '0 2px 10px rgba(0, 0, 0, 0.48)' }}>{t('auth.loginTemplates.copyright')}</Typography.Text>
      </section>
      <section style={{ flex: 1, display: 'grid', placeItems: 'center', padding: 48, background: 'linear-gradient(135deg, #f8fafc 0%, #eef2f7 100%)' }}>
        <div className="iaf-login-standard-card" style={{ width: 'min(100%, 448px)', padding: 40, background: loginTokens.formBg, borderRadius: 24, border: `1px solid ${loginTokens.formBorder}`, boxShadow: loginTokens.formShadow }}>
          <Space direction="vertical" size={4} style={{ marginBottom: 32 }}>
            <Typography.Title level={2} style={{ margin: 0, color: '#0f172a', fontSize: 32, fontWeight: 850, letterSpacing: -0.4 }}>{t('auth.loginTemplates.welcomeBack')}</Typography.Title>
            <Typography.Text style={{ color: '#475569', fontSize: 14, fontWeight: 500 }}>{t('auth.loginTemplates.standardIndustrial.subtitle')}</Typography.Text>
          </Space>
          <LoginForm props={props} variant="light" submitLabel={t('auth.loginTemplates.standardIndustrial.submit')} />
        </div>
      </section>
    </main>
  );
};

export const CyberAiLogin = (props: LoginTemplateProps) => {
  const { brandConfig, t } = props;
  const { token } = theme.useToken();
  const palette = props.designTokens.loginTemplates.terminal;
  const grid = `linear-gradient(to right, ${palette.gridLine} 1px, transparent 1px), linear-gradient(to bottom, ${palette.gridLine} 1px, transparent 1px)`;

  return (
    <main className="iaf-login-page iaf-login-dark" style={{ ...pageBase, display: 'grid', placeItems: 'center', padding: 16, background: palette.pageBg, backgroundImage: `${grid}, radial-gradient(circle at 50% 50%, ${palette.glow}, transparent 42%)`, backgroundSize: '40px 40px, auto', fontFamily: token.fontFamilyCode }}>
      <section style={{ width: 'min(100%, 1024px)', display: 'flex', overflow: 'hidden', borderRadius: 16, border: `1px solid ${palette.border}`, background: palette.panel, boxShadow: props.designTokens.elevation.level3, backdropFilter: 'blur(20px)' }}>
        <div style={{ width: '50%', padding: 40, borderRight: `1px solid ${palette.border}`, position: 'relative' }}>
          <div style={{ position: 'absolute', top: 0, left: 0, right: 0, height: 4, background: `linear-gradient(90deg, ${palette.accent}, ${props.designTokens.loginTemplates.glass.accent})` }} />
          <Space align="center" style={{ color: palette.accent, marginBottom: 32 }}>
            <CodeOutlined style={{ fontSize: 24 }} />
            <Typography.Text strong style={{ color: palette.accent, fontSize: 20, letterSpacing: 3, textTransform: 'uppercase' }}>{t('auth.loginTemplates.cyberAi.systemName')}</Typography.Text>
          </Space>
          <Typography.Title level={1} style={{ color: palette.text, fontSize: 30, marginBottom: 16 }}>{t(brandConfig.loginHeroTitleKey)}</Typography.Title>
          <Typography.Text style={{ display: 'flex', alignItems: 'center', gap: 8, color: palette.accent, fontSize: 12, letterSpacing: 3, textTransform: 'uppercase', marginBottom: 32 }}>
            <DeploymentUnitOutlined /> {t('auth.loginTemplates.cyberAi.kicker')}
          </Typography.Text>
          <Space direction="vertical" size={16} style={{ color: palette.muted, fontSize: 13 }}>
            <span>&gt; INITIALIZING IAF CORE... <Typography.Text style={{ color: palette.accent }}>OK</Typography.Text></span>
            <span>&gt; LOADING MODULES [WMS, MES, SRM, QMS]... <Typography.Text style={{ color: palette.accent }}>OK</Typography.Text></span>
          </Space>
        </div>
        <div style={{ width: '50%', padding: 40, background: palette.panel, display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
          <div style={{ textAlign: 'center', marginBottom: 32 }}>
            <SafetyCertificateOutlined style={{ color: palette.subtle, fontSize: 48, marginBottom: 16 }} />
            <Typography.Title level={3} style={{ margin: 0, color: palette.text, letterSpacing: 1 }}>{t('auth.loginTemplates.cyberAi.title')}</Typography.Title>
            <Typography.Text style={{ color: palette.subtle, fontSize: 12 }}>{t('auth.loginTemplates.cyberAi.subtitle')}</Typography.Text>
          </div>
          <LoginForm props={props} variant="terminal" submitLabel={t('auth.loginTemplates.cyberAi.submit')} />
        </div>
      </section>
    </main>
  );
};

export const ImmersiveGlassLogin = (props: LoginTemplateProps) => {
  const { brandConfig, t } = props;
  const palette = props.designTokens.loginTemplates.glass;
  const grid = `linear-gradient(to right, ${palette.gridLine} 1px, transparent 1px), linear-gradient(to bottom, ${palette.gridLine} 1px, transparent 1px)`;

  return (
    <main className="iaf-login-page iaf-login-dark" style={{ ...pageBase, display: 'grid', placeItems: 'center', padding: 32, background: palette.pageBg, backgroundImage: `${grid}, radial-gradient(circle at -10% -10%, ${palette.glow}, transparent 34%), radial-gradient(circle at 110% 110%, ${palette.glow}, transparent 34%)`, backgroundSize: '64px 64px, auto, auto' }}>
      <section style={{ width: 'min(100%, 1152px)', display: 'flex', overflow: 'hidden', borderRadius: 24, border: `1px solid ${palette.border}`, background: palette.panel, boxShadow: props.designTokens.elevation.level3, backdropFilter: 'blur(24px)' }}>
        <div style={{ width: '60%', padding: 48, borderRight: `1px solid ${palette.border}`, position: 'relative' }}>
          <Space size={12} style={{ marginBottom: 32 }}>
            <span style={{ display: 'grid', placeItems: 'center', width: 48, height: 48, borderRadius: 12, border: `1px solid ${palette.accentBorder}`, background: palette.accentBg, color: palette.accent }}>
              <DeploymentUnitOutlined style={{ fontSize: 28 }} />
            </span>
            <div>
              <Typography.Text strong style={{ display: 'block', color: palette.text, fontSize: 20, letterSpacing: 2 }}>{t('auth.loginTemplates.immersiveGlass.cockpit')}</Typography.Text>
              <Typography.Text style={{ color: palette.accent, fontSize: 11, letterSpacing: 2, textTransform: 'uppercase' }}>{t('auth.loginTemplates.immersiveGlass.hub')}</Typography.Text>
            </div>
          </Space>
          <Typography.Title level={1} style={{ color: palette.text, fontSize: 38, lineHeight: 1.12 }}>{t(brandConfig.loginHeroTitleKey)}</Typography.Title>
          <Typography.Paragraph style={{ color: palette.muted, fontSize: 18 }}>{t(brandConfig.loginHeroSubtitleKey)}</Typography.Paragraph>
          <PipelineMonitor props={props} />
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 24, color: palette.muted, fontSize: 12, marginTop: 24 }}>
            {[
              [<SafetyCertificateOutlined key="a" />, 'auth.loginTemplates.immersiveGlass.features.platform'],
              [<DatabaseOutlined key="b" />, 'auth.loginTemplates.immersiveGlass.features.model'],
              [<ApiOutlined key="c" />, 'auth.loginTemplates.immersiveGlass.features.dsl']
            ].map(([icon, key]) => (
              <Space key={String(key)}>{icon}<span>{t(String(key))}</span></Space>
            ))}
          </div>
        </div>
        <div style={{ width: '40%', padding: 48, background: palette.panel, display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
          <Space direction="vertical" size={4} style={{ marginBottom: 32 }}>
            <Typography.Title level={3} style={{ margin: 0, color: palette.text }}>{t('auth.loginTemplates.welcomeBack')}</Typography.Title>
            <Typography.Text style={{ color: palette.muted }}>{t('auth.loginTemplates.immersiveGlass.subtitle')}</Typography.Text>
          </Space>
          <LoginForm props={props} variant="glass" submitLabel={t('auth.loginTemplates.immersiveGlass.submit')} showRemember />
        </div>
      </section>
    </main>
  );
};

const PipelineMonitor = ({ props }: { props: LoginTemplateProps }) => {
  const { t } = props;
  const palette = props.designTokens.loginTemplates.glass;
  const terminal = props.designTokens.loginTemplates.terminal;

  return (
    <div style={{ background: terminal.controlBg, border: `1px solid ${palette.border}`, borderRadius: 16, padding: 24, marginTop: 32, fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', color: palette.subtle, fontSize: 11, marginBottom: 16 }}>
        <span><ThunderboltOutlined style={{ color: palette.accent }} /> {t('auth.loginTemplates.immersiveGlass.pipeline')}</span>
        <span>NODE_ID: IAF-9281</span>
      </div>
      <div style={{ height: 112, display: 'grid', placeItems: 'center', background: terminal.panel, border: `1px solid ${palette.border}`, borderRadius: 12 }}>
        <svg width="100%" height="100%" viewBox="0 0 400 100" role="img" aria-label={t('auth.loginTemplates.immersiveGlass.pipeline')}>
          {['WMS', 'MES', 'SRM', 'QMS'].map((node, index) => {
            const x = 40 + index * 100;
            return (
              <g key={node}>
                <circle cx={x} cy="48" r="16" fill={terminal.pageBg} stroke={palette.accentBorder} />
                <text x={x} y="52" textAnchor="middle" fill={palette.accent} fontSize="10" fontWeight="700">{node}</text>
                {index < 3 && <path d={`M ${x + 16} 48 L ${x + 84} 48`} stroke={terminal.border} strokeWidth="2" />}
              </g>
            );
          })}
          <circle r="4" fill={palette.accent}>
            <animateMotion dur="3s" repeatCount="indefinite" path="M 56 48 L 124 48" />
          </circle>
          <circle r="4" fill={terminal.accent}>
            <animateMotion dur="4s" repeatCount="indefinite" path="M 156 48 L 224 48" />
          </circle>
          <circle r="4" fill={props.designTokens.dataViz.categorical[4]}>
            <animateMotion dur="3.5s" repeatCount="indefinite" path="M 256 48 L 324 48" />
          </circle>
        </svg>
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 16, marginTop: 16, textAlign: 'center' }}>
        {[
          ['GENERATION RATE', '98.4%', palette.accent],
          ['DSL PARSING', 'ACTIVE', terminal.accent],
          ['MODEL MATCH', '100%', props.designTokens.dataViz.categorical[4]]
        ].map(([label, value, color]) => (
          <div key={String(label)} style={{ padding: 10, borderRadius: 8, border: `1px solid ${palette.border}`, background: terminal.panel }}>
            <div style={{ color: palette.subtle, fontSize: 10 }}>{label}</div>
            <div style={{ color: String(color), fontSize: 14, fontWeight: 800 }}>{value}</div>
          </div>
        ))}
      </div>
    </div>
  );
};

export const MinimalTechnicalLogin = (props: LoginTemplateProps) => {
  const { brandConfig, t } = props;
  const { token } = theme.useToken();
  const palette = props.designTokens.loginTemplates.brutalist;
  return (
    <main className="iaf-login-page iaf-login-dark" style={{ ...pageBase, display: 'grid', placeItems: 'center', padding: 32, background: palette.pageBg, fontFamily: token.fontFamilyCode, position: 'relative' }}>
      <Typography.Text style={{ position: 'absolute', top: 16, left: 16, color: palette.accent, opacity: 0.5, fontSize: 10 }}>{t('auth.loginTemplates.minimalTechnical.loc')}</Typography.Text>
      <Typography.Text style={{ position: 'absolute', right: 16, bottom: 16, color: palette.accent, opacity: 0.5, fontSize: 10 }}>{t('auth.loginTemplates.minimalTechnical.version')}</Typography.Text>
      <section style={{ width: 'min(100%, 1024px)', display: 'flex', border: `2px solid ${palette.accentBorder}`, background: palette.panel, boxShadow: `12px 12px 0 ${palette.accentBg}`, borderRadius: 0 }}>
        <div style={{ width: '60%', padding: 48, borderRight: `2px solid ${palette.accentBorder}`, position: 'relative', background: palette.controlBg }}>
          <div style={{ position: 'absolute', top: 0, left: 0, background: palette.accent, color: palette.panel, fontSize: 9, fontWeight: 800, padding: '2px 8px', textTransform: 'uppercase' }}>{t('auth.loginTemplates.minimalTechnical.specLabel')}</div>
          <Space size={12} style={{ marginTop: 24, marginBottom: 32 }}>
            <span style={{ width: 32, height: 32, display: 'grid', placeItems: 'center', background: palette.accent, color: palette.panel, fontWeight: 900 }}>#</span>
            <div>
              <Typography.Text strong style={{ display: 'block', color: palette.text, fontSize: 20 }}>IAF STRUCTURE</Typography.Text>
              <Typography.Text style={{ color: palette.accent, fontSize: 10, letterSpacing: 2, textTransform: 'uppercase' }}>Bauhaus System Engine</Typography.Text>
            </div>
          </Space>
          <Typography.Title level={1} style={{ color: palette.text, fontSize: 34, textTransform: 'uppercase', lineHeight: 1.12 }}>{t(brandConfig.loginHeroTitleKey)}</Typography.Title>
          <Typography.Paragraph style={{ color: palette.muted, fontSize: 13, maxWidth: 460 }}>{t('auth.loginTemplates.minimalTechnical.description')}</Typography.Paragraph>
          <Space direction="vertical" size={16} style={{ width: '100%', marginTop: 32 }}>
            {['infra', 'source'].map((key, index) => (
              <div key={key} style={{ display: 'flex', gap: 16, padding: 16, border: `1px solid ${palette.accentBorder}`, background: palette.panel }}>
                <Typography.Text style={{ color: palette.accent, fontWeight: 800 }}>{String(index + 1).padStart(2, '0')}</Typography.Text>
                <div>
                  <Typography.Text strong style={{ display: 'block', color: palette.text, fontSize: 12, textTransform: 'uppercase' }}>{t(`auth.loginTemplates.minimalTechnical.blocks.${key}.title`)}</Typography.Text>
                  <Typography.Text style={{ color: palette.muted, fontSize: 11 }}>{t(`auth.loginTemplates.minimalTechnical.blocks.${key}.description`)}</Typography.Text>
                </div>
              </div>
            ))}
          </Space>
        </div>
        <div style={{ width: '40%', padding: 48, position: 'relative', background: palette.panel, display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
          <div style={{ position: 'absolute', top: 0, left: 0, background: palette.accentBg, color: palette.accent, fontSize: 9, fontWeight: 800, padding: '2px 8px', textTransform: 'uppercase' }}>{t('auth.loginTemplates.minimalTechnical.accessLabel')}</div>
          <Typography.Title level={3} style={{ color: palette.text, marginBottom: 4, textTransform: 'uppercase' }}>{t('auth.loginTemplates.minimalTechnical.title')}</Typography.Title>
          <Typography.Text style={{ color: palette.muted, fontSize: 11, marginBottom: 32, display: 'block' }}>{t('auth.loginTemplates.minimalTechnical.subtitle')}</Typography.Text>
          <LoginForm props={props} variant="brutalist" submitLabel={t('auth.loginTemplates.minimalTechnical.submit')} />
        </div>
      </section>
    </main>
  );
};

export const BentoDashboardLogin = (props: LoginTemplateProps) => {
  const { t } = props;
  const { token } = theme.useToken();
  const loginTokens = props.designTokens.loginTemplates.bento;
  const featureIcons = [<FieldTimeOutlined key="workflow" />, <BuildOutlined key="model" />, <DatabaseOutlined key="document" />, <CodeOutlined key="ai" />];
  return (
    <main className="iaf-login-page iaf-login-bento" style={{ ...pageBase, display: 'grid', placeItems: 'center', padding: 32, background: token.colorFillAlter }}>
      <section style={{ width: 'min(100%, 1152px)', display: 'flex', overflow: 'hidden', borderRadius: 24, background: token.colorBgContainer, boxShadow: token.boxShadowSecondary }}>
        <div style={{ width: '60%', padding: 48, color: token.colorTextLightSolid, display: 'flex', flexDirection: 'column', position: 'relative', backgroundImage: `${loginTokens.imageOverlay}, url(${bentoIndustrialImage})`, backgroundSize: 'cover', backgroundPosition: 'center' }}>
          <div style={{ marginBottom: 48 }}>
            <Typography.Title level={1} style={{ color: token.colorTextLightSolid, margin: 0, fontSize: 40 }}>IAF</Typography.Title>
            <Typography.Text style={{ color: token.colorTextTertiary, fontSize: 20, fontWeight: 300 }}>{t('auth.loginTemplates.bento.subtitle')}</Typography.Text>
          </div>
          <div style={{ flex: 1 }}>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', gap: 16, maxWidth: 560 }}>
              {['workflow', 'model', 'document', 'ai'].map((key, index) => (
                <div key={key} style={{ padding: 20, borderRadius: 16, border: `1px solid ${token.colorBorder}`, background: token.colorBgElevated }}>
                  <div style={{ color: [token.colorInfo, token.colorSuccess, token.colorWarning, token.colorPrimary][index], fontSize: 24, marginBottom: 12 }}>{featureIcons[index]}</div>
                  <Typography.Text strong style={{ display: 'block', color: token.colorTextLightSolid }}>{t(`auth.loginTemplates.bento.features.${key}.title`)}</Typography.Text>
                  <Typography.Text style={{ color: token.colorTextSecondary, fontSize: 13 }}>{t(`auth.loginTemplates.bento.features.${key}.description`)}</Typography.Text>
                </div>
              ))}
            </div>
          </div>
          <Typography.Text style={{ color: token.colorTextTertiary, marginTop: 32 }}>{t('auth.loginTemplates.bento.poweredBy')}</Typography.Text>
        </div>
        <div style={{ width: '40%', padding: 48, position: 'relative', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
          <div style={{ position: 'absolute', top: 32, right: 32, display: 'flex', gap: 8 }}>
            {[0, 1, 2].map((dot) => <span key={dot} style={{ width: 8, height: 8, borderRadius: '50%', background: token.colorBorderSecondary }} />)}
          </div>
          <Typography.Title level={2} style={{ margin: 0, fontSize: 30 }}>{t('auth.loginTemplates.bento.title')}</Typography.Title>
          <Typography.Text type="secondary" style={{ marginTop: 8, marginBottom: 32 }}>{t('auth.loginTemplates.bento.description')}</Typography.Text>
          <LoginForm props={props} variant="bento" submitLabel={t('auth.loginTemplates.bento.submit')} showRemember ssoMode="icon" />
          <Typography.Text type="secondary" style={{ marginTop: 32, textAlign: 'center' }}>{t('auth.loginTemplates.bento.contactAdmin')}</Typography.Text>
        </div>
      </section>
    </main>
  );
};

export const loginTemplateRenderers: Record<IafLoginTemplateName, (props: LoginTemplateProps) => ReactNode> = {
  'standard-industrial': (props) => <StandardIndustrialLogin {...props} />,
  'cyber-ai': (props) => <CyberAiLogin {...props} />,
  'immersive-glass': (props) => <ImmersiveGlassLogin {...props} />,
  'minimal-technical': (props) => <MinimalTechnicalLogin {...props} />,
  'bento-dashboard': (props) => <BentoDashboardLogin {...props} />
};
