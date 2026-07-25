/**
 * admin-web Token 存储工具
 * 使用 admin_ 前缀隔离，禁止被其他端读取
 */

const TOKEN_KEY = 'admin_token';

/** 保存 Token */
export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

/** 获取 Token */
export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

/** 移除 Token */
export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}