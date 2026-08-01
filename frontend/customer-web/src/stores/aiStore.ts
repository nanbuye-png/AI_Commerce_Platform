import { create } from 'zustand';
import type { AIMessage } from '../services/ai/aiTypes';
import { aiClient } from '../services/ai/aiClient';

interface AIState {
  messages: AIMessage[];
  sessionId: string | null;
  loading: boolean;
  error: string | null;

  initializeSession: () => Promise<void>;
  sendMessage: (content: string) => Promise<void>;
  cancelStream: () => void;
  clearConversation: () => void;
  setError: (error: string | null) => void;
}

let activeController: AbortController | null = null;

const useAIStore = create<AIState>((set, get) => ({
  messages: [],
  sessionId: null,
  loading: false,
  error: null,

  initializeSession: async () => {
    try {
      const session = await aiClient.createSession();
      set({
        sessionId: session.id,
        messages: [],
        error: null,
      });
    } catch {
      set({ error: '初始化 AI 会话失败' });
    }
  },

  sendMessage: async (content: string) => {
    const { loading, messages } = get();
    if (loading) return;

    let { sessionId } = get();
    if (!sessionId) {
      const session = await aiClient.createSession();
      sessionId = session.id;
      set({ sessionId });
    }

    const userMessage: AIMessage = {
      id: crypto.randomUUID(),
      role: 'user',
      content,
      createdAt: new Date().toISOString(),
    };
    const assistantMessageId = crypto.randomUUID();
    const assistantMessage: AIMessage = {
      id: assistantMessageId,
      role: 'assistant',
      content: '',
      createdAt: new Date().toISOString(),
    };

    const controller = new AbortController();
    activeController = controller;
    set({ messages: [...messages, userMessage, assistantMessage], loading: true, error: null });

    try {
      await aiClient.sendMessage(
        sessionId,
        content,
        {
          onToken: (token) => {
            set((state) => ({
              messages: state.messages.map((message) =>
                message.id === assistantMessageId
                  ? { ...message, content: message.content + token }
                  : message,
              ),
            }));
          },
          onMeta: (meta) => {
            set((state) => ({
              messages: state.messages.map((message) => {
                if (message.id !== assistantMessageId) return message;
                const productSearch = meta.type === 'product_search'
                  ? {
                      query: meta.query,
                      products: meta.products,
                      total: meta.total,
                    }
                  : {
                      products: [],
                      total: 0,
                      error: meta.message,
                    };
                return {
                  ...message,
                  metadata: {
                    ...message.metadata,
                    actionType: 'product_search',
                    productSearch,
                  },
                };
              }),
            }));
          },
          onDone: ({ conversationId, messageId }) => {
            set((state) => ({
              sessionId: conversationId,
              messages: state.messages.map((message) =>
                message.id === assistantMessageId ? { ...message, id: messageId } : message,
              ),
            }));
          },
        },
        controller.signal,
      );
      set({ loading: false });
    } catch (error) {
      if (error instanceof DOMException && error.name === 'AbortError') {
        set({ loading: false });
      } else {
        set((state) => ({
          messages: state.messages.filter(
            (message) => message.id !== assistantMessageId || message.content.length > 0,
          ),
          loading: false,
          error: error instanceof Error ? error.message : 'AI 响应失败，请稍后重试',
        }));
      }
    } finally {
      if (activeController === controller) activeController = null;
    }
  },

  cancelStream: () => {
    activeController?.abort();
    activeController = null;
    set({ loading: false });
  },

  clearConversation: () => {
    activeController?.abort();
    activeController = null;
    set({ messages: [], sessionId: crypto.randomUUID(), loading: false, error: null });
  },

  setError: (error) => set({ error }),
}));

export default useAIStore;