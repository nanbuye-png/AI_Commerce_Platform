import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import useCartStore from '../cart/store/cartStore';
import AddressSelector from './components/AddressSelector';
import PaymentSelector from './components/PaymentSelector';
import { cartService } from '../../services/cart';
import { orderService } from '../../services/order';
import { profileService, type Address } from '../../services/profile';
import { getToken } from '../../utils/token';

/** 立即购买条目（从 URL query 携带） */
interface BuyNowItem {
  skuId: number;
  productId: number;
  quantity: number;
  price: number;
  name: string;
  image: string;
}

const CheckoutPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { items, getSummary, clearChecked, setItems } = useCartStore();
  const summary = getSummary();
  const [selectedAddr, setSelectedAddr] = useState<number | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [addresses, setAddresses] = useState<Address[]>([]);

  // 立即购买模式：从 URL 读取
  const buyNowItem: BuyNowItem | null = (() => {
    const skuId = searchParams.get('skuId');
    if (!skuId) return null;
    return {
      skuId: Number(skuId),
      productId: Number(searchParams.get('productId') || 0),
      quantity: Number(searchParams.get('quantity') || 1),
      price: Number(searchParams.get('price') || 0),
      name: decodeURIComponent(searchParams.get('name') || ''),
      image: decodeURIComponent(searchParams.get('image') || ''),
    };
  })();

  // 立即购买商品的展示列表
  const buyNowDisplayItems = buyNowItem ? [buyNowItem] : [];

  useEffect(() => {
    // 加载收货地址
    profileService.listAddresses()
      .then((list) => {
        setAddresses(list);
        const def = list.find((a) => a.isDefault) || list[0];
        if (def) setSelectedAddr(def.id);
      })
      .catch(() => {});
  }, []);

  const handleSubmit = async () => {
    if (!getToken()) {
      navigate('/login');
      return;
    }

    if (!selectedAddr) {
      setError('请先选择收货地址');
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      if (buyNowItem) {
        // ===== 立即购买模式：库存校验 + 直接下单 =====
        const ok = await profileService.checkStock(buyNowItem.skuId, buyNowItem.quantity);
        if (!ok) {
          const stock = await profileService.getStock(buyNowItem.skuId);
          setError(`商品库存不足，当前仅剩 ${stock} 件`);
          return;
        }
        // 直接创建订单
        const result = await orderService.createOrder({
          skuItems: [{ skuId: buyNowItem.skuId, quantity: buyNowItem.quantity }],
          addressId: selectedAddr,
        });
        console.info('立即购买下单成功:', result.orderNo);
        navigate('/orders', { replace: true });
      } else {
        // ===== 购物车结算模式：库存校验 + 结算 =====
        const checkedItems = items.filter((i) => i.checked);
        if (checkedItems.length === 0) {
          setError('请先选择要结算的商品');
          return;
        }

        // 逐项校验库存
        for (const item of checkedItems) {
          if (!item.skuId) continue;
          const ok = await profileService.checkStock(item.skuId, item.quantity);
          if (!ok) {
            const stock = await profileService.getStock(item.skuId);
            setError(`「${item.name}」库存不足，当前仅剩 ${stock} 件，请调整购买数量`);
            return;
          }
        }

        const cartItemIds = checkedItems
          .map((i) => i.backendId)
          .filter((id): id is number => typeof id === 'number' && id > 0);

        await cartService.checkout(cartItemIds, selectedAddr, 'BALANCE');
        clearChecked();
        // 重新拉取后端购物车（已结算商品移除）
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
          // ignore
        }
        navigate('/orders', { replace: true });
      }
    } catch (err: unknown) {
      console.error('提交订单失败:', err);
      setError('提交订单失败，请稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  // 展示的商品条目（合并两种场景）
  const displayItems = buyNowItem
    ? buyNowDisplayItems.map((item) => ({
        id: `buy-now-${item.skuId}`,
        name: item.name,
        thumbnail: item.image,
        price: item.price,
        quantity: item.quantity,
      }))
    : items.filter((i) => i.checked).map((item) => ({
        id: `${item.productId}-${item.specInfo}`,
        name: item.name,
        thumbnail: item.thumbnail,
        price: item.price,
        quantity: item.quantity,
      }));

  const totalAmount = buyNowItem
    ? buyNowItem.price * buyNowItem.quantity
    : summary.checkedAmount;

  return (
    <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 800, margin: '0 auto' }}>
      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-xl)' }}>
        {buyNowItem ? '确认购买' : '确认订单'}
      </h1>

      {error && (
        <div
          style={{
            padding: 'var(--spacing-sm) var(--spacing-md)',
            background: 'rgba(255, 59, 48, 0.08)',
            color: '#FF3B30',
            borderRadius: 'var(--radius-md)',
            marginBottom: 'var(--spacing-md)',
            fontSize: '14px',
          }}
        >
          {error}
        </div>
      )}

      {/* Address */}
      <div style={{ marginBottom: 'var(--spacing-xl)' }}>
        {addresses.length > 0 ? (
          <AddressSelector
            addresses={addresses}
            selectedId={selectedAddr != null ? String(selectedAddr) : undefined}
            onSelect={(a) => setSelectedAddr(Number(a.id))}
            onAddNew={() => navigate('/profile/address')}
          />
        ) : (
          <div
            style={{
              padding: 'var(--spacing-lg)',
              borderRadius: 'var(--radius-md)',
              background: 'var(--color-bg-primary)',
              textAlign: 'center',
            }}
          >
            <p style={{ fontSize: '14px', color: 'var(--color-text-secondary)', marginBottom: 'var(--spacing-sm)' }}>
              还没有收货地址，请先添加
            </p>
            <button
              onClick={() => navigate('/profile/address')}
              style={{
                padding: '8px 20px',
                borderRadius: 'var(--radius-sm)',
                background: 'var(--color-accent)',
                color: '#fff',
                border: 'none',
                cursor: 'pointer',
                fontSize: '14px',
              }}
            >
              去添加地址
            </button>
          </div>
        )}
      </div>

      {/* Order Items */}
      <div style={{ marginBottom: 'var(--spacing-xl)' }}>
        <h3 style={{ fontSize: '16px', fontWeight: 600, color: 'var(--color-text-primary)', marginBottom: 'var(--spacing-md)' }}>
          商品清单
        </h3>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-sm)' }}>
          {displayItems.map((item) => (
            <div
              key={item.id}
              style={{
                display: 'flex',
                gap: 'var(--spacing-md)',
                padding: 'var(--spacing-sm) var(--spacing-md)',
                background: 'var(--color-bg-primary)',
                borderRadius: 'var(--radius-md)',
              }}
            >
              <div style={{ width: 60, height: 60, borderRadius: 'var(--radius-sm)', background: 'var(--color-bg-secondary)', flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '10px', color: 'var(--color-text-tertiary)', overflow: 'hidden' }}>
                {item.thumbnail ? <img src={item.thumbnail} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} /> : '图'}
              </div>
              <div style={{ flex: 1 }}>
                <p style={{ fontSize: '14px', fontWeight: 500, color: 'var(--color-text-primary)' }}>{item.name}</p>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 4 }}>
                  <span style={{ fontSize: '14px', fontWeight: 600, color: 'var(--color-accent)' }}>¥{Number(item.price).toFixed(2)}</span>
                  <span style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>x{item.quantity}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Payment */}
      <div style={{ marginBottom: 'var(--spacing-xl)' }}>
        <PaymentSelector />
      </div>

      {/* Order Summary */}
      <div
        style={{
          padding: 'var(--spacing-md)',
          borderRadius: 'var(--radius-md)',
          background: 'var(--color-bg-secondary)',
          marginBottom: 'var(--spacing-xl)',
        }}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8, fontSize: '14px' }}>
          <span style={{ color: 'var(--color-text-secondary)' }}>商品小计</span>
          <span style={{ color: 'var(--color-text-primary)' }}>¥{totalAmount.toFixed(2)}</span>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8, fontSize: '14px' }}>
          <span style={{ color: 'var(--color-text-secondary)' }}>运费</span>
          <span style={{ color: 'var(--color-success)' }}>免运费</span>
        </div>
        <div style={{ borderTop: '1px solid var(--color-border-light)', marginTop: 8, paddingTop: 8, display: 'flex', justifyContent: 'space-between' }}>
          <span style={{ fontSize: '15px', fontWeight: 600, color: 'var(--color-text-primary)' }}>应付总额</span>
          <span style={{ fontSize: '22px', fontWeight: 700, color: 'var(--color-accent)' }}>¥{totalAmount.toFixed(2)}</span>
        </div>
      </div>

      {/* Submit */}
      <button
        onClick={handleSubmit}
        disabled={submitting || displayItems.length === 0 || !selectedAddr}
        style={{
          width: '100%',
          height: 52,
          borderRadius: 'var(--radius-sm)',
          background: submitting || displayItems.length === 0 || !selectedAddr ? 'var(--color-border)' : 'var(--color-accent)',
          color: '#fff',
          fontSize: '17px',
          fontWeight: 600,
          border: 'none',
          cursor: submitting || displayItems.length === 0 || !selectedAddr ? 'not-allowed' : 'pointer',
        }}
      >
        {submitting ? '提交中...' : `提交订单 ¥${totalAmount.toFixed(2)}`}
      </button>
    </div>
  );
};

export default CheckoutPage;