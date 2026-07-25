import React from 'react';

const StatCard: React.FC<{ title: string; value: string; change: string; color: string }> = ({ title, value, change, color }) => (
  <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', padding: 'var(--spacing-lg)', boxShadow: 'var(--shadow-sm)' }}>
    <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)', marginBottom: 8 }}>{title}</p>
    <p style={{ fontSize: '28px', fontWeight: 700, color: 'var(--color-text-primary)', marginBottom: 4 }}>{value}</p>
    <p style={{ fontSize: '12px', color }}>{change}</p>
  </div>
);

const DashboardPage: React.FC = () => {
  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-xl)' }}>仪表盘</h1>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: 'var(--spacing-md)', marginBottom: 'var(--spacing-xl)' }}>
        <StatCard title="今日销售额" value="¥12,580" change="↑ 12.5% 较昨日" color="var(--color-success)" />
        <StatCard title="今日订单数" value="86" change="↑ 8.3% 较昨日" color="var(--color-success)" />
        <StatCard title="商品总数" value="1,234" change="新增 5 件" color="var(--color-accent)" />
        <StatCard title="客户总数" value="3,456" change="↑ 23 较昨日" color="var(--color-info)" />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 'var(--spacing-lg)' }}>
        <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', padding: 'var(--spacing-lg)', boxShadow: 'var(--shadow-sm)' }}>
          <h2 style={{ fontSize: '16px', fontWeight: 600, marginBottom: 'var(--spacing-md)' }}>销售趋势</h2>
          <div style={{ height: 200, background: 'var(--color-bg-secondary)', borderRadius: 'var(--radius-sm)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--color-text-tertiary)', fontSize: '14px' }}>
            图表区域 (未来接入)
          </div>
        </div>
        <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', padding: 'var(--spacing-lg)', boxShadow: 'var(--shadow-sm)' }}>
          <h2 style={{ fontSize: '16px', fontWeight: 600, marginBottom: 'var(--spacing-md)' }}>最近订单</h2>
          {[1, 2, 3].map((i) => (
            <div key={i} style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid var(--color-border-light)' }}>
              <span style={{ fontSize: '13px', color: 'var(--color-text-primary)' }}>订单 #{1000 + i}</span>
              <span style={{ fontSize: '13px', color: 'var(--color-accent)', fontWeight: 600 }}>¥{(i * 99).toFixed(2)}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;