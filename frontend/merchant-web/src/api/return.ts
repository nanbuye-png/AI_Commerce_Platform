import request from './request';

export interface ReturnRequestVO {
  id: number;
  orderId: number;
  userId: number;
  refundId: number | null;
  reason: string;
  status: string;
  createdAt: string;
  approvedAt: string | null;
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

export const returnApi = {
  list: (params?: { page?: number; pageSize?: number; status?: string }) =>
    request.get<Result<PageResponse<ReturnRequestVO>>, Result<PageResponse<ReturnRequestVO>>>('/api/merchant/returns', { params }),

  getDetail: (id: number) =>
    request.get<Result<ReturnRequestVO>, Result<ReturnRequestVO>>(`/api/merchant/returns/${id}`),

  approve: (id: number) =>
    request.post<Result<ReturnRequestVO>, Result<ReturnRequestVO>>(`/api/merchant/returns/${id}/approve`),

  reject: (id: number) =>
    request.post<Result<ReturnRequestVO>, Result<ReturnRequestVO>>(`/api/merchant/returns/${id}/reject`),
};