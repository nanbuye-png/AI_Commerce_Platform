import request from './request';

export const orderApi = {
  list: (params?: { page?: number; pageSize?: number; status?: string; orderNo?: string; merchantId?: number }) =>
    request.get<any>('/admin/orders', { params }),

  getDetail: (orderNo: string) =>
    request.get<any>(`/admin/orders/${orderNo}`),
};