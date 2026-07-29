import request from './request';

export const productApi = {
  listPending: (params?: { page?: number; pageSize?: number }) =>
    request.get<any>('/admin/products/pending', { params }),

  getDetail: (id: number) =>
    request.get<any>(`/admin/products/${id}`),

  approve: (id: number, remark?: string) =>
    request.put<any>(`/admin/products/${id}/approve`, { auditRemark: remark || '审核通过' }),

  reject: (id: number, remark?: string) =>
    request.put<any>(`/admin/products/${id}/reject`, { auditRemark: remark || '审核驳回' }),

  offShelf: (id: number, remark?: string) =>
    request.put<any>(`/admin/products/${id}/off-shelf`, { auditRemark: remark || '强制下架' }),

  restore: (id: number, remark?: string) =>
    request.put<any>(`/admin/products/${id}/restore`, { auditRemark: remark || '恢复上架' }),
};