/**
 * Token 存储工具
 * 基于 localStorage 存储 JWT Token
 */

const TOKEN_KEY = 'access_token';

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