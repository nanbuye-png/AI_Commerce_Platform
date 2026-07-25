import React from 'react';

interface AuthLayoutProps {
  children?: React.ReactNode;
}

const AuthLayout: React.FC<AuthLayoutProps> = ({ children }) => {
  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        background: 'var(--color-bg-secondary)',
      }}
    >
      <div
        style={{
          width: '100%',
          maxWidth: 420,
          padding: 'var(--spacing-lg)',
        }}
      >
        <div style={{ textAlign: 'center', marginBottom: 'var(--spacing-xl)' }}>
          <h1
            style={{
              fontSize: 'var(--font-size-h1)',
              fontWeight: 700,
              color: 'var(--color-text-primary)',
              marginBottom: 'var(--spacing-sm)',
            }}
          >
            AI Commerce
          </h1>
          <p style={{ color: 'var(--color-text-secondary)', fontSize: 'var(--font-size-body)' }}>
            智能电商平台
          </p>
        </div>
        {children}
      </div>
    </div>
  );
};

export default AuthLayout;