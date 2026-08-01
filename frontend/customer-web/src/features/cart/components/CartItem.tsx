import React, { useState } from 'react';
import type { CartItem as CartItemType } from '../types/cart';
import useCartStore from '../store/cartStore';
import { cartService } from '../../../services/cart';

interface CartItemProps {
  item: CartItemType;
}

const CartItem: React.FC<CartItemProps> = ({ item }) => {
  const { toggleCheck, setItems } = useCartStore();
  const [syncing, setSyncing] = useState(false);

  const syncCart = async () => {
    try {
      const cart = await cartService.getCart();
      setItems(
        cart.items.map((ci) => ({
          backendId: ci.id,
          skuId: ci.skuId,
          productId: String(ci.productId),
          name: ci.productName,
          thumbnail: ci.productImage ?? '',
          price: Number(ci.price) || 0,
          quantity: ci.quantity,
          stock: 99,
          checked: ci.selected,
          maxQuantity: 99,
        })),
      );
    } catch {
      // 同步失败保持本地状态，不阻塞交互
    }
  };

  const handleToggle = async () => {
    toggleCheck(item.productId, item.specInfo);
  };

  const handleUpdateQuantity = async (next: number) => {
    if (!item.skuId) return;
    setSyncing(true);
    try {
      await cartService.updateQuantity(item.skuId, next);
      await syncCart();
    } catch {
      alert('修改数量失败，请稍后重试');
    } finally {
      setSyncing(false);
    }
  };

  const handleRemove = async () => {
    if (!item.skuId) return;
    setSyncing(true);
    try {
      await cartService.removeItem(item.skuId);
      await syncCart();
    } catch {
      alert('删除失败，请稍后重试');
    } finally {
      setSyncing(false);
    }
  };

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
        onClick={handleToggle}
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
              onClick={() => {
                const next = item.quantity - 1;
                if (next >= 1) handleUpdateQuantity(next);
              }}
              disabled={item.quantity <= 1 || syncing}
              style={{
                width: 28,
                height: 28,
                border: '1px solid var(--color-border)',
                borderRadius: '4px 0 0 4px',
                background: 'var(--color-bg-secondary)',
                cursor: item.quantity <= 1 || syncing ? 'not-allowed' : 'pointer',
                opacity: item.quantity <= 1 || syncing ? 0.5 : 1,
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
              onClick={() => handleUpdateQuantity(item.quantity + 1)}
              disabled={item.quantity >= item.maxQuantity || syncing}
              style={{
                width: 28,
                height: 28,
                border: '1px solid var(--color-border)',
                borderRadius: '0 4px 4px 0',
                background: 'var(--color-bg-secondary)',
                cursor: item.quantity >= item.maxQuantity || syncing ? 'not-allowed' : 'pointer',
                opacity: item.quantity >= item.maxQuantity || syncing ? 0.5 : 1,
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
        onClick={handleRemove}
        disabled={syncing}
        style={{
          padding: '4px 8px',
          fontSize: '12px',
          color: 'var(--color-text-tertiary)',
          background: 'none',
          border: 'none',
          cursor: syncing ? 'not-allowed' : 'pointer',
          whiteSpace: 'nowrap',
        }}
      >
        删除
      </button>
    </div>
  );
};

export default React.memo(CartItem);