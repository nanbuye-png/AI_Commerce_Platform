/**
 * 商品 API 服务 - customer-web
 * 封装 C 端商品浏览接口：列表、详情
 * 并将后端 DTO（productName/coverImage/minPrice）适配为前端 Product 类型（name/thumbnail/price）
 */
import request from '../api/request';

/** 后端统一响应 */
export interface ApiResult<T = unknown> {
  code: number;
  message: string;
  data: T;
}

/** 后端分页结构 */
export interface PageResult<T> {
  list: T[];
  page: number;
  size: number;
  total: number;
  pages: number;
}

/** 后端商品卡片 DTO */
export interface BackendProductCard {
  id: number;
  productName: string;
  description?: string;
  brand?: string;
  categoryId?: number;
  categoryName?: string;
  minPrice?: number | string;
  maxPrice?: number | string;
  coverImage?: string;
  salesCount?: number;
  createdTime?: string;
}

/** 后端商品详情 DTO */
export interface BackendProductDetail {
  id: number;
  productName: string;
  description?: string;
  brand?: string;
  categoryId?: number;
  categoryName?: string;
  storeName?: string;
  storeId?: number;
  salesCount?: number;
  createdTime?: string;
  images?: {
    url: string;
    imageType: string;
    sort?: number;
    isCover?: boolean;
  }[];
  specs?: {
    specName: string;
    specValues: string;
    sort?: number;
  }[];
  skus?: {
    id: number;
    skuCode: string;
    attributesJson?: string;
    price: number | string;
    originalPrice?: number | string;
    weight?: number | string;
    status?: string;
    stock?: number;
  }[];
}

/** SKU 视图（供加购/下单使用） */
export interface ProductSkuView {
  id: number;
  skuCode?: string;
  price: number;
  originalPrice?: number;
  stock?: number;
}

/** 适配后的前端统一商品结构（供 ProductCard/ProductDetailPage 使用） */
export interface ProductView {
  id: number;
  name: string;
  description: string;
  brand?: string;
  categoryName?: string;
  thumbnail: string;
  images: string[];
  price: number;
  originalPrice?: number;
  salesCount: number;
  rating: number;
  reviewCount: number;
  stock?: number;
  specs?: { name: string; options: { name: string; value: string }[] }[];
  skus?: ProductSkuView[];
}

/**
 * 后端卡片 DTO → 前端 ProductView
 */
function toCardView(p: BackendProductCard): ProductView {
  const minPrice = Number(p.minPrice ?? 0);
  const maxPrice = Number(p.maxPrice ?? minPrice);
  const price = minPrice > 0 ? minPrice : maxPrice;
  return {
    id: p.id,
    name: p.productName,
    description: p.description ?? '',
    brand: p.brand,
    categoryName: p.categoryName,
    thumbnail: p.coverImage ?? '',
    images: p.coverImage ? [p.coverImage] : [],
    price,
    originalPrice: undefined,
    salesCount: p.salesCount ?? 0,
    rating: 0,
    reviewCount: 0,
    stock: 0,
  };
}

/**
 * 后端详情 DTO → 前端 ProductView
 */
function toDetailView(p: BackendProductDetail): ProductView {
  const skus = p.skus ?? [];
  const prices = skus.map((s) => Number(s.price)).filter((v) => Number.isFinite(v) && v > 0);
  const minPrice = prices.length ? Math.min(...prices) : 0;
  const originalPrices = skus
    .map((s) => Number(s.originalPrice))
    .filter((v) => Number.isFinite(v) && v > 0);
  const originalPrice = originalPrices.length ? Math.min(...originalPrices) : undefined;
  // 商品总库存：仅当有任何 SKU 存在库存记录时才求和；
  // 若所有 SKU 均缺失库存记录（存量商品），返回 undefined 供前端"软判断"不拦截加购
  const hasAnyStockRecord = skus.some((s) => s.stock !== undefined && s.stock !== null);
  const totalStock = skus.reduce((sum, s) => sum + (Number(s.stock) || 0), 0);
  const stock = hasAnyStockRecord ? totalStock : undefined;

  const images = (p.images ?? [])
    .slice()
    .sort((a, b) => Number(a.isCover ? 0 : 1) - Number(b.isCover ? 0 : 1))
    .map((i) => i.url);

  const specs = (p.specs ?? [])
    .filter((s) => s.specName && s.specValues)
    .map((s) => {
      let options: { name: string; value: string }[] = [];
      try {
        const parsed = JSON.parse(s.specValues);
        if (Array.isArray(parsed)) {
          options = parsed.map((v) => (typeof v === 'string' ? { name: v, value: v } : { name: String(v), value: String(v) }));
        }
      } catch {
        options = [{ name: s.specValues, value: s.specValues }];
      }
      return { name: s.specName, options };
    });

  return {
    id: p.id,
    name: p.productName,
    description: p.description ?? '',
    brand: p.brand,
    categoryName: p.categoryName,
    thumbnail: images[0] ?? '',
    images,
    price: minPrice,
    originalPrice,
    salesCount: p.salesCount ?? 0,
    rating: 0,
    reviewCount: 0,
    stock,
    specs,
    skus: skus.map((s) => ({
      id: s.id,
      skuCode: s.skuCode,
      price: Number(s.price) || 0,
      originalPrice: s.originalPrice ? Number(s.originalPrice) : undefined,
      stock: s.stock,
    })),
  };
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

export const productService = {
  /**
   * 分类树
   * GET /api/categories/tree
   */
  async getCategoryTree(): Promise<CategoryNode[]> {
    const res = await request.get<ApiResult<CategoryNode[]>, ApiResult<CategoryNode[]>>('/api/categories/tree');
    return res.data ?? [];
  },

  /**
   * 商品列表
   * GET /api/products?page=1&size=20&keyword=xxx
   */
  async listProducts(params: { page?: number; size?: number; keyword?: string; categoryId?: number } = {}): Promise<{
    items: ProductView[];
    total: number;
    page: number;
    pages: number;
  }> {
    const res = await request.get<ApiResult<PageResult<BackendProductCard>>, ApiResult<PageResult<BackendProductCard>>>('/api/products', {
      params: {
        page: params.page ?? 1,
        size: params.size ?? 20,
        ...(params.keyword ? { keyword: params.keyword } : {}),
        ...(params.categoryId ? { categoryId: params.categoryId } : {}),
      },
    });
    const data = res.data;
    return {
      items: (data.list ?? []).map(toCardView),
      total: data.total ?? 0,
      page: data.page ?? 1,
      pages: data.pages ?? 0,
    };
  },

  /**
   * 商品详情
   * GET /api/products/{id}
   */
  async getProductDetail(id: number | string): Promise<ProductView> {
    const res = await request.get<ApiResult<BackendProductDetail>, ApiResult<BackendProductDetail>>(`/api/products/${id}`);
    return toDetailView(res.data);
  },
};