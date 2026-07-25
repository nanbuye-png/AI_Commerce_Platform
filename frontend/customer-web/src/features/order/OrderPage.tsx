import React from 'react';
import { EmptyState } from '../../components/common';
import OrderCard from './components/OrderCard';
import type { Order as OrderType } from './types/order';

const OrderPage: React.FC = () => {
  // Empty state — no orders yet
  const orders: OrderType[] = [];

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