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

export interface MerchantVO {
  id: number;
  username: string;
  email: string;
  nickname: string | null;
  phone: string | null;
  role: string;
  status: string;
  createdTime: string;
  updatedTime?: string;
}

export const merchantApi = {
  list: (params?: { page?: number; pageSize?: number; status?: string; keyword?: string }): Promise<Result<PageResponse<MerchantVO>>> =>
    request.get<Result<PageResponse<MerchantVO>>, Result<PageResponse<MerchantVO>>>('/admin/merchants', { params }),
  getDetail: (id: number): Promise<Result<MerchantVO>> =>
    request.get<Result<MerchantVO>, Result<MerchantVO>>(`/admin/merchants/${id}`),
  updateStatus: (id: number, status: string) =>
    request.put<Result<MerchantVO>, Result<MerchantVO>>(`/admin/merchants/${id}/status`, { status }),
};