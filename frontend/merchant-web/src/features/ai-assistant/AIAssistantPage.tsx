import React, { useEffect, useRef, useState } from 'react';
import { merchantAiApi, type MerchantAIMeta } from '../../api/ai';

interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  meta?: MerchantAIMeta;
  streaming?: boolean;
}

const presetQuestions = [
  '帮我优化商品标题，提升搜索曝光',
  '推荐几个热门关键词',
  '如何提高商品转化率',
  '帮我分析定价策略',
];

const AIAssistantPage: React.FC = () => {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [sending, setSending] = useState(false);
  const [conversationId, setConversationId] = useState<string | undefined>(undefined);
  const abortRef = useRef<AbortController | null>(null);
  const bottomRef = useRef<HTMLDivElement | null>(null);

  // 初始欢迎消息
  useEffect(() => {
    setMessages([{
      id: 'welcome',
      role: 'assistant',
      content: '你好！我是 AI 商品助手，可以帮你优化商品标题、推荐关键词、分析定价策略、提升转化率。有什么可以帮你的？',
    }]);
  }, []);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const sendMessage = async (text?: string) => {
    const content = (text ?? input).trim();
    if (!content || sending) return;

    setInput('');
    setSending(true);

    const userMsg: ChatMessage = { id: `u_${Date.now()}`, role: 'user', content };
    const assistantMsg: ChatMessage = { id: `a_${Date.now()}`, role: 'assistant', content: '', streaming: true };
    setMessages((prev) => [...prev, userMsg, assistantMsg]);

    const controller = new AbortController();
    abortRef.current = controller;

    try {
      await merchantAiApi.streamChat(content, conversationId, {
        onToken: (token) => {
          setMessages((prev) => prev.map((m) =>
            m.id === assistantMsg.id ? { ...m, content: m.content + token } : m,
          ));
        },
        onMeta: (meta) => {
          setMessages((prev) => prev.map((m) =>
            m.id === assistantMsg.id ? { ...m, meta } : m,
          ));
        },
        onDone: (result) => {
          setConversationId(result.conversationId);
          setMessages((prev) => prev.map((m) =>
            m.id === assistantMsg.id ? { ...m, streaming: false } : m,
          ));
        },
      }, controller.signal);
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : 'AI 请求失败，请稍后重试';
      setMessages((prev) => prev.map((m) =>
        m.id === assistantMsg.id
          ? { ...m, content: m.content || errorMsg, streaming: false }
          : m,
      ));
    } finally {
      abortRef.current = null;
      setSending(false);
    }
  };

  const stopGenerating = () => {
    abortRef.current?.abort();
    setMessages((prev) => prev.map((m) =>
      m.streaming ? { ...m, streaming: false } : m,
    ));
    setSending(false);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      void sendMessage();
    }
  };

  return (
    <div style={{ maxWidth: 900, margin: '0 auto', display: 'flex', flexDirection: 'column', height: 'calc(100vh - 140px)' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-lg)' }}>
        <h1 style={{ fontSize: 'var(--font-size-h1)', fontWeight: 600, display: 'flex', alignItems: 'center', gap: 8, margin: 0 }}>
          <span style={{ background: 'linear-gradient(135deg, #0071E3, #5AC8FA)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>AI 商品助手</span>
        </h1>
        {messages.length > 1 && (
          <button
            onClick={() => {
              abortRef.current?.abort();
              setMessages([messages[0]]);
              setConversationId(undefined);
              setSending(false);
            }}
            style={{ padding: '6px 14px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: 'var(--color-text-secondary)', fontSize: '13px', cursor: 'pointer' }}
          >
            清空对话
          </button>
        )}
      </div>

      {/* 对话区 */}
      <div style={{ flex: 1, overflowY: 'auto', background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', padding: 'var(--spacing-lg)', marginBottom: 'var(--spacing-md)' }}>
        {messages.map((msg) => (
          <div key={msg.id} style={{ display: 'flex', justifyContent: msg.role === 'user' ? 'flex-end' : 'flex-start', marginBottom: 'var(--spacing-md)' }}>
            <div
              style={{
                maxWidth: '80%',
                padding: '10px 16px',
                borderRadius: 'var(--radius-md)',
                fontSize: '14px',
                lineHeight: 1.6,
                whiteSpace: 'pre-wrap',
                wordBreak: 'break-word',
                background: msg.role === 'user' ? 'var(--color-accent)' : 'var(--color-bg-secondary)',
                color: msg.role === 'user' ? '#fff' : 'var(--color-text-primary)',
              }}
            >
              {msg.content || (msg.streaming ? '思考中...' : '')}
              {msg.streaming && <span style={{ opacity: 0.6 }}>▍</span>}
              {msg.meta && msg.meta.type === 'product_search' && (
                <div style={{ marginTop: 8, padding: '8px 12px', borderRadius: 'var(--radius-sm)', background: 'rgba(0,113,227,0.06)', fontSize: '12px', color: 'var(--color-text-secondary)' }}>
                  🔍 已检索到 {msg.meta.total ?? 0} 件相关商品
                </div>
              )}
              {msg.meta && msg.meta.type === 'product_search_error' && (
                <div style={{ marginTop: 8, padding: '8px 12px', borderRadius: 'var(--radius-sm)', background: 'rgba(255,59,48,0.06)', fontSize: '12px', color: '#FF3B30' }}>
                  {msg.meta.message || '商品搜索暂时不可用'}
                </div>
              )}
            </div>
          </div>
        ))}
        <div ref={bottomRef} />
      </div>

      {/* 预设问题 */}
      {messages.length <= 1 && (
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 'var(--spacing-md)' }}>
          {presetQuestions.map((q) => (
            <button
              key={q}
              onClick={() => void sendMessage(q)}
              disabled={sending}
              style={{ padding: '6px 14px', borderRadius: 'var(--radius-md)', border: '1px solid var(--color-border)', background: 'var(--color-bg-primary)', color: 'var(--color-text-secondary)', fontSize: '13px', cursor: 'pointer', transition: 'all var(--transition-fast)' }}
              onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.borderColor = 'var(--color-accent)'; (e.currentTarget as HTMLElement).style.color = 'var(--color-accent)'; }}
              onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.borderColor = 'var(--color-border)'; (e.currentTarget as HTMLElement).style.color = 'var(--color-text-secondary)'; }}
            >
              {q}
            </button>
          ))}
        </div>
      )}

      {/* 输入区 */}
      <div style={{ background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)', padding: 'var(--spacing-md)' }}>
        <textarea
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="输入你的问题，例如：帮我优化商品标题..."
          rows={2}
          disabled={sending}
          style={{ width: '100%', border: 'none', outline: 'none', resize: 'none', fontSize: '14px', lineHeight: 1.5, background: 'transparent', color: 'var(--color-text-primary)', fontFamily: 'inherit' }}
        />
        <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', gap: 8, marginTop: 8 }}>
          <span style={{ fontSize: '12px', color: 'var(--color-text-tertiary)' }}>Enter 发送，Shift+Enter 换行</span>
          {sending && (
            <button
              onClick={stopGenerating}
              style={{ padding: '8px 18px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-border)', background: 'transparent', color: 'var(--color-text-secondary)', fontSize: '14px', cursor: 'pointer' }}
            >
              停止生成
            </button>
          )}
          <button
            onClick={() => void sendMessage()}
            disabled={sending || !input.trim()}
            style={{ padding: '8px 24px', borderRadius: 'var(--radius-sm)', border: 'none', background: sending || !input.trim() ? 'var(--color-bg-secondary)' : 'var(--color-accent)', color: sending || !input.trim() ? 'var(--color-text-tertiary)' : '#fff', fontSize: '14px', cursor: sending || !input.trim() ? 'not-allowed' : 'pointer', fontWeight: 500 }}
          >
            {sending ? '生成中...' : '发送'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default AIAssistantPage;