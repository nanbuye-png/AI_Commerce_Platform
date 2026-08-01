import request from './request';

/** 后端统一响应包装 */
export interface ApiResult<T = unknown> {
  code: number;
  message: string;
  data: T;
}

/** 分类树节点 */
export interface CategoryNode {
  id: number;
  categoryName: string;
  parentId?: number;
  level?: number;
  sort?: number;
  children?: CategoryNode[];
}

export const categoryApi = {
  /**
   * 分类树（公开接口，无需登录）
   * GET /api/categories/tree
   */
  listTree: () =>
    request.get<ApiResult<CategoryNode[]>, ApiResult<CategoryNode[]>>('/api/categories/tree'),
};