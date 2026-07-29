import request from './request';

export const merchantApi = {
  list: (params?: { page?: number; pageSize?: number; status?: string; keyword?: string }) =>
    request.get<any>('/admin/merchants', { params }),

  getDetail: (id: number) =>
    request.get<any>(`/admin/merchants/${id}`),

  updateStatus: (id: number, status: string) =>
    request.put<any>(`/admin/merchants/${id}/status`, { status }),
};