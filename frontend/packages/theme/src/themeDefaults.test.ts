import { describe, expect, it } from 'vitest';
import { iafDefaultExperienceSettings, iafDesignTokens, iafLightShellTokens, iafLoginTemplates, iafSurfaceWidths, iafThemeNames, iafThemes } from './index';

describe('theme defaults', () => {
  it('includes shell preferences in the default experience settings', () => {
    expect(iafDefaultExperienceSettings).toMatchObject({
      sidebarCollapsed: false,
      sidebarWidth: 248,
      sidebarMode: 'dark'
    });
  });

  it('keeps light sidebar text readable and form surfaces wide enough', () => {
    expect(iafLightShellTokens['light-industrial'].sidebarText).toBe('#0f172a');
    expect(iafLightShellTokens['light-industrial'].sidebarMuted).toBe('#475569');
    expect(iafSurfaceWidths.standard).toContain('760px');
    expect(iafSurfaceWidths.wide).toContain('960px');
  });

  it('declares every supported theme with runtime and design tokens', () => {
    expect(iafThemeNames).toEqual([
      'light-industrial',
      'dark-industrial',
      'compact-industrial',
      'dashboard-industrial',
      'mobile-work',
      'high-contrast',
      'customer-brand'
    ]);

    for (const themeName of iafThemeNames) {
      expect(iafThemes[themeName]).toBeDefined();
      expect(iafDesignTokens[themeName].global.colorPrimary).toBeTruthy();
      expect(iafDesignTokens[themeName].semantic.statusPendingColor).toBeTruthy();
      expect(iafDesignTokens[themeName].component.tableHeaderBg).toBeTruthy();
      expect(iafDesignTokens[themeName].loginTemplates.terminal.accent).toBeTruthy();
      expect(iafDesignTokens[themeName].loginTemplates.standard.heroOverlay).toContain('linear-gradient');
    }
  });

  it('declares five configurable login templates', () => {
    expect(iafLoginTemplates).toEqual([
      'standard-industrial',
      'cyber-ai',
      'immersive-glass',
      'minimal-technical',
      'bento-dashboard'
    ]);
  });
});
