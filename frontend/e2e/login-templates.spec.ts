import { expect, test, type Page } from '@playwright/test';

const templates = [
  'standard-industrial',
  'cyber-ai',
  'immersive-glass',
  'minimal-technical',
  'bento-dashboard'
] as const;

const viewports = [
  { name: 'desktop', width: 1366, height: 768 },
  { name: 'mobile', width: 390, height: 844 }
] as const;

const expectNoHorizontalOverflow = async (page: Page) => {
  const overflow = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth);
  expect(overflow).toBeLessThanOrEqual(1);
};

const expectLoginTemplateReady = async (page: Page) => {
  await expect(page.locator('.iaf-login-page')).toBeVisible();
  await expect(page.locator('input[autocomplete="username"]')).toBeVisible();
  await expect(page.locator('input[autocomplete="current-password"]')).toBeVisible();
  await expect(page.locator('button[htmltype="submit"], button[type="submit"]').first()).toBeVisible();
  await expectNoHorizontalOverflow(page);
  const screenshot = await page.screenshot({ fullPage: false });
  expect(screenshot.length).toBeGreaterThan(10_000);
};

test.describe('login templates visual readiness', () => {
  for (const viewport of viewports) {
    for (const template of templates) {
      test(`${template} renders on ${viewport.name}`, async ({ page }) => {
        await page.setViewportSize({ width: viewport.width, height: viewport.height });
        await page.goto(`/login?loginTemplate=${template}`);
        await expectLoginTemplateReady(page);
      });
    }
  }

  test('selected template can submit through mock auth', async ({ page }) => {
    await page.setViewportSize({ width: 1366, height: 768 });
    await page.goto('/login?loginTemplate=standard-industrial');
    await page.locator('input[autocomplete="username"]').fill('admin');
    await page.locator('input[autocomplete="current-password"]').fill('admin123');
    await page.locator('button[type="submit"]').click();
    await expect(page.locator('.iaf-shell-root')).toBeVisible();
  });
});
