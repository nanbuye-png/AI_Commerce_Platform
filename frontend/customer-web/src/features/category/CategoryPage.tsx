import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { productService, type CategoryNode } from '../../services/product';

const CategoryPage: React.FC = () => {
  const navigate = useNavigate();
  const [categories, setCategories] = useState<CategoryNode[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    productService
      .getCategoryTree()
      .then((res) => {
        if (!cancelled) setCategories(res);
      })
      .catch((err: unknown) => {
        if (!cancelled) {
          console.error('加载分类失败:', err);
          setError('分类加载失败，请稍后重试');
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  const goCategory = (id: number) => {
    navigate(`/products?categoryId=${id}`);
  };

  return (
    <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 960, margin: '0 auto' }}>
      <h1
        style={{
          fontSize: 'var(--font-size-h1)',
          fontWeight: 600,
          color: 'var(--color-text-primary)',
          marginBottom: 'var(--spacing-xl)',
        }}
      >
        商品分类
      </h1>

      {loading && (
        <p style={{ color: 'var(--color-text-tertiary)', fontSize: '14px' }}>分类加载中...</p>
      )}

      {error && !loading && (
        <p style={{ color: 'var(--color-text-tertiary)', fontSize: '14px' }}>{error}</p>
      )}

      {!loading && !error && categories.length === 0 && (
        <p style={{ color: 'var(--color-text-tertiary)', fontSize: '14px' }}>暂无分类数据</p>
      )}

      {!loading && !error && categories.length > 0 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-lg)' }}>
          {categories.map((cat) => (
            <div
              key={cat.id}
              style={{
                background: 'var(--color-bg-primary)',
                borderRadius: 'var(--radius-md)',
                boxShadow: 'var(--shadow-sm)',
                padding: 'var(--spacing-lg)',
              }}
            >
              {/* 一级分类 */}
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  cursor: 'pointer',
                  marginBottom: cat.children?.length ? 'var(--spacing-md)' : 0,
                }}
                onClick={() => goCategory(cat.id)}
              >
                <span
                  style={{
                    fontWeight: 600,
                    fontSize: '16px',
                    color: 'var(--color-text-primary)',
                    flex: 1,
                  }}
                >
                  {cat.categoryName}
                </span>
                {cat.children && cat.children.length > 0 && (
                  <span
                    style={{
                      fontSize: '12px',
                      color: 'var(--color-text-tertiary)',
                    }}
                  >
                    查看全部 ›
                  </span>
                )}
              </div>

              {/* 二级分类 */}
              {cat.children && cat.children.length > 0 && (
                <div
                  style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fill, minmax(120px, 1fr))',
                    gap: 'var(--spacing-sm)',
                  }}
                >
                  {cat.children.map((child) => (
                    <button
                      key={child.id}
                      onClick={() => goCategory(child.id)}
                      style={{
                        padding: '10px 12px',
                        border: '1px solid var(--color-border-light)',
                        borderRadius: 'var(--radius-sm)',
                        background: 'var(--color-bg-secondary)',
                        color: 'var(--color-text-primary)',
                        fontSize: '13px',
                        cursor: 'pointer',
                        textAlign: 'center',
                        transition: 'all var(--transition-fast)',
                      }}
                      onMouseEnter={(e) => {
                        (e.currentTarget as HTMLElement).style.borderColor = 'var(--color-accent)';
                        (e.currentTarget as HTMLElement).style.color = 'var(--color-accent)';
                      }}
                      onMouseLeave={(e) => {
                        (e.currentTarget as HTMLElement).style.borderColor = 'var(--color-border-light)';
                        (e.currentTarget as HTMLElement).style.color = 'var(--color-text-primary)';
                      }}
                    >
                      {child.categoryName}
                    </button>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default CategoryPage;