import request from './request';

export interface Result<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageResponse<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface OrderVO {
  id: number;
  orderNo: string;
  buyerId: number;
  merchantId: number;
  totalAmount: number;
  payAmount: number;
  orderStatus: string;
  createdTime: string;
  updatedTime?: string;
  paymentStatus?: string;
  shippingStatus?: string;
  logisticsCompany?: string;
  trackingNo?: string;
  items?: Array<{ productName?: string; quantity?: number; price?: number }>;
}

export interface OrderActionRequest { cancelReason?: string; closeReason?: string }

export const orderApi = {
  list: (params: { page?: number; pageSize?: number; status?: string; userId?: number; merchantId?: number }): Promise<Result<PageResponse<OrderVO>>> =>
    request.get<Result<PageResponse<OrderVO>>, Result<PageResponse<OrderVO>>>('/api/admin/orders', { params }),
  getDetail: (orderNo: string): Promise<Result<OrderVO>> =>
    request.get<Result<OrderVO>, Result<OrderVO>>(`/api/admin/orders/${orderNo}`),
  cancel: (orderNo: string, reason: string) =>
    request.post<Result<OrderVO>, Result<OrderVO>>(`/api/admin/orders/${orderNo}/cancel`, { cancelReason: reason }),
  close: (orderNo: string, reason: string) =>
    request.post<Result<OrderVO>, Result<OrderVO>>(`/api/admin/orders/${orderNo}/close`, { closeReason: reason }),
};
