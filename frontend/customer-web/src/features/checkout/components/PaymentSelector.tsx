import React, { useState } from 'react';

interface PaymentMethod {
  id: string;
  name: string;
  icon: string;
  description?: string;
}

const paymentMethods: PaymentMethod[] = [
  { id: 'wechat', name: '微信支付', icon: '💚', description: '推荐微信用户使用' },
  { id: 'alipay', name: '支付宝', icon: '💙', description: '支持花呗、余额宝' },
  { id: 'card', name: '银行卡', icon: '💳', description: '支持各大银行储蓄卡/信用卡' },
];

const PaymentSelector: React.FC = () => {
  const [selected, setSelected] = useState('wechat');

  return (
    <div>
      <h3 style={{ fontSize: '16px', fontWeight: 600, color: 'var(--color-text-primary)', marginBottom: 'var(--spacing-md)' }}>
        支付方式
      </h3>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-sm)' }}>
        {paymentMethods.map((method) => (
          <div
            key={method.id}
            onClick={() => setSelected(method.id)}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 'var(--spacing-md)',
              padding: 'var(--spacing-md)',
              borderRadius: 'var(--radius-md)',
              border: `2px solid ${selected === method.id ? 'var(--color-accent)' : 'var(--color-border-light)'}`,
              background: selected === method.id ? 'var(--color-accent-light)' : 'var(--color-bg-primary)',
              cursor: 'pointer',
              transition: 'all var(--transition-fast)',
            }}
          >
            <div
              style={{
                width: 22,
                height: 22,
                borderRadius: '50%',
                border: `2px solid ${selected === method.id ? 'var(--color-accent)' : 'var(--color-border)'}`,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexShrink: 0,
              }}
            >
              {selected === method.id && (
                <div style={{ width: 12, height: 12, borderRadius: '50%', background: 'var(--color-accent)' }} />
              )}
            </div>
            <span style={{ fontSize: '20px' }}>{method.icon}</span>
            <div>
              <p style={{ fontSize: '14px', fontWeight: 500, color: 'var(--color-text-primary)' }}>{method.name}</p>
              {method.description && (
                <p style={{ fontSize: '12px', color: 'var(--color-text-tertiary)' }}>{method.description}</p>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default PaymentSelector;