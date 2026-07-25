import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import useCartStore from '../cart/store/cartStore';
import AddressSelector from './components/AddressSelector';
import PaymentSelector from './components/PaymentSelector';

const CheckoutPage: React.FC = () => {
  const navigate = useNavigate();
  const { items, getSummary } = useCartStore();
  const summary = getSummary();
  const [selectedAddr, setSelectedAddr] = useState('1');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = () => {
    setSubmitting(true);
    // Mock submission
    setTimeout(() => {
      setSubmitting(false);
      navigate('/orders', { replace: true });
    }, 1500);
  };

  const checkedItems = items.filter((i) => i.checked);

  return (
    <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 800, margin: '0 auto' }}>
      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-xl)' }}>
        确认订单
      </h1>

      {/* Address */}
      <div style={{ marginBottom: 'var(--spacing-xl)' }}>
        <AddressSelector
          selectedId={selectedAddr}
          onSelect={(a) => setSelectedAddr(a.id)}
          onAddNew={() => {}}
        />
      </div>

      {/* Order Items */}
      <div style={{ marginBottom: 'var(--spacing-xl)' }}>
        <h3 style={{ fontSize: '16px', fontWeight: 600, color: 'var(--color-text-primary)', marginBottom: 'var(--spacing-md)' }}>
          商品清单
        </h3>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-sm)' }}>
          {checkedItems.map((item) => (
            <div
              key={`${item.productId}-${item.specInfo}`}
              style={{
                display: 'flex',
                gap: 'var(--spacing-md)',
                padding: 'var(--spacing-sm) var(--spacing-md)',
                background: 'var(--color-bg-primary)',
                borderRadius: 'var(--radius-md)',
              }}
            >
              <div style={{ width: 60, height: 60, borderRadius: 'var(--radius-sm)', background: 'var(--color-bg-secondary)', flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '10px', color: 'var(--color-text-tertiary)' }}>
                {item.thumbnail ? <img src={item.thumbnail} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} /> : '图'}
              </div>
              <div style={{ flex: 1 }}>
                <p style={{ fontSize: '14px', fontWeight: 500, color: 'var(--color-text-primary)' }}>{item.name}</p>
                {item.specInfo && <p style={{ fontSize: '12px', color: 'var(--color-text-tertiary)' }}>{item.specInfo}</p>}
                <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 4 }}>
                  <span style={{ fontSize: '14px', fontWeight: 600, color: 'var(--color-accent)' }}>¥{item.price.toFixed(2)}</span>
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
          <span style={{ color: 'var(--color-text-primary)' }}>¥{summary.checkedAmount.toFixed(2)}</span>
        </div>
        {summary.discount > 0 && (
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8, fontSize: '14px' }}>
            <span style={{ color: 'var(--color-text-secondary)' }}>优惠</span>
            <span style={{ color: 'var(--color-promotion)' }}>-¥{summary.discount.toFixed(2)}</span>
          </div>
        )}
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8, fontSize: '14px' }}>
          <span style={{ color: 'var(--color-text-secondary)' }}>运费</span>
          <span style={{ color: 'var(--color-success)' }}>免运费</span>
        </div>
        <div style={{ borderTop: '1px solid var(--color-border-light)', marginTop: 8, paddingTop: 8, display: 'flex', justifyContent: 'space-between' }}>
          <span style={{ fontSize: '15px', fontWeight: 600, color: 'var(--color-text-primary)' }}>应付总额</span>
          <span style={{ fontSize: '22px', fontWeight: 700, color: 'var(--color-accent)' }}>¥{summary.checkedAmount.toFixed(2)}</span>
        </div>
      </div>

      {/* Submit */}
      <button
        onClick={handleSubmit}
        disabled={submitting || summary.checkedCount === 0}
        style={{
          width: '100%',
          height: 52,
          borderRadius: 'var(--radius-sm)',
          background: submitting || summary.checkedCount === 0 ? 'var(--color-border)' : 'var(--color-accent)',
          color: '#fff',
          fontSize: '17px',
          fontWeight: 600,
          border: 'none',
          cursor: submitting || summary.checkedCount === 0 ? 'not-allowed' : 'pointer',
        }}
      >
        {submitting ? '提交中...' : `提交订单 ¥${summary.checkedAmount.toFixed(2)}`}
      </button>
    </div>
  );
};

export default CheckoutPage;