import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { profileService, type BrowseHistoryItem } from '../../../services/profile';

const HistoryPage: React.FC = () => {
  const navigate = useNavigate();
  const [history, setHistory] = useState<BrowseHistoryItem[]>([]);
  const [loading, setLoading] = useState(true);

  const load = () => {
    profileService.listBrowseHistory(50)
      .then(setHistory)
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const clearAll = async () => {
    if (!window.confirm('确定清空全部浏览历史吗？')) return;
    try {
      await profileService.clearBrowseHistory();
      setHistory([]);
    } catch { alert('清空失败'); }
  };

  return (
    <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 700, margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-lg)' }}>
        <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600 }}>浏览历史</h1>
        {history.length > 0 && (
          <button onClick={clearAll} style={{
            padding: '6px 14px', fontSize: '13px', color: 'var(--color-text-tertiary)',
            background: 'none', border: '1px solid var(--color-border)', borderRadius: 'var(--radius-sm)', cursor: 'pointer',
          }}>清空历史</button>
        )}
      </div>

      {loading && <p style={{ color: 'var(--color-text-tertiary)' }}>加载中...</p>}

      {!loading && history.length === 0 && (
        <div style={{ textAlign: 'center', padding: 'var(--spacing-2xl)', color: 'var(--color-text-secondary)' }}>
          <p style={{ fontSize: '40px', marginBottom: 8 }}>🕐</p>
          <p>暂无浏览记录</p>
        </div>
      )}

      <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-sm)' }}>
        {history.map((h) => (
          <div
            key={h.id}
            style={{
              display: 'flex', alignItems: 'center', gap: 'var(--spacing-md)',
              padding: 'var(--spacing-sm)', background: 'var(--color-bg-primary)',
              borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', cursor: 'pointer',
            }}
            onClick={() => navigate(`/products/${h.productId}`)}
          >
            <div style={{
              width: 80, height: 80, borderRadius: 'var(--radius-sm)', flexShrink: 0,
              background: 'var(--color-bg-secondary)', display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontSize: '11px', color: 'var(--color-text-tertiary)', overflow: 'hidden',
            }}>
              {h.productImage ? <img src={h.productImage} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} /> : '图'}
            </div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <p style={{ fontSize: '14px', fontWeight: 500, color: 'var(--color-text-primary)', marginBottom: 4, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                {h.productName}
              </p>
              <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>
                {h.price != null ? `¥${Number(h.price).toFixed(2)}` : ''}
                {h.viewedTime && <span style={{ marginLeft: 8 }}>{new Date(h.viewedTime).toLocaleString()}</span>}
              </p>
            </div>
            <span style={{ fontSize: '13px', color: 'var(--color-accent)' }}>查看 →</span>
          </div>
        ))}
      </div>
    </div>
  );
};

export default HistoryPage;