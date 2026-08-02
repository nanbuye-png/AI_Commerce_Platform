/**
 * 个人中心 API 服务 - customer-web
 * 封装 C 端个人中心接口：账号、地址、优惠券、收藏、浏览历史、库存校验
 */
import request from '../api/request';

/** 后端统一响应 */
export interface ApiResult<T = unknown> {
  code: number;
  message: string;
  data: T;
}

/** 用户个人资料 */
export interface UserProfile {
  id: number;
  username: string;
  email: string;
  nickname?: string;
  avatar?: string;
  phone?: string;
}

/** 收货地址 */
export interface Address {
  id: number;
  receiver: string;
  phone: string;
  province?: string;
  city?: string;
  district?: string;
  detailAddress?: string;
  postalCode?: string;
  isDefault?: boolean;
}

/** 地址请求 */
export interface AddressRequest {
  receiver: string;
  phone: string;
  province?: string;
  city?: string;
  district?: string;
  detailAddress?: string;
  postalCode?: string;
  isDefault?: boolean;
}

/** 优惠券 */
export interface Coupon {
  id: number;
  couponName: string;
  couponType: string;
  discountAmount: number;
  minAmount: number;
  status: string;
  expireTime?: string;
  createdTime?: string;
}

/** 收藏条目 */
export interface Favorite {
  id: number;
  productId: number;
  productName: string;
  productImage?: string;
  price?: number;
  createdTime?: string;
}

/** 浏览历史条目 */
export interface BrowseHistoryItem {
  id: number;
  productId: number;
  productName: string;
  productImage?: string;
  price?: number;
  viewedTime?: string;
}

/** 分页结构 */
export interface PageResult<T> {
  list: T[];
  page: number;
  size: number;
  total: number;
  pages: number;
}

export const profileService = {
  // ==================== 账号 ====================

  getProfile(): Promise<UserProfile> {
    return request.get<ApiResult<UserProfile>, ApiResult<UserProfile>>('/api/profile/me').then((r) => r.data);
  },

  updateProfile(data: { nickname?: string; avatar?: string; phone?: string }): Promise<UserProfile> {
    return request.put<ApiResult<UserProfile>, ApiResult<UserProfile>>('/api/profile/me', data).then((r) => r.data);
  },

  changePassword(data: { oldPassword: string; newPassword: string }): Promise<void> {
    return request.put<ApiResult<void>, ApiResult<void>>('/api/profile/me/password', data).then(() => undefined);
  },

  // ==================== 收货地址 ====================

  listAddresses(): Promise<Address[]> {
    return request.get<ApiResult<Address[]>, ApiResult<Address[]>>('/api/profile/addresses').then((r) => r.data ?? []);
  },

  createAddress(data: AddressRequest): Promise<Address> {
    return request.post<ApiResult<Address>, ApiResult<Address>>('/api/profile/addresses', data).then((r) => r.data);
  },

  updateAddress(id: number, data: AddressRequest): Promise<Address> {
    return request.put<ApiResult<Address>, ApiResult<Address>>(`/api/profile/addresses/${id}`, data).then((r) => r.data);
  },

  deleteAddress(id: number): Promise<void> {
    return request.delete<ApiResult<void>, ApiResult<void>>(`/api/profile/addresses/${id}`).then(() => undefined);
  },

  setDefaultAddress(id: number): Promise<void> {
    return request.put<ApiResult<void>, ApiResult<void>>(`/api/profile/addresses/${id}/default`).then(() => undefined);
  },

  // ==================== 优惠券 ====================

  listCoupons(status?: string): Promise<Coupon[]> {
    return request
      .get<ApiResult<Coupon[]>, ApiResult<Coupon[]>>('/api/profile/coupons', {
        params: status && status !== 'ALL' ? { status } : {},
      })
      .then((r) => r.data ?? []);
  },

  // ==================== 收藏夹 ====================

  listFavorites(page = 1, size = 20): Promise<PageResult<Favorite>> {
    return request
      .get<ApiResult<PageResult<Favorite>>, ApiResult<PageResult<Favorite>>>('/api/profile/favorites', {
        params: { page, size },
      })
      .then((r) => r.data);
  },

  addFavorite(data: { productId: number; productName?: string; productImage?: string; price?: number }): Promise<void> {
    return request.post<ApiResult<void>, ApiResult<void>>('/api/profile/favorites', data).then(() => undefined);
  },

  removeFavorite(productId: number): Promise<void> {
    return request.delete<ApiResult<void>, ApiResult<void>>(`/api/profile/favorites/${productId}`).then(() => undefined);
  },

  // ==================== 浏览历史 ====================

  listBrowseHistory(limit = 20): Promise<BrowseHistoryItem[]> {
    return request
      .get<ApiResult<BrowseHistoryItem[]>, ApiResult<BrowseHistoryItem[]>>('/api/profile/history', {
        params: { limit },
      })
      .then((r) => r.data ?? []);
  },

  addBrowseHistory(data: { productId: number; productName?: string; productImage?: string; price?: number }): Promise<void> {
    return request.post<ApiResult<void>, ApiResult<void>>('/api/profile/history', data).then(() => undefined);
  },

  clearBrowseHistory(): Promise<void> {
    return request.delete<ApiResult<void>, ApiResult<void>>('/api/profile/history').then(() => undefined);
  },

  // ==================== 库存校验 ====================

  getStock(skuId: number): Promise<number> {
    return request.get<ApiResult<number>, ApiResult<number>>(`/api/profile/stock/${skuId}`).then((r) => r.data ?? 0);
  },

  checkStock(skuId: number, quantity: number): Promise<boolean> {
    return request
      .get<ApiResult<boolean>, ApiResult<boolean>>('/api/profile/stock/check', { params: { skuId, quantity } })
      .then((r) => r.data === true);
  },
};

export default profileService;