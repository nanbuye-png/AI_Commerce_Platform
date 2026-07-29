import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { refundApi } from '../../api/refund';
import type { RefundVO } from '../../api/refund';

const statusColors: Record<string, string> = {
  REQUESTED: '#FF9F0A',
  APPROVED: '#0071E3',
  PROCESSING: '#34C759',
  COMPLETED: '#86868B',
  REJECTED: '#FF3B30',
  FAILED: '#A1A1A6',
};

const statusLabels: Record<string, string> = {
  REQUESTED: '待审核',
  APPROVED: '已批准',
  PROCESSING: '处理中',
  COMPLETED: '已完成',
  REJECTED: '已拒绝',
  FAILED: '失败',
};

const reasonLabels: Record<string, string> = {
  QUALITY_ISSUE: '质量问题',
  WRONG_PRODUCT: '商品错误',
  CUSTOMER_CHANGE_MIND: '用户改变主意',
  DAMAGED: '商品损坏',
  OTHER: '其他',
};

const RefundListPage: React.FC = () => {
  const navigate = useNavigate();
  const [refunds, setRefunds] = useState<RefundVO[]>([]);
  const [loading, setLoading] = useState(false);
  const [statusFilter, setStatusFilter] = useState('');

  const loadRefunds = async () => {
    setLoading(true);
    try {
      const params: { page?: number; pageSize?: number; status?: string } = { page: 0, pageSize: 50 };
      if (statusFilter) params.status = statusFilter;
      const res = await refundApi.list(params);
      const data = (res as any).data;
      setRefunds(data?.content || []);
    } catch (err) {
      console.error('加载退款列表失败:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRefunds();
  }, [statusFilter]);

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-lg)' }}>
        <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, margin: 0 }}>退款管理</h1>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          style={{ padding: '8px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', fontSize: '14px' }}
        >
          <option value="">全部状态</option>
          <option value="REQUESTED">待审核</option>
          <option value="APPROVED">已批准</option>
          <option value="PROCESSING">处理中</option>
          <option value="COMPLETED">已完成</option>
          <option value="REJECTED">已拒绝</option>
          <option value="FAILED">失败</option>
        </select>
      </div>

      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--color-border-light)', background: 'var(--color-bg-secondary)' }}>
              {['ID', '订单ID', '金额', '原因', '状态', '创建时间', '操作'].map((h) => (
                <th key={h} style={{ padding: '12px 16px', textAlign: 'left', fontSize: '13px', color: 'var(--color-text-secondary)', fontWeight: 500 }}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={7} style={{ padding: '24px', textAlign: 'center', color: 'var(--color-text-tertiary)' }}>加载中...</td>
              </tr>
            ) : refunds.length === 0 ? (
              <tr>
                <td colSpan={7} style={{ padding: '24px', textAlign: 'center', color: 'var(--color-text-tertiary)' }}>暂无退款记录</td>
              </tr>
            ) : (
              refunds.map((r) => (
                <tr key={r.id} style={{ borderBottom: '1px solid var(--color-border-light)' }}>
                  <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-primary)' }}>#{r.id}</td>
                  <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-primary)' }}>#{r.orderId}</td>
                  <td style={{ padding: '12px 16px', fontSize: '14px', fontWeight: 600, color: 'var(--color-accent)' }}>¥{r.amount.toFixed(2)}</td>
                  <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--color-text-secondary)' }}>{reasonLabels[r.reason] || r.reason}</td>
                  <td style={{ padding: '12px 16px' }}>
                    <span style={{ fontSize: '12px', padding: '2px 8px', borderRadius: '4px', background: `${statusColors[r.status] || '#86868B'}18`, color: statusColors[r.status] || '#86868B' }}>
                      {statusLabels[r.status] || r.status}
                    </span>
                  </td>
                  <td style={{ padding: '12px 16px', fontSize: '12px', color: 'var(--color-text-tertiary)' }}>{r.createdAt ? new Date(r.createdAt).toLocaleDateString('zh-CN') : '-'}</td>
                  <td style={{ padding: '12px 16px' }}>
                    <button
                      onClick={() => navigate(`/refunds/${r.id}`)}
                      style={{ padding: '4px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: 'var(--color-text-secondary)', fontSize: '12px', cursor: 'pointer' }}
                    >
                      详情
                    </button>
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

export default RefundListPage;