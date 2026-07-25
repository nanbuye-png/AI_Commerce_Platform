import React from 'react';

interface EmptyStateProps {
  icon?: string;
  title: string;
  description?: string;
  action?: React.ReactNode;
}

const EmptyState: React.FC<EmptyStateProps> = ({ icon = '📭', title, description, action }) => {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 'var(--spacing-3xl) var(--spacing-lg)',
        textAlign: 'center',
      }}
    >
      <div style={{ fontSize: '48px', marginBottom: 'var(--spacing-md)' }}>{icon}</div>
      <h3
        style={{
          fontSize: 'var(--font-size-h3)',
          fontWeight: 600,
          color: 'var(--color-text-primary)',
          marginBottom: 'var(--spacing-sm)',
        }}
      >
        {title}
      </h3>
      {description && (
        <p
          style={{
            fontSize: '14px',
            color: 'var(--color-text-secondary)',
            maxWidth: 320,
            marginBottom: action ? 'var(--spacing-lg)' : undefined,
          }}
        >
          {description}
        </p>
      )}
      {action && <div>{action}</div>}
    </div>
  );
};

export default EmptyState;