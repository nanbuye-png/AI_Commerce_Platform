import React from 'react';
import ChatPanel from './assistant/components/ChatPanel';

const AIPage: React.FC = () => {
  return (
    <div style={{ padding: 'var(--spacing-xl) var(--spacing-lg)', maxWidth: 800, margin: '0 auto' }}>
      <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, marginBottom: 'var(--spacing-lg)', display: 'flex', alignItems: 'center', gap: 'var(--spacing-sm)' }}>
        <span style={{ background: 'linear-gradient(135deg, #0071E3, #5AC8FA)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
          AI 智能助手
        </span>
      </h1>
      <ChatPanel />
    </div>
  );
};

export default AIPage;