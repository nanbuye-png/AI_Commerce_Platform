import React, { useState } from 'react';
import ProductGrid from './components/ProductGrid';
import ProductSkeleton from './components/ProductSkeleton';
import type { Product, ProductSortBy } from './types/product';

const sortOptions: { label: string; value: ProductSortBy }[] = [
  { label: '综合', value: 'default' },
  { label: '销量', value: 'sales' },
  { label: '价格 ↑', value: 'price_asc' },
  { label: '价格 ↓', value: 'price_desc' },
  { label: '新品', value: 'newest' },
  { label: '好评', value: 'rating' },
];

const filters = [
  { label: '全部', value: 'all' },
  { label: '电子产品', value: 'electronics' },
  { label: '服装', value: 'clothing' },
  { label: '家居', value: 'home' },
  { label: '图书', value: 'books' },
  { label: '运动', value: 'sports' },
];

const ProductListPage: React.FC = () => {
  const [activeFilter, setActiveFilter] = useState('all');
  const [activeSort, setActiveSort] = useState<ProductSortBy>('default');
  const loading = false;
  const products: Product[] = [];

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
          共 0 件商品
        </span>
      </div>

      {/* Filter Area */}
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
        {filters.map((f) => (
          <button
            key={f.value}
            onClick={() => setActiveFilter(f.value)}
            style={{
              padding: '6px 18px',
              borderRadius: 'var(--radius-full)',
              border: 'none',
              background: activeFilter === f.value ? 'var(--color-accent)' : 'var(--color-bg-secondary)',
              color: activeFilter === f.value ? '#fff' : 'var(--color-text-primary)',
              fontSize: '14px',
              fontWeight: activeFilter === f.value ? 500 : 400,
              cursor: 'pointer',
              transition: 'all var(--transition-fast)',
            }}
          >
            {f.label}
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