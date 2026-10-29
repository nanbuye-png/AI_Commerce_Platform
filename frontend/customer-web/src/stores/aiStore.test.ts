import { beforeEach, describe, expect, it, vi } from 'vitest';
import { aiClient } from '../services/ai/aiClient';
import { setToken } from '../utils/token';
import useAIStore from './aiStore';

vi.mock('../services/ai/aiClient', () => ({
  aiClient: {
    sendMessage: vi.fn(async (sessionId, _message, handlers) => {
      handlers.onToken('找到了商品');
      handlers.onDone({ conversationId: sessionId, messageId: 'msg_test' });
    }),
  },
}));

function loginAs(userId: string): void {
  setToken(`header.${btoa(JSON.stringify({ sub: userId }))}.signature`);
}

describe('AI conversation state', () => {
  beforeEach(() => {
    sessionStorage.clear();
    vi.clearAllMocks();
    loginAs('12');
    useAIStore.setState({ messages: [], sessionId: null, ownerId: null, loading: false, error: null });
  });

  it('keeps messages across page visits and reloads, and sends history', async () => {
    await useAIStore.getState().initializeSession();
    await useAIStore.getState().sendMessage('推荐托尔斯泰');
    const firstSession = useAIStore.getState().sessionId;

    await useAIStore.getState().initializeSession();
    expect(useAIStore.getState().messages).toHaveLength(2);

    useAIStore.setState({ messages: [], sessionId: null, ownerId: null });
    await useAIStore.getState().initializeSession();
    expect(useAIStore.getState().sessionId).toBe(firstSession);
    expect(useAIStore.getState().messages).toHaveLength(2);

    await useAIStore.getState().sendMessage('还有吗');
    expect(vi.mocked(aiClient.sendMessage).mock.calls[1][5]).toEqual([
      { role: 'user', content: '推荐托尔斯泰' },
      { role: 'assistant', content: '找到了商品' },
    ]);
  });

  it('does not show another customer’s conversation', async () => {
    await useAIStore.getState().sendMessage('推荐耳机');
    loginAs('13');
    await useAIStore.getState().initializeSession();
    expect(useAIStore.getState().messages).toEqual([]);
  });
});
