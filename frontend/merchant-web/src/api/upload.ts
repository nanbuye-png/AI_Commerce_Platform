/**
 * 文件上传 API - merchant-web
 * 使用原生 fetch 上传（axios 全局 Content-Type: application/json 会破坏 multipart 边界）
 */
import { getToken } from '../utils/token';

/** 后端统一响应 */
export interface ApiResult<T = unknown> {
  code: number;
  message: string;
  data: T;
}

export const uploadApi = {
  /**
   * 上传图片
   * POST /api/upload/image (multipart/form-data)
   * @param file 图片文件
   */
  async uploadImage(file: File): Promise<string> {
    const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

    const formData = new FormData();
    formData.append('file', file);

    const token = getToken();
    const headers: Record<string, string> = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    // 注意：绝不能手动设置 Content-Type，浏览器会为 FormData 自动生成带 boundary 的 multipart 头

    let resp: Response;
    try {
      resp = await fetch(`${baseURL}/api/upload/image`, {
        method: 'POST',
        body: formData,
        headers,
      });
    } catch (err) {
      console.error('上传网络错误:', err);
      throw new Error('网络错误，请检查网络连接', { cause: err });
    }

    if (!resp.ok) {
      let message = `上传失败（HTTP ${resp.status}）`;
      try {
        const body = await resp.json();
        if (body?.message) message = body.message;
      } catch {
        // ignore
      }
      throw new Error(message);
    }

    const result = (await resp.json()) as ApiResult<{ url: string }>;
    return result?.data?.url ?? '';
  },
};