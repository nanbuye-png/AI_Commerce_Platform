/**
 * 认证 API 服务 - merchant-web
 * 自动携带 clientType: MERCHANT_WEB
 */
import request from '../api/request';
import type {
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  ApiResult,
  UserInfo,
} from '@shared/types/auth';

export function registerApi(data: RegisterRequest): Promise<ApiResult<UserInfo>> {
  return request.post('/api/auth/register', {
    ...data,
    role: 'MERCHANT',
  });
}

export function loginApi(data: LoginRequest): Promise<ApiResult<AuthResponse>> {
  return request.post('/api/auth/login', {
    ...data,
    clientType: 'MERCHANT_WEB',
  });
}