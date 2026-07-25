import React from 'react';
const UserListPage: React.FC = () => (
  <div style={{ maxWidth: 1200, margin: '0 auto' }}>
    <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>用户管理</h1>
    <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', overflow: 'hidden' }}>
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead><tr style={{ borderBottom: '1px solid var(--color-border-light)', background: 'var(--color-bg-secondary)' }}>
          {['用户名', '邮箱', '角色', '状态', '注册时间'].map(h => <th key={h} style={{ padding: '12px 16px', textAlign: 'left', fontSize: '13px', color: 'var(--color-text-secondary)', fontWeight: 500 }}>{h}</th>)}
        </tr></thead>
        <tbody>{[1,2,3,4,5].map(i => (
          <tr key={i} style={{ borderBottom: '1px solid var(--color-border-light)' }}>
            <td style={{ padding: '12px 16px', display: 'flex', alignItems: 'center', gap: 10 }}>
              <div style={{ width: 32, height: 32, borderRadius: '50%', background: 'var(--color-accent-light)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '12px', color: 'var(--color-accent)' }}>U</div>
              <span style={{ fontSize: '14px' }}>user_{i}</span>
            </td>
            <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-secondary)' }}>user{i}@example.com</td>
            <td style={{ padding: '12px 16px' }}><span style={{ fontSize: '12px', padding: '2px 8px', borderRadius: '4px', background: 'var(--color-accent-light)', color: 'var(--color-accent)' }}>CUSTOMER</span></td>
            <td style={{ padding: '12px 16px' }}><span style={{ fontSize: '12px', padding: '2px 8px', borderRadius: '4px', background: 'var(--color-success)18', color: 'var(--color-success)' }}>ACTIVE</span></td>
            <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-tertiary)' }}>2026-07-{10+i}</td>
          </tr>
        ))}</tbody>
      </table>
    </div>
  </div>
);
export default UserListPage;