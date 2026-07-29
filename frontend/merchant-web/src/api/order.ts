import request from './request';

export interface OrderVO {
  id: number;
  orderNo: string;
  buyerId: number;
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

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export const orderApi = {
  list: (params?: { page?: number; pageSize?: number; status?: string }) =>
    request.get<PageResponse<OrderVO>>('/merchant/orders', { params }),

  getDetail: (orderNo: string) =>
    request.get<OrderVO>(`/merchant/orders/${orderNo}`),
};