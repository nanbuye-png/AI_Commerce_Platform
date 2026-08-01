import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { EmptyState } from '../../components/common';
import OrderCard from './components/OrderCard';
import { orderService, type OrderVO } from '../../services/order';
import { getToken } from '../../utils/token';
import type { Order, OrderItem, OrderStatus } from './types/order';

/** 后端订单状态 → 前端订单状态 */
function toFrontendStatus(status: string): OrderStatus {
  const map: Record<string, OrderStatus> = {
    PENDING_PAYMENT: 'pending_payment',
    PROCESSING: 'pending_ship',
    PAID: 'pending_ship',
    SHIPPED: 'shipped',
    COMPLETED: 'completed',
    CANCELLED: 'cancelled',
    REFUNDING: 'refunding',
    REFUNDED: 'refunding',
    CLOSED: 'cancelled',
  };
  return map[status] ?? 'pending_payment';
}

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
  };
}

const OrderPage: React.FC = () => {
  const navigate = useNavigate();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    if (!getToken()) {
      navigate('/login');
      return;
    }

    orderService
      .myOrders({ page: 1, pageSize: 20 })
      .then((res) => {
        if (!cancelled) setOrders((res.list ?? []).map(toOrder));
      })
      .catch((err: unknown) => {
        if (!cancelled) console.error('加载订单失败:', err);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [navigate]);

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
        />
      ))}
    </div>
  );
};

export default OrderPage;