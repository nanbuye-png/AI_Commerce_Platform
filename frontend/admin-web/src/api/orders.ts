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
}

export const orderApi = {
  list: (params: { page?: number; pageSize?: number; status?: string; userId?: number; merchantId?: number }): Promise<Result<PageResponse<OrderVO>>> =>
    request.get<Result<PageResponse<OrderVO>>, Result<PageResponse<OrderVO>>>('/api/admin/orders', { params }),
  getDetail: (orderNo: string): Promise<Result<OrderVO>> =>
    request.get<Result<OrderVO>, Result<OrderVO>>(`/api/admin/orders/${orderNo}`),
};