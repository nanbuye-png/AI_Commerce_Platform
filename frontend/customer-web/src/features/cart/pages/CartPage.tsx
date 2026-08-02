import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import useCartStore from '../store/cartStore';
import CartItem from '../components/CartItem';
import CartSummary from '../components/CartSummary';
import { EmptyState } from '../../../components/common';
import { cartService } from '../../../services/cart';
import { profileService } from '../../../services/profile';
import { getToken } from '../../../utils/token';

const CartPage: React.FC = () => {
  const { items, setItems } = useCartStore();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [checking, setChecking] = useState(false);

  /** 去结算：先校验库存，库存不足则不进入结算页 */
  const handleCheckout = async () => {
    const checkedItems = items.filter((i) => i.checked);
    if (checkedItems.length === 0) return;
    setChecking(true);
    try {
      for (const item of checkedItems) {
        if (!item.skuId) continue;
        const ok = await profileService.checkStock(item.skuId, item.quantity);
        if (!ok) {
          const stock = await profileService.getStock(item.skuId);
          alert(`「${item.name}」库存不足，当前仅剩 ${stock} 件，请调整购买数量`);
          return;
        }
      }
      navigate('/checkout');
    } catch {
      alert('库存校验失败，请稍后重试');
    } finally {
      setChecking(false);
    }
  };

  useEffect(() => {
    let cancelled = false;

    if (!getToken()) {
      navigate('/login');
      return;
    }

    cartService
      .getCart()
      .then((cart) => {
        if (cancelled) return;
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
      })
      .catch((err: unknown) => {
        if (cancelled) return;
        console.error('加载购物车失败:', err);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [navigate, setItems]);

  if (loading) {
    return (
      <div style={{ padding: 'var(--spacing-2xl) var(--spacing-lg)', maxWidth: 800, margin: '0 auto' }}>
        <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)' }}>
          购物车
        </h1>
        <p style={{ color: 'var(--color-text-tertiary)', fontSize: '14px' }}>加载中...</p>
      </div>
    );
  }

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
      <CartSummary onCheckout={handleCheckout} checking={checking} />
    </div>
  );
};

export default CartPage;