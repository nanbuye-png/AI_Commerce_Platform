/**
 * Axios 封装 - admin-web
 * 请求拦截器：自动注入 Authorization: Bearer {admin_token}
 * 响应拦截器：统一处理 401/403
 */
import axios, { AxiosError } from 'axios';
import type { InternalAxiosRequestConfig } from 'axios';
import { getToken, removeToken } from '../utils/token';

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});

request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken();
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  },
);

request.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error: AxiosError) => {
    if (error.response) {
      const { status } = error.response;
      switch (status) {
        case 401:
          removeToken();
          window.location.href = '/login';
          break;
        case 403:
          console.error('权限不足：您没有权限执行此操作');
          break;
        default:
          if (error.response.data && typeof error.response.data === 'object') {
            const data = error.response.data as { message?: string };
            if (data.message) console.error(data.message);
          }
          break;
      }
    } else if (error.request) {
      console.error('网络错误，请检查网络连接');
    }
    return Promise.reject(error);
  },
);

export default request;