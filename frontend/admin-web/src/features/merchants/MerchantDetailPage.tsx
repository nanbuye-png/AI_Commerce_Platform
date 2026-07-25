import React from 'react';
import { useNavigate } from 'react-router-dom';
const MerchantDetailPage: React.FC = () => {
  const navigate = useNavigate();
  return (
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <button onClick={() => navigate('/merchants')} style={{ marginBottom: 'var(--spacing-md)', padding: '6px 14px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', cursor: 'pointer', fontSize: '13px' }}>← 返回</button>
      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>商家详情</h1>
      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', padding: 'var(--spacing-xl)', boxShadow: 'var(--shadow-sm)' }}>
        <p style={{ fontSize: '14px', color: 'var(--color-text-secondary)' }}>商家详细信息（待接入 API）</p>
      </div>
    </div>
  );
};
export default MerchantDetailPage;