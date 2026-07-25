import React from 'react';
const AICenterPage: React.FC = () => (
  <div style={{ maxWidth: 1200, margin: '0 auto' }}>
    <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>AI 中心</h1>
    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 'var(--spacing-lg)', marginBottom: 'var(--spacing-xl)' }}>
      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', padding: 'var(--spacing-lg)', boxShadow: 'var(--shadow-sm)' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 600, marginBottom: 'var(--spacing-md)' }}>AI 模型管理</h2>
        <div style={{ background: 'var(--color-bg-secondary)', borderRadius: 'var(--radius-sm)', padding: 'var(--spacing-md)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8, fontSize: '13px' }}><span>推荐模型 v2.1</span><span style={{ color: 'var(--color-success)' }}>在线</span></div>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8, fontSize: '13px' }}><span>搜索模型 v1.8</span><span style={{ color: 'var(--color-success)' }}>在线</span></div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px' }}><span>客服模型 v3.2</span><span style={{ color: 'var(--color-warning)' }}>维护中</span></div>
        </div>
      </div>
      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', padding: 'var(--spacing-lg)', boxShadow: 'var(--shadow-sm)' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 600, marginBottom: 'var(--spacing-md)' }}>Prompt 管理</h2>
        <div style={{ background: 'var(--color-bg-secondary)', borderRadius: 'var(--radius-sm)', padding: 'var(--spacing-md)' }}>
          {['商品推荐 Prompt', '搜索理解 Prompt', '客服对话 Prompt'].map(p => <div key={p} style={{ padding: '6px 0', fontSize: '13px', color: 'var(--color-text-primary)' }}>{p}</div>)}
        </div>
      </div>
      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', padding: 'var(--spacing-lg)', boxShadow: 'var(--shadow-sm)' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 600, marginBottom: 'var(--spacing-md)' }}>调用统计</h2>
        <div style={{ background: 'var(--color-bg-secondary)', borderRadius: 'var(--radius-sm)', padding: 'var(--spacing-md)' }}>
          <p style={{ fontSize: '24px', fontWeight: 700, color: 'var(--color-accent)', marginBottom: 4 }}>892,451</p>
          <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>本月总调用次数</p>
          <p style={{ fontSize: '12px', color: 'var(--color-success)', marginTop: 8 }}>↑ 23.5% 较上月</p>
        </div>
      </div>
    </div>
  </div>
);
export default AICenterPage;