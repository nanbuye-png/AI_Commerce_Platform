import React, { useEffect, useState } from 'react';
import { aiStatsApi, type AiStatsVO } from '../../api/aiStats';

interface ModelInfo {
  name: string;
  version: string;
  status: 'online' | 'maintenance';
}

const defaultModels: ModelInfo[] = [
  { name: '推荐模型', version: 'v2.1', status: 'online' },
  { name: '搜索模型', version: 'v1.8', status: 'online' },
  { name: '客服模型', version: 'v3.2', status: 'maintenance' },
];

const formatNumber = (n: number): string => n.toLocaleString();

const AICenterPage: React.FC = () => {
  const [stats, setStats] = useState<AiStatsVO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);

  useEffect(() => {
    let cancelled = false;
    let timer: ReturnType<typeof setInterval> | undefined;

    const loadStats = async () => {
      try {
        const res = await aiStatsApi.getStats();
        if (cancelled) return;
        setStats(res.data);
        setError(null);
        setLastUpdated(new Date());
      } catch (err) {
        if (!cancelled) {
          console.error('加载 AI 统计失败:', err);
          setError('AI 服务统计暂时不可用');
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    void loadStats();
    timer = setInterval(() => void loadStats(), 5000);

    return () => {
      cancelled = true;
      if (timer) clearInterval(timer);
    };
  }, []);

  const callMinutePairs = stats?.recent_calls_per_minute
    ? Object.entries(stats.recent_calls_per_minute).map(([ts, count]) => ({
        time: new Date(Number(ts) * 1000).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
        count,
      }))
    : [];
  const maxCalls = Math.max(1, ...callMinutePairs.map((p) => p.count));

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-lg)' }}>
        <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, margin: 0 }}>
          AI 中心
          {lastUpdated && (
            <span style={{ fontSize: '12px', color: 'var(--color-text-tertiary)', fontWeight: 400, marginLeft: 12 }}>
              更新于 {lastUpdated.toLocaleTimeString('zh-CN')}（每 5 秒自动刷新）
            </span>
          )}
        </h1>
        <button
          onClick={() => { setLoading(true); void (async () => {
            try {
              const res = await aiStatsApi.getStats();
              setStats(res.data);
              setError(null);
              setLastUpdated(new Date());
            } catch {
              setError('AI 服务统计暂时不可用');
            } finally {
              setLoading(false);
            }
          })(); }}
          style={{ padding: '8px 18px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: 'var(--color-text-secondary)', fontSize: '13px', cursor: 'pointer' }}
        >
          刷新
        </button>
      </div>

      {error && (
        <div style={{ marginBottom: 'var(--spacing-lg)', padding: '12px 16px', borderRadius: 'var(--radius-sm)', background: 'rgba(255,59,48,0.06)', color: '#FF3B30', fontSize: '13px' }}>{error}</div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: 'var(--spacing-md)', marginBottom: 'var(--spacing-xl)' }}>
        <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', padding: 'var(--spacing-lg)', boxShadow: 'var(--shadow-sm)' }}>
          <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)', marginBottom: 8 }}>总调用次数</p>
          <p style={{ fontSize: '28px', fontWeight: 700, color: 'var(--color-accent)', marginBottom: 4 }}>
            {loading ? '...' : formatNumber(stats?.total_calls ?? 0)}
          </p>
          <p style={{ fontSize: '12px', color: 'var(--color-success)' }}>实时统计</p>
        </div>
        <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', padding: 'var(--spacing-lg)', boxShadow: 'var(--shadow-sm)' }}>
          <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)', marginBottom: 8 }}>成功/失败</p>
          <p style={{ fontSize: '28px', fontWeight: 700, color: 'var(--color-text-primary)', marginBottom: 4 }}>
            {loading ? '...' : `${formatNumber(stats?.succeeded ?? 0)} / ${formatNumber(stats?.failed ?? 0)}`}
          </p>
          <p style={{ fontSize: '12px', color: stats && stats.success_rate > 0.9 ? 'var(--color-success)' : 'var(--color-warning)' }}>
            成功率 {stats ? `${(stats.success_rate * 100).toFixed(1)}%` : '—'}
          </p>
        </div>
        <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', padding: 'var(--spacing-lg)', boxShadow: 'var(--shadow-sm)' }}>
          <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)', marginBottom: 8 }}>Token 用量</p>
          <p style={{ fontSize: '28px', fontWeight: 700, color: 'var(--color-info)', marginBottom: 4 }}>
            {loading ? '...' : formatNumber(stats?.total_tokens ?? 0)}
          </p>
          <p style={{ fontSize: '12px', color: 'var(--color-text-tertiary)' }}>累计消耗 Token</p>
        </div>
        <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', padding: 'var(--spacing-lg)', boxShadow: 'var(--shadow-sm)' }}>
          <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)', marginBottom: 8 }}>服务运行时长</p>
          <p style={{ fontSize: '28px', fontWeight: 700, color: 'var(--color-warning)', marginBottom: 4 }}>
            {loading || !stats ? '...' : `${Math.floor(stats.uptime_seconds / 3600)}h ${Math.floor((stats.uptime_seconds % 3600) / 60)}m`}
          </p>
          <p style={{ fontSize: '12px', color: 'var(--color-text-tertiary)' }}>AI 服务运行时间</p>
        </div>
      </div>

      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', padding: 'var(--spacing-lg)', boxShadow: 'var(--shadow-sm)', marginBottom: 'var(--spacing-lg)' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 600, marginBottom: 'var(--spacing-md)' }}>AI 模型管理</h2>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          {defaultModels.map((m) => (
            <div key={m.name} style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 14px', borderRadius: 'var(--radius-sm)', background: 'var(--color-bg-secondary)', fontSize: '13px' }}>
              <span>{m.name} {m.version}</span>
              <span style={{ color: m.status === 'online' ? 'var(--color-success)' : 'var(--color-warning)' }}>
                {m.status === 'online' ? '在线' : '维护中'}
              </span>
            </div>
          ))}
        </div>
      </div>

      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', padding: 'var(--spacing-lg)', boxShadow: 'var(--shadow-sm)' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 600, marginBottom: 'var(--spacing-md)' }}>调用频率（近 60 分钟）</h2>
        {callMinutePairs.length === 0 ? (
          <p style={{ textAlign: 'center', color: 'var(--color-text-tertiary)', fontSize: '13px', padding: 'var(--spacing-xl)' }}>
            {loading ? '加载中...' : '暂无调用数据'}
          </p>
        ) : (
          <div style={{ display: 'flex', alignItems: 'flex-end', gap: 2, height: 120, overflowX: 'auto' }}>
            {callMinutePairs.map((p) => (
              <div key={p.time} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', minWidth: 28 }}>
                <span style={{ fontSize: '10px', color: 'var(--color-text-tertiary)', marginBottom: 4 }}>{p.count}</span>
                <div
                  title={`${p.time}: ${p.count} 次`}
                  style={{
                    width: 18,
                    height: `${Math.max(4, (p.count / maxCalls) * 80)}px`,
                    background: 'var(--color-accent)',
                    borderRadius: '2px 2px 0 0',
                  }}
                />
                <span style={{ fontSize: '9px', color: 'var(--color-text-tertiary)', marginTop: 4 }}>{p.time}</span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default AICenterPage;