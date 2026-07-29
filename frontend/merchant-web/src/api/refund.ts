import request from './request';

export interface RefundVO {
  id: number;
  orderId: number;
  userId: number;
  amount: number;
  reason: string;
  status: string;
  createdAt: string;
  completedAt: string | null;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

interface Result<T> {
  code: number;
  message: string;
  data: T;
}

export const refundApi = {
  list: (params?: { page?: number; pageSize?: number; status?: string }) =>
    request.get<Result<PageResponse<RefundVO>>, Result<PageResponse<RefundVO>>>('/merchant/refunds', { params }),

  getDetail: (id: number) =>
    request.get<Result<RefundVO>, Result<RefundVO>>(`/merchant/refunds/${id}`),

  approve: (id: number) =>
    request.post<Result<RefundVO>, Result<RefundVO>>(`/merchant/refunds/${id}/approve`),

  reject: (id: number) =>
    request.post<Result<RefundVO>, Result<RefundVO>>(`/merchant/refunds/${id}/reject`),
};