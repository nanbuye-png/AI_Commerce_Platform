import React from 'react';
const ProductReviewPage: React.FC = () => {
  const statusColor = (s: string) => s === 'pending' ? '#FF9F0A' : s === 'approved' ? '#34C759' : '#FF453A';
  const statusText = (s: string) => s === 'pending' ? '待审核' : s === 'approved' ? '通过' : '拒绝';
  const statuses = ['pending', 'approved', 'rejected', 'pending', 'approved'];
  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>商品审核</h1>
      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead><tr style={{ borderBottom: '1px solid var(--color-border-light)', background: 'var(--color-bg-secondary)' }}>
            {['商品信息', '商家', '价格', '提交时间', '状态', '操作'].map(h => <th key={h} style={{ padding: '12px 16px', textAlign: 'left', fontSize: '13px', fontWeight: 500, color: 'var(--color-text-secondary)' }}>{h}</th>)}
          </tr></thead>
          <tbody>{[1,2,3,4,5].map(i => {
            const s = statuses[i % statuses.length];
            return (
              <tr key={i} style={{ borderBottom: '1px solid var(--color-border-light)' }}>
                <td style={{ padding: '12px 16px', display: 'flex', alignItems: 'center', gap: 10 }}>
                  <div style={{ width: 48, height: 48, borderRadius: 'var(--radius-sm)', background: 'var(--color-bg-secondary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '10px', color: 'var(--color-text-tertiary)' }}>图</div>
                  <span style={{ fontSize: '14px', fontWeight: 500 }}>待审核商品 {i}</span>
                </td>
                <td style={{ padding: '12px 16px', fontSize: '13px' }}>商家 {i}</td>
                <td style={{ padding: '12px 16px', fontSize: '14px', fontWeight: 600, color: 'var(--color-accent)' }}>¥{Math.round(Math.random()*1000)}</td>
                <td style={{ padding: '12px 16px', fontSize: '12px', color: 'var(--color-text-tertiary)' }}>2026-07-{10+i}</td>
                <td style={{ padding: '12px 16px' }}><span style={{ fontSize: '12px', padding: '2px 8px', borderRadius: '4px', background: `${statusColor(s)}18`, color: statusColor(s) }}>{statusText(s)}</span></td>
                <td style={{ padding: '12px 16px' }}>
                  {s === 'pending' && <div style={{ display: 'flex', gap: 4 }}><button style={{ padding: '4px 10px', borderRadius: 'var(--radius-sm)', border: 'none', background: 'var(--color-success)', color: '#fff', fontSize: '12px', cursor: 'pointer' }}>通过</button><button style={{ padding: '4px 10px', borderRadius: 'var(--radius-sm)', border: 'none', background: 'var(--color-error)', color: '#fff', fontSize: '12px', cursor: 'pointer' }}>拒绝</button></div>}
                </td>
              </tr>
            );
          })}</tbody>
        </table>
      </div>
    </div>
  );
};
export default ProductReviewPage;