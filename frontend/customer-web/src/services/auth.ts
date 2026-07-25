/**
 * 认证 API 服务 - customer-web
 * 提供登录、注册接口调用
 * 自动携带 clientType: CUSTOMER_WEB
 */
import request from '../api/request';
import type {
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  ApiResult,
  UserInfo,
} from '@shared/types/auth';

/**
 * 用户注册
 * POST /api/auth/register
 */
export function registerApi(data: RegisterRequest): Promise<ApiResult<UserInfo>> {
  return request.post('/api/auth/register', data);
}

/**
 * 用户登录
 * POST /api/auth/login
 * 自动注入 clientType: CUSTOMER_WEB
 */
export function loginApi(data: LoginRequest): Promise<ApiResult<AuthResponse>> {
  return request.post('/api/auth/login', {
    ...data,
    clientType: 'CUSTOMER_WEB',
  });
}