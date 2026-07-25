import React from 'react';
import { useNavigate } from 'react-router-dom';

const ProductEditPage: React.FC = () => {
  const navigate = useNavigate();
  return (
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>编辑商品</h1>
      <div style={{ padding: 'var(--spacing-xl)', background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)' }}>
        <p style={{ fontSize: '14px', color: 'var(--color-text-secondary)' }}>商品编辑表单（待接入 API）</p>
        <div style={{ display: 'flex', gap: 'var(--spacing-sm)', marginTop: 'var(--spacing-lg)' }}>
          <button style={{ padding: '10px 24px', borderRadius: 'var(--radius-sm)', background: 'var(--color-accent)', color: '#fff', border: 'none', cursor: 'pointer' }}>保存修改</button>
          <button onClick={() => navigate('/products')} style={{ padding: '10px 24px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', cursor: 'pointer' }}>返回</button>
        </div>
      </div>
    </div>
  );
};

export default ProductEditPage;