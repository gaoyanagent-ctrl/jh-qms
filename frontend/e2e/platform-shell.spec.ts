import { expect, test, type Page } from '@playwright/test';

const login = async (page: Page) => {
  await page.goto('/login');
  await page.getByLabel('用户名').fill('admin');
  await page.getByLabel('密码').fill('password');
  await page.getByRole('button', { name: /登\s*录/ }).click();
  await expect(page.locator('.iaf-shell-root')).toBeVisible();
};

const expectShellStable = async (page: Page) => {
  const viewport = page.viewportSize();
  const sidebar = await page.locator('.iaf-shell-sidebar').boundingBox();
  const menuScroll = await page.locator('.iaf-shell-menu-scroll').boundingBox();
  const profile = await page.locator('.iaf-shell-profile').boundingBox();

  expect(viewport).not.toBeNull();
  expect(sidebar).not.toBeNull();
  expect(menuScroll).not.toBeNull();
  expect(profile).not.toBeNull();

  if (!viewport || !sidebar || !menuScroll || !profile) return;

  expect(sidebar.height).toBeLessThanOrEqual(viewport.height + 1);
  expect(profile.y + profile.height).toBeLessThanOrEqual(viewport.height + 1);
  expect(menuScroll.y + menuScroll.height).toBeLessThanOrEqual(profile.y + 1);
};

test.describe('platform shell visual stability', () => {
  test('keeps sidebar profile fixed while navigating platform pages', async ({ page }) => {
    await page.setViewportSize({ width: 1366, height: 768 });
    await login(page);

    await expectShellStable(page);

    for (const menuName of ['用户管理', '角色权限', '平台 Kanban', '审批任务']) {
      await page.getByRole('menuitem', { name: new RegExp(menuName) }).click();
      await expect(page.locator('.iaf-shell-root')).toBeVisible();
      await expectShellStable(page);
      const screenshot = await page.screenshot({ fullPage: false });
      expect(screenshot.length).toBeGreaterThan(10_000);
    }
  });

  test('keeps shell stable in dark theme and mobile-width viewport', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await login(page);
    await page.getByLabel('主题').click();
    await expectShellStable(page);
    const screenshot = await page.screenshot({ fullPage: false });
    expect(screenshot.length).toBeGreaterThan(10_000);
  });
});
