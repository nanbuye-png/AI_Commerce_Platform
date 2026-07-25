import React from 'react';
const statusColors: Record<string, string> = { pending: '#FF9F0A', processing: '#0071E3', shipped: '#34C759', completed: '#86868B', refunding: '#FF453A' };
const statusText: Record<string, string> = { pending: '待处理', processing: '处理中', shipped: '已发货', completed: '已完成', refunding: '退款中' };
const OrderListPage: React.FC = () => (
  <div style={{ maxWidth: 1200, margin: '0 auto' }}>
    <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>订单监控</h1>
    <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', overflow: 'hidden' }}>
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead><tr style={{ borderBottom: '1px solid var(--color-border-light)', background: 'var(--color-bg-secondary)' }}>
          {['订单号', '商家', '金额', '状态', '时间'].map(h => <th key={h} style={{ padding: '12px 16px', textAlign: 'left', fontSize: '13px', color: 'var(--color-text-secondary)', fontWeight: 500 }}>{h}</th>)}
        </tr></thead>
        <tbody>{[1,2,3,4,5].map(i => {
          const s = ['pending','processing','shipped','completed','refunding'][i%5];
          return (
            <tr key={i} style={{ borderBottom: '1px solid var(--color-border-light)' }}>
              <td style={{ padding: '12px 16px', fontSize: '13px' }}>ORD-{2026000+i}</td>
              <td style={{ padding: '12px 16px', fontSize: '13px' }}>商家 {i}</td>
              <td style={{ padding: '12px 16px', fontSize: '14px', fontWeight: 600, color: 'var(--color-accent)' }}>¥{Math.round(Math.random()*1000)}</td>
              <td style={{ padding: '12px 16px' }}><span style={{ fontSize: '12px', padding: '2px 8px', borderRadius: '4px', background: `${statusColors[s]}18`, color: statusColors[s] }}>{statusText[s]}</span></td>
              <td style={{ padding: '12px 16px', fontSize: '12px', color: 'var(--color-text-tertiary)' }}>2026-07-{10+i}</td>
            </tr>
          );
        })}</tbody>
      </table>
    </div>
  </div>
);
export default OrderListPage;