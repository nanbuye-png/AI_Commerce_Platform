/* ============================================================
   Product API — Interface Only
   No real requests. Use adapter pattern for implementation.
   ============================================================ */

import type { Product, ProductListResult, ProductFilters, ProductCategory, ProductReview } from '../types/product';

/**
 * 商品 API 接口定义
 *
 * 实现方式：
 *   import request from '@/api/request';
 *   export const productApi: ProductApi = { ... };
 */
export interface ProductApi {
  /** 获取商品列表（支持分页、筛选、排序） */
  getProducts(filters: ProductFilters): Promise<ProductListResult>;

  /** 获取商品详情 */
  getProductById(productId: string): Promise<Product>;

  /** 获取商品评价列表 */
  getProductReviews(productId: string, page?: number, pageSize?: number): Promise<{
    items: ProductReview[];
    total: number;
    page: number;
    pageSize: number;
  }>;

  /** 获取商品分类树 */
  getCategories(): Promise<ProductCategory[]>;

  /** 搜索商品 */
  searchProducts(keyword: string, filters?: ProductFilters): Promise<ProductListResult>;

  /** 获取推荐商品 */
  getRecommendedProducts(productId?: string, limit?: number): Promise<Product[]>;

  /** 获取热门商品 */
  getHotProducts(limit?: number): Promise<Product[]>;

  /** 获取新品商品 */
  getNewProducts(limit?: number): Promise<Product[]>;
}

/**
 * Product API Key — 用于依赖注入或 React Query key
 */
export const PRODUCT_API_KEY = 'productApi';

/**
 * Product Query Keys — React Query 缓存键
 */
export const productKeys = {
  all: ['products'] as const,
  lists: () => [...productKeys.all, 'list'] as const,
  list: (filters: ProductFilters) => [...productKeys.lists(), filters] as const,
  details: () => [...productKeys.all, 'detail'] as const,
  detail: (id: string) => [...productKeys.details(), id] as const,
  categories: () => [...productKeys.all, 'categories'] as const,
  search: (keyword: string) => [...productKeys.all, 'search', keyword] as const,
} as const;