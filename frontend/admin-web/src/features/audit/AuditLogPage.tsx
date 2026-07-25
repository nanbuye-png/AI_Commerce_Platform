import React from 'react';
const AuditLogPage: React.FC = () => (
  <div style={{ maxWidth: 1200, margin: '0 auto' }}>
    <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>审计日志</h1>
    <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', overflow: 'hidden' }}>
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead><tr style={{ borderBottom: '1px solid var(--color-border-light)', background: 'var(--color-bg-secondary)' }}>
          {['操作人', '操作类型', '目标', 'IP', '时间', '结果'].map(h => <th key={h} style={{ padding: '12px 16px', textAlign: 'left', fontSize: '13px', color: 'var(--color-text-secondary)', fontWeight: 500 }}>{h}</th>)}
        </tr></thead>
        <tbody>{[1,2,3,4,5].map(i => (
          <tr key={i} style={{ borderBottom: '1px solid var(--color-border-light)' }}>
            <td style={{ padding: '12px 16px', fontSize: '13px' }}>admin@{i}</td>
            <td style={{ padding: '12px 16px' }}><span style={{ fontSize: '12px', padding: '2px 8px', borderRadius: '4px', background: 'var(--color-accent-light)', color: 'var(--color-accent)' }}>{['登录','商品审核','商家审核','系统配置','用户管理'][i%5]}</span></td>
            <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-secondary)' }}>ID: {1000+i}</td>
            <td style={{ padding: '12px 16px', fontSize: '12px', color: 'var(--color-text-tertiary)' }}>192.168.1.{i}</td>
            <td style={{ padding: '12px 16px', fontSize: '12px', color: 'var(--color-text-tertiary)' }}>2026-07-{10+i} 10:3{i}:00</td>
            <td style={{ padding: '12px 16px' }}><span style={{ fontSize: '12px', padding: '2px 8px', borderRadius: '4px', background: 'var(--color-success)18', color: 'var(--color-success)' }}>成功</span></td>
          </tr>
        ))}</tbody>
      </table>
    </div>
  </div>
);
export default AuditLogPage;