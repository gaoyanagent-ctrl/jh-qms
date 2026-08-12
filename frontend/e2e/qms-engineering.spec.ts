import { expect, test, type Page } from '@playwright/test';

const login = async (page: Page) => {
  await page.goto('/login');
  await page.getByLabel('租户编码').fill('default');
  await page.getByLabel('用户名').fill('admin');
  await page.getByLabel('密码').fill('password');
  await page.getByRole('button', { name: /登\s*录/ }).click();
  await expect(page.locator('.iaf-shell-root')).toBeVisible();
};

const openEngineeringData = async (page: Page) => {
  await page.getByRole('menuitem', { name: /工程数据/ }).click();
  await expect(page.getByRole('heading', { name: '工程数据' })).toBeVisible();
};

test.describe('QMS engineering data', () => {
  test('creates a part, drawing, and metadata-only revision', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await login(page);
    await openEngineeringData(page);

    await page.getByRole('button', { name: /新\s*增/ }).click();
    await page.getByLabel('零件号').fill('JH-E2E-001');
    await page.getByLabel('零件名称').fill('E2E bracket');
    await page.getByLabel('物料号').fill('MAT-E2E-001');
    await page.getByRole('button', { name: /保\s*存/ }).click();
    await expect(page.getByText('JH-E2E-001')).toBeVisible();

    await page.getByRole('button', { name: /查\s*看/ }).first().click();
    await expect(page.getByRole('heading', { name: 'JH-E2E-001 · E2E bracket' })).toBeVisible();

    await page.getByRole('button', { name: /新\s*增\s*图\s*纸/ }).click();
    await page.getByLabel('图号').fill('DWG-E2E-001');
    await page.getByLabel('图纸名称').fill('E2E drawing');
    await page.getByRole('button', { name: /保\s*存/ }).click();
    await expect(page.getByRole('cell', { name: 'DWG-E2E-001', exact: true })).toBeVisible();

    await page.getByRole('button', { name: /新\s*增\s*版\s*本/ }).click();
    await page.getByLabel('版本号').fill('A');
    await page.getByLabel('生效日期').fill('2026-08-12');
    await page.getByRole('button', { name: /保\s*存/ }).click();
    await expect(page.getByRole('cell', { name: 'A', exact: true })).toBeVisible();
  });

  test('keeps the engineering list inside a mobile viewport', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await login(page);
    await openEngineeringData(page);
    const overflow = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth);
    expect(overflow).toBeLessThanOrEqual(1);
    const screenshot = await page.screenshot({ fullPage: false });
    expect(screenshot.length).toBeGreaterThan(10_000);
  });
});
