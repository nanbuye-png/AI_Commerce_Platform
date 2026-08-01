import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
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

const RefundDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [refund, setRefund] = useState<RefundVO | null>(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    let cancelled = false;

    void refundApi.getDetail(Number(id))
      .then((res) => {
        if (!cancelled) {
          setRefund(res.data);
          setLoading(false);
        }
      })
      .catch((err: unknown) => {
        if (!cancelled) {
          console.error('加载退款详情失败:', err);
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [id]);

  const loadRefund = async () => {
    setLoading(true);
    try {
      const res = await refundApi.getDetail(Number(id));
      setRefund(res.data);
    } catch (err: unknown) {
      console.error('加载退款详情失败:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async () => {
    if (!refund) return;
    setActionLoading(true);
    try {
      await refundApi.approve(refund.id);
      alert('退款已批准');
      await loadRefund();
    } catch (err) {
      console.error('批准失败:', err);
      alert('操作失败');
    } finally {
      setActionLoading(false);
    }
  };

  const handleReject = async () => {
    if (!refund) return;
    setActionLoading(true);
    try {
      await refundApi.reject(refund.id);
      alert('退款已拒绝');
      await loadRefund();
    } catch (err) {
      console.error('拒绝失败:', err);
      alert('操作失败');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return <div style={{ padding: '24px', textAlign: 'center', color: 'var(--color-text-tertiary)' }}>加载中...</div>;
  }

  if (!refund) {
    return <div style={{ padding: '24px', textAlign: 'center', color: 'var(--color-text-tertiary)' }}>退款不存在</div>;
  }

  return (
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <button
        onClick={() => navigate('/refunds')}
        style={{ marginBottom: 'var(--spacing-md)', padding: '6px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: 'var(--color-text-secondary)', fontSize: '13px', cursor: 'pointer' }}
      >
        ← 返回列表
      </button>

      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>退款详情 # {refund.id}</h1>

      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', padding: 'var(--spacing-xl)' }}>
        <div style={{ display: 'grid', gridTemplateColumns: '120px 1fr', gap: '12px', fontSize: '14px' }}>
          <div style={{ color: 'var(--color-text-secondary)' }}>退款ID</div>
          <div style={{ color: 'var(--color-text-primary)' }}>#{refund.id}</div>

          <div style={{ color: 'var(--color-text-secondary)' }}>订单ID</div>
          <div style={{ color: 'var(--color-text-primary)' }}>#{refund.orderId}</div>

          <div style={{ color: 'var(--color-text-secondary)' }}>用户ID</div>
          <div style={{ color: 'var(--color-text-primary)' }}>#{refund.userId}</div>

          <div style={{ color: 'var(--color-text-secondary)' }}>退款金额</div>
          <div style={{ fontWeight: 600, color: 'var(--color-accent)' }}>¥{refund.amount.toFixed(2)}</div>

          <div style={{ color: 'var(--color-text-secondary)' }}>原因</div>
          <div style={{ color: 'var(--color-text-primary)' }}>{reasonLabels[refund.reason] || refund.reason}</div>

          <div style={{ color: 'var(--color-text-secondary)' }}>状态</div>
          <div>
            <span style={{ fontSize: '12px', padding: '2px 8px', borderRadius: '4px', background: `${statusColors[refund.status] || '#86868B'}18`, color: statusColors[refund.status] || '#86868B' }}>
              {statusLabels[refund.status] || refund.status}
            </span>
          </div>

          <div style={{ color: 'var(--color-text-secondary)' }}>创建时间</div>
          <div style={{ color: 'var(--color-text-primary)' }}>{refund.createdAt ? new Date(refund.createdAt).toLocaleString('zh-CN') : '-'}</div>

          <div style={{ color: 'var(--color-text-secondary)' }}>完成时间</div>
          <div style={{ color: 'var(--color-text-primary)' }}>{refund.completedAt ? new Date(refund.completedAt).toLocaleString('zh-CN') : '-'}</div>
        </div>

        {refund.status === 'REQUESTED' && (
          <div style={{ marginTop: 'var(--spacing-xl)', display: 'flex', gap: '12px', paddingTop: 'var(--spacing-lg)', borderTop: '1px solid var(--color-border-light)' }}>
            <button
              onClick={handleApprove}
              disabled={actionLoading}
              style={{ padding: '8px 24px', borderRadius: 'var(--radius-sm)', border: 'none', background: '#34C759', color: '#fff', fontSize: '14px', cursor: 'pointer', fontWeight: 500 }}
            >
              批准退款
            </button>
            <button
              onClick={handleReject}
              disabled={actionLoading}
              style={{ padding: '8px 24px', borderRadius: 'var(--radius-sm)', border: 'none', background: '#FF3B30', color: '#fff', fontSize: '14px', cursor: 'pointer', fontWeight: 500 }}
            >
              拒绝退款
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default RefundDetailPage;