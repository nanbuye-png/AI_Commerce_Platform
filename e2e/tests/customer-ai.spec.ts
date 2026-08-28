import { expect, test } from '@playwright/test';

const customerAccount = process.env.DEMO_CUSTOMER_USERNAME ?? 'demo_customer';
const customerPassword = process.env.DEMO_PASSWORD ?? 'Demo123!';

test.describe('customer AI commerce flow', () => {
  test('searches products and requires confirmation before add-to-cart', async ({ page }) => {
    await page.goto('/login');
    await page.getByLabel('账号').fill(customerAccount);
    await page.getByLabel('密码').fill(customerPassword);
    await page.getByTestId('login-submit').click();
    await expect(page).toHaveURL(/\/$/);

    await page.goto('/ai');
    await page.getByPlaceholder('输入你想了解的商品信息...').fill('帮我找500到1000元的耳机');
    await page.getByRole('button', { name: '发送消息' }).click();

    await expect(page.getByText(/为你找到/)).toBeVisible({ timeout: 20_000 });
    await expect(page.getByText('星云降噪蓝牙耳机', { exact: true })).toBeVisible();

    await page.getByRole('button', { name: '加入购物车' }).first().click();
    await expect(page.getByRole('button', { name: '确认加入购物车' })).toBeVisible();

    await page.getByRole('button', { name: '确认加入购物车' }).click();
    await expect(page.getByTestId('add-to-cart-success')).toBeVisible({ timeout: 20_000 });
  });

  test('does not call AI without a JWT', async ({ page }) => {
    await page.addInitScript(() => localStorage.removeItem('customer_token'));
    let aiCalls = 0;
    await page.route('**/api/customer/ai/chat/stream', async (route) => {
      aiCalls += 1;
      await route.continue();
    });
    await page.goto('/ai');
    await expect(page).toHaveURL(/\/login$/);
    expect(aiCalls).toBe(0);
  });
});
