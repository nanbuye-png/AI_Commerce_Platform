/**
 * 购物车 API 服务 - customer-web
 * 封装 C 端购物车接口：查询、添加、修改数量、删除、结算
 */
import request from '../api/request';

/** 后端统一响应 */
export interface ApiResult<T = unknown> {
  code: number;
  message: string;
  data: T;
}

/** 后端购物车条目 DTO */
export interface BackendCartItem {
  id: number;
  skuId: number;
  productId: number;
  productName: string;
  productImage?: string;
  price: number | string;
  quantity: number;
  selected: boolean;
}

/** 后端购物车 DTO */
export interface BackendCart {
  id: number;
  userId: number;
  items: BackendCartItem[];
}

export interface AddCartItemParams {
  skuId: number;
  productId: number;
  productName: string;
  productImage?: string;
  price: number;
  quantity: number;
}

export const cartService = {
  /**
   * 获取购物车
   * GET /api/cart
   */
  async getCart(): Promise<BackendCart> {
    const res = await request.get<ApiResult<BackendCart>, ApiResult<BackendCart>>('/api/cart');
    return res.data;
  },

  /**
   * 添加商品到购物车
   * POST /api/cart/items
   */
  async addItem(params: AddCartItemParams): Promise<BackendCart> {
    const res = await request.post<ApiResult<BackendCart>, ApiResult<BackendCart>>('/api/cart/items', params);
    return res.data;
  },

  /**
   * 更新商品数量
   * PUT /api/cart/items
   */
  async updateQuantity(skuId: number, quantity: number): Promise<BackendCart> {
    const res = await request.put<ApiResult<BackendCart>, ApiResult<BackendCart>>('/api/cart/items', { skuId, quantity });
    return res.data;
  },

  /**
   * 删除购物车商品
   * DELETE /api/cart/items
   */
  async removeItem(skuId: number): Promise<BackendCart> {
    const res = await request.delete<ApiResult<BackendCart>, ApiResult<BackendCart>>('/api/cart/items', {
      data: { skuId },
    });
    return res.data;
  },

  /**
   * 购物车结算
   * POST /api/cart/checkout
   * 返回结算单号（如 CHK...）
   */
  async checkout(cartItemIds: number[], addressId: number, paymentMethod: string): Promise<string> {
    const res = await request.post<ApiResult<string>, ApiResult<string>>('/api/cart/checkout', {
      cartItemIds,
      addressId,
      paymentMethod,
    });
    return res.data;
  },
};