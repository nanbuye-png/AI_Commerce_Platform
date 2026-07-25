import React from 'react';
const StatCard: React.FC<{ title: string; value: string; icon: string; color: string }> = ({ title, value, icon, color }) => (
  <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', padding: 'var(--spacing-lg)', boxShadow: 'var(--shadow-sm)' }}>
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
      <span style={{ fontSize: '24px' }}>{icon}</span>
      <span style={{ fontSize: '12px', color: color, fontWeight: 600 }}>↑ 12.5%</span>
    </div>
    <p style={{ fontSize: '28px', fontWeight: 700, color: 'var(--color-text-primary)', marginBottom: 4 }}>{value}</p>
    <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>{title}</p>
  </div>
);
const DashboardPage: React.FC = () => (
  <div style={{ maxWidth: 1200, margin: '0 auto' }}>
    <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-xl)' }}>平台仪表盘</h1>
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 'var(--spacing-md)', marginBottom: 'var(--spacing-xl)' }}>
      <StatCard title="注册用户" value="12,458" icon="👤" color="var(--color-accent)" />
      <StatCard title="入驻商家" value="856" icon="🏪" color="var(--color-success)" />
      <StatCard title="商品总数" value="45,239" icon="📦" color="var(--color-info)" />
      <StatCard title="订单总量" value="128,936" icon="📋" color="var(--color-warning)" />
      <StatCard title="AI 调用" value="892,451" icon="🤖" color="var(--color-error)" />
    </div>
    <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 'var(--spacing-lg)' }}>
      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', padding: 'var(--spacing-lg)', boxShadow: 'var(--shadow-sm)' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 600, marginBottom: 'var(--spacing-md)' }}>平台指标趋势</h2>
        <div style={{ height: 200, background: 'var(--color-bg-secondary)', borderRadius: 'var(--radius-sm)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--color-text-tertiary)' }}>图表区域</div>
      </div>
      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', padding: 'var(--spacing-lg)', boxShadow: 'var(--shadow-sm)' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 600, marginBottom: 'var(--spacing-md)' }}>最近活动</h2>
        {['用户 #1234 注册', '商家 #99 入驻', '商品 #5678 审核通过', 'AI 调用量突破 80万'].map((a, i) => (
          <div key={i} style={{ padding: '8px 0', borderBottom: i < 3 ? '1px solid var(--color-border-light)' : 'none', fontSize: '13px', color: 'var(--color-text-secondary)' }}>{a}</div>
        ))}
      </div>
    </div>
  </div>
);
export default DashboardPage;