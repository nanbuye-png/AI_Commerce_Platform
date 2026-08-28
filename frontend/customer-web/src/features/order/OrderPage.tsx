import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { EmptyState } from '../../components/common';
import OrderCard from './components/OrderCard';
import { orderService, type OrderVO, type PaymentDetail } from '../../services/order';
import { getToken } from '../../utils/token';
import type { Order, OrderItem } from './types/order';
import { toFrontendStatus } from './orderStatus';

/** 后端订单 VO → 前端 Order */
function toOrder(vo: OrderVO): Order {
  const items: OrderItem[] = (vo.items ?? []).map((it) => ({
    productId: String(it.productId ?? ''),
    name: it.productName ?? '',
    thumbnail: it.productImage ?? '',
    price: Number(it.price) || 0,
    quantity: it.quantity ?? 1,
  }));

  return {
    id: vo.id ? String(vo.id) : vo.orderNo,
    orderNo: vo.orderNo,
    status: toFrontendStatus(vo.orderStatus),
    items,
    totalAmount: Number(vo.totalAmount ?? vo.payAmount ?? 0) || 0,
    discount: Number(vo.discountAmount) || 0,
    actualAmount: Number(vo.payAmount ?? vo.totalAmount ?? 0) || 0,
    address: '',
    createdAt: vo.createdTime ?? '',
    canCancel: vo.canCancel,
    canConfirm: vo.canConfirm,
    canRefund: vo.canRefund,
    logisticsCompany: vo.logisticsCompany,
    trackingNo: vo.trackingNo,
  };
}

const OrderPage: React.FC = () => {
  const navigate = useNavigate();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  // 支付弹窗状态
  const [payment, setPayment] = useState<PaymentDetail | null>(null);
  const [payLoading, setPayLoading] = useState(false);
  const [payError, setPayError] = useState<string | null>(null);
  const [paid, setPaid] = useState(false);

  const loadOrders = useCallback(() => {
    setLoading(true);
    return orderService
      .myOrders({ page: 1, pageSize: 20 })
      .then((res) => {
        setOrders((res.list ?? []).map(toOrder));
      })
      .catch((err: unknown) => {
        console.error('加载订单失败:', err);
      })
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (!getToken()) {
      void navigate('/login');
      return;
    }

    void loadOrders();

    const refreshWhenVisible = () => {
      if (document.visibilityState === 'visible') void loadOrders();
    };
    window.addEventListener('focus', refreshWhenVisible);
    document.addEventListener('visibilitychange', refreshWhenVisible);

    return () => {
      window.removeEventListener('focus', refreshWhenVisible);
      document.removeEventListener('visibilitychange', refreshWhenVisible);
    };
  }, [loadOrders, navigate]);

  /**
   * "去付款"：先查询该订单的支付流水（商家已接单并发起收款后才有）
   * 查到后展示支付确认弹窗供用户确认支付（模拟扫码）
   */
  const handlePay = async (orderNo: string) => {
    setPayError(null);
    setPaid(false);
    setPayment(null);
    try {
      const detail = await orderService.paymentByOrder(orderNo);
      setPayment(detail);
    } catch (err) {
      console.error('查询支付信息失败:', err);
      alert('商家尚未发起收款，请等待商家接单并生成收款二维码后重试');
    }
  };

  /** 确认支付 */
  const handleConfirmPay = async () => {
    if (!payment) return;
    setPayLoading(true);
    setPayError(null);
    try {
      await orderService.payByToken(payment.qrToken);
      setPaid(true);
      void loadOrders();
      setTimeout(() => setPayment(null), 1500);
    } catch (err) {
      console.error('支付失败:', err);
      setPayError('支付失败，请重试或确认二维码未过期');
    } finally {
      setPayLoading(false);
    }
  };

  /** 取消支付 */
  const handleCancelPay = async () => {
    if (!payment) return;
    setPayLoading(true);
    setPayError(null);
    try {
      await orderService.cancelPayment(payment.qrToken);
      setPayment(null);
    } catch (err) {
      console.error('取消支付失败:', err);
      setPayError('取消失败，请稍后重试');
    } finally {
      setPayLoading(false);
    }
  };

  const handleCancelOrder = async (orderNo: string) => {
    if (!window.confirm('确定取消这个订单吗？')) return;
    try {
      await orderService.cancelOrder(orderNo);
      void loadOrders();
    } catch (err) {
      console.error('取消订单失败:', err);
      alert('取消订单失败，请稍后重试');
    }
  };

  const handleConfirmReceipt = async (orderNo: string) => {
    if (!window.confirm('确认已经收到商品吗？')) return;
    try {
      await orderService.confirmReceipt(orderNo);
      void loadOrders();
    } catch (err) {
      console.error('确认收货失败:', err);
      alert('确认收货失败，请稍后重试');
    }
  };

  const handleAfterSales = async (orderNo: string, type: 'refund' | 'return') => {
    const reason = window.prompt(type === 'refund' ? '请输入退款原因：' : '请输入退货原因：', type === 'refund' ? 'OTHER' : 'OTHER');
    if (!reason) return;
    try {
      if (type === 'refund') await orderService.requestRefund(orderNo, reason);
      else await orderService.requestReturn(orderNo, reason);
      alert(type === 'refund' ? '退款申请已提交，等待商家审核' : '退货申请已提交，等待商家审核');
      void loadOrders();
    } catch (err) {
      console.error('提交售后申请失败:', err);
      alert('提交失败，请确认订单状态或稍后重试');
    }
  };

  if (loading) {
    return (
      <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 800, margin: '0 auto' }}>
        <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>
          我的订单
        </h1>
        <p style={{ color: 'var(--color-text-tertiary)', fontSize: '14px' }}>加载中...</p>
      </div>
    );
  }

  if (orders.length === 0) {
    return (
      <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 800, margin: '0 auto' }}>
        <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>
          我的订单
        </h1>
        <EmptyState
          icon="📦"
          title="暂无订单"
          description="您还没有下过订单，去逛逛吧"
        />
      </div>
    );
  }

  return (
    <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 800, margin: '0 auto' }}>
      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>
        我的订单
      </h1>
      {orders.map((order) => (
        <OrderCard
          key={order.id}
          order={order}
          onPay={handlePay}
          onCancel={handleCancelOrder}
          onConfirm={handleConfirmReceipt}
          onAfterSales={handleAfterSales}
          onTrack={(orderNo) => navigate(`/orders/${orderNo}`)}
        />
      ))}

      {/* 支付确认弹窗（模拟扫码支付） */}
      {payment && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'rgba(0,0,0,0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
          }}
          onClick={() => !payLoading && setPayment(null)}
        >
          <div
            style={{
              background: '#fff',
              borderRadius: 12,
              padding: 24,
              width: 360,
              textAlign: 'center',
            }}
            onClick={(e) => e.stopPropagation()}
          >
            {paid ? (
              <>
                <div style={{ fontSize: 48, marginBottom: 12 }}>✅</div>
                <h2 style={{ fontSize: 16, fontWeight: 600, marginBottom: 8 }}>支付成功</h2>
                <p style={{ fontSize: 13, color: 'var(--color-text-tertiary)' }}>订单已付款，商家将尽快处理</p>
              </>
            ) : (
              <>
                <h2 style={{ fontSize: 16, fontWeight: 600, marginBottom: 12 }}>商户收款确认</h2>
                <div style={{ margin: '12px 0', fontSize: 14 }}>
                  <p style={{ color: 'var(--color-text-secondary)' }}>订单号</p>
                  <p style={{ fontWeight: 600, wordBreak: 'break-all' }}>{payment.orderNo}</p>
                  <p style={{ color: 'var(--color-text-secondary)', marginTop: 8 }}>应付金额</p>
                  <p style={{ fontSize: 24, fontWeight: 700, color: '#ff4d4f' }}>
                    ¥{Number(payment.amount).toFixed(2)}
                  </p>
                  <p style={{ color: 'var(--color-text-secondary)', marginTop: 8 }}>收款凭证</p>
                  <p
                    style={{
                      fontSize: 12,
                      fontFamily: 'monospace',
                      wordBreak: 'break-all',
                      background: 'var(--color-bg-secondary)',
                      padding: '6px 8px',
                      borderRadius: 6,
                      marginTop: 4,
                    }}
                  >
                    {payment.qrToken}
                  </p>
                </div>
                <p style={{ fontSize: 12, color: '#999', marginBottom: 12 }}>
                  有效期至 {new Date(payment.expireTime).toLocaleTimeString('zh-CN')}
                </p>
                {payError && (
                  <p style={{ fontSize: 13, color: '#FF3B30', marginBottom: 8 }}>{payError}</p>
                )}
                <div style={{ display: 'flex', gap: 10, justifyContent: 'center' }}>
                  <button
                    onClick={handleCancelPay}
                    disabled={payLoading}
                    style={{
                      padding: '8px 20px',
                      borderRadius: 6,
                      border: '1px solid var(--color-border)',
                      background: 'transparent',
                      color: 'var(--color-text-secondary)',
                      cursor: payLoading ? 'not-allowed' : 'pointer',
                    }}
                  >
                    取消支付
                  </button>
                  <button
                    onClick={handleConfirmPay}
                    disabled={payLoading}
                    style={{
                      padding: '8px 24px',
                      borderRadius: 6,
                      background: 'var(--color-accent)',
                      color: '#fff',
                      border: 'none',
                      cursor: payLoading ? 'not-allowed' : 'pointer',
                      opacity: payLoading ? 0.6 : 1,
                    }}
                  >
                    {payLoading ? '支付中...' : '确认支付'}
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default OrderPage;
