import React from 'react';

interface ProductSkeletonProps {
  variant?: 'card' | 'detail';
}

const shimmer = `
  @keyframes shimmer {
    0% { background-position: -200% 0; }
    100% { background-position: 200% 0; }
  }
`;

const SkeletonBar: React.FC<{ width?: string; height?: string; mb?: string }> = ({
  width = '100%',
  height = '14px',
  mb = '8px',
}) => (
  <div
    style={{
      width,
      height,
      borderRadius: '4px',
      background: 'linear-gradient(90deg, var(--color-bg-secondary) 25%, var(--color-bg-tertiary) 50%, var(--color-bg-secondary) 75%)',
      backgroundSize: '200% 100%',
      animation: 'shimmer 1.5s infinite',
      marginBottom: mb,
    }}
  />
);

const ProductSkeleton: React.FC<ProductSkeletonProps> = ({ variant = 'card' }) => {
  if (variant === 'detail') {
    return (
      <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 960, margin: '0 auto' }}>
        <style>{shimmer}</style>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--spacing-xl)' }}>
          <div>
            <div
              style={{
                aspectRatio: '1/1',
                borderRadius: 'var(--radius-md)',
                background: 'linear-gradient(90deg, var(--color-bg-secondary) 25%, var(--color-bg-tertiary) 50%, var(--color-bg-secondary) 75%)',
                backgroundSize: '200% 100%',
                animation: 'shimmer 1.5s infinite',
                marginBottom: 'var(--spacing-sm)',
              }}
            />
            <div style={{ display: 'flex', gap: 'var(--spacing-sm)' }}>
              {[1, 2, 3, 4].map((i) => (
                <div
                  key={i}
                  style={{
                    width: 64,
                    height: 64,
                    borderRadius: 'var(--radius-sm)',
                    background: 'linear-gradient(90deg, var(--color-bg-secondary) 25%, var(--color-bg-tertiary) 50%, var(--color-bg-secondary) 75%)',
                    backgroundSize: '200% 100%',
                    animation: 'shimmer 1.5s infinite',
                  }}
                />
              ))}
            </div>
          </div>
          <div>
            <SkeletonBar width="80%" height="24px" mb="12px" />
            <SkeletonBar width="60%" height="16px" mb="20px" />
            <SkeletonBar width="30%" height="28px" mb="24px" />
            <SkeletonBar width="100%" height="40px" mb="12px" />
            <SkeletonBar width="100%" height="40px" />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div
      style={{
        background: 'var(--color-bg-primary)',
        borderRadius: 'var(--radius-md)',
        overflow: 'hidden',
      }}
    >
      <style>{shimmer}</style>
      <div
        style={{
          aspectRatio: '1/1',
          background: 'linear-gradient(90deg, var(--color-bg-secondary) 25%, var(--color-bg-tertiary) 50%, var(--color-bg-secondary) 75%)',
          backgroundSize: '200% 100%',
          animation: 'shimmer 1.5s infinite',
        }}
      />
      <div style={{ padding: 'var(--spacing-sm) var(--spacing-md) var(--spacing-md)' }}>
        <SkeletonBar width="90%" height="14px" mb="6px" />
        <SkeletonBar width="50%" height="14px" mb="8px" />
        <SkeletonBar width="40%" height="18px" />
      </div>
    </div>
  );
};

export default React.memo(ProductSkeleton);