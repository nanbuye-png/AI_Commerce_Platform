/**
 * 共享认证类型定义
 * 三端复用：LoginRequest / AuthResponse / UserInfo
 */

/** 登录请求 */
export interface LoginRequest {
  /** 账号（用户名或邮箱） */
  account: string;
  /** 密码 */
  password: string;
  /** 客户端类型：CUSTOMER_WEB / MERCHANT_WEB / ADMIN_WEB */
  clientType?: string;
}

/** 注册请求 */
export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  nickname?: string;
  phone?: string;
}

/** 认证响应 */
export interface AuthResponse {
  /** JWT Token */
  token: string;
  /** 用户 ID */
  userId: number;
  /** 用户名 */
  username: string;
  /** 角色（单角色兼容） */
  role: string;
  /** 角色列表 */
  roles?: string[];
  /** 客户端类型 */
  clientType?: string;
}

/** 用户基本信息 */
export interface UserInfo {
  id: number;
  username: string;
  nickname: string;
  email: string;
  role: string;
  status: string;
}

/** 统一 API 响应 */
export interface ApiResult<T = unknown> {
  code: number;
  message: string;
  data: T;
}