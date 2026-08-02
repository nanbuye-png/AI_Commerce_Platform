/* ============================================================
   Product Domain Types
   ============================================================ */

/** 商品规格选项 */
export interface ProductSpecOption {
  readonly name: string;
  readonly value: string;
  readonly priceAdjust?: number;
  readonly stock?: number;
  readonly image?: string;
}

/** 商品规格组（如颜色、尺寸） */
export interface ProductSpecGroup {
  readonly name: string;
  readonly options: ProductSpecOption[];
}

/** 商品图片 */
export interface ProductImage {
  readonly id: string;
  readonly url: string;
  readonly alt?: string;
  readonly width?: number;
  readonly height?: number;
  readonly isPrimary?: boolean;
}

/** 商品评价 */
export interface ProductReview {
  readonly id: string;
  readonly userId: string;
  readonly username: string;
  readonly rating: number;
  readonly content: string;
  readonly images?: string[];
  readonly createdAt: string;
  readonly helpfulCount?: number;
}

/** 商品状态 */
export type ProductStatus = 'ACTIVE' | 'INACTIVE' | 'OUT_OF_STOCK' | 'DISCONTINUED';

/** 商品排序方式 */
export type ProductSortBy = 'default' | 'price_asc' | 'price_desc' | 'sales' | 'rating' | 'newest';

/** 商品筛选条件 */
export interface ProductFilters {
  readonly categoryId?: string;
  readonly brand?: string;
  readonly minPrice?: number;
  readonly maxPrice?: number;
  readonly rating?: number;
  readonly attributes?: Record<string, string[]>;
  readonly sortBy?: ProductSortBy;
  readonly keyword?: string;
  readonly page?: number;
  readonly pageSize?: number;
}

/** 商品核心实体 */
export interface Product {
  readonly id: string;
  readonly name: string;
  readonly description: string;
  readonly brand?: string;
  readonly categoryId: string;
  readonly categoryName?: string;
  readonly images: ProductImage[];
  readonly thumbnail: string;
  readonly price: number;
  readonly originalPrice?: number;
  readonly discount?: number;
  readonly currency: string;
  readonly rating: number;
  readonly reviewCount: number;
  readonly salesCount: number;
  readonly stock?: number;
  readonly status: ProductStatus;
  readonly specs?: ProductSpecGroup[];
  readonly tags?: string[];
  readonly isNew?: boolean;
  readonly isHot?: boolean;
  readonly isRecommended?: boolean;
  readonly createdAt: string;
  readonly updatedAt: string;
}

/** 商品列表（分页） */
export interface ProductListResult {
  readonly items: Product[];
  readonly total: number;
  readonly page: number;
  readonly pageSize: number;
  readonly totalPages: number;
}

/** 商品分类 */
export interface ProductCategory {
  readonly id: string;
  readonly name: string;
  readonly slug: string;
  readonly icon?: string;
  readonly image?: string;
  readonly parentId?: string;
  readonly children?: ProductCategory[];
  readonly productCount?: number;
  readonly sortOrder: number;
}