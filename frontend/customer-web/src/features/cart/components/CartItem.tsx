import React from 'react';
import type { CartItem as CartItemType } from '../types/cart';
import useCartStore from '../store/cartStore';

interface CartItemProps {
  item: CartItemType;
}

const CartItem: React.FC<CartItemProps> = ({ item }) => {
  const { updateQuantity, removeItem, toggleCheck } = useCartStore();

  return (
    <div
      style={{
        display: 'flex',
        gap: 'var(--spacing-md)',
        padding: 'var(--spacing-md)',
        background: 'var(--color-bg-primary)',
        borderRadius: 'var(--radius-md)',
        marginBottom: 'var(--spacing-sm)',
        boxShadow: 'var(--shadow-sm)',
        alignItems: 'center',
      }}
    >
      {/* Checkbox */}
      <div
        onClick={() => toggleCheck(item.productId, item.specInfo)}
        style={{
          width: 22,
          height: 22,
          borderRadius: '50%',
          border: `2px solid ${item.checked ? 'var(--color-accent)' : 'var(--color-border)'}`,
          background: item.checked ? 'var(--color-accent)' : 'transparent',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          cursor: 'pointer',
          flexShrink: 0,
          color: '#fff',
          fontSize: '12px',
          fontWeight: 700,
        }}
      >
        {item.checked && '✓'}
      </div>

      {/* Thumbnail */}
      <div
        style={{
          width: 80,
          height: 80,
          borderRadius: 'var(--radius-sm)',
          background: 'var(--color-bg-secondary)',
          overflow: 'hidden',
          flexShrink: 0,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: 'var(--color-text-tertiary)',
          fontSize: '11px',
        }}
      >
        {item.thumbnail ? (
          <img src={item.thumbnail} alt={item.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
        ) : '暂无图'}
      </div>

      {/* Info */}
      <div style={{ flex: 1, minWidth: 0 }}>
        <h3
          style={{
            fontSize: '14px',
            fontWeight: 500,
            color: 'var(--color-text-primary)',
            marginBottom: 4,
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
          }}
        >
          {item.name}
        </h3>
        {item.specInfo && (
          <p style={{ fontSize: '12px', color: 'var(--color-text-tertiary)', marginBottom: 4 }}>
            {item.specInfo}
          </p>
        )}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span style={{ fontSize: '16px', fontWeight: 700, color: 'var(--color-accent)' }}>
            ¥{item.price.toFixed(2)}
          </span>
          {/* Quantity Control */}
          <div style={{ display: 'flex', alignItems: 'center', gap: 0 }}>
            <button
              onClick={() => updateQuantity(item.productId, item.quantity - 1, item.specInfo)}
              disabled={item.quantity <= 1}
              style={{
                width: 28,
                height: 28,
                border: '1px solid var(--color-border)',
                borderRadius: '4px 0 0 4px',
                background: 'var(--color-bg-secondary)',
                cursor: item.quantity <= 1 ? 'not-allowed' : 'pointer',
                opacity: item.quantity <= 1 ? 0.5 : 1,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '14px',
              }}
            >
              −
            </button>
            <span
              style={{
                width: 40,
                height: 28,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                borderTop: '1px solid var(--color-border)',
                borderBottom: '1px solid var(--color-border)',
                fontSize: '13px',
                color: 'var(--color-text-primary)',
              }}
            >
              {item.quantity}
            </span>
            <button
              onClick={() => updateQuantity(item.productId, item.quantity + 1, item.specInfo)}
              disabled={item.quantity >= item.maxQuantity}
              style={{
                width: 28,
                height: 28,
                border: '1px solid var(--color-border)',
                borderRadius: '0 4px 4px 0',
                background: 'var(--color-bg-secondary)',
                cursor: item.quantity >= item.maxQuantity ? 'not-allowed' : 'pointer',
                opacity: item.quantity >= item.maxQuantity ? 0.5 : 1,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '14px',
              }}
            >
              +
            </button>
          </div>
        </div>
      </div>

      {/* Remove */}
      <button
        onClick={() => removeItem(item.productId, item.specInfo)}
        style={{
          padding: '4px 8px',
          fontSize: '12px',
          color: 'var(--color-text-tertiary)',
          background: 'none',
          border: 'none',
          cursor: 'pointer',
          whiteSpace: 'nowrap',
        }}
      >
        删除
      </button>
    </div>
  );
};

export default React.memo(CartItem);