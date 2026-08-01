import request from './request';

export interface Result<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageResponse<T> {
  list: T[];
  page: number;
  size: number;
  total: number;
  pages: number;
}

export interface ProductVO {
  id: number;
  productName: string;
  merchantId: number;
  status: string;
  createdTime: string;
}

export const productApi = {
  listPending: (params?: { page?: number; size?: number }): Promise<Result<PageResponse<ProductVO>>> =>
    request.get<Result<PageResponse<ProductVO>>, Result<PageResponse<ProductVO>>>('/admin/products/pending', { params }),

  getDetail: (id: number) =>
    request.get<Result<ProductVO>, Result<ProductVO>>(`/admin/products/${id}`),

  approve: (id: number, remark?: string) =>
    request.put<Result<void>, Result<void>>(`/admin/products/${id}/approve`, { auditRemark: remark || '审核通过' }),

  reject: (id: number, remark?: string) =>
    request.put<Result<void>, Result<void>>(`/admin/products/${id}/reject`, { auditRemark: remark || '审核驳回' }),

  offShelf: (id: number, remark?: string) =>
    request.put<Result<void>, Result<void>>(`/admin/products/${id}/off-shelf`, { auditRemark: remark || '强制下架' }),

  restore: (id: number, remark?: string) =>
    request.put<Result<void>, Result<void>>(`/admin/products/${id}/restore`, { auditRemark: remark || '恢复上架' }),
};