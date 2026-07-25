import React from 'react';
import { useSearchParams } from 'react-router-dom';
import SearchBar from './components/SearchBar';
import ProductGrid from '../product/components/ProductGrid';

const SearchPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const keyword = searchParams.get('q') || '';

  return (
    <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 1200, margin: '0 auto' }}>
      {/* Search Header */}
      <div style={{ marginBottom: 'var(--spacing-xl)' }}>
        <SearchBar
          placeholder="搜索商品..."
          autoFocus={!keyword}
          size="lg"
        />
      </div>

      {/* Search Results */}
      {keyword ? (
        <>
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: 'var(--spacing-lg)',
            }}
          >
            <h1 style={{ fontSize: 'var(--font-size-h2)', fontWeight: 600, color: 'var(--color-text-primary)' }}>
              搜索：{keyword}
            </h1>
            <span style={{ fontSize: '14px', color: 'var(--color-text-secondary)' }}>
              找到 0 件商品
            </span>
          </div>

          {/* Sort Options */}
          <div
            style={{
              display: 'flex',
              gap: 'var(--spacing-sm)',
              marginBottom: 'var(--spacing-lg)',
              flexWrap: 'wrap',
            }}
          >
            {[
              { label: '综合', value: 'default' },
              { label: '销量', value: 'sales' },
              { label: '价格 ↑', value: 'price_asc' },
              { label: '价格 ↓', value: 'price_desc' },
              { label: '新品', value: 'newest' },
            ].map((opt) => (
              <button
                key={opt.value}
                style={{
                  padding: '6px 16px',
                  borderRadius: 'var(--radius-full)',
                  border: '1px solid var(--color-border)',
                  background: 'var(--color-bg-primary)',
                  color: 'var(--color-text-primary)',
                  fontSize: '13px',
                  cursor: 'pointer',
                  transition: 'all var(--transition-fast)',
                }}
              >
                {opt.label}
              </button>
            ))}
          </div>

          {/* Results */}
          <ProductGrid products={[]} loading={false} />
        </>
      ) : (
        /* Empty search state */
        <div
          style={{
            textAlign: 'center',
            padding: 'var(--spacing-3xl) var(--spacing-lg)',
          }}
        >
          <div style={{ fontSize: '48px', marginBottom: 'var(--spacing-md)' }}>🔍</div>
          <h2 style={{ fontSize: 'var(--font-size-h3)', fontWeight: 600, color: 'var(--color-text-primary)', marginBottom: 'var(--spacing-sm)' }}>
            搜索商品
          </h2>
          <p style={{ fontSize: '14px', color: 'var(--color-text-secondary)' }}>
            输入关键词，找到你想要的商品
          </p>
          {/* Trending Searches */}
          <div style={{ marginTop: 'var(--spacing-xl)' }}>
            <h3 style={{ fontSize: '14px', color: 'var(--color-text-secondary)', marginBottom: 'var(--spacing-sm)' }}>
              热门搜索
            </h3>
            <div style={{ display: 'flex', gap: 'var(--spacing-sm)', justifyContent: 'center', flexWrap: 'wrap' }}>
              {['手机', '电脑', '耳机', '手表', '书包', '运动鞋'].map((word) => (
                <span
                  key={word}
                  style={{
                    padding: '4px 12px',
                    borderRadius: 'var(--radius-full)',
                    background: 'var(--color-bg-secondary)',
                    color: 'var(--color-text-primary)',
                    fontSize: '13px',
                    cursor: 'pointer',
                  }}
                >
                  {word}
                </span>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default SearchPage;