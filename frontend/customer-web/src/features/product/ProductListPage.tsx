import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import ProductGrid from './components/ProductGrid';
import ProductSkeleton from './components/ProductSkeleton';
import { productService, type ProductView, type CategoryNode } from '../../services/product';
import type { Product } from './types/product';

const sortOptions: { label: string; value: string }[] = [
  { label: '综合', value: 'default' },
  { label: '销量', value: 'sales' },
  { label: '价格 ↑', value: 'price_asc' },
  { label: '价格 ↓', value: 'price_desc' },
  { label: '新品', value: 'newest' },
  { label: '好评', value: 'rating' },
];

/** 将 productService 的 ProductView 映射为现有 Product 类型（供 ProductGrid/ProductCard 渲染） */
function toProduct(p: ProductView): Product {
  return {
    id: String(p.id),
    name: p.name,
    description: p.description,
    brand: p.brand,
    categoryId: p.categoryName ?? '',
    categoryName: p.categoryName,
    images: p.images.map((url, idx) => ({
      id: `${p.id}-img-${idx}`,
      url,
      alt: p.name,
      isPrimary: idx === 0,
    })),
    thumbnail: p.thumbnail,
    price: p.price,
    originalPrice: p.originalPrice,
    currency: 'CNY',
    rating: p.rating,
    reviewCount: p.reviewCount,
    salesCount: p.salesCount,
    stock: p.stock,
    status: 'ACTIVE',
    createdAt: '',
    updatedAt: '',
  };
}

const ProductListPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const categoryIdFromUrl = Number(searchParams.get('categoryId') || 0) || 0;

  const [activeFilter, setActiveFilter] = useState<string>(categoryIdFromUrl ? String(categoryIdFromUrl) : 'all');
  const [activeSort, setActiveSort] = useState('default');
  const [categories, setCategories] = useState<CategoryNode[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [total, setTotal] = useState(0);

  // 加载分类树
  useEffect(() => {
    let cancelled = false;
    productService
      .getCategoryTree()
      .then((res) => {
        if (!cancelled) setCategories(res);
      })
      .catch((err: unknown) => {
        if (!cancelled) console.error('加载分类失败:', err);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  // 当 URL 的 categoryId 变化时同步筛选状态
  useEffect(() => {
    setActiveFilter(categoryIdFromUrl ? String(categoryIdFromUrl) : 'all');
  }, [categoryIdFromUrl]);

  // 加载商品（支持分类过滤）
  useEffect(() => {
    let cancelled = false;
    setLoading(true);

    const categoryId = activeFilter !== 'all' ? Number(activeFilter) : undefined;

    productService
      .listProducts({ page: 1, size: 20, ...(categoryId ? { categoryId } : {}) })
      .then((res) => {
        if (!cancelled) {
          setProducts(res.items.map(toProduct));
          setTotal(res.total);
        }
      })
      .catch((err: unknown) => {
        if (!cancelled) console.error('加载商品列表失败:', err);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [activeFilter, activeSort]);

  return (
    <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 1200, margin: '0 auto' }}>
      {/* Header */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: 'var(--spacing-lg)',
          flexWrap: 'wrap',
          gap: 'var(--spacing-md)',
        }}
      >
        <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, color: 'var(--color-text-primary)' }}>
          全部商品
        </h1>
        <span style={{ fontSize: '14px', color: 'var(--color-text-secondary)' }}>
          共 {total} 件商品
        </span>
      </div>

      {/* Filter Area - 分类筛选 */}
      <div
        style={{
          display: 'flex',
          gap: 'var(--spacing-sm)',
          marginBottom: 'var(--spacing-md)',
          flexWrap: 'wrap',
          paddingBottom: 'var(--spacing-md)',
          borderBottom: '1px solid var(--color-border-light)',
        }}
      >
        <button
          onClick={() => setActiveFilter('all')}
          style={{
            padding: '6px 18px',
            borderRadius: 'var(--radius-full)',
            border: 'none',
            background: activeFilter === 'all' ? 'var(--color-accent)' : 'var(--color-bg-secondary)',
            color: activeFilter === 'all' ? '#fff' : 'var(--color-text-primary)',
            fontSize: '14px',
            fontWeight: activeFilter === 'all' ? 500 : 400,
            cursor: 'pointer',
            transition: 'all var(--transition-fast)',
          }}
        >
          全部
        </button>
        {categories.map((cat) => (
          <button
            key={cat.id}
            onClick={() => setActiveFilter(String(cat.id))}
            style={{
              padding: '6px 18px',
              borderRadius: 'var(--radius-full)',
              border: 'none',
              background: activeFilter === String(cat.id) ? 'var(--color-accent)' : 'var(--color-bg-secondary)',
              color: activeFilter === String(cat.id) ? '#fff' : 'var(--color-text-primary)',
              fontSize: '14px',
              fontWeight: activeFilter === String(cat.id) ? 500 : 400,
              cursor: 'pointer',
              transition: 'all var(--transition-fast)',
            }}
          >
            {cat.categoryName}
          </button>
        ))}
      </div>

      {/* Sort Area */}
      <div
        style={{
          display: 'flex',
          gap: 'var(--spacing-sm)',
          marginBottom: 'var(--spacing-lg)',
          flexWrap: 'wrap',
          alignItems: 'center',
        }}
      >
        <span style={{ fontSize: '13px', color: 'var(--color-text-secondary)', marginRight: 'var(--spacing-xs)' }}>
          排序：
        </span>
        {sortOptions.map((opt) => (
          <button
            key={opt.value}
            onClick={() => setActiveSort(opt.value)}
            style={{
              padding: '4px 12px',
              borderRadius: 'var(--radius-sm)',
              border: '1px solid',
              borderColor: activeSort === opt.value ? 'var(--color-accent)' : 'var(--color-border)',
              background: activeSort === opt.value ? 'var(--color-accent-light)' : 'transparent',
              color: activeSort === opt.value ? 'var(--color-accent)' : 'var(--color-text-secondary)',
              fontSize: '13px',
              cursor: 'pointer',
              transition: 'all var(--transition-fast)',
            }}
          >
            {opt.label}
          </button>
        ))}
      </div>

      {/* Product Grid */}
      {loading ? (
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))',
            gap: 'var(--spacing-md)',
          }}
        >
          {Array.from({ length: 8 }, (_, i) => (
            <ProductSkeleton key={i} />
          ))}
        </div>
      ) : (
        <ProductGrid
          products={products}
          loading={false}
        />
      )}
    </div>
  );
};

export default ProductListPage;