import React, { useCallback, useEffect, useState } from 'react';
import { profileService, type Coupon } from '../../../services/profile';

const CouponPage: React.FC = () => {
  const [tab, setTab] = useState<'UNUSED' | 'USED' | 'EXPIRED' | 'ALL'>('UNUSED');
  const [coupons, setCoupons] = useState<Coupon[]>([]);
  const [loading, setLoading] = useState(true);

  const tabs = [
    { key: 'UNUSED', label: '未使用' },
    { key: 'USED', label: '已使用' },
    { key: 'EXPIRED', label: '已过期' },
    { key: 'ALL', label: '全部' },
  ] as const;

  const load = useCallback(() => {
    setLoading(true);
    profileService.listCoupons(tab)
      .then(setCoupons)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [tab]);

  useEffect(() => { load(); }, [load]);

  return (
    <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 700, margin: '0 auto' }}>
      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>优惠券</h1>

      <div style={{ display: 'flex', gap: 'var(--spacing-sm)', marginBottom: 'var(--spacing-lg)', borderBottom: '1px solid var(--color-border-light)' }}>
        {tabs.map((t) => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            style={{
              padding: '10px 18px',
              fontSize: '14px',
              fontWeight: tab === t.key ? 600 : 400,
              color: tab === t.key ? 'var(--color-accent)' : 'var(--color-text-secondary)',
              background: 'none',
              border: 'none',
              borderBottom: tab === t.key ? '2px solid var(--color-accent)' : '2px solid transparent',
              cursor: 'pointer',
            }}
          >
            {t.label}
          </button>
        ))}
      </div>

      {loading && <p style={{ color: 'var(--color-text-tertiary)' }}>加载中...</p>}

      {!loading && coupons.length === 0 && (
        <div style={{ textAlign: 'center', padding: 'var(--spacing-2xl)', color: 'var(--color-text-secondary)' }}>
          <p style={{ fontSize: '40px', marginBottom: 8 }}>🎫</p>
          <p>暂无优惠券</p>
        </div>
      )}

      <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-sm)' }}>
        {coupons.map((c) => (
          <div
            key={c.id}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 'var(--spacing-md)',
              padding: 'var(--spacing-md) var(--spacing-lg)',
              background: 'var(--color-bg-primary)',
              borderRadius: 'var(--radius-md)',
              boxShadow: 'var(--shadow-sm)',
              border: c.status === 'UNUSED' ? '1px solid var(--color-accent-light)' : '1px solid var(--color-border-light)',
              opacity: c.status === 'UNUSED' ? 1 : 0.6,
            }}
          >
            <div style={{
              width: 72, height: 72, borderRadius: 'var(--radius-sm)',
              background: c.status === 'UNUSED' ? 'var(--color-accent)' : 'var(--color-bg-secondary)',
              color: c.status === 'UNUSED' ? '#fff' : 'var(--color-text-tertiary)',
              display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
              flexShrink: 0,
            }}>
              <span style={{ fontSize: '18px', fontWeight: 700 }}>¥{Number(c.discountAmount).toFixed(0)}</span>
              <span style={{ fontSize: '10px' }}>优惠券</span>
            </div>
            <div style={{ flex: 1 }}>
              <p style={{ fontSize: '15px', fontWeight: 600, color: 'var(--color-text-primary)', marginBottom: 4 }}>{c.couponName}</p>
              <p style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>
                满 ¥{Number(c.minAmount).toFixed(2)} 可用
                {c.expireTime && <> · {new Date(c.expireTime).toLocaleDateString()} 前有效</>}
              </p>
            </div>
            <span style={{
              fontSize: '12px', padding: '3px 10px', borderRadius: 4,
              background: c.status === 'UNUSED' ? 'var(--color-accent-light)' : 'var(--color-bg-secondary)',
              color: c.status === 'UNUSED' ? 'var(--color-accent)' : 'var(--color-text-tertiary)',
            }}>
              {c.status === 'UNUSED' ? '可使用' : c.status === 'USED' ? '已使用' : '已过期'}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
};

export default CouponPage;