import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { merchantApi } from '../../api/merchants';
import type { MerchantVO } from '../../api/merchants';

const statusLabels: Record<string, string> = { ACTIVE: '正常', INACTIVE: '停用', LOCKED: '锁定' };
const statusColors: Record<string, string> = { ACTIVE: '#34C759', INACTIVE: '#A1A1A6', LOCKED: '#FF3B30' };

const MerchantDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [merchant, setMerchant] = useState<MerchantVO | null>(null);
  const [loading, setLoading] = useState(true);

  const loadMerchant = async () => {
    setLoading(true);
    try {
      const res = await merchantApi.getDetail(Number(id));
      setMerchant(res.data);
    } catch (err) {
      console.error('加载商家详情失败:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    let cancelled = false;
    void merchantApi.getDetail(Number(id))
      .then((res) => {
        if (!cancelled) setMerchant(res.data);
      })
      .catch((err: unknown) => {
        if (!cancelled) console.error('加载商家详情失败:', err);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [id]);

  const handleStatusChange = async (newStatus: string) => {
    try {
      await merchantApi.updateStatus(Number(id), newStatus);
      await loadMerchant();
    } catch (err) {
      console.error('更新状态失败:', err);
      alert('操作失败');
    }
  };

  if (loading) return <div style={{ padding: '24px', textAlign: 'center', color: 'var(--color-text-tertiary)' }}>加载中...</div>;
  if (!merchant) return <div style={{ padding: '24px', textAlign: 'center', color: 'var(--color-text-tertiary)' }}>商家不存在</div>;

  return (
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <button onClick={() => navigate('/merchants')} style={{ marginBottom: 'var(--spacing-md)', padding: '6px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: 'var(--color-text-secondary)', fontSize: '13px', cursor: 'pointer' }}>← 返回列表</button>
      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>商家详情 # {merchant.id}</h1>
      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', padding: 'var(--spacing-xl)' }}>
        <div style={{ display: 'grid', gridTemplateColumns: '120px 1fr', gap: '12px', fontSize: '14px' }}>
          <div style={{ color: 'var(--color-text-secondary)' }}>用户名</div><div style={{ color: 'var(--color-text-primary)' }}>{merchant.username}</div>
          <div style={{ color: 'var(--color-text-secondary)' }}>邮箱</div><div style={{ color: 'var(--color-text-primary)' }}>{merchant.email}</div>
          <div style={{ color: 'var(--color-text-secondary)' }}>昵称</div><div style={{ color: 'var(--color-text-primary)' }}>{merchant.nickname || '-'}</div>
          <div style={{ color: 'var(--color-text-secondary)' }}>手机</div><div style={{ color: 'var(--color-text-primary)' }}>{merchant.phone || '-'}</div>
          <div style={{ color: 'var(--color-text-secondary)' }}>状态</div>
          <div><span style={{ fontSize: '12px', padding: '2px 8px', borderRadius: '4px', background: `${statusColors[merchant.status] || '#86868B'}18`, color: statusColors[merchant.status] || '#86868B' }}>{statusLabels[merchant.status] || merchant.status}</span></div>
          <div style={{ color: 'var(--color-text-secondary)' }}>注册时间</div><div style={{ color: 'var(--color-text-primary)' }}>{merchant.createdTime ? new Date(merchant.createdTime).toLocaleString('zh-CN') : '-'}</div>
        </div>
        <div style={{ marginTop: 'var(--spacing-xl)', paddingTop: 'var(--spacing-lg)', borderTop: '1px solid var(--color-border-light)', display: 'flex', gap: '12px', alignItems: 'center' }}>
          <span style={{ fontSize: '14px', color: 'var(--color-text-secondary)' }}>状态管理:</span>
          <select value={merchant.status} onChange={(e) => handleStatusChange(e.target.value)} style={{ padding: '6px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', fontSize: '14px' }}>
            <option value="ACTIVE">正常</option>
            <option value="INACTIVE">停用</option>
            <option value="LOCKED">锁定</option>
          </select>
        </div>
      </div>
    </div>
  );
};

export default MerchantDetailPage;