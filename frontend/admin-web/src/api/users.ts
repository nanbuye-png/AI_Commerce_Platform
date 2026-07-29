import request from './request';

export interface UserVO {
  id: number;
  username: string;
  email: string;
  nickname: string | null;
  phone: string | null;
  avatar: string | null;
  role: string;
  status: string;
  createdTime: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export const userApi = {
  list: (params?: { page?: number; pageSize?: number; role?: string; status?: string; keyword?: string }) =>
    request.get<any>('/admin/users', { params }),

  getDetail: (id: number) =>
    request.get<any>(`/admin/users/${id}`),

  updateStatus: (id: number, status: string) =>
    request.put<any>(`/admin/users/${id}/status`, { status }),
};