import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { orderApi, type OrderVO } from '../../api/order';

const statusLabels: Record<string, string> = {
  PENDING_PAYMENT: '待付款', PAID: '已付款', PROCESSING: '处理中', SHIPPED: '已发货',
  COMPLETED: '已完成', CANCELLED: '已取消', REFUNDING: '退款中', REFUNDED: '已退款', CLOSED: '已关闭',
};

const OrderDetailPage: React.FC = () => {
  const { orderNo } = useParams<{ orderNo: string }>();
  const navigate = useNavigate();
  const [order, setOrder] = useState<OrderVO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!orderNo) return;
    let cancelled = false;
    orderApi.getDetail(orderNo).then((res) => {
      if (!cancelled) setOrder(res.data);
    }).catch((err) => {
      if (!cancelled) { console.error('加载订单详情失败:', err); setError('订单不存在或加载失败'); }
    }).finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [orderNo]);

  if (loading) return <p style={{ textAlign: 'center', padding: 'var(--spacing-3xl)', color: 'var(--color-text-tertiary)' }}>加载中...</p>;

  if (error || !order) {
    return (
      <div style={{ maxWidth: 800, margin: '0 auto', textAlign: 'center' }}>
        <p style={{ color: 'var(--color-text-secondary)', marginBottom: 'var(--spacing-md)' }}>{error}</p>
        <button onClick={() => navigate('/orders')} style={{ padding: '8px 20px', borderRadius: 'var(--radius-sm)', background: 'var(--color-accent)', color: '#fff', border: 'none', cursor: 'pointer' }}>返回订单列表</button>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>订单详情</h1>
      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', padding: 'var(--spacing-xl)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 'var(--spacing-md)' }}>
          <span style={{ fontSize: '14px', color: 'var(--color-text-secondary)' }}>订单号</span>
          <span style={{ fontSize: '14px', fontWeight: 500 }}>{order.orderNo}</span>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 'var(--spacing-md)' }}>
          <span style={{ fontSize: '14px', color: 'var(--color-text-secondary)' }}>状态</span>
          <span style={{ fontSize: '14px', fontWeight: 500 }}>{statusLabels[order.orderStatus] || order.orderStatus}</span>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 'var(--spacing-md)' }}>
          <span style={{ fontSize: '14px', color: 'var(--color-text-secondary)' }}>订单金额</span>
          <span style={{ fontSize: '18px', fontWeight: 700, color: 'var(--color-accent)' }}>¥{Number(order.payAmount || order.totalAmount || 0).toFixed(2)}</span>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 'var(--spacing-md)' }}>
          <span style={{ fontSize: '14px', color: 'var(--color-text-secondary)' }}>下单时间</span>
          <span style={{ fontSize: '14px' }}>{order.createdTime ? new Date(order.createdTime).toLocaleString('zh-CN') : '-'}</span>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 'var(--spacing-lg)' }}>
          <span style={{ fontSize: '14px', color: 'var(--color-text-secondary)' }}>买家备注</span>
          <span style={{ fontSize: '14px' }}>{order.buyerRemark || '-'}</span>
        </div>

        <h3 style={{ fontSize: '15px', fontWeight: 600, marginBottom: 'var(--spacing-sm)' }}>商品明细</h3>
        {(order.items ?? []).length === 0 ? (
          <p style={{ color: 'var(--color-text-tertiary)', fontSize: '13px' }}>暂无商品明细</p>
        ) : (
          (order.items ?? []).map((it, i) => (
            <div key={i} style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 0', borderTop: '1px solid var(--color-border-light)', fontSize: '14px' }}>
              <span>{it.productName} <span style={{ color: 'var(--color-text-tertiary)' }}>x{it.quantity}</span></span>
              <span style={{ fontWeight: 600 }}>¥{Number(it.price * it.quantity).toFixed(2)}</span>
            </div>
          ))
        )}

        <div style={{ marginTop: 'var(--spacing-lg)' }}>
          <button onClick={() => navigate('/orders')} style={{ padding: '8px 20px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: 'var(--color-text-secondary)', cursor: 'pointer' }}>返回订单列表</button>
        </div>
      </div>
    </div>
  );
};

export default OrderDetailPage;