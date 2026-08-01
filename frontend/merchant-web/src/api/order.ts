import request from './request';

/** 后端统一响应包装 */
export interface ApiResult<T = unknown> {
  code: number;
  message: string;
  data: T;
}

/** Spring Data Page 响应结构 */
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface OrderVO {
  id: number;
  orderNo: string;
  buyerId: number;
  buyerName?: string;
  merchantId: number;
  storeId: number;
  totalAmount: number;
  productAmount: number;
  freightAmount: number;
  discountAmount: number;
  payAmount: number;
  orderStatus: string;
  paymentStatus: string;
  shippingStatus: string;
  buyerRemark?: string;
  merchantRemark?: string;
  createdTime: string;
  updatedTime: string;
  displayStatus?: string;
  items?: OrderItemVO[];
}

export interface OrderItemVO {
  id: number;
  skuId: number;
  productId: number;
  productName: string;
  skuCode: string;
  price: number;
  quantity: number;
  subtotal: number;
}

export const orderApi = {
  /**
   * 商家订单列表
   * GET /api/merchant/orders?page=1&pageSize=10&status=xxx
   */
  list: (params?: { page?: number; pageSize?: number; status?: string }) =>
    request.get<ApiResult<PageResponse<OrderVO>>, ApiResult<PageResponse<OrderVO>>>('/api/merchant/orders', {
      params: { page: params?.page ?? 1, pageSize: params?.pageSize ?? 10, ...(params?.status ? { status: params.status } : {}) },
    }),

  /**
   * 商家订单详情
   * GET /api/merchant/orders/{orderNo}
   */
  getDetail: (orderNo: string) =>
    request.get<ApiResult<OrderVO>, ApiResult<OrderVO>>(`/api/merchant/orders/${orderNo}`),
};