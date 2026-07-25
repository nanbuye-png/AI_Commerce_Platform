/**
 * authStore - merchant-web 认证状态管理
 * Token 存储 key: merchant_token (三端隔离)
 */
import { create } from 'zustand';
import { setToken, getToken, removeToken } from '../utils/token';
import type { UserInfo } from '@shared/types/auth';

interface AuthState {
  token: string | null;
  userInfo: UserInfo | null;
  isAuthenticated: boolean;
  login: (token: string, userInfo: UserInfo) => void;
  logout: () => void;
  setUser: (userInfo: UserInfo) => void;
  init: () => void;
}

const useAuthStore = create<AuthState>((set) => ({
  token: null,
  userInfo: null,
  isAuthenticated: false,

  login: (token: string, userInfo: UserInfo) => {
    setToken(token);
    set({ token, userInfo, isAuthenticated: true });
  },

  logout: () => {
    removeToken();
    set({ token: null, userInfo: null, isAuthenticated: false });
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