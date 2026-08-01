import React, { useEffect, useRef } from 'react';
import useAIStore from '../../../../stores/aiStore';
import MessageBubble from './MessageBubble';
import QuickActions from './QuickActions';

interface ChatPanelProps {
  showQuickActions?: boolean;
  placeholder?: string;
}

const ChatPanel: React.FC<ChatPanelProps> = ({ showQuickActions = true, placeholder = '输入你想了解的商品信息...' }) => {
  const { messages, loading, error, sendMessage, cancelStream, initializeSession } = useAIStore();
  const [input, setInput] = React.useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    void initializeSession();
  }, [initializeSession]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSend = (text: string) => {
    const msg = text.trim();
    if (!msg || loading) return;
    void sendMessage(msg);
    setInput('');
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend(input);
    }
  };

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        height: '100%',
        minHeight: 500,
        borderRadius: 'var(--radius-lg)',
        overflow: 'hidden',
        border: '1px solid var(--color-border-light)',
        background: 'var(--color-bg-primary)',
      }}
    >
      {/* Header */}
      <div
        style={{
          padding: 'var(--spacing-md) var(--spacing-lg)',
          borderBottom: '1px solid var(--color-border-light)',
          background: 'var(--color-bg-secondary)',
          display: 'flex',
          alignItems: 'center',
          gap: 'var(--spacing-sm)',
        }}
      >
        <div
          style={{
            width: 36,
            height: 36,
            borderRadius: '50%',
            background: 'linear-gradient(135deg, #0071E3, #5AC8FA)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#fff',
            fontSize: '14px',
            fontWeight: 700,
          }}
        >
          AI
        </div>
        <div>
          <h3 style={{ fontSize: '15px', fontWeight: 600, color: 'var(--color-text-primary)' }}>
            AI 购物助手
          </h3>
          <p style={{ fontSize: '12px', color: 'var(--color-text-tertiary)' }}>
            24 小时在线，随时为您服务
          </p>
        </div>
      </div>

      {/* Messages */}
      <div
        style={{
          flex: 1,
          overflow: 'auto',
          padding: 'var(--spacing-md) var(--spacing-lg)',
          background: 'var(--color-bg-primary)',
        }}
      >
        {messages.length === 0 && (
          <div
            style={{
              textAlign: 'center',
              padding: 'var(--spacing-2xl) var(--spacing-lg)',
              color: 'var(--color-text-tertiary)',
            }}
          >
            <div style={{ fontSize: '48px', marginBottom: 'var(--spacing-md)' }}>🤖</div>
            <p style={{ fontSize: '14px', marginBottom: 'var(--spacing-sm)' }}>
              您好！我是 AI 购物助手
            </p>
            <p style={{ fontSize: '13px' }}>
              您可以问我任何关于商品的问题，我会尽力帮您解答
            </p>
          </div>
        )}
        {messages.map((msg) => (
          <MessageBubble key={msg.id} message={msg} />
        ))}

        {/* Loading indicator */}
        {loading && (
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '8px 0' }}>
            <div
              style={{
                width: 28,
                height: 28,
                borderRadius: '50%',
                background: 'linear-gradient(135deg, #0071E3, #5AC8FA)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#fff',
                fontSize: '10px',
                fontWeight: 700,
              }}
            >
              AI
            </div>
            <div style={{ display: 'flex', gap: 4 }}>
              {[0, 1, 2].map((i) => (
                <div
                  key={i}
                  style={{
                    width: 8,
                    height: 8,
                    borderRadius: '50%',
                    background: 'var(--color-accent)',
                    opacity: 0.4,
                    animation: `typing 1.4s ${i * 0.2}s infinite`,
                  }}
                />
              ))}
            </div>
            <style>{`
              @keyframes typing {
                0%, 60%, 100% { opacity: 0.4; transform: scale(1); }
                30% { opacity: 1; transform: scale(1.2); }
              }
            `}</style>
          </div>
        )}

        {error && (
          <p role="alert" style={{ color: 'var(--color-error)', fontSize: 13, padding: '8px 0' }}>
            {error}
          </p>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Quick Actions */}
      {showQuickActions && messages.length === 0 && (
        <div style={{ borderTop: '1px solid var(--color-border-light)' }}>
          <QuickActions onAction={(prompt) => handleSend(prompt)} />
        </div>
      )}

      {/* Input */}
      <div
        style={{
          padding: 'var(--spacing-sm) var(--spacing-lg)',
          borderTop: '1px solid var(--color-border-light)',
          background: 'var(--color-bg-primary)',
        }}
      >
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 'var(--spacing-sm)',
            borderRadius: 'var(--radius-full)',
            border: '1px solid var(--color-border)',
            padding: '4px 4px 4px 16px',
            background: 'var(--color-bg-secondary)',
          }}
        >
          <input
            ref={inputRef}
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder={placeholder}
            disabled={loading}
            style={{
              flex: 1,
              border: 'none',
              outline: 'none',
              background: 'transparent',
              fontSize: '14px',
              color: 'var(--color-text-primary)',
              height: 36,
            }}
          />
          <button
            onClick={() => (loading ? cancelStream() : handleSend(input))}
            disabled={!loading && !input.trim()}
            aria-label={loading ? '停止生成' : '发送消息'}
            title={loading ? '停止生成' : '发送消息'}
            style={{
              width: 36,
              height: 36,
              borderRadius: '50%',
              background: loading || input.trim() ? 'var(--color-accent)' : 'var(--color-border)',
              color: '#fff',
              border: 'none',
              cursor: loading || input.trim() ? 'pointer' : 'not-allowed',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '16px',
              transition: 'background var(--transition-fast)',
            }}
          >
            {loading ? '■' : '↑'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ChatPanel;