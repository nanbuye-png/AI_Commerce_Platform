import React from 'react';
import { defaultQuickActions } from '../types/chat';
import type { QuickAction } from '../types/chat';

interface QuickActionsProps {
  onAction: (prompt: string) => void;
  actions?: QuickAction[];
}

const QuickActions: React.FC<QuickActionsProps> = ({ onAction, actions = defaultQuickActions }) => {
  return (
    <div
      style={{
        display: 'flex',
        gap: 'var(--spacing-sm)',
        flexWrap: 'wrap',
        padding: 'var(--spacing-md)',
        justifyContent: 'center',
      }}
    >
      {actions.map((action) => (
        <button
          key={action.id}
          onClick={() => onAction(action.prompt)}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 6,
            padding: '8px 14px',
            borderRadius: 'var(--radius-full)',
            border: '1px solid var(--color-border)',
            background: 'var(--color-bg-primary)',
            color: 'var(--color-text-primary)',
            fontSize: '13px',
            cursor: 'pointer',
            transition: 'all var(--transition-fast)',
          }}
          onMouseEnter={(e) => {
            (e.currentTarget as HTMLElement).style.borderColor = 'var(--color-accent)';
            (e.currentTarget as HTMLElement).style.background = 'var(--color-accent-light)';
          }}
          onMouseLeave={(e) => {
            (e.currentTarget as HTMLElement).style.borderColor = 'var(--color-border)';
            (e.currentTarget as HTMLElement).style.background = 'var(--color-bg-primary)';
          }}
        >
          <span>{action.icon}</span>
          <span>{action.label}</span>
        </button>
      ))}
    </div>
  );
};

export default QuickActions;