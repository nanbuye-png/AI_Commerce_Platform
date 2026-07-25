import React from 'react';

interface Address {
  id: string;
  name: string;
  phone: string;
  province: string;
  city: string;
  district: string;
  detail: string;
  isDefault?: boolean;
}

interface AddressSelectorProps {
  selectedId?: string;
  onSelect: (address: Address) => void;
  onAddNew: () => void;
}

const demoAddresses: Address[] = [
  {
    id: '1',
    name: '张三',
    phone: '138****1234',
    province: '广东省',
    city: '深圳市',
    district: '南山区',
    detail: '科技园南区A栋1001',
    isDefault: true,
  },
  {
    id: '2',
    name: '李四',
    phone: '139****5678',
    province: '广东省',
    city: '广州市',
    district: '天河区',
    detail: '珠江新城B座2002',
  },
];

const AddressSelector: React.FC<AddressSelectorProps> = ({ selectedId, onSelect, onAddNew }) => {
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
      <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-sm)' }}>
        {demoAddresses.map((addr) => (
          <div
            key={addr.id}
            onClick={() => onSelect(addr)}
            style={{
              padding: 'var(--spacing-md)',
              borderRadius: 'var(--radius-md)',
              border: `2px solid ${selectedId === addr.id ? 'var(--color-accent)' : 'var(--color-border-light)'}`,
              background: selectedId === addr.id ? 'var(--color-accent-light)' : 'var(--color-bg-primary)',
              cursor: 'pointer',
              transition: 'all var(--transition-fast)',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
              <span style={{ fontSize: '14px', fontWeight: 600, color: 'var(--color-text-primary)' }}>{addr.name}</span>
              <span style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>{addr.phone}</span>
              {addr.isDefault && (
                <span style={{ fontSize: '11px', padding: '1px 6px', borderRadius: '3px', background: 'var(--color-accent)', color: '#fff' }}>
                  默认
                </span>
              )}
            </div>
            <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)' }}>
              {addr.province}{addr.city}{addr.district} {addr.detail}
            </p>
          </div>
        ))}
      </div>
    </div>
  );
};

export default AddressSelector;