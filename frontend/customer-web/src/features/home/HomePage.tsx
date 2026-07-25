import React from 'react';
import { Card } from '../../components/common';

const HomePage: React.FC = () => {
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
          {['电子产品', '服装', '家居', '图书', '运动', '美妆'].map((cat) => (
            <div
              key={cat}
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
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: 'var(--spacing-md)' }}>
          {[1, 2, 3, 4].map((i) => (
            <Card key={i} padding="0" onClick={() => {}}>
              <div style={{ aspectRatio: '1/1', background: 'var(--color-bg-secondary)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--color-text-tertiary)', fontSize: '14px' }}>
                商品图片 {i}
              </div>
              <div style={{ padding: 'var(--spacing-md)' }}>
                <h3 style={{ fontSize: '14px', fontWeight: 500, marginBottom: 'var(--spacing-xs)', color: 'var(--color-text-primary)' }}>
                  推荐商品 {i}
                </h3>
                <p style={{ fontSize: '16px', fontWeight: 600, color: 'var(--color-accent)' }}>
                  ¥99.00
                </p>
              </div>
            </Card>
          ))}
        </div>
      </section>
    </div>
  );
};

export default HomePage;