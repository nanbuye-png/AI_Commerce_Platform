import React from 'react';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
  loading?: boolean;
  icon?: React.ReactNode;
  fullWidth?: boolean;
}

const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  size = 'md',
  loading = false,
  icon,
  fullWidth = false,
  children,
  disabled,
  style,
  ...rest
}) => {
  const sizeMap = {
    sm: { height: 32, fontSize: 13, padding: '0 12px' },
    md: { height: 40, fontSize: 14, padding: '0 20px' },
    lg: { height: 48, fontSize: 16, padding: '0 28px' },
  };

  const variantStyles = {
    primary: {
      background: 'var(--color-accent)',
      color: '#fff',
      border: 'none',
    },
    secondary: {
      background: 'transparent',
      color: 'var(--color-accent)',
      border: '1px solid var(--color-accent)',
    },
    ghost: {
      background: 'transparent',
      color: 'var(--color-text-primary)',
      border: '1px solid var(--color-border)',
    },
  };

  const sizeStyle = sizeMap[size];
  const variantStyle = variantStyles[variant];

  return (
    <button
      disabled={disabled || loading}
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 6,
        height: sizeStyle.height,
        fontSize: sizeStyle.fontSize,
        padding: sizeStyle.padding,
        borderRadius: 'var(--radius-sm)',
        fontWeight: 500,
        cursor: disabled || loading ? 'not-allowed' : 'pointer',
        opacity: disabled ? 0.5 : 1,
        transition: 'all var(--transition-fast)',
        width: fullWidth ? '100%' : undefined,
        ...variantStyle,
        ...style,
      }}
      onMouseEnter={(e) => {
        if (!disabled && !loading) {
          if (variant === 'primary') {
            (e.target as HTMLElement).style.background = 'var(--color-accent-hover)';
          } else if (variant === 'secondary') {
            (e.target as HTMLElement).style.background = 'var(--color-accent-light)';
          }
        }
      }}
      onMouseLeave={(e) => {
        if (!disabled && !loading) {
          (e.target as HTMLElement).style.background = variantStyle.background;
        }
      }}
      {...rest}
    >
      {loading ? (
        <span style={{ display: 'inline-block', width: 16, height: 16, border: '2px solid currentColor', borderTopColor: 'transparent', borderRadius: '50%', animation: 'spin 0.6s linear infinite' }} />
      ) : icon ? (
        <span>{icon}</span>
      ) : null}
      {children}
    </button>
  );
};

export default Button;