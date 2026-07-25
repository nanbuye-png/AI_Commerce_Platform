/**
 * authStore - customer-web 认证状态管理
 * 使用 Zustand 管理登录态、Token 和用户信息
 * Token 存储 key: customer_token (三端隔离)
 */
import { create } from 'zustand';
import { setToken, getToken, removeToken } from '../utils/token';
import type { UserInfo } from '@shared/types/auth';

interface AuthState {
  /** JWT Token */
  token: string | null;
  /** 用户信息 */
  userInfo: UserInfo | null;
  /** 是否已认证 */
  isAuthenticated: boolean;

  /** 登录：保存 token 和用户信息 */
  login: (token: string, userInfo: UserInfo) => void;
  /** 退出：清除 token 和用户信息 */
  logout: () => void;
  /** 更新用户信息 */
  setUser: (userInfo: UserInfo) => void;
  /** 初始化：从 localStorage 恢复 token */
  init: () => void;
}

const useAuthStore = create<AuthState>((set) => ({
  token: null,
  userInfo: null,
  isAuthenticated: false,

  login: (token: string, userInfo: UserInfo) => {
    setToken(token);
    set({
      token,
      userInfo,
      isAuthenticated: true,
    });
  },

  logout: () => {
    removeToken();
    set({
      token: null,
      userInfo: null,
      isAuthenticated: false,
    });
  },

  setUser: (userInfo: UserInfo) => {
    set({ userInfo });
  },

  init: () => {
    const token = getToken();
    if (token) {
      set({ token, isAuthenticated: true });
    }
  },
}));

export default useAuthStore;