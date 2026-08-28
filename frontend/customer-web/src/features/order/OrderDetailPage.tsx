import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { orderService, type OrderVO } from '../../services/order';

const statusLabels: Record<string, string> = {
  PENDING_PAYMENT: '待付款', PAID: '已付款', PROCESSING: '处理中', SHIPPED: '已发货',
  COMPLETED: '已完成', CANCELLED: '已取消', REFUNDING: '退款中', REFUNDED: '已退款', CLOSED: '已关闭',
};

const OrderDetailPage: React.FC = () => {
  const { orderNo = '' } = useParams<{ orderNo: string }>();
  const navigate = useNavigate();
  const [order, setOrder] = useState<OrderVO | null>(null);
  const [loading, setLoading] = useState(true);

  const loadOrder = useCallback(async () => {
    setLoading(true);
    try {
      setOrder(await orderService.orderDetail(orderNo));
    } catch (error) {
      console.error('加载订单详情失败:', error);
    } finally {
      setLoading(false);
    }
  }, [orderNo]);

  useEffect(() => {
    void loadOrder();

    const refreshWhenVisible = () => {
      if (document.visibilityState === 'visible') void loadOrder();
    };
    window.addEventListener('focus', refreshWhenVisible);
    document.addEventListener('visibilitychange', refreshWhenVisible);

    return () => {
      window.removeEventListener('focus', refreshWhenVisible);
      document.removeEventListener('visibilitychange', refreshWhenVisible);
    };
  }, [loadOrder]);

  const runAction = async (action: () => Promise<unknown>, message: string) => {
    try {
      await action();
      alert(message);
      await loadOrder();
    } catch (error) {
      console.error(message, error);
      alert('操作失败，请稍后重试');
    }
  };

  if (loading) return <div style={{ padding: 40, textAlign: 'center' }}>加载中...</div>;
  if (!order) return <div style={{ padding: 40, textAlign: 'center' }}>订单不存在</div>;

  return (
    <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 900, margin: '0 auto' }}>
      <button onClick={() => navigate('/orders')} style={{ marginBottom: 16, padding: '7px 14px', border: '1px solid var(--color-border)', borderRadius: 6, background: 'transparent', cursor: 'pointer' }}>← 返回订单</button>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <div>
          <h1 style={{ margin: 0, fontSize: 'var(--font-size-h1)' }}>订单详情</h1>
          <p style={{ color: 'var(--color-text-secondary)', fontSize: 13 }}>订单号：{order.orderNo}</p>
        </div>
        <strong style={{ color: 'var(--color-accent)' }}>{statusLabels[order.orderStatus] || order.orderStatus}</strong>
      </div>

      <section style={{ background: 'var(--color-bg-primary)', borderRadius: 10, padding: 20, boxShadow: 'var(--shadow-sm)', marginBottom: 16 }}>
        <h2 style={{ fontSize: 16, marginTop: 0 }}>物流信息</h2>
        {order.logisticsCompany || order.trackingNo ? (
          <p style={{ fontSize: 14 }}>承运商：{order.logisticsCompany || '-'} / 运单号：{order.trackingNo || '-'}</p>
        ) : <p style={{ color: 'var(--color-text-tertiary)', fontSize: 14 }}>商家尚未发货</p>}
        {order.shippingTime && <p style={{ color: 'var(--color-text-secondary)', fontSize: 13 }}>发货时间：{new Date(order.shippingTime).toLocaleString('zh-CN')}</p>}
      </section>

      <section style={{ background: 'var(--color-bg-primary)', borderRadius: 10, padding: 20, boxShadow: 'var(--shadow-sm)' }}>
        <h2 style={{ fontSize: 16, marginTop: 0 }}>商品明细</h2>
        {(order.items || []).map((item, index) => (
          <div key={`${item.productId}-${index}`} style={{ display: 'flex', justifyContent: 'space-between', padding: '12px 0', borderBottom: '1px solid var(--color-border-light)', fontSize: 14 }}>
            <span>{item.productName || '商品'} × {item.quantity || 1}</span>
            <strong>¥{(Number(item.price || 0) * Number(item.quantity || 1)).toFixed(2)}</strong>
          </div>
        ))}
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 18, fontSize: 18 }}>
          <span>实付金额</span><strong style={{ color: 'var(--color-accent)' }}>¥{Number(order.payAmount || order.totalAmount || 0).toFixed(2)}</strong>
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 18 }}>
          {order.canCancel && <button onClick={() => window.confirm('确定取消订单吗？') && void runAction(() => orderService.cancelOrder(order.orderNo), '订单已取消')} style={buttonStyle}>取消订单</button>}
          {order.canConfirm && <button onClick={() => window.confirm('确认已收到商品吗？') && void runAction(() => orderService.confirmReceipt(order.orderNo), '已确认收货')} style={primaryButtonStyle}>确认收货</button>}
          {order.canRefund && <button onClick={() => navigate(`/after-sales?orderNo=${encodeURIComponent(order.orderNo)}`)} style={buttonStyle}>申请售后</button>}
        </div>
      </section>
    </div>
  );
};

const buttonStyle: React.CSSProperties = { padding: '8px 16px', border: '1px solid var(--color-border)', borderRadius: 6, background: 'transparent', cursor: 'pointer' };
const primaryButtonStyle: React.CSSProperties = { ...buttonStyle, border: 'none', background: 'var(--color-accent)', color: '#fff' };

export default OrderDetailPage;
