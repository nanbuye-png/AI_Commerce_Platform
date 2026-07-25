import React from 'react';
import { useNavigate } from 'react-router-dom';
const MerchantListPage: React.FC = () => {
  const navigate = useNavigate();
  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>商家管理</h1>
      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead><tr style={{ borderBottom: '1px solid var(--color-border-light)', background: 'var(--color-bg-secondary)' }}>
            {['商家名称', '联系人', '电话', '商品数', '状态', '操作'].map(h => <th key={h} style={{ padding: '12px 16px', textAlign: 'left', fontSize: '13px', color: 'var(--color-text-secondary)', fontWeight: 500 }}>{h}</th>)}
          </tr></thead>
          <tbody>{[1,2,3,4,5].map(i => (
            <tr key={i} style={{ borderBottom: '1px solid var(--color-border-light)', cursor: 'pointer' }} onClick={() => navigate(`/merchants/${i}`)}>
              <td style={{ padding: '12px 16px', display: 'flex', alignItems: 'center', gap: 10 }}>
                <div style={{ width: 36, height: 36, borderRadius: 'var(--radius-sm)', background: 'var(--color-bg-secondary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '14px' }}>🏪</div>
                <span style={{ fontSize: '14px', fontWeight: 500 }}>商家 {i}</span>
              </td>
              <td style={{ padding: '12px 16px', fontSize: '13px' }}>联系人 {i}</td>
              <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-secondary)' }}>138****{1000+i}</td>
              <td style={{ padding: '12px 16px', fontSize: '13px' }}>{Math.floor(Math.random()*200)}</td>
              <td style={{ padding: '12px 16px' }}><span style={{ fontSize: '12px', padding: '2px 8px', borderRadius: '4px', background: 'var(--color-success)18', color: 'var(--color-success)' }}>ACTIVE</span></td>
              <td style={{ padding: '12px 16px' }}><button style={{ padding: '4px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', fontSize: '12px', cursor: 'pointer' }}>查看</button></td>
            </tr>
          ))}</tbody>
        </table>
      </div>
    </div>
  );
};
export default MerchantListPage;