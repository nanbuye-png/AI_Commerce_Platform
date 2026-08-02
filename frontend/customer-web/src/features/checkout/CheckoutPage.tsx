import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import useCartStore from '../cart/store/cartStore';
import AddressSelector from './components/AddressSelector';
import PaymentSelector from './components/PaymentSelector';
import { cartService } from '../../services/cart';
import { orderService, type PaymentDetail } from '../../services/order';
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
  // 下单成功后的支付引导
  const [paymentGuide, setPaymentGuide] = useState<PaymentDetail | null>(null);
  const [paid, setPaid] = useState(false);
  const [payLoading, setPayLoading] = useState(false);
  const [payError, setPayError] = useState<string | null>(null);

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
      void navigate('/login');
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
        // 展示支付引导（自动生成的收款凭证）
        try {
          const detail = await orderService.paymentByOrder(result.orderNo);
          setPaymentGuide(detail);
        } catch {
          // 未生成凭证时直接进入订单列表
          void navigate('/orders', { replace: true });
        }
      } else {
        // ===== 购物车结算模式：统一走 POST /api/orders 创建订单（与"立即购买"完全一致）=====
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

        // 与"立即购买"共用同一接口：创建订单并自动接单、自动生成收款二维码
        const skuItems = checkedItems
          .filter((i) => i.skuId)
          .map((i) => ({ skuId: i.skuId as number, quantity: i.quantity }));
        const result = await orderService.createOrder({
          skuItems,
          addressId: selectedAddr,
        });
        console.info('购物车结算下单成功:', result.orderNo);

        // 从购物车移除已结算商品
        for (const item of checkedItems) {
          if (item.skuId) {
            await cartService.removeItem(item.skuId).catch(() => {});
          }
        }
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

        // 展示支付引导（与"立即购买"一致）
        try {
          const detail = await orderService.paymentByOrder(result.orderNo);
          setPaymentGuide(detail);
        } catch {
          void navigate('/orders', { replace: true });
        }
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

      {/* 支付引导弹窗：展示自动生成的收款凭证，用户可直接确认支付 */}
      {paymentGuide && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'rgba(0,0,0,0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
          }}
        >
          <div
            style={{
              background: '#fff',
              borderRadius: 12,
              padding: 24,
              width: 360,
              textAlign: 'center',
            }}
          >
            {paid ? (
              <>
                <div style={{ fontSize: 48, marginBottom: 12 }}>✅</div>
                <h2 style={{ fontSize: 16, fontWeight: 600, marginBottom: 8 }}>支付成功</h2>
                <button
                  onClick={() => navigate('/orders', { replace: true })}
                  style={{
                    marginTop: 16,
                    padding: '8px 24px',
                    borderRadius: 6,
                    background: 'var(--color-accent)',
                    color: '#fff',
                    border: 'none',
                    cursor: 'pointer',
                  }}
                >
                  查看订单
                </button>
              </>
            ) : (
              <>
                <h2 style={{ fontSize: 16, fontWeight: 600, marginBottom: 12 }}>订单已提交，请确认支付</h2>
                <div style={{ margin: '12px 0', fontSize: 14 }}>
                  <p style={{ color: 'var(--color-text-secondary)' }}>订单号</p>
                  <p style={{ fontWeight: 600, wordBreak: 'break-all' }}>{paymentGuide.orderNo}</p>
                  <p style={{ color: 'var(--color-text-secondary)', marginTop: 8 }}>应付金额</p>
                  <p style={{ fontSize: 24, fontWeight: 700, color: '#ff4d4f' }}>
                    ¥{Number(paymentGuide.amount).toFixed(2)}
                  </p>
                  <p style={{ color: 'var(--color-text-secondary)', marginTop: 8 }}>收款凭证</p>
                  <p
                    style={{
                      fontSize: 12,
                      fontFamily: 'monospace',
                      wordBreak: 'break-all',
                      background: 'var(--color-bg-secondary)',
                      padding: '6px 8px',
                      borderRadius: 6,
                      marginTop: 4,
                    }}
                  >
                    {paymentGuide.qrToken}
                  </p>
                </div>
                <p style={{ fontSize: 12, color: '#999', marginBottom: 12 }}>
                  商家已接单并生成收款码（自动）
                </p>
                {payError && (
                  <p style={{ fontSize: 13, color: '#FF3B30', marginBottom: 8 }}>{payError}</p>
                )}
                <div style={{ display: 'flex', gap: 10, justifyContent: 'center' }}>
                  <button
                    onClick={() => navigate('/orders', { replace: true })}
                    style={{
                      padding: '8px 20px',
                      borderRadius: 6,
                      border: '1px solid var(--color-border)',
                      background: 'transparent',
                      color: 'var(--color-text-secondary)',
                      cursor: 'pointer',
                    }}
                  >
                    稍后支付
                  </button>
                  <button
                    onClick={async () => {
                      setPayLoading(true);
                      setPayError(null);
                      try {
                        await orderService.payByToken(paymentGuide.qrToken);
                        setPaid(true);
                      } catch (e) {
                        console.error('支付失败:', e);
                        setPayError('支付失败，请重试或到订单列表再支付');
                      } finally {
                        setPayLoading(false);
                      }
                    }}
                    disabled={payLoading}
                    style={{
                      padding: '8px 24px',
                      borderRadius: 6,
                      background: 'var(--color-accent)',
                      color: '#fff',
                      border: 'none',
                      cursor: payLoading ? 'not-allowed' : 'pointer',
                      opacity: payLoading ? 0.6 : 1,
                    }}
                  >
                    {payLoading ? '支付中...' : '确认支付'}
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      )}

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