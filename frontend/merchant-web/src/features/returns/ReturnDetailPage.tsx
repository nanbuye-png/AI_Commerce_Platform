import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { returnApi } from '../../api/return';
import type { ReturnRequestVO } from '../../api/return';

const statusColors: Record<string, string> = {
  REQUESTED: '#FF9F0A',
  APPROVED: '#0071E3',
  RETURNING: '#34C759',
  RECEIVED: '#5AC8FA',
  COMPLETED: '#86868B',
  REJECTED: '#FF3B30',
  FAILED: '#A1A1A6',
};

const statusLabels: Record<string, string> = {
  REQUESTED: '待审核',
  APPROVED: '已批准',
  RETURNING: '退回中',
  RECEIVED: '已收货',
  COMPLETED: '已完成',
  REJECTED: '已拒绝',
  FAILED: '失败',
};

const reasonLabels: Record<string, string> = {
  QUALITY_ISSUE: '质量问题',
  WRONG_PRODUCT: '商品错误',
  SIZE_ISSUE: '尺码问题',
  COLOR_ISSUE: '颜色问题',
  NOT_AS_DESCRIBED: '与描述不符',
  OTHER: '其他',
};

const ReturnDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [returnRequest, setReturnRequest] = useState<ReturnRequestVO | null>(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  const loadReturn = async () => {
    setLoading(true);
    try {
      const res = await returnApi.getDetail(Number(id));
      setReturnRequest((res as any).data);
    } catch (err) {
      console.error('加载退货详情失败:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadReturn();
  }, [id]);

  const handleApprove = async () => {
    if (!returnRequest) return;
    setActionLoading(true);
    try {
      await returnApi.approve(returnRequest.id);
      alert('退货已批准');
      loadReturn();
    } catch (err) {
      console.error('批准失败:', err);
      alert('操作失败');
    } finally {
      setActionLoading(false);
    }
  };

  const handleReject = async () => {
    if (!returnRequest) return;
    setActionLoading(true);
    try {
      await returnApi.reject(returnRequest.id);
      alert('退货已拒绝');
      loadReturn();
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

  if (!returnRequest) {
    return <div style={{ padding: '24px', textAlign: 'center', color: 'var(--color-text-tertiary)' }}>退货不存在</div>;
  }

  return (
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <button
        onClick={() => navigate('/returns')}
        style={{ marginBottom: 'var(--spacing-md)', padding: '6px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: 'var(--color-text-secondary)', fontSize: '13px', cursor: 'pointer' }}
      >
        ← 返回列表
      </button>

      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>退货详情 # {returnRequest.id}</h1>

      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', padding: 'var(--spacing-xl)' }}>
        <div style={{ display: 'grid', gridTemplateColumns: '120px 1fr', gap: '12px', fontSize: '14px' }}>
          <div style={{ color: 'var(--color-text-secondary)' }}>退货ID</div>
          <div style={{ color: 'var(--color-text-primary)' }}>#{returnRequest.id}</div>

          <div style={{ color: 'var(--color-text-secondary)' }}>订单ID</div>
          <div style={{ color: 'var(--color-text-primary)' }}>#{returnRequest.orderId}</div>

          <div style={{ color: 'var(--color-text-secondary)' }}>用户ID</div>
          <div style={{ color: 'var(--color-text-primary)' }}>#{returnRequest.userId}</div>

          {returnRequest.refundId && (
            <>
              <div style={{ color: 'var(--color-text-secondary)' }}>关联退款</div>
              <div style={{ color: 'var(--color-text-primary)' }}>#{returnRequest.refundId}</div>
            </>
          )}

          <div style={{ color: 'var(--color-text-secondary)' }}>原因</div>
          <div style={{ color: 'var(--color-text-primary)' }}>{reasonLabels[returnRequest.reason] || returnRequest.reason}</div>

          <div style={{ color: 'var(--color-text-secondary)' }}>状态</div>
          <div>
            <span style={{ fontSize: '12px', padding: '2px 8px', borderRadius: '4px', background: `${statusColors[returnRequest.status] || '#86868B'}18`, color: statusColors[returnRequest.status] || '#86868B' }}>
              {statusLabels[returnRequest.status] || returnRequest.status}
            </span>
          </div>

          <div style={{ color: 'var(--color-text-secondary)' }}>创建时间</div>
          <div style={{ color: 'var(--color-text-primary)' }}>{returnRequest.createdAt ? new Date(returnRequest.createdAt).toLocaleString('zh-CN') : '-'}</div>

          <div style={{ color: 'var(--color-text-secondary)' }}>批准时间</div>
          <div style={{ color: 'var(--color-text-primary)' }}>{returnRequest.approvedAt ? new Date(returnRequest.approvedAt).toLocaleString('zh-CN') : '-'}</div>

          <div style={{ color: 'var(--color-text-secondary)' }}>完成时间</div>
          <div style={{ color: 'var(--color-text-primary)' }}>{returnRequest.completedAt ? new Date(returnRequest.completedAt).toLocaleString('zh-CN') : '-'}</div>
        </div>

        {returnRequest.status === 'REQUESTED' && (
          <div style={{ marginTop: 'var(--spacing-xl)', display: 'flex', gap: '12px', paddingTop: 'var(--spacing-lg)', borderTop: '1px solid var(--color-border-light)' }}>
            <button
              onClick={handleApprove}
              disabled={actionLoading}
              style={{ padding: '8px 24px', borderRadius: 'var(--radius-sm)', border: 'none', background: '#34C759', color: '#fff', fontSize: '14px', cursor: 'pointer', fontWeight: 500 }}
            >
              批准退货
            </button>
            <button
              onClick={handleReject}
              disabled={actionLoading}
              style={{ padding: '8px 24px', borderRadius: 'var(--radius-sm)', border: 'none', background: '#FF3B30', color: '#fff', fontSize: '14px', cursor: 'pointer', fontWeight: 500 }}
            >
              拒绝退货
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default ReturnDetailPage;