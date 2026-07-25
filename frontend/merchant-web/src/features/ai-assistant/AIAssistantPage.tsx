import React from 'react';

const OptimizationCard: React.FC<{ title: string; items: string[] }> = ({ title, items }) => (
  <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', padding: 'var(--spacing-lg)', boxShadow: 'var(--shadow-sm)' }}>
    <h3 style={{ fontSize: '15px', fontWeight: 600, marginBottom: 'var(--spacing-md)', color: 'var(--color-text-primary)' }}>{title}</h3>
    {items.map((item, i) => (
      <div key={i} style={{ padding: '8px 0', borderBottom: i < items.length - 1 ? '1px solid var(--color-border-light)' : 'none', fontSize: '13px', color: 'var(--color-text-secondary)' }}>{item}</div>
    ))}
  </div>
);

const AIAssistantPage: React.FC = () => {
  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-lg)' }}>
        <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, display: 'flex', alignItems: 'center', gap: 8 }}>
          <span style={{ background: 'linear-gradient(135deg, #0071E3, #5AC8FA)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>AI 商品助手</span>
        </h1>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--spacing-lg)', marginBottom: 'var(--spacing-xl)' }}>
        <OptimizationCard title="商品标题优化建议" items={[
          '① 添加核心关键词: "2026新款 无线蓝牙耳机"',
          '② 突出卖点: "30小时超长续航"',
          '③ 添加场景词: "运动健身、通勤必备"',
        ]} />
        <OptimizationCard title="关键词推荐" items={[
          '📈 热门关键词: 蓝牙耳机, 无线耳机, 降噪耳机',
          '📈 长尾词: 百元内最好的蓝牙耳机',
          '📈 季节词: 夏季运动耳机, 暑假必备',
        ]} />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 'var(--spacing-lg)' }}>
        <OptimizationCard title="商品描述建议" items={['AI 正在分析您的商品描述...', '预计可提升 23% 的转化率', '优化后预计提升 15% 搜索曝光']} />
        <OptimizationCard title="图片优化建议" items={['主图建议: 白底突出产品', '添加卖点标签', '建议尺寸: 800x800px']} />
        <OptimizationCard title="定价策略建议" items={['竞品均价: ¥199', '建议售价: ¥179-229', '推荐搭配: 套装省¥50']} />
      </div>
    </div>
  );
};

export default AIAssistantPage;