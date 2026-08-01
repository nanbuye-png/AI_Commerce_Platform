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

export const userApi = {
  list: (params?: { page?: number; pageSize?: number; role?: string; status?: string; keyword?: string }): Promise<Result<PageResponse<UserVO>>> =>
    request.get<Result<PageResponse<UserVO>>, Result<PageResponse<UserVO>>>('/admin/users', { params }),
  getDetail: (id: number): Promise<Result<UserVO>> =>
    request.get<Result<UserVO>, Result<UserVO>>(`/admin/users/${id}`),
  updateStatus: (id: number, status: string) =>
    request.put<Result<UserVO>, Result<UserVO>>(`/admin/users/${id}/status`, { status }),
};