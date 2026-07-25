import React from 'react';

interface ProductPriceProps {
  price: number;
  originalPrice?: number;
  currency?: string;
  size?: 'sm' | 'md' | 'lg';
  showDiscount?: boolean;
}

const sizeMap = {
  sm: { current: '14px', original: '12px', discount: '11px' },
  md: { current: '18px', original: '14px', discount: '12px' },
  lg: { current: '28px', original: '16px', discount: '13px' },
};

const ProductPrice: React.FC<ProductPriceProps> = ({
  price,
  originalPrice,
  currency = '¥',
  size = 'md',
  showDiscount = true,
}) => {
  const s = sizeMap[size];
  const discount = originalPrice && originalPrice > price
    ? Math.round((1 - price / originalPrice) * 100)
    : null;

  return (
    <span style={{ display: 'inline-flex', alignItems: 'baseline', gap: '6px', flexWrap: 'wrap' }}>
      <span
        style={{
          fontSize: s.current,
          fontWeight: 700,
          color: 'var(--color-accent)',
          lineHeight: 1.2,
        }}
      >
        {currency}{price.toFixed(2)}
      </span>
      {originalPrice && originalPrice > price && (
        <span
          style={{
            fontSize: s.original,
            color: 'var(--color-text-tertiary)',
            textDecoration: 'line-through',
          }}
        >
          {currency}{originalPrice.toFixed(2)}
        </span>
      )}
      {discount !== null && showDiscount && (
        <span
          style={{
            fontSize: s.discount,
            color: '#fff',
            background: 'var(--color-promotion)',
            padding: '1px 6px',
            borderRadius: '4px',
            fontWeight: 600,
            lineHeight: 1.4,
          }}
        >
          -{discount}%
        </span>
      )}
    </span>
  );
};

export default React.memo(ProductPrice);