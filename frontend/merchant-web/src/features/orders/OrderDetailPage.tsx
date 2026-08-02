import React, { useCallback, useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { orderApi, type OrderVO, type CreatePaymentResult } from '../../api/order';

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
  const [payment, setPayment] = useState<CreatePaymentResult | null>(null);
  const [actionLoading, setActionLoading] = useState(false);

  const load = useCallback(() => {
    if (!orderNo) return;
    orderApi.getDetail(orderNo).then((res) => {
      setOrder(res.data);
      // 自动查询该订单已生成的收款凭证（下单时后端已自动生成）
      orderApi.getPaymentByOrder(orderNo).then((pRes) => {
        setPayment(pRes.data);
      }).catch(() => {
        // 尚无支付流水时忽略（未自动生成，可手动发起收款）
      });
    }).catch((err) => {
      console.error('加载订单详情失败:', err);
      setError('订单不存在或加载失败');
    }).finally(() => setLoading(false));
  }, [orderNo]);

  useEffect(() => { load(); }, [load]);

  const handleAccept = async () => {
    if (!orderNo) return;
    setActionLoading(true);
    try {
      await orderApi.acceptOrder(orderNo);
      alert('接单成功');
      load();
    } catch {
      alert('接单失败');
    } finally {
      setActionLoading(false);
    }
  };

  const handleCreatePayment = async () => {
    if (!orderNo) return;
    setActionLoading(true);
    try {
      const res = await orderApi.createPayment(orderNo);
      setPayment(res.data);
      load();
    } catch {
      alert('发起收款失败，请先接单');
    } finally {
      setActionLoading(false);
    }
  };

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

        {/* 商家操作区 */}
        {order.orderStatus === 'PENDING_PAYMENT' && (
          <div style={{ marginTop: 'var(--spacing-lg)', display: 'flex', gap: 'var(--spacing-sm)' }}>
            <button
              onClick={handleAccept}
              disabled={actionLoading}
              style={{ padding: '8px 20px', borderRadius: 'var(--radius-sm)', background: 'var(--color-accent)', color: '#fff', border: 'none', cursor: 'pointer' }}
            >
              接单
            </button>
            <button
              onClick={handleCreatePayment}
              disabled={actionLoading}
              style={{ padding: '8px 20px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-accent)', background: 'transparent', color: 'var(--color-accent)', cursor: 'pointer' }}
            >
              发起收款（生成二维码）
            </button>
          </div>
        )}

        <div style={{ marginTop: 'var(--spacing-lg)' }}>
          <button onClick={() => navigate('/orders')} style={{ padding: '8px 20px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: 'var(--color-text-secondary)', cursor: 'pointer' }}>返回订单列表</button>
        </div>
      </div>

      {/* 二维码收款弹窗 */}
      {payment && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }} onClick={() => setPayment(null)}>
          <div style={{ background: '#fff', borderRadius: '12px', padding: '24px', width: 360, textAlign: 'center' }} onClick={(e) => e.stopPropagation()}>
            <h2 style={{ fontSize: '16px', fontWeight: 600, marginBottom: '12px' }}>商户收款码已生成</h2>
            <div style={{ margin: '12px 0', fontSize: '14px' }}>
              <p style={{ color: 'var(--color-text-secondary)' }}>订单号</p>
              <p style={{ fontWeight: 600 }}>{payment.orderNo}</p>
              <p style={{ color: 'var(--color-text-secondary)', marginTop: '8px' }}>收款金额</p>
              <p style={{ fontSize: '24px', fontWeight: 700, color: '#ff4d4f' }}>¥{Number(payment.amount).toFixed(2)}</p>
            </div>
            <div style={{ display: 'inline-block', padding: '8px', border: '1px dashed #d9d9d9', borderRadius: '8px' }}>
              <p style={{ fontSize: '12px', color: '#999', marginBottom: '6px' }}>扫码凭证 Token</p>
              <p style={{ fontSize: '12px', fontFamily: 'monospace', wordBreak: 'break-all' }}>{payment.qrToken}</p>
            </div>
            <p style={{ fontSize: '12px', color: '#999', marginTop: '12px' }}>
              有效期至 {new Date(payment.expireTime).toLocaleTimeString('zh-CN')}，请让顾客在 APP 中使用此 Token 支付
            </p>
            <button onClick={() => setPayment(null)} style={{ marginTop: '16px', padding: '8px 24px', borderRadius: '6px', background: 'var(--color-accent)', color: '#fff', border: 'none', cursor: 'pointer' }}>关闭</button>
          </div>
        </div>
      )}
    </div>
  );
};

export default OrderDetailPage;
