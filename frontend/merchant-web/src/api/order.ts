import request from './request';

export interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

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

export interface CreatePaymentResult {
  paymentNo: string;
  orderNo: string;
  amount: number;
  qrToken: string;
  qrContent: string;
  expireTime: string;
}

export const orderApi = {
  list: (params?: { page?: number; pageSize?: number; status?: string }) =>
    request.get<ApiResult<PageResponse<OrderVO>>, ApiResult<PageResponse<OrderVO>>>('/api/merchant/orders', {
      params: { page: params?.page ?? 1, pageSize: params?.pageSize ?? 10, ...(params?.status ? { status: params.status } : {}) },
    }),

  getDetail: (orderNo: string) =>
    request.get<ApiResult<OrderVO>, ApiResult<OrderVO>>(`/api/merchant/orders/${orderNo}`),

  /** 商家接单 */
  acceptOrder: (orderNo: string) =>
    request.post<ApiResult<null>, ApiResult<null>>(`/api/merchant/payments/orders/${orderNo}/accept`),

  /** 商家发起收款（生成商户二维码） */
  createPayment: (orderNo: string) =>
    request.post<ApiResult<CreatePaymentResult>, ApiResult<CreatePaymentResult>>(`/api/merchant/payments/orders/${orderNo}`),

  /** 查询订单支付流水（收款凭证 Token） */
  getPaymentByOrder: (orderNo: string) =>
    request.get<ApiResult<CreatePaymentResult>, ApiResult<CreatePaymentResult>>(`/api/payments/orders/${orderNo}`),
};
