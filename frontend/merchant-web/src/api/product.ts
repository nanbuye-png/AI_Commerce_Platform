import request from './request';

/** 后端统一响应包装 */
export interface ApiResult<T = unknown> {
  code: number;
  message: string;
  data: T;
}

/** Spring Data Page 响应结构 */
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

/** 后端商品列表 VO（MerchantProductController.listMyProducts 返回） */
export interface ProductVO {
  id: number;
  productCode: string;
  productName: string;
  brand?: string;
  description?: string;
  categoryId: number;
  status: string;
  salesCount: number;
  coverImage?: string;
  createdTime?: string;
  updatedTime?: string;
  version?: number;
}

/** 后端 SKU VO */
export interface ProductSkuVO {
  id: number;
  skuCode: string;
  price: number;
  originalPrice?: number;
  weight?: number;
  attributesJson: string;
  status: string;
}

/** 后端图片 VO */
export interface ProductImageVO {
  id: number;
  url: string;
  isCover: boolean;
  imageType: string;
  sort: number;
}

/** 后端规格 VO */
export interface ProductSpecVO {
  id: number;
  specName: string;
  specValues: string;
  sort: number;
}

/** 商家商品详情（含 skus/images/specs） */
export interface ProductDetailVO extends ProductVO {
  skus?: ProductSkuVO[];
  images?: ProductImageVO[];
  specs?: ProductSpecVO[];
}

/** 创建商品请求（对齐后端 CreateProductRequest） */
export interface ProductSkuRequest {
  skuCode: string;
  /** JSON 字符串，如 {"颜色":"黑色","尺寸":"M"} */
  attributesJson: string;
  price: number;
  originalPrice?: number;
  weight?: number;
  stock?: number;
}

export interface ProductImageRequest {
  url: string;
  imageType: 'MAIN' | 'DETAIL' | 'SKU';
  sort?: number;
  isCover?: boolean;
}

export interface ProductSpecRequest {
  specName: string;
  specValues: string;
  sort?: number;
}

export interface ProductCreateRequest {
  productName: string;
  description?: string;
  brand?: string;
  categoryId: number;
  images?: ProductImageRequest[];
  specs?: ProductSpecRequest[];
  skus: ProductSkuRequest[];
}

export type ProductUpdateRequest = ProductCreateRequest;

export const productApi = {
  /**
   * 查询我的商品列表
   * GET /api/merchant/products?page=1&pageSize=10&status=xxx
   */
  list: (params?: { page?: number; pageSize?: number; status?: string }) =>
    request.get<ApiResult<PageResponse<ProductVO>>, ApiResult<PageResponse<ProductVO>>>('/api/merchant/products', {
      params: { page: params?.page ?? 1, pageSize: params?.pageSize ?? 10, ...(params?.status ? { status: params.status } : {}) },
    }),

  /**
   * 商品详情
   * GET /api/merchant/products/{id}
   */
  getById: (id: number) =>
    request.get<ApiResult<ProductDetailVO>, ApiResult<ProductDetailVO>>(`/api/merchant/products/${id}`),

  /**
   * 创建商品
   * POST /api/merchant/products
   */
  create: (data: ProductCreateRequest) =>
    request.post<ApiResult<number>, ApiResult<number>>('/api/merchant/products', data),

  /**
   * 更新商品
   * PUT /api/merchant/products/{id}
   */
  update: (id: number, data: ProductUpdateRequest) =>
    request.put<ApiResult<void>, ApiResult<void>>(`/api/merchant/products/${id}`, data),

  /**
   * 删除商品（软删除）
   * DELETE /api/merchant/products/{id}
   */
  delete: (id: number) =>
    request.delete<ApiResult<void>, ApiResult<void>>(`/api/merchant/products/${id}`),
};