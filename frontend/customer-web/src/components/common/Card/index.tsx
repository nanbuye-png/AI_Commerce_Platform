import React from 'react';

interface CardProps {
  children: React.ReactNode;
  padding?: string;
  shadow?: 'sm' | 'md' | 'lg';
  radius?: 'sm' | 'md' | 'lg';
  onClick?: () => void;
  style?: React.CSSProperties;
  className?: string;
}

const shadowMap = {
  sm: 'var(--shadow-sm)',
  md: 'var(--shadow-md)',
  lg: 'var(--shadow-lg)',
};

const radiusMap = {
  sm: 'var(--radius-sm)',
  md: 'var(--radius-md)',
  lg: 'var(--radius-lg)',
};

const Card: React.FC<CardProps> = ({
  children,
  padding = 'var(--spacing-md)',
  shadow = 'sm',
  radius = 'md',
  onClick,
  style,
}) => {
  return (
    <div
      onClick={onClick}
      style={{
        background: 'var(--color-bg-primary)',
        borderRadius: radiusMap[radius],
        boxShadow: shadowMap[shadow],
        padding,
        cursor: onClick ? 'pointer' : undefined,
        transition: 'box-shadow var(--transition-fast), transform var(--transition-fast)',
        ...style,
      }}
      onMouseEnter={(e) => {
        if (onClick) {
          (e.currentTarget as HTMLElement).style.boxShadow = 'var(--shadow-md)';
          (e.currentTarget as HTMLElement).style.transform = 'translateY(-2px)';
        }
      }}
      onMouseLeave={(e) => {
        if (onClick) {
          (e.currentTarget as HTMLElement).style.boxShadow = shadowMap[shadow];
          (e.currentTarget as HTMLElement).style.transform = 'translateY(0)';
        }
      }}
    >
      {children}
    </div>
  );
};

export default Card;