import React from 'react';

type BadgeType = 'new' | 'hot' | 'sale' | 'recommended' | 'custom';

interface ProductBadgeProps {
  type?: BadgeType;
  text?: string;
  size?: 'sm' | 'md';
}

const badgeDefaults: Record<BadgeType, { text: string; color: string }> = {
  new: { text: '新品', color: '#34C759' },
  hot: { text: '热卖', color: '#FF453A' },
  sale: { text: '促销', color: '#FF9F0A' },
  recommended: { text: '推荐', color: '#0071E3' },
  custom: { text: '', color: '#86868B' },
};

const sizeStyles = {
  sm: { fontSize: '10px', padding: '1px 5px', borderRadius: '3px' },
  md: { fontSize: '11px', padding: '2px 7px', borderRadius: '4px' },
};

const ProductBadge: React.FC<ProductBadgeProps> = ({ type = 'custom', text, size = 'sm' }) => {
  const def = badgeDefaults[type];
  const displayText = text || def.text;
  const s = sizeStyles[size];

  return (
    <span
      style={{
        display: 'inline-block',
        fontSize: s.fontSize,
        fontWeight: 600,
        padding: s.padding,
        borderRadius: s.borderRadius,
        background: def.color,
        color: '#fff',
        lineHeight: 1.3,
        letterSpacing: '0.3px',
      }}
    >
      {displayText}
    </span>
  );
};

export default React.memo(ProductBadge);