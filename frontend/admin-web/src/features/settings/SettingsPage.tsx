import React from 'react';

const SettingsPage: React.FC = () => {
  return (
    <div style={{ maxWidth: 1000, margin: '0 auto' }}>
      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-xl)' }}>系统设置</h1>
      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', padding: 'var(--spacing-xl)' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 600, marginBottom: 'var(--spacing-md)' }}>平台信息</h2>
        <p style={{ fontSize: '13px', color: 'var(--color-text-tertiary)' }}>系统设置功能即将上线，敬请期待。</p>
      </div>
    </div>
  );
};

export default SettingsPage;