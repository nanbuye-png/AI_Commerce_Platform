import { expect, test } from '@playwright/test';

const customerAccount = process.env.DEMO_CUSTOMER_USERNAME ?? 'demo_customer';
const customerPassword = process.env.DEMO_PASSWORD ?? 'Demo123!';
const adminAccount = process.env.DEMO_ADMIN_USERNAME ?? 'demo_admin';
const adminPassword = process.env.DEMO_ADMIN_PASSWORD ?? 'Demo123!';

test('admin AI center reflects a completed product search', async ({ browser }) => {
  const customer = await browser.newPage({ baseURL: 'http://127.0.0.1:5173' });
  await customer.goto('/login');
  await customer.getByLabel('账号').fill(customerAccount);
  await customer.getByLabel('密码').fill(customerPassword);
  await customer.getByTestId('login-submit').click();
  await customer.goto('/ai');
  await customer.getByPlaceholder('输入你想了解的商品信息...').fill('帮我找500到1000元的耳机');
  await customer.getByRole('button', { name: '发送消息' }).click();
  await expect(customer.getByText(/为你找到/)).toBeVisible({ timeout: 20_000 });
  await customer.close();

  const admin = await browser.newPage({ baseURL: 'http://127.0.0.1:5175' });
  await admin.goto('/login');
  await admin.getByLabel('账号').fill(adminAccount);
  await admin.getByLabel('密码').fill(adminPassword);
  await admin.getByTestId('login-submit').click();
  await admin.goto('/ai-center');
  await expect(admin.getByText('AI 中心')).toBeVisible();
  await expect(admin.getByText('总调用')).toBeVisible();
  await expect(admin.getByText('product_search')).toBeVisible({ timeout: 20_000 });
  await expect(admin.getByText(/当前运行配置/)).toBeVisible();
  await admin.close();
});
