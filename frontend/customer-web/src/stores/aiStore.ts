import { create } from 'zustand';
import type { AIMessage, AISendMessageOptions, AIConversationTurn } from '../services/ai/aiTypes';
import { aiClient } from '../services/ai/aiClient';
import { createUuid } from '../utils/uuid';
import { getToken } from '../utils/token';

interface AIState {
  messages: AIMessage[];
  sessionId: string | null;
  ownerId: string | null;
  loading: boolean;
  error: string | null;

  initializeSession: () => Promise<void>;
  sendMessage: (content: string, options?: AISendMessageOptions) => Promise<void>;
  cancelStream: () => void;
  clearConversation: () => void;
  setError: (error: string | null) => void;
}

let activeController: AbortController | null = null;

function currentOwnerId(): string | null {
  try {
    const payload = getToken()?.split('.')[1];
    const subject = payload && JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/'))).sub;
    return typeof subject === 'string' ? subject : null;
  } catch {
    return null;
  }
}

function storageKey(ownerId: string): string {
  return `customer_ai_conversation_${ownerId}`;
}

function saveConversation(state: AIState): void {
  if (!state.ownerId || !state.sessionId) return;
  try {
    sessionStorage.setItem(storageKey(state.ownerId), JSON.stringify({
      sessionId: state.sessionId,
      messages: state.messages.slice(-30),
    }));
  } catch {
    // Storage can be disabled or full; the in-memory conversation still works.
  }
}

const useAIStore = create<AIState>((set, get) => ({
  messages: [],
  sessionId: null,
  ownerId: null,
  loading: false,
  error: null,

  initializeSession: async () => {
    const ownerId = currentOwnerId();
    if (get().ownerId === ownerId && get().sessionId) return;
    activeController?.abort();
    activeController = null;
    try {
      let parsed: { sessionId?: string; messages?: AIMessage[] } | null = null;
      try {
        const saved = ownerId ? sessionStorage.getItem(storageKey(ownerId)) : null;
        parsed = saved ? JSON.parse(saved) : null;
      } catch {
        // Storage is optional; keep the conversation in memory for this page.
      }
      set({
        sessionId: typeof parsed?.sessionId === 'string' ? parsed.sessionId : createUuid(),
        messages: Array.isArray(parsed?.messages) ? parsed.messages.slice(-30) : [],
        ownerId,
        loading: false,
        error: null,
      });
    } catch {
      set({ error: '初始化 AI 会话失败' });
    }
  },

  sendMessage: async (content: string, options?: AISendMessageOptions) => {
    await get().initializeSession();
    const { loading, messages } = get();
    if (loading) return;

    const sessionId = get().sessionId;
    if (!sessionId) return;
    const history: AIConversationTurn[] = messages
      .filter((message): message is AIMessage & { role: 'user' | 'assistant' } =>
        (message.role === 'user' || message.role === 'assistant') && !!message.content.trim())
      .slice(-12)
      .map(({ role, content }) => ({ role, content: content.slice(0, 2000) }));

    const userMessage: AIMessage = {
      id: createUuid(),
      role: 'user',
      content,
      createdAt: new Date().toISOString(),
    };
    const assistantMessageId = createUuid();
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
            if (get().sessionId !== sessionId) return;
            set((state) => ({
              messages: state.messages.map((message) =>
                message.id === assistantMessageId
                  ? { ...message, content: message.content + token }
                  : message,
              ),
            }));
          },
          onMeta: (meta) => {
            if (get().sessionId !== sessionId) return;
            set((state) => ({
              messages: state.messages.map((message) => {
                if (message.id !== assistantMessageId) return message;
                if (meta.type === 'add_to_cart_confirmation' || meta.type === 'add_to_cart_result') {
                  return {
                    ...message,
                    metadata: {
                      ...message.metadata,
                      actionType: 'general_chat',
                      addToCart: {
                        type: meta.type,
                        skuId: meta.sku_id,
                        quantity: meta.quantity,
                        success: 'success' in meta ? meta.success : undefined,
                        message: 'message' in meta ? meta.message : undefined,
                      },
                    },
                  };
                }
                const productSearch = meta.type === 'product_search'
                  ? { query: meta.query, products: meta.products, total: meta.total }
                  : { products: [], total: 0, error: meta.message };
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
            if (get().sessionId !== sessionId) return;
            set((state) => ({
              sessionId: conversationId,
              messages: state.messages.map((message) =>
                message.id === assistantMessageId ? { ...message, id: messageId } : message,
              ),
            }));
            saveConversation(get());
          },
        },
        options,
        controller.signal,
        history,
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
      saveConversation(get());
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
    set({ messages: [], sessionId: createUuid(), loading: false, error: null });
    saveConversation(get());
  },

  setError: (error) => set({ error }),
}));

export default useAIStore;
