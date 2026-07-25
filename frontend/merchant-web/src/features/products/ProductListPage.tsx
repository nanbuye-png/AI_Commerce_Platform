import React from 'react';
import { useNavigate } from 'react-router-dom';

const ProductListPage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-lg)' }}>
        <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600 }}>商品管理</h1>
        <button
          onClick={() => navigate('/products/create')}
          style={{ padding: '10px 20px', borderRadius: 'var(--radius-sm)', background: 'var(--color-accent)', color: '#fff', fontSize: '14px', fontWeight: 500, border: 'none', cursor: 'pointer' }}
        >
          + 添加商品
        </button>
      </div>

      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--color-border-light)', background: 'var(--color-bg-secondary)' }}>
              {['商品信息', '价格', '库存', '状态', '操作'].map((h) => (
                <th key={h} style={{ padding: '12px 16px', textAlign: 'left', fontSize: '13px', color: 'var(--color-text-secondary)', fontWeight: 500 }}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {[1, 2, 3, 4, 5].map((i) => (
              <tr key={i} style={{ borderBottom: '1px solid var(--color-border-light)' }}>
                <td style={{ padding: '12px 16px', display: 'flex', alignItems: 'center', gap: 12 }}>
                  <div style={{ width: 48, height: 48, borderRadius: 'var(--radius-sm)', background: 'var(--color-bg-secondary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '10px', color: 'var(--color-text-tertiary)' }}>图</div>
                  <div>
                    <p style={{ fontSize: '14px', fontWeight: 500, color: 'var(--color-text-primary)' }}>商品名称 {i}</p>
                    <p style={{ fontSize: '12px', color: 'var(--color-text-tertiary)' }}>SKU-{1000 + i}</p>
                  </div>
                </td>
                <td style={{ padding: '12px 16px', fontSize: '14px', fontWeight: 600, color: 'var(--color-accent)' }}>¥{Math.round(Math.random() * 1000)}</td>
                <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-primary)' }}>{Math.floor(Math.random() * 200)}</td>
                <td style={{ padding: '12px 16px' }}>
                  <span style={{ fontSize: '12px', padding: '2px 8px', borderRadius: '4px', background: 'var(--color-success)18', color: 'var(--color-success)' }}>上架</span>
                </td>
                <td style={{ padding: '12px 16px' }}>
                  <button onClick={() => navigate(`/products/${i}/edit`)} style={{ padding: '4px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: 'var(--color-text-secondary)', fontSize: '12px', cursor: 'pointer' }}>编辑</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default ProductListPage;