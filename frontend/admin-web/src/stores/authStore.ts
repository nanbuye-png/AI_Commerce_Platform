/**
 * authStore - admin-web 认证状态管理
 * Token 存储 key: admin_token (三端隔离)
 */
import { create } from 'zustand';
import { setToken, getToken, removeToken } from '../utils/token';
import type { UserInfo } from '@shared/types/auth';

interface AuthState {
  token: string | null;
  userInfo: UserInfo | null;
  isAuthenticated: boolean;
  initialized: boolean;
  login: (token: string, userInfo: UserInfo) => void;
  logout: () => void;
  setUser: (userInfo: UserInfo) => void;
  init: () => void;
}

const USER_INFO_KEY = 'admin_user_info';

const readUserInfo = (): UserInfo | null => {
  try {
    const value = localStorage.getItem(USER_INFO_KEY);
    return value ? JSON.parse(value) as UserInfo : null;
  } catch {
    return null;
  }
};

const useAuthStore = create<AuthState>((set) => ({
  token: null,
  userInfo: null,
  isAuthenticated: false,
  initialized: false,

  login: (token: string, userInfo: UserInfo) => {
    setToken(token);
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo));
    set({ token, userInfo, isAuthenticated: true, initialized: true });
  },

  logout: () => {
    removeToken();
    localStorage.removeItem(USER_INFO_KEY);
    set({ token: null, userInfo: null, isAuthenticated: false, initialized: true });
  },

  setUser: (userInfo: UserInfo) => {
    set({ userInfo });
  },

  init: () => {
    const token = getToken();
    const userInfo = token ? readUserInfo() : null;
    set({ token, userInfo, isAuthenticated: Boolean(token), initialized: true });
  },
}));

export default useAuthStore;
