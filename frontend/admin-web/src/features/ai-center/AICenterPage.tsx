import { useEffect, useState } from 'react';
import { aiStatsApi, type AiStatsVO } from '../../api/aiStats';

const formatNumber = (value: number): string => value.toLocaleString();
const formatMs = (value: number): string => `${value.toFixed(0)} ms`;

const panelStyle = {
  background: 'var(--color-bg-primary)',
  borderRadius: 'var(--radius-md)',
  padding: 'var(--spacing-lg)',
  boxShadow: 'var(--shadow-sm)',
};

export default function AICenterPage() {
  const [stats, setStats] = useState<AiStatsVO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);

  const loadStats = async () => {
    try {
      const result = await aiStatsApi.getStats();
      setStats(result.data);
      setError(null);
      setLastUpdated(new Date());
    } catch (loadError) {
      console.error('加载 AI 统计失败:', loadError);
      setError('AI 服务统计暂时不可用');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadStats();
    const timer = window.setInterval(() => void loadStats(), 5000);
    return () => window.clearInterval(timer);
  }, []);

  const tools = Object.entries(stats?.tool_stats ?? {});
  const calls = stats?.recent_calls_per_minute
    ? Object.entries(stats.recent_calls_per_minute).map(([timestamp, count]) => ({
        timestamp,
        label: new Date(Number(timestamp) * 1000).toLocaleTimeString('zh-CN', {
          hour: '2-digit',
          minute: '2-digit',
        }),
        count,
      }))
    : [];
  const maxCalls = Math.max(1, ...calls.map((item) => item.count));

  const cards = [
    ['总调用', formatNumber(stats?.total_calls ?? 0), `成功率 ${((stats?.success_rate ?? 0) * 100).toFixed(1)}%`],
    ['首 Token 延迟', formatMs(stats?.avg_ttft_ms ?? 0), `P95 ${formatMs(stats?.p95_ttft_ms ?? 0)}`],
    ['总响应耗时', formatMs(stats?.avg_latency_ms ?? 0), `P95 ${formatMs(stats?.p95_latency_ms ?? 0)}`],
    ['估算 Token', formatNumber(stats?.total_tokens ?? 0), `输入 ${formatNumber(stats?.estimated_input_tokens ?? 0)} / 输出 ${formatNumber(stats?.estimated_output_tokens ?? 0)}`],
  ];

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-lg)' }}>
        <div>
          <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, margin: 0 }}>AI 中心</h1>
          <p style={{ margin: '6px 0 0', color: 'var(--color-text-tertiary)', fontSize: 12 }}>
            {lastUpdated ? `更新于 ${lastUpdated.toLocaleTimeString('zh-CN')}，每 5 秒刷新` : '读取实时 AI Service 指标'}
          </p>
        </div>
        <button onClick={() => { setLoading(true); void loadStats(); }}>刷新</button>
      </div>

      {error && <div style={{ marginBottom: 16, color: '#FF3B30' }}>{error}</div>}

      <div style={{ ...panelStyle, marginBottom: 'var(--spacing-lg)' }}>
        <h2 style={{ fontSize: 16, marginTop: 0 }}>当前运行配置</h2>
        <div style={{ display: 'flex', gap: 32, flexWrap: 'wrap', fontSize: 14 }}>
          <span>Provider：<strong>{loading ? '...' : stats?.provider ?? 'unknown'}</strong></span>
          <span>模型：<strong>{loading ? '...' : stats?.model ?? 'unknown'}</strong></span>
          <span>Token 口径：<strong>{stats?.token_accounting === 'estimated' ? 'estimated（估算）' : 'unknown'}</strong></span>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 16, marginBottom: 20 }}>
        {cards.map(([label, value, detail]) => (
          <div key={label} style={panelStyle}>
            <p style={{ margin: 0, color: 'var(--color-text-secondary)', fontSize: 13 }}>{label}</p>
            <p style={{ margin: '10px 0 4px', fontSize: 26, fontWeight: 700 }}>{loading ? '...' : value}</p>
            <p style={{ margin: 0, color: 'var(--color-text-tertiary)', fontSize: 12 }}>{detail}</p>
          </div>
        ))}
      </div>

      <div style={{ ...panelStyle, marginBottom: 20 }}>
        <h2 style={{ fontSize: 16, marginTop: 0 }}>工具调用</h2>
        {tools.length === 0 ? (
          <p style={{ color: 'var(--color-text-tertiary)', fontSize: 13 }}>暂无工具调用数据</p>
        ) : tools.map(([name, tool]) => (
          <div key={name} style={{ display: 'grid', gridTemplateColumns: '1.5fr repeat(4, 1fr)', gap: 12, padding: '10px 0', borderTop: '1px solid var(--color-border)', fontSize: 13 }}>
            <strong>{name}</strong>
            <span>调用 {tool.calls}</span>
            <span>成功率 {(tool.success_rate * 100).toFixed(1)}%</span>
            <span>平均 {formatMs(tool.avg_latency_ms)}</span>
            <span>P95 {formatMs(tool.p95_latency_ms)}</span>
          </div>
        ))}
      </div>

      <div style={panelStyle}>
        <h2 style={{ fontSize: 16, marginTop: 0 }}>调用频率（近 60 分钟）</h2>
        {calls.length === 0 ? (
          <p style={{ color: 'var(--color-text-tertiary)', fontSize: 13 }}>暂无调用数据</p>
        ) : (
          <div style={{ display: 'flex', alignItems: 'flex-end', gap: 3, height: 130, overflowX: 'auto' }}>
            {calls.map((item) => (
              <div key={item.timestamp} title={`${item.label}: ${item.count} 次`} style={{ minWidth: 24, textAlign: 'center' }}>
                <div style={{ height: Math.max(4, (item.count / maxCalls) * 90), background: 'var(--color-accent)', borderRadius: '2px 2px 0 0' }} />
                <span style={{ fontSize: 9, color: 'var(--color-text-tertiary)' }}>{item.label}</span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
