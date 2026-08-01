import React from 'react';
import { useNavigate } from 'react-router-dom';
import type { Product } from '../types/product';
import ProductPrice from './ProductPrice';
import ProductBadge from './ProductBadge';

interface ProductCardProps {
  product: Product;
  variant?: 'compact' | 'full' | 'horizontal';
  onAddToCart?: (productId: string) => void;
  onFavorite?: (productId: string) => void;
}

const ProductCard: React.FC<ProductCardProps> = ({
  product,
  variant = 'full',
  onAddToCart,
  onFavorite,
}) => {
  const navigate = useNavigate();

  const handleClick = () => {
    void navigate(`/products/${product.id}`);
  };

  // Compact variant (用于推荐区域/关联商品)
  if (variant === 'compact') {
    return (
      <div
        onClick={handleClick}
        style={{
          cursor: 'pointer',
          background: 'var(--color-bg-primary)',
          borderRadius: 'var(--radius-md)',
          overflow: 'hidden',
          transition: 'box-shadow var(--transition-fast), transform var(--transition-fast)',
        }}
        onMouseEnter={(e) => {
          (e.currentTarget as HTMLElement).style.boxShadow = 'var(--shadow-md)';
          (e.currentTarget as HTMLElement).style.transform = 'translateY(-2px)';
        }}
        onMouseLeave={(e) => {
          (e.currentTarget as HTMLElement).style.boxShadow = 'none';
          (e.currentTarget as HTMLElement).style.transform = 'translateY(0)';
        }}
      >
        <div
          style={{
            aspectRatio: '1/1',
            background: 'var(--color-bg-secondary)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '12px',
            color: 'var(--color-text-tertiary)',
            position: 'relative',
          }}
        >
          {product.thumbnail && (
            <img
              src={product.thumbnail}
              alt={product.name}
              style={{ width: '100%', height: '100%', objectFit: 'cover' }}
              loading="lazy"
            />
          )}
          {!product.thumbnail && <span>暂无图片</span>}
          {/* Badges */}
          <div style={{ position: 'absolute', top: 6, left: 6, display: 'flex', gap: 4 }}>
            {product.isNew && <ProductBadge type="new" />}
            {product.isHot && <ProductBadge type="hot" />}
          </div>
        </div>
        <div style={{ padding: '8px 10px' }}>
          <p
            style={{
              fontSize: '13px',
              fontWeight: 500,
              color: 'var(--color-text-primary)',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
              marginBottom: 4,
            }}
          >
            {product.name}
          </p>
          <ProductPrice price={product.price} originalPrice={product.originalPrice} size="sm" />
        </div>
      </div>
    );
  }

  // Horizontal variant (用于搜索/收藏夹)
  if (variant === 'horizontal') {
    return (
      <div
        onClick={handleClick}
        style={{
          display: 'flex',
          gap: 'var(--spacing-md)',
          cursor: 'pointer',
          background: 'var(--color-bg-primary)',
          borderRadius: 'var(--radius-md)',
          overflow: 'hidden',
          transition: 'box-shadow var(--transition-fast)',
        }}
        onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.boxShadow = 'var(--shadow-sm)'; }}
        onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.boxShadow = 'none'; }}
      >
        <div
          style={{
            width: 120,
            height: 120,
            flexShrink: 0,
            background: 'var(--color-bg-secondary)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '12px',
            color: 'var(--color-text-tertiary)',
          }}
        >
          {product.thumbnail ? (
            <img src={product.thumbnail} alt={product.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} loading="lazy" />
          ) : '暂无图片'}
        </div>
        <div style={{ flex: 1, padding: 'var(--spacing-sm) var(--spacing-sm) var(--spacing-sm) 0', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
          <h3 style={{ fontSize: '15px', fontWeight: 500, color: 'var(--color-text-primary)', marginBottom: 4, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            {product.name}
          </h3>
          <p style={{ fontSize: '12px', color: 'var(--color-text-secondary)', marginBottom: 6 }}>
            {product.brand}
          </p>
          <ProductPrice price={product.price} originalPrice={product.originalPrice} size="sm" />
        </div>
      </div>
    );
  }

  // Full variant (默认竖版卡片)
  return (
    <div
      onClick={handleClick}
      style={{
        cursor: 'pointer',
        background: 'var(--color-bg-primary)',
        borderRadius: 'var(--radius-md)',
        overflow: 'hidden',
        transition: 'box-shadow var(--transition-fast), transform var(--transition-fast)',
        boxShadow: 'var(--shadow-sm)',
        position: 'relative',
      }}
      onMouseEnter={(e) => {
        (e.currentTarget as HTMLElement).style.boxShadow = 'var(--shadow-md)';
        (e.currentTarget as HTMLElement).style.transform = 'translateY(-2px)';
      }}
      onMouseLeave={(e) => {
        (e.currentTarget as HTMLElement).style.boxShadow = 'var(--shadow-sm)';
        (e.currentTarget as HTMLElement).style.transform = 'translateY(0)';
      }}
    >
      {/* Image */}
      <div
        style={{
          position: 'relative',
          aspectRatio: '1/1',
          background: 'var(--color-bg-secondary)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: '13px',
          color: 'var(--color-text-tertiary)',
        }}
      >
        {product.thumbnail ? (
          <img src={product.thumbnail} alt={product.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} loading="lazy" />
        ) : '暂无图片'}
        {/* Badges */}
        <div style={{ position: 'absolute', top: 8, left: 8, display: 'flex', gap: 4, flexWrap: 'wrap' }}>
          {product.isNew && <ProductBadge type="new" />}
          {product.isHot && <ProductBadge type="hot" />}
          {product.discount && <ProductBadge type="sale" text={`-${Math.round(product.discount * 100)}%`} />}
        </div>
        {/* Favorite */}
        {onFavorite && (
          <button
            onClick={(e) => { e.stopPropagation(); onFavorite(product.id); }}
            style={{
              position: 'absolute',
              top: 8,
              right: 8,
              width: 32,
              height: 32,
              borderRadius: '50%',
              background: 'rgba(255,255,255,0.8)',
              border: 'none',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '16px',
            }}
          >
            ♡
          </button>
        )}
      </div>

      {/* Info */}
      <div style={{ padding: 'var(--spacing-sm) var(--spacing-md) var(--spacing-md)' }}>
        {/* Title */}
        <h3
          style={{
            fontSize: '14px',
            fontWeight: 500,
            color: 'var(--color-text-primary)',
            marginBottom: 4,
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            display: '-webkit-box',
            WebkitLineClamp: 2,
            WebkitBoxOrient: 'vertical',
            lineHeight: 1.4,
            minHeight: '2.8em',
          }}
        >
          {product.name}
        </h3>

        {/* Price */}
        <ProductPrice price={product.price} originalPrice={product.originalPrice} size="sm" />

        {/* Rating & Sales */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 6, fontSize: '12px', color: 'var(--color-text-tertiary)' }}>
          {product.rating > 0 && (
            <span>★ {product.rating.toFixed(1)}</span>
          )}
          {product.salesCount > 0 && (
            <span>已售 {product.salesCount}</span>
          )}
        </div>

        {/* Quick Add Button */}
        {onAddToCart && (
          <button
            onClick={(e) => { e.stopPropagation(); onAddToCart(product.id); }}
            style={{
              width: '100%',
              height: 32,
              marginTop: 8,
              borderRadius: 'var(--radius-sm)',
              background: 'var(--color-accent)',
              color: '#fff',
              fontSize: '13px',
              fontWeight: 500,
              border: 'none',
              cursor: 'pointer',
            }}
          >
            加入购物车
          </button>
        )}
      </div>
    </div>
  );
};

export default React.memo(ProductCard);