import React from 'react';

interface LoadingProps {
  size?: 'sm' | 'md' | 'lg';
  text?: string;
  fullScreen?: boolean;
}

const sizeMap = {
  sm: 20,
  md: 32,
  lg: 48,
};

const Loading: React.FC<LoadingProps> = ({ size = 'md', text, fullScreen = false }) => {
  const dim = sizeMap[size];

  const spinner = (
    <div
      style={{
        width: dim,
        height: dim,
        border: `3px solid var(--color-border-light)`,
        borderTopColor: 'var(--color-accent)',
        borderRadius: '50%',
        animation: 'spin 0.8s linear infinite',
      }}
    />
  );

  if (fullScreen) {
    return (
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '60vh',
          gap: 'var(--spacing-md)',
        }}
      >
        {spinner}
        {text && <p style={{ color: 'var(--color-text-secondary)', fontSize: '14px' }}>{text}</p>}
        <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--spacing-sm)', justifyContent: 'center', padding: 'var(--spacing-md)' }}>
      {spinner}
      {text && <span style={{ color: 'var(--color-text-secondary)', fontSize: '14px' }}>{text}</span>}
      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
    </div>
  );
};

export default Loading;