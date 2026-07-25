import React from 'react';
import type { Order } from '../types/order';
import OrderStatus from './OrderStatus';

interface OrderCardProps {
  order: Order;
  onPay?: (orderNo: string) => void;
  onCancel?: (orderNo: string) => void;
  onTrack?: (orderNo: string) => void;
}

const OrderCard: React.FC<OrderCardProps> = ({ order, onPay, onCancel, onTrack }) => {
  return (
    <div
      style={{
        background: 'var(--color-bg-primary)',
        borderRadius: 'var(--radius-md)',
        boxShadow: 'var(--shadow-sm)',
        overflow: 'hidden',
        marginBottom: 'var(--spacing-md)',
      }}
    >
      {/* Header */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          padding: 'var(--spacing-sm) var(--spacing-md)',
          borderBottom: '1px solid var(--color-border-light)',
          background: 'var(--color-bg-secondary)',
        }}
      >
        <span style={{ fontSize: '12px', color: 'var(--color-text-tertiary)' }}>
          订单号：{order.orderNo}
        </span>
        <OrderStatus status={order.status} />
      </div>

      {/* Items */}
      <div style={{ padding: 'var(--spacing-sm) var(--spacing-md)' }}>
        {order.items.map((item, index) => (
          <div
            key={index}
            style={{
              display: 'flex',
              gap: 'var(--spacing-sm)',
              padding: 'var(--spacing-sm) 0',
              borderBottom: index < order.items.length - 1 ? '1px solid var(--color-border-light)' : 'none',
            }}
          >
            <div
              style={{
                width: 60,
                height: 60,
                borderRadius: 'var(--radius-sm)',
                background: 'var(--color-bg-secondary)',
                flexShrink: 0,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '10px',
                color: 'var(--color-text-tertiary)',
              }}
            >
              {item.thumbnail ? <img src={item.thumbnail} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} /> : '图'}
            </div>
            <div style={{ flex: 1 }}>
              <p style={{ fontSize: '14px', fontWeight: 500, color: 'var(--color-text-primary)' }}>{item.name}</p>
              {item.specInfo && <p style={{ fontSize: '12px', color: 'var(--color-text-tertiary)' }}>{item.specInfo}</p>}
            </div>
            <div style={{ textAlign: 'right' }}>
              <p style={{ fontSize: '14px', fontWeight: 600, color: 'var(--color-text-primary)' }}>¥{item.price.toFixed(2)}</p>
              <p style={{ fontSize: '12px', color: 'var(--color-text-tertiary)' }}>x{item.quantity}</p>
            </div>
          </div>
        ))}
      </div>

      {/* Footer */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          padding: 'var(--spacing-sm) var(--spacing-md)',
          borderTop: '1px solid var(--color-border-light)',
        }}
      >
        <span style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>
          {new Date(order.createdAt).toLocaleString('zh-CN')}
        </span>
        <div style={{ display: 'flex', alignItems: 'baseline', gap: 4 }}>
          <span style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>合计：</span>
          <span style={{ fontSize: '18px', fontWeight: 700, color: 'var(--color-accent)' }}>¥{order.actualAmount.toFixed(2)}</span>
        </div>
      </div>

      {/* Actions */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'flex-end',
          gap: 'var(--spacing-sm)',
          padding: 'var(--spacing-sm) var(--spacing-md)',
          borderTop: '1px solid var(--color-border-light)',
        }}
      >
        {order.status === 'pending_payment' && onPay && (
          <button
            onClick={() => onPay(order.orderNo)}
            style={{
              padding: '6px 18px',
              borderRadius: 'var(--radius-sm)',
              background: 'var(--color-accent)',
              color: '#fff',
              fontSize: '13px',
              fontWeight: 500,
              border: 'none',
              cursor: 'pointer',
            }}
          >
            去付款
          </button>
        )}
        {order.status === 'pending_payment' && onCancel && (
          <button
            onClick={() => onCancel(order.orderNo)}
            style={{
              padding: '6px 18px',
              borderRadius: 'var(--radius-sm)',
              border: '1px solid var(--color-border)',
              background: 'transparent',
              color: 'var(--color-text-secondary)',
              fontSize: '13px',
              cursor: 'pointer',
            }}
          >
            取消订单
          </button>
        )}
        {(order.status === 'shipped' || order.status === 'completed') && onTrack && (
          <button
            onClick={() => onTrack(order.orderNo)}
            style={{
              padding: '6px 18px',
              borderRadius: 'var(--radius-sm)',
              border: '1px solid var(--color-border)',
              background: 'transparent',
              color: 'var(--color-text-secondary)',
              fontSize: '13px',
              cursor: 'pointer',
            }}
          >
            查看物流
          </button>
        )}
      </div>
    </div>
  );
};

export default OrderCard;