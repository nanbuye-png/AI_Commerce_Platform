import React from 'react';

interface InputProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'prefix'> {
  label?: string;
  error?: string;
  prefix?: React.ReactNode;
  suffix?: React.ReactNode;
  fullWidth?: boolean;
}

const Input: React.FC<InputProps> = ({
  label,
  error,
  prefix,
  suffix,
  fullWidth = true,
  style,
  ...rest
}) => {
  return (
    <div style={{ width: fullWidth ? '100%' : undefined }}>
      {label && (
        <label
          style={{
            display: 'block',
            fontSize: '14px',
            fontWeight: 500,
            color: 'var(--color-text-primary)',
            marginBottom: '6px',
          }}
        >
          {label}
        </label>
      )}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          height: 40,
          borderRadius: 'var(--radius-sm)',
          border: `1px solid ${error ? 'var(--color-error)' : 'var(--color-border)'}`,
          background: 'var(--color-bg-primary)',
          padding: '0 12px',
          transition: 'border-color var(--transition-fast)',
        }}
      >
        {prefix && <span style={{ marginRight: 8, color: 'var(--color-text-tertiary)' }}>{prefix}</span>}
        <input
          style={{
            flex: 1,
            border: 'none',
            outline: 'none',
            background: 'transparent',
            fontSize: '14px',
            color: 'var(--color-text-primary)',
            width: '100%',
            height: '100%',
            ...style,
          }}
          {...rest}
        />
        {suffix && <span style={{ marginLeft: 8, color: 'var(--color-text-tertiary)' }}>{suffix}</span>}
      </div>
      {error && (
        <p style={{ fontSize: '12px', color: 'var(--color-error)', marginTop: '4px' }}>{error}</p>
      )}
    </div>
  );
};

export default Input;