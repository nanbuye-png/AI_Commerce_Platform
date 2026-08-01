import request from './request';

export interface ProductVO {
  id: number;
  productCode: string;
  productName: string;
  brand?: string;
  description?: string;
  categoryId: number;
  status: string;
  salesCount: number;
  skus?: ProductSkuVO[];
  images?: ProductImageVO[];
}

export interface ProductSkuVO {
  id: number;
  skuCode: string;
  price: number;
  originalPrice?: number;
  attributesJson: string;
  status: string;
}

export interface ProductImageVO {
  id: number;
  url: string;
  isCover: boolean;
  imageType: 'MAIN' | 'DETAIL' | 'SKU';
  sort: number;
}

export interface ProductCreateRequest {
  productName: string;
  categoryId: number;
  brand?: string;
  description?: string;
  productCode?: string;
  storeId: number;
  skus?: {
    skuCode: string;
    price: number;
    originalPrice?: number;
    attributes: Record<string, string>;
  }[];
}

export type ProductUpdateRequest = ProductCreateRequest;

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export const productApi = {
  list: (params?: { page?: number; pageSize?: number; status?: string }) =>
    request.get<PageResponse<ProductVO>>('/merchant/products', { params }),

  getById: (id: number) =>
    request.get<ProductVO>(`/merchant/products/${id}`),

  create: (data: ProductCreateRequest) =>
    request.post<ProductVO>('/merchant/products', data),

  update: (id: number, data: ProductUpdateRequest) =>
    request.put<ProductVO>(`/merchant/products/${id}`, data),

  delete: (id: number) =>
    request.delete(`/merchant/products/${id}`),
};