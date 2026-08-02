import React from 'react';
import type { Product } from '../types/product';
import ProductCard from './ProductCard';
import ProductSkeleton from './ProductSkeleton';
import { EmptyState } from '../../../components/common';

interface ProductGridProps {
  products: Product[];
  loading?: boolean;
  compact?: boolean;
  onAddToCart?: (productId: string) => void;
  onFavorite?: (productId: string) => void;
  /** 已收藏的商品 ID 集合（用于卡片心形高亮） */
  favoritedIds?: Set<string>;
}

const ProductGrid: React.FC<ProductGridProps> = ({
  products,
  loading = false,
  compact = false,
  onAddToCart,
  onFavorite,
  favoritedIds,
}) => {
  if (loading) {
    return (
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: `repeat(auto-fill, minmax(${compact ? '160px' : '220px'}, 1fr))`,
          gap: 'var(--spacing-md)',
        }}
      >
        {Array.from({ length: 8 }, (_, i) => (
          <ProductSkeleton key={i} />
        ))}
      </div>
    );
  }

  if (!products.length) {
    return (
      <EmptyState
        icon="📦"
        title="暂无商品"
        description="没有找到符合条件的商品"
      />
    );
  }

  return (
    <div
      style={{
        display: 'grid',
        gridTemplateColumns: `repeat(auto-fill, minmax(${compact ? '160px' : '220px'}, 1fr))`,
        gap: 'var(--spacing-md)',
      }}
    >
      {products.map((product) => (
        <ProductCard
          key={product.id}
          product={product}
          variant={compact ? 'compact' : 'full'}
          onAddToCart={onAddToCart}
          onFavorite={onFavorite}
          favorited={favoritedIds ? favoritedIds.has(product.id) : false}
        />
      ))}
    </div>
  );
};

export default React.memo(ProductGrid);