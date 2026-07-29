import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { merchantApi } from '../../api/merchants';

interface MerchantVO {
  id: number;
  username: string;
  email: string;
  nickname: string | null;
  status: string;
  createdTime: string;
}

const statusLabels: Record<string, string> = {
  ACTIVE: '正常',
  INACTIVE: '停用',
  LOCKED: '锁定',
};

const statusColors: Record<string, string> = {
  ACTIVE: '#34C759',
  INACTIVE: '#A1A1A6',
  LOCKED: '#FF3B30',
};

const MerchantListPage: React.FC = () => {
  const navigate = useNavigate();
  const [merchants, setMerchants] = useState<MerchantVO[]>([]);
  const [loading, setLoading] = useState(false);

  const loadMerchants = async () => {
    setLoading(true);
    try {
      const res = await merchantApi.list({ page: 0, pageSize: 20 });
      const data = res?.data || res;
      setMerchants(data?.content || []);
    } catch (err) {
      console.error('加载商家列表失败:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMerchants();
  }, []);

  const handleStatusChange = async (id: number, newStatus: string) => {
    try {
      await merchantApi.updateStatus(id, newStatus);
      loadMerchants();
    } catch (err) {
      console.error('更新状态失败:', err);
      alert('操作失败');
    }
  };

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>商家管理</h1>
      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--color-border-light)', background: 'var(--color-bg-secondary)' }}>
              {['ID', '用户名', '邮箱', '昵称', '状态', '注册时间', '操作'].map((h) => (
                <th key={h} style={{ padding: '12px 16px', textAlign: 'left', fontSize: '13px', color: 'var(--color-text-secondary)', fontWeight: 500 }}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={7} style={{ padding: '24px', textAlign: 'center', color: 'var(--color-text-tertiary)' }}>加载中...</td></tr>
            ) : merchants.length === 0 ? (
              <tr><td colSpan={7} style={{ padding: '24px', textAlign: 'center', color: 'var(--color-text-tertiary)' }}>暂无数据</td></tr>
            ) : (
              merchants.map((m) => (
                <tr key={m.id} style={{ borderBottom: '1px solid var(--color-border-light)', cursor: 'pointer' }}
                  onClick={() => navigate(`/merchants/${m.id}`)}>
                  <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-primary)' }}>#{m.id}</td>
                  <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-primary)' }}>{m.username}</td>
                  <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-secondary)' }}>{m.email}</td>
                  <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-secondary)' }}>{m.nickname || '-'}</td>
                  <td style={{ padding: '12px 16px' }}>
                    <span style={{ fontSize: '12px', padding: '2px 8px', borderRadius: '4px', background: `${statusColors[m.status] || '#86868B'}18`, color: statusColors[m.status] || '#86868B' }}>
                      {statusLabels[m.status] || m.status}
                    </span>
                  </td>
                  <td style={{ padding: '12px 16px', fontSize: '12px', color: 'var(--color-text-tertiary)' }}>{m.createdTime ? new Date(m.createdTime).toLocaleDateString('zh-CN') : '-'}</td>
                  <td style={{ padding: '12px 16px' }} onClick={(e) => e.stopPropagation()}>
                    <select
                      value={m.status}
                      onChange={(e) => handleStatusChange(m.id, e.target.value)}
                      style={{ padding: '4px 8px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', fontSize: '12px' }}
                    >
                      <option value="ACTIVE">正常</option>
                      <option value="INACTIVE">停用</option>
                      <option value="LOCKED">锁定</option>
                    </select>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default MerchantListPage;