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

export interface AuditLogVO {
  id: number;
  productId: number;
  reviewerId: number;
  action: string;
  beforeStatus: string;
  afterStatus: string;
  auditRemark?: string | null;
  createdTime: string;
}

export const auditLogApi = {
  list: (params?: { page?: number; pageSize?: number }): Promise<Result<PageResponse<AuditLogVO>>> =>
    request.get<Result<PageResponse<AuditLogVO>>, Result<PageResponse<AuditLogVO>>>('/api/admin/audit-logs', { params }),
};
