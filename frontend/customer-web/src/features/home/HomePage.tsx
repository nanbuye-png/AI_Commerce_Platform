import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { productService, type ProductView, type CategoryNode } from '../../services/product';

const HomePage: React.FC = () => {
  const navigate = useNavigate();
  const [products, setProducts] = useState<ProductView[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [categories, setCategories] = useState<CategoryNode[]>([]);

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

  useEffect(() => {
    let cancelled = false;

    productService
      .listProducts({ page: 1, size: 12 })
      .then((res) => {
        if (!cancelled) setProducts(res.items);
      })
      .catch((err: unknown) => {
        if (!cancelled) {
          console.error('加载商品列表失败:', err);
          setError('商品加载失败，请稍后重试');
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  const handleDetail = (id: number) => {
    void navigate(`/products/${id}`);
  };

  return (
    <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 1200, margin: '0 auto' }}>
      {/* Hero Banner Area */}
      <section
        style={{
          background: 'linear-gradient(135deg, #0071E3 0%, #5AC8FA 100%)',
          borderRadius: 'var(--radius-lg)',
          padding: 'var(--spacing-3xl) var(--spacing-xl)',
          color: '#fff',
          marginBottom: 'var(--spacing-xl)',
          textAlign: 'center',
        }}
      >
        <h1 style={{ fontSize: 'var(--font-size-hero)', fontWeight: 700, marginBottom: 'var(--spacing-sm)' }}>
          AI Commerce
        </h1>
        <p style={{ fontSize: 'var(--font-size-h3)', opacity: 0.9 }}>
          智能购物体验，从这里开始
        </p>
      </section>

      {/* Categories */}
      <section style={{ marginBottom: 'var(--spacing-xl)' }}>
        <h2 style={{ fontSize: 'var(--font-size-h2)', fontWeight: 600, marginBottom: 'var(--spacing-md)' }}>
          商品分类
        </h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(80px, 1fr))', gap: 'var(--spacing-md)' }}>
          {categories.length > 0
            ? categories.map((cat) => (
                <div
                  key={cat.id}
                  onClick={() => navigate(`/products?categoryId=${cat.id}`)}
                  style={{
                    textAlign: 'center',
                    padding: 'var(--spacing-md)',
                    background: 'var(--color-bg-secondary)',
                    borderRadius: 'var(--radius-md)',
                    cursor: 'pointer',
                    fontSize: '14px',
                    color: 'var(--color-text-primary)',
                  }}
                >
                  {cat.categoryName}
                </div>
              ))
            : ['电子产品', '服装', '家居', '图书', '运动', '美妆'].map((cat) => (
                <div
                  key={cat}
                  onClick={() => navigate('/products')}
                  style={{
                    textAlign: 'center',
                    padding: 'var(--spacing-md)',
                    background: 'var(--color-bg-secondary)',
                    borderRadius: 'var(--radius-md)',
                    cursor: 'pointer',
                    fontSize: '14px',
                    color: 'var(--color-text-primary)',
                  }}
                >
                  {cat}
                </div>
              ))}
        </div>
      </section>

      {/* Recommended Products */}
      <section style={{ marginBottom: 'var(--spacing-xl)' }}>
        <h2 style={{ fontSize: 'var(--font-size-h2)', fontWeight: 600, marginBottom: 'var(--spacing-md)' }}>
          为你推荐
        </h2>

        {loading && (
          <p style={{ color: 'var(--color-text-tertiary)', fontSize: '14px' }}>商品加载中...</p>
        )}

        {error && !loading && (
          <p style={{ color: 'var(--color-text-tertiary)', fontSize: '14px' }}>{error}</p>
        )}

        {!loading && !error && products.length === 0 && (
          <p style={{ color: 'var(--color-text-tertiary)', fontSize: '14px' }}>
            暂无上架商品，敬请期待
          </p>
        )}

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: 'var(--spacing-md)' }}>
          {products.map((p) => (
            <div
              key={p.id}
              onClick={() => handleDetail(p.id)}
              style={{
                cursor: 'pointer',
                background: 'var(--color-bg-primary)',
                borderRadius: 'var(--radius-md)',
                overflow: 'hidden',
                boxShadow: 'var(--shadow-sm)',
                transition: 'box-shadow var(--transition-fast), transform var(--transition-fast)',
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
              <div
                style={{
                  aspectRatio: '1/1',
                  background: 'var(--color-bg-secondary)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: 'var(--color-text-tertiary)',
                  fontSize: '14px',
                  overflow: 'hidden',
                }}
              >
                {p.thumbnail ? (
                  <img
                    src={p.thumbnail}
                    alt={p.name}
                    loading="lazy"
                    style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                  />
                ) : (
                  '暂无图片'
                )}
              </div>
              <div style={{ padding: 'var(--spacing-md)' }}>
                <h3
                  style={{
                    fontSize: '14px',
                    fontWeight: 500,
                    marginBottom: 'var(--spacing-xs)',
                    color: 'var(--color-text-primary)',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    whiteSpace: 'nowrap',
                  }}
                >
                  {p.name}
                </h3>
                <p style={{ fontSize: '16px', fontWeight: 600, color: 'var(--color-accent)' }}>
                  {p.price > 0 ? `¥${p.price.toFixed(2)}` : '价格待定'}
                </p>
              </div>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
};

export default HomePage;