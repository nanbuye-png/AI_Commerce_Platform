import React from 'react';
import type { OrderStatus as OrderStatusType } from '../types/order';
import { orderStatusLabels, orderStatusColors } from '../types/order';

interface OrderStatusProps {
  status: OrderStatusType;
  size?: 'sm' | 'md';
}

const OrderStatus: React.FC<OrderStatusProps> = ({ status, size = 'sm' }) => {
  const label = orderStatusLabels[status];
  const color = orderStatusColors[status];

  const fontSize = size === 'sm' ? '12px' : '14px';
  const padding = size === 'sm' ? '2px 8px' : '4px 12px';

  return (
    <span
      style={{
        display: 'inline-block',
        fontSize,
        fontWeight: 600,
        padding,
        borderRadius: '4px',
        background: `${color}18`,
        color,
        lineHeight: 1.4,
      }}
    >
      {label}
    </span>
  );
};

export default OrderStatus;