import React from 'react';
import useCartStore from '../store/cartStore';

interface CartSummaryProps {
  onCheckout: () => void;
}

const CartSummary: React.FC<CartSummaryProps> = ({ onCheckout }) => {
  const { getSummary, toggleCheckAll, items } = useCartStore();
  const summary = getSummary();
  const allChecked = items.length > 0 && items.every((item) => item.checked);

  return (
    <div
      style={{
        position: 'sticky',
        bottom: 0,
        background: 'var(--color-bg-primary)',
        borderTop: '1px solid var(--color-border-light)',
        padding: 'var(--spacing-md) var(--spacing-lg)',
        boxShadow: '0 -4px 12px rgba(0,0,0,0.04)',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', maxWidth: 800, margin: '0 auto' }}>
        {/* Select All */}
        <div
          onClick={() => toggleCheckAll(!allChecked)}
          style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer' }}
        >
          <div
            style={{
              width: 22,
              height: 22,
              borderRadius: '50%',
              border: `2px solid ${allChecked ? 'var(--color-accent)' : 'var(--color-border)'}`,
              background: allChecked ? 'var(--color-accent)' : 'transparent',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#fff',
              fontSize: '12px',
              fontWeight: 700,
            }}
          >
            {allChecked && '✓'}
          </div>
          <span style={{ fontSize: '14px', color: 'var(--color-text-primary)' }}>全选</span>
        </div>

        {/* Total */}
        <div style={{ textAlign: 'right' }}>
          <div style={{ display: 'flex', alignItems: 'baseline', gap: 4 }}>
            <span style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>
              合计 ({summary.checkedCount}件)：
            </span>
            <span style={{ fontSize: '20px', fontWeight: 700, color: 'var(--color-accent)' }}>
              ¥{summary.checkedAmount.toFixed(2)}
            </span>
          </div>
          {summary.discount > 0 && (
            <p style={{ fontSize: '12px', color: 'var(--color-text-tertiary)' }}>
              已优惠 ¥{summary.discount.toFixed(2)}
            </p>
          )}
        </div>

        {/* Checkout Button */}
        <button
          onClick={onCheckout}
          disabled={summary.checkedCount === 0}
          style={{
            padding: '12px 32px',
            borderRadius: 'var(--radius-sm)',
            background: summary.checkedCount > 0 ? 'var(--color-accent)' : 'var(--color-border)',
            color: '#fff',
            fontSize: '15px',
            fontWeight: 600,
            border: 'none',
            cursor: summary.checkedCount > 0 ? 'pointer' : 'not-allowed',
            transition: 'background var(--transition-fast)',
          }}
        >
          去结算
        </button>
      </div>
    </div>
  );
};

export default CartSummary;