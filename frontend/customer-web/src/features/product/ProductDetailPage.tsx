import React, { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import ProductGallery from './components/ProductGallery';
import ProductPrice from './components/ProductPrice';
import ProductBadge from './components/ProductBadge';
import ProductSkeleton from './components/ProductSkeleton';
import type { Product } from './types/product';

const ProductDetailPage: React.FC = () => {
  const { productId } = useParams<{ productId: string }>();
  const navigate = useNavigate();
  const loading = false;
  // const { product, loading, error, fetchProduct } = useProductDetail();
  const product = null as Product | null;

  useEffect(() => {
    if (productId) {
      // fetchProduct(productId);
    }
  }, [productId]);

  if (loading) {
    return <ProductSkeleton variant="detail" />;
  }

  if (!product) {
    return (
      <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 960, margin: '0 auto', textAlign: 'center' }}>
        <h2 style={{ fontSize: 'var(--font-size-h2)', color: 'var(--color-text-secondary)', marginBottom: 'var(--spacing-md)' }}>
          商品信息加载中...
        </h2>
        <ProductSkeleton variant="detail" />
      </div>
    );
  }

  return (
    <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 960, margin: '0 auto' }}>
      {/* Breadcrumb */}
      <div style={{ fontSize: '13px', color: 'var(--color-text-tertiary)', marginBottom: 'var(--spacing-lg)', display: 'flex', gap: 4, alignItems: 'center' }}>
        <span onClick={() => navigate('/')} style={{ cursor: 'pointer' }}>首页</span>
        <span>/</span>
        <span onClick={() => navigate('/products')} style={{ cursor: 'pointer' }}>商品</span>
        {product.categoryName && (
          <>
            <span>/</span>
            <span>{product.categoryName}</span>
          </>
        )}
      </div>

      {/* Product Main Area */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--spacing-xl)', marginBottom: 'var(--spacing-xl)' }}>
        {/* Gallery */}
        <div>
          <ProductGallery images={product.images} thumbnail={product.thumbnail} />
        </div>

        {/* Info */}
        <div>
          {/* Title & Badges */}
          <div style={{ display: 'flex', gap: 'var(--spacing-sm)', alignItems: 'center', marginBottom: 'var(--spacing-sm)', flexWrap: 'wrap' }}>
            {product.isNew && <ProductBadge type="new" />}
            {product.isHot && <ProductBadge type="hot" />}
          </div>
          <h1 style={{ fontSize: 'var(--font-size-h2)', fontWeight: 600, color: 'var(--color-text-primary)', marginBottom: 'var(--spacing-sm)' }}>
            {product.name}
          </h1>
          {product.brand && (
            <p style={{ fontSize: '14px', color: 'var(--color-text-secondary)', marginBottom: 'var(--spacing-sm)' }}>
              品牌：{product.brand}
            </p>
          )}
          <p style={{ fontSize: '14px', color: 'var(--color-text-tertiary)', marginBottom: 'var(--spacing-md)' }}>
            {product.description}
          </p>

          {/* Price */}
          <div style={{ padding: 'var(--spacing-md)', background: 'var(--color-bg-secondary)', borderRadius: 'var(--radius-md)', marginBottom: 'var(--spacing-lg)' }}>
            <ProductPrice price={product.price} originalPrice={product.originalPrice} size="lg" />
            {/* Installment placeholder */}
            {product.price >= 1000 && (
              <p style={{ fontSize: '12px', color: 'var(--color-text-tertiary)', marginTop: 4 }}>
                支持分期付款，低至 ¥{(product.price / 12).toFixed(2)}/月
              </p>
            )}
          </div>

          {/* Ratings & Sales */}
          <div style={{ display: 'flex', gap: 'var(--spacing-lg)', marginBottom: 'var(--spacing-lg)', fontSize: '14px', color: 'var(--color-text-secondary)' }}>
            <span>★ {product.rating.toFixed(1)} ({product.reviewCount}条评价)</span>
            <span>已售 {product.salesCount}</span>
            <span>库存 {product.stock}</span>
          </div>

          {/* Specifications Selector */}
          {product.specs?.map((specGroup) => (
            <div key={specGroup.name} style={{ marginBottom: 'var(--spacing-md)' }}>
              <h3 style={{ fontSize: '14px', fontWeight: 500, color: 'var(--color-text-primary)', marginBottom: 'var(--spacing-sm)' }}>
                {specGroup.name}
              </h3>
              <div style={{ display: 'flex', gap: 'var(--spacing-sm)', flexWrap: 'wrap' }}>
                {specGroup.options.map((opt) => (
                  <button
                    key={opt.value}
                    style={{
                      padding: '8px 16px',
                      borderRadius: 'var(--radius-sm)',
                      border: '1px solid var(--color-border)',
                      background: 'transparent',
                      color: 'var(--color-text-primary)',
                      fontSize: '13px',
                      cursor: 'pointer',
                      transition: 'all var(--transition-fast)',
                    }}
                  >
                    {opt.name}
                  </button>
                ))}
              </div>
            </div>
          ))}

          {/* Quantity */}
          <div style={{ marginBottom: 'var(--spacing-lg)' }}>
            <h3 style={{ fontSize: '14px', fontWeight: 500, color: 'var(--color-text-primary)', marginBottom: 'var(--spacing-sm)' }}>
              数量
            </h3>
            <div style={{ display: 'flex', alignItems: 'center', gap: 0 }}>
              <button
                style={{
                  width: 36,
                  height: 36,
                  borderRadius: 'var(--radius-sm) 0 0 var(--radius-sm)',
                  border: '1px solid var(--color-border)',
                  background: 'var(--color-bg-secondary)',
                  cursor: 'pointer',
                  fontSize: '16px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                −
              </button>
              <input
                type="number"
                value={1}
                style={{
                  width: 60,
                  height: 36,
                  textAlign: 'center',
                  border: '1px solid var(--color-border)',
                  borderLeft: 'none',
                  borderRight: 'none',
                  fontSize: '14px',
                  outline: 'none',
                  color: 'var(--color-text-primary)',
                  background: 'var(--color-bg-primary)',
                }}
                readOnly
              />
              <button
                style={{
                  width: 36,
                  height: 36,
                  borderRadius: '0 var(--radius-sm) var(--radius-sm) 0',
                  border: '1px solid var(--color-border)',
                  background: 'var(--color-bg-secondary)',
                  cursor: 'pointer',
                  fontSize: '16px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                +
              </button>
            </div>
          </div>

          {/* Action Buttons */}
          <div style={{ display: 'flex', gap: 'var(--spacing-sm)' }}>
            <button
              style={{
                flex: 1,
                height: 48,
                borderRadius: 'var(--radius-sm)',
                background: 'var(--color-accent)',
                color: '#fff',
                fontSize: '16px',
                fontWeight: 500,
                border: 'none',
                cursor: 'pointer',
              }}
            >
              加入购物车
            </button>
            <button
              style={{
                flex: 1,
                height: 48,
                borderRadius: 'var(--radius-sm)',
                background: 'var(--color-text-primary)',
                color: '#fff',
                fontSize: '16px',
                fontWeight: 500,
                border: 'none',
                cursor: 'pointer',
              }}
            >
              立即购买
            </button>
          </div>
        </div>
      </div>

      {/* AI Advisor Placeholder */}
      <div
        style={{
          padding: 'var(--spacing-xl)',
          background: 'linear-gradient(135deg, var(--color-accent-light) 0%, #f0f7ff 100%)',
          borderRadius: 'var(--radius-lg)',
          marginBottom: 'var(--spacing-xl)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: 'var(--spacing-md)',
        }}
      >
        <div>
          <h3 style={{ fontSize: '16px', fontWeight: 600, color: 'var(--color-accent)', marginBottom: 4 }}>
            AI 购买顾问
          </h3>
          <p style={{ fontSize: '14px', color: 'var(--color-text-secondary)' }}>
            想了解更多？问问 AI 助手这款商品是否适合你
          </p>
        </div>
        <button
          onClick={() => navigate('/ai')}
          style={{
            padding: '10px 24px',
            borderRadius: 'var(--radius-sm)',
            background: 'var(--color-accent)',
            color: '#fff',
            fontSize: '14px',
            fontWeight: 500,
            border: 'none',
            cursor: 'pointer',
          }}
        >
          咨询 AI
        </button>
      </div>

      {/* Related Products Placeholder */}
      <div>
        <h2 style={{ fontSize: 'var(--font-size-h2)', fontWeight: 600, color: 'var(--color-text-primary)', marginBottom: 'var(--spacing-md)' }}>
          相关推荐
        </h2>
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(160px, 1fr))',
            gap: 'var(--spacing-md)',
          }}
        >
          {[1, 2, 3, 4].map((i) => (
            <div
              key={i}
              style={{
                background: 'var(--color-bg-secondary)',
                borderRadius: 'var(--radius-md)',
                overflow: 'hidden',
                cursor: 'pointer',
              }}
            >
              <div style={{ aspectRatio: '1/1', background: 'var(--color-bg-tertiary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '12px', color: 'var(--color-text-tertiary)' }}>
                推荐 {i}
              </div>
              <div style={{ padding: 'var(--spacing-sm)' }}>
                <p style={{ fontSize: '13px', color: 'var(--color-text-primary)', marginBottom: 2 }}>推荐商品 {i}</p>
                <p style={{ fontSize: '14px', fontWeight: 600, color: 'var(--color-accent)' }}>¥99.00</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default ProductDetailPage;