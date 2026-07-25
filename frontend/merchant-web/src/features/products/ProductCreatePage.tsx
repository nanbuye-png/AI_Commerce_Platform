import React from 'react';
import { useNavigate } from 'react-router-dom';

const fields = [
  { label: '商品名称', type: 'text' },
  { label: '商品描述', type: 'textarea' },
  { label: '价格', type: 'number' },
  { label: '库存', type: 'number' },
  { label: '分类', type: 'select' },
];

const ProductCreatePage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>添加商品</h1>
      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', padding: 'var(--spacing-xl)', boxShadow: 'var(--shadow-sm)' }}>
        {fields.map((f) => (
          <div key={f.label} style={{ marginBottom: 'var(--spacing-md)' }}>
            <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, color: 'var(--color-text-primary)', marginBottom: 6 }}>{f.label}</label>
            {f.type === 'textarea' ? (
              <textarea rows={4} style={{ width: '100%', padding: '10px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', fontSize: '14px', outline: 'none', resize: 'vertical' }} />
            ) : f.type === 'select' ? (
              <select style={{ width: '100%', height: 40, padding: '0 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', fontSize: '14px', outline: 'none', background: 'var(--color-bg-primary)' }}>
                <option>选择分类</option>
              </select>
            ) : (
              <input type={f.type} style={{ width: '100%', height: 40, padding: '0 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', fontSize: '14px', outline: 'none' }} />
            )}
          </div>
        ))}
        <div style={{ display: 'flex', gap: 'var(--spacing-sm)', marginTop: 'var(--spacing-lg)' }}>
          <button style={{ padding: '10px 24px', borderRadius: 'var(--radius-sm)', background: 'var(--color-accent)', color: '#fff', fontSize: '14px', fontWeight: 500, border: 'none', cursor: 'pointer' }}>保存</button>
          <button onClick={() => navigate('/products')} style={{ padding: '10px 24px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: 'var(--color-text-secondary)', fontSize: '14px', cursor: 'pointer' }}>取消</button>
        </div>
      </div>
    </div>
  );
};

export default ProductCreatePage;