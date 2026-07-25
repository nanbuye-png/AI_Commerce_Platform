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
  clearConversation: () => void;
  setError: (error: string | null) => void;
}

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
    } catch (err) {
      set({ error: '初始化 AI 会话失败' });
    }
  },

  sendMessage: async (content: string) => {
    const { sessionId, messages } = get();
    if (!sessionId) return;

    const userMessage: AIMessage = {
      id: crypto.randomUUID(),
      role: 'user',
      content,
      createdAt: new Date().toISOString(),
    };

    set({ messages: [...messages, userMessage], loading: true, error: null });

    try {
      const response = await aiClient.sendMessage(sessionId, content);
      set((state) => ({
        messages: [...state.messages, response.message],
        loading: false,
      }));
    } catch (err) {
      set({ loading: false, error: 'AI 响应失败，请稍后重试' });
    }
  },

  clearConversation: () => {
    set({ messages: [], error: null });
  },

  setError: (error) => set({ error }),
}));

export default useAIStore;