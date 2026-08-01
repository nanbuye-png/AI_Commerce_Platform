/**
 * 文件上传 API - merchant-web
 * 上传商品图片，返回可访问的 URL
 */
import request from './request';

/** 后端统一响应 */
export interface ApiResult<T = unknown> {
  code: number;
  message: string;
  data: T;
}

/** 上传响应 */
export interface UploadResult {
  url: string;
}

export const uploadApi = {
  /**
   * 上传图片
   * POST /api/upload/image (multipart/form-data)
   * @param file 图片文件
   */
  async uploadImage(file: File): Promise<string> {
    const formData = new FormData();
    formData.append('file', file);
    // 注意：不要手动设置 Content-Type，axios 会为 FormData 自动添加 boundary
    const res = await request.post<ApiResult<UploadResult>, ApiResult<UploadResult>>('/api/upload/image', formData, {
      timeout: 30000,
    });
    return res.data?.url ?? '';
  },
};