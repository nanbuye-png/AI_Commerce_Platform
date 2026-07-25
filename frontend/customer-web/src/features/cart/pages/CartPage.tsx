import React from 'react';
import { useNavigate } from 'react-router-dom';
import useCartStore from '../store/cartStore';
import CartItem from '../components/CartItem';
import CartSummary from '../components/CartSummary';
import { EmptyState } from '../../../components/common';

const CartPage: React.FC = () => {
  const { items } = useCartStore();
  const navigate = useNavigate();

  if (items.length === 0) {
    return (
      <div style={{ padding: 'var(--spacing-2xl) var(--spacing-lg)', maxWidth: 800, margin: '0 auto' }}>
        <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>
          购物车
        </h1>
        <EmptyState
          icon="🛒"
          title="购物车是空的"
          description="快去挑选心仪的商品吧"
        />
      </div>
    );
  }

  return (
    <div style={{ paddingBottom: 80 }}>
      <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg) var(--spacing-md)', maxWidth: 800, margin: '0 auto' }}>
        <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>
          购物车 <span style={{ fontSize: '16px', color: 'var(--color-text-secondary)', fontWeight: 400 }}>({items.length}件)</span>
        </h1>
        <div>
          {items.map((item) => (
            <CartItem key={`${item.productId}-${item.specInfo}`} item={item} />
          ))}
        </div>
      </div>
      <CartSummary onCheckout={() => navigate('/checkout')} />
    </div>
  );
};

export default CartPage;