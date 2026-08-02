import React from 'react';

/** 结算页地址类型（由 CheckoutPage 传入真实数据） */
export interface CheckoutAddress {
  id: number;
  receiver: string;
  phone: string;
  province?: string;
  city?: string;
  district?: string;
  detailAddress?: string;
  isDefault?: boolean;
}

interface AddressSelectorProps {
  addresses: CheckoutAddress[];
  selectedId?: string;
  onSelect: (address: CheckoutAddress) => void;
  onAddNew: () => void;
}

const AddressSelector: React.FC<AddressSelectorProps> = ({ addresses, selectedId, onSelect, onAddNew }) => {
  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-md)' }}>
        <h3 style={{ fontSize: '16px', fontWeight: 600, color: 'var(--color-text-primary)' }}>收货地址</h3>
        <button
          onClick={onAddNew}
          style={{
            padding: '6px 14px',
            borderRadius: 'var(--radius-sm)',
            border: '1px solid var(--color-accent)',
            background: 'transparent',
            color: 'var(--color-accent)',
            fontSize: '13px',
            cursor: 'pointer',
          }}
        >
          + 新增地址
        </button>
      </div>
      {addresses.length === 0 ? (
        <div style={{ textAlign: 'center', padding: 'var(--spacing-xl)', color: 'var(--color-text-secondary)', fontSize: '14px' }}>
          暂无收货地址，请点击右上角添加
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-sm)' }}>
          {addresses.map((addr) => (
            <div
              key={addr.id}
              onClick={() => onSelect(addr)}
              style={{
                padding: 'var(--spacing-md)',
                borderRadius: 'var(--radius-md)',
                border: `2px solid ${selectedId === String(addr.id) ? 'var(--color-accent)' : 'var(--color-border-light)'}`,
                background: selectedId === String(addr.id) ? 'var(--color-accent-light)' : 'var(--color-bg-primary)',
                cursor: 'pointer',
                transition: 'all var(--transition-fast)',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                <span style={{ fontSize: '14px', fontWeight: 600, color: 'var(--color-text-primary)' }}>{addr.receiver}</span>
                <span style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>{addr.phone}</span>
                {addr.isDefault && (
                  <span style={{ fontSize: '11px', padding: '1px 6px', borderRadius: '3px', background: 'var(--color-accent)', color: '#fff' }}>
                    默认
                  </span>
                )}
              </div>
              <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>
                {[addr.province, addr.city, addr.district, addr.detailAddress].filter(Boolean).join(' ')}
              </p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default AddressSelector;