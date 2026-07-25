import React from 'react';
import type { AIMessage } from '../../../../services/ai/aiTypes';

interface MessageBubbleProps {
  message: AIMessage;
  onProductClick?: (productId: string) => void;
}

const MessageBubble: React.FC<MessageBubbleProps> = ({ message }) => {
  const isUser = message.role === 'user';

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: isUser ? 'flex-end' : 'flex-start',
        marginBottom: 'var(--spacing-md)',
      }}
    >
      {!isUser && (
        <div
          style={{
            width: 32,
            height: 32,
            borderRadius: '50%',
            background: 'linear-gradient(135deg, #0071E3, #5AC8FA)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#fff',
            fontSize: '12px',
            fontWeight: 700,
            marginRight: 8,
            flexShrink: 0,
            marginTop: 4,
          }}
        >
          AI
        </div>
      )}
      <div
        style={{
          maxWidth: '75%',
          padding: '10px 14px',
          borderRadius: isUser ? '16px 4px 16px 16px' : '4px 16px 16px 16px',
          background: isUser ? 'var(--color-accent)' : 'var(--color-bg-secondary)',
          color: isUser ? '#fff' : 'var(--color-text-primary)',
          fontSize: '14px',
          lineHeight: 1.5,
          wordBreak: 'break-word',
        }}
      >
        {message.content}
      </div>
      {isUser && (
        <div
          style={{
            width: 32,
            height: 32,
            borderRadius: '50%',
            background: 'var(--color-bg-secondary)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '14px',
            marginLeft: 8,
            flexShrink: 0,
            marginTop: 4,
          }}
        >
          👤
        </div>
      )}
    </div>
  );
};

export default React.memo(MessageBubble);