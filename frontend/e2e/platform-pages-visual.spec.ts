import { expect, test, type Locator, type Page } from '@playwright/test';

type Viewport = { name: string; width: number; height: number };

const desktopViewports: Viewport[] = [
  { name: '1366x768', width: 1366, height: 768 },
  { name: '1440x900', width: 1440, height: 900 },
  { name: '1920x1080', width: 1920, height: 1080 }
];

const mobileViewport: Viewport = { name: '390x844', width: 390, height: 844 };

const routes = [
  { path: '/', title: /平台工作台/ },
  { path: '/platform/users', title: /用户管理/ },
  { path: '/platform/orgs', title: /组织管理/ },
  { path: '/platform/roles', title: /角色权限/ },
  { path: '/platform/menus', title: /菜单权限/ },
  { path: '/platform/dictionaries', title: /字典与参数/ },
  { path: '/platform/audit-logs', title: /操作日志/ },
  { path: '/platform/approval/tasks', title: /审批任务/ },
  { path: '/platform/kanban', title: /平台 Kanban/ }
];

const login = async (page: Page) => {
  await page.goto('/login');
  await page.getByLabel('用户名').fill('admin');
  await page.getByLabel('密码').fill('password');
  await page.getByRole('button', { name: /登\s*录/ }).click();
  await expect(page.locator('.iaf-shell-root')).toBeVisible();
};

const expectNoHorizontalOverflow = async (page: Page) => {
  const overflow = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth);
  expect(overflow).toBeLessThanOrEqual(1);
};

const relativeLuminance = ([r, g, b]: [number, number, number]) => {
  const values = [r, g, b].map((value) => {
    const channel = value / 255;
    return channel <= 0.03928 ? channel / 12.92 : ((channel + 0.055) / 1.055) ** 2.4;
  });
  return 0.2126 * values[0] + 0.7152 * values[1] + 0.0722 * values[2];
};

const contrastRatio = (foreground: [number, number, number], background: [number, number, number]) => {
  const foregroundLum = relativeLuminance(foreground);
  const backgroundLum = relativeLuminance(background);
  const lighter = Math.max(foregroundLum, backgroundLum);
  const darker = Math.min(foregroundLum, backgroundLum);
  return (lighter + 0.05) / (darker + 0.05);
};

const parseRgb = (value: string): [number, number, number] => {
  const match = value.match(/rgba?\((\d+),\s*(\d+),\s*(\d+)/);
  if (!match) {
    throw new Error(`Unsupported color value: ${value}`);
  }
  return [Number(match[1]), Number(match[2]), Number(match[3])];
};

const expectReadableText = async (page: Page, selector: string) => {
  await expectReadableLocator(page.locator(selector).first());
};

const expectReadableLocator = async (locator: Locator) => {
  const ratio = await locator.evaluate((element) => {
    const textStyles = window.getComputedStyle(element);
    let backgroundElement: Element | null = element;
    let background = 'rgba(0, 0, 0, 0)';

    while (backgroundElement) {
      const candidate = window.getComputedStyle(backgroundElement).backgroundColor;
      if (candidate && !candidate.endsWith(', 0)') && candidate !== 'transparent') {
        background = candidate;
        break;
      }
      backgroundElement = backgroundElement.parentElement;
    }

    return { color: textStyles.color, background };
  });

  expect(contrastRatio(parseRgb(ratio.color), parseRgb(ratio.background))).toBeGreaterThanOrEqual(3);
};

const setDarkTheme = async (page: Page) => {
  await page.getByLabel('主题').click();
  await expect(page.locator('.iaf-shell-root')).toBeVisible();
};

const navigateInApp = async (page: Page, path: string) => {
  await page.evaluate((nextPath) => {
    window.history.pushState({}, '', nextPath);
    window.dispatchEvent(new PopStateEvent('popstate'));
  }, path);
};

test.describe('platform page visual baseline', () => {
  test('renders the current login page without blank screenshots or overflow', async ({ page }) => {
    for (const viewport of [...desktopViewports, mobileViewport]) {
      await page.setViewportSize({ width: viewport.width, height: viewport.height });
      await page.goto('/login');
      await expect(page.getByRole('heading', { name: '平台登录' })).toBeVisible();
      await expect(page.getByLabel('用户名')).toBeVisible();
      await expectNoHorizontalOverflow(page);
      await expectReadableText(page, 'h1');
      const screenshot = await page.screenshot({ fullPage: false });
      expect(screenshot.length, viewport.name).toBeGreaterThan(10_000);
    }
  });

  for (const viewport of [desktopViewports[0], mobileViewport]) {
    test(`keeps platform pages readable in light theme at ${viewport.name}`, async ({ page }) => {
      await page.setViewportSize({ width: viewport.width, height: viewport.height });
      await login(page);

      for (const route of routes) {
        await navigateInApp(page, route.path);
        await expect(page.locator('.iaf-shell-root')).toBeVisible();
        const heading = page.getByRole('heading', { name: route.title });
        await expect(heading).toBeVisible();
        await expectNoHorizontalOverflow(page);
        await expectReadableLocator(heading);
        const screenshot = await page.screenshot({ fullPage: false });
        expect(screenshot.length, `${viewport.name} ${route.path}`).toBeGreaterThan(12_000);
      }
    });
  }

  test('keeps key platform pages readable in dark theme', async ({ page }) => {
    await page.setViewportSize({ width: 1366, height: 768 });
    await login(page);
    await setDarkTheme(page);

    for (const route of routes.slice(0, 5)) {
      await navigateInApp(page, route.path);
        await expect(page.locator('.iaf-shell-root')).toBeVisible();
      await expect(page.getByRole('heading', { name: route.title })).toBeVisible();
      await expectNoHorizontalOverflow(page);
      await expectReadableText(page, '[data-testid="iaf-page-container"] h3');
      const screenshot = await page.screenshot({ fullPage: false });
      expect(screenshot.length, route.path).toBeGreaterThan(12_000);
    }
  });
});
