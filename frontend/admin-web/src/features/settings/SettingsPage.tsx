import React, { useCallback, useEffect, useState } from 'react';

interface HealthResponse {
  status?: string;
}

const SettingsPage: React.FC = () => {
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [lastChecked, setLastChecked] = useState<Date | null>(null);

  const checkHealth = useCallback(async () => {
    setLoading(true);
    try {
      const baseUrl = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
      const response = await fetch(`${baseUrl}/actuator/health`);
      if (!response.ok) throw new Error(`health check failed: ${response.status}`);
      setHealth(await response.json() as HealthResponse);
    } catch (error) {
      console.error('检查平台状态失败:', error);
      setHealth(null);
    } finally {
      setLastChecked(new Date());
      setLoading(false);
    }
  }, []);

  useEffect(() => { void checkHealth(); }, [checkHealth]);

  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
  const isUp = health?.status === 'UP';

  return (
    <div style={{ maxWidth: 1000, margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-xl)' }}>
        <div>
          <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, margin: 0 }}>系统状态</h1>
          <p style={{ margin: '6px 0 0', color: 'var(--color-text-tertiary)', fontSize: 13 }}>查看当前演示环境运行信息，不在浏览器中修改服务端密钥</p>
        </div>
        <button onClick={() => void checkHealth()} disabled={loading} style={buttonStyle}>{loading ? '检查中...' : '重新检查'}</button>
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 16 }}>
        <section style={cardStyle}><h2 style={headingStyle}>Commerce Core</h2><p style={{ fontSize: 24, fontWeight: 700, color: isUp ? 'var(--color-success)' : 'var(--color-error)' }}>{loading ? '检查中' : isUp ? '运行正常' : '无法连接'}</p><p style={mutedStyle}>{lastChecked ? `最近检查：${lastChecked.toLocaleTimeString('zh-CN')}` : '尚未检查'}</p></section>
        <section style={cardStyle}><h2 style={headingStyle}>API 地址</h2><p style={{ fontSize: 14, wordBreak: 'break-all' }}>{apiBaseUrl}</p><p style={mutedStyle}>由 VITE_API_BASE_URL 配置</p></section>
        <section style={cardStyle}><h2 style={headingStyle}>前端环境</h2><p style={{ fontSize: 18, fontWeight: 600 }}>{import.meta.env.MODE}</p><p style={mutedStyle}>{window.location.origin}</p></section>
      </div>
      {!loading && !isUp && <p style={{ marginTop: 16, color: 'var(--color-error)' }}>无法读取后端健康状态，请确认 Commerce Core 已启动并检查数据库连接。</p>}
    </div>
  );
};

const cardStyle: React.CSSProperties = { background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', padding: 'var(--spacing-xl)' };
const headingStyle: React.CSSProperties = { fontSize: 16, fontWeight: 600, margin: '0 0 14px' };
const mutedStyle: React.CSSProperties = { fontSize: 12, color: 'var(--color-text-tertiary)', marginTop: 8 };
const buttonStyle: React.CSSProperties = { padding: '8px 16px', border: '1px solid var(--color-border)', borderRadius: 'var(--radius-sm)', background: 'transparent', color: 'var(--color-text-secondary)', cursor: 'pointer' };

export default SettingsPage;
