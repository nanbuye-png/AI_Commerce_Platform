import React from 'react';

export interface Address {
  id: string;
  name: string;
  phone: string;
  region: string;
  detail: string;
  isDefault?: boolean;
}

interface AddressCardProps {
  address: Address;
  onEdit?: (id: string) => void;
  onDelete?: (id: string) => void;
  onSetDefault?: (id: string) => void;
}

const AddressCard: React.FC<AddressCardProps> = ({ address, onEdit, onDelete, onSetDefault }) => {
  return (
    <div
      style={{
        padding: 'var(--spacing-md)',
        borderRadius: 'var(--radius-md)',
        background: 'var(--color-bg-primary)',
        boxShadow: 'var(--shadow-sm)',
        marginBottom: 'var(--spacing-sm)',
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 'var(--spacing-xs)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <span style={{ fontSize: '15px', fontWeight: 600, color: 'var(--color-text-primary)' }}>{address.name}</span>
          <span style={{ fontSize: '14px', color: 'var(--color-text-secondary)' }}>{address.phone}</span>
          {address.isDefault && (
            <span style={{ fontSize: '11px', padding: '1px 6px', borderRadius: '3px', background: 'var(--color-accent)', color: '#fff' }}>
              默认
            </span>
          )}
        </div>
      </div>
      <p style={{ fontSize: '13px', color: 'var(--color-text-secondary)', marginBottom: 'var(--spacing-sm)' }}>
        {address.region} {address.detail}
      </p>
      <div style={{ display: 'flex', gap: 'var(--spacing-md)' }}>
        {!address.isDefault && onSetDefault && (
          <button onClick={() => onSetDefault(address.id)} style={{ fontSize: '13px', color: 'var(--color-accent)', background: 'none', border: 'none', cursor: 'pointer' }}>
            设为默认
          </button>
        )}
        {onEdit && (
          <button onClick={() => onEdit(address.id)} style={{ fontSize: '13px', color: 'var(--color-text-secondary)', background: 'none', border: 'none', cursor: 'pointer' }}>
            编辑
          </button>
        )}
        {onDelete && (
          <button onClick={() => onDelete(address.id)} style={{ fontSize: '13px', color: 'var(--color-error)', background: 'none', border: 'none', cursor: 'pointer' }}>
            删除
          </button>
        )}
      </div>
    </div>
  );
};

export default AddressCard;