import type {
  AIRecommendation,
  AISearchIntent,
  AIService,
  AISession,
  AIStreamHandlers,
  AIStreamMeta,
  AIStreamResult,
} from './aiTypes';
import { getToken, removeToken } from '../../utils/token';

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '');

interface TokenEventData {
  type: 'token';
  content: string;
}

interface DoneEventData {
  conversation_id: string;
  message_id: string;
}

interface ErrorEventData {
  type: 'stream_error';
  message: string;
}

function isTokenEventData(value: unknown): value is TokenEventData {
  if (!value || typeof value !== 'object') return false;
  const data = value as Record<string, unknown>;
  return data.type === 'token' && typeof data.content === 'string';
}

function isDoneEventData(value: unknown): value is DoneEventData {
  if (!value || typeof value !== 'object') return false;
  const data = value as Record<string, unknown>;
  return typeof data.conversation_id === 'string' && typeof data.message_id === 'string';
}

function isErrorEventData(value: unknown): value is ErrorEventData {
  if (!value || typeof value !== 'object') return false;
  const data = value as Record<string, unknown>;
  return data.type === 'stream_error' && typeof data.message === 'string';
}

function isStreamMeta(value: unknown): value is AIStreamMeta {
  if (!value || typeof value !== 'object') return false;
  const data = value as Record<string, unknown>;
  if (data.type === 'product_search_error') return typeof data.message === 'string';
  return (
    data.type === 'product_search'
    && typeof data.total === 'number'
    && Array.isArray(data.products)
    && !!data.query
    && typeof data.query === 'object'
  );
}

function dispatchEventFrame(frame: string, handlers: AIStreamHandlers): void {
  let eventName = 'message';
  const dataLines: string[] = [];

  for (const line of frame.split('\n')) {
    if (line.startsWith('event:')) eventName = line.slice(6).trim();
    if (line.startsWith('data:')) dataLines.push(line.slice(5).trimStart());
  }

  if (dataLines.length === 0) return;

  let data: unknown;
  try {
    data = JSON.parse(dataLines.join('\n'));
  } catch {
    throw new Error('AI 服务返回了无效的流式数据');
  }

  if (eventName === 'message' && isTokenEventData(data)) {
    handlers.onToken(data.content);
    return;
  }
  if (eventName === 'meta' && isStreamMeta(data)) {
    handlers.onMeta(data);
    return;
  }
  if (eventName === 'error' && isErrorEventData(data)) {
    throw new Error(data.message);
  }
  if (eventName === 'done' && isDoneEventData(data)) {
    const result: AIStreamResult = {
      conversationId: data.conversation_id,
      messageId: data.message_id,
    };
    handlers.onDone(result);
  }
}

async function readEventStream(response: Response, handlers: AIStreamHandlers): Promise<void> {
  if (!response.body) throw new Error('当前浏览器不支持流式响应');

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = '';

  while (true) {
    const { done, value } = await reader.read();
    buffer = `${buffer}${decoder.decode(value, { stream: !done })}`.replace(/\r\n/g, '\n');

    let boundary = buffer.indexOf('\n\n');
    while (boundary >= 0) {
      dispatchEventFrame(buffer.slice(0, boundary), handlers);
      buffer = buffer.slice(boundary + 2);
      boundary = buffer.indexOf('\n\n');
    }

    if (done) break;
  }

  if (buffer.trim()) dispatchEventFrame(buffer, handlers);
}

export const aiClient: AIService = {
  async sendMessage(sessionId, message, handlers, signal): Promise<void> {
    const token = getToken();
    if (!token) throw new Error('请先登录后使用 AI 购物助手');

    const response = await fetch(`${API_BASE_URL}/api/customer/ai/chat/stream`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
        Accept: 'text/event-stream',
      },
      body: JSON.stringify({ message, conversation_id: sessionId }),
      signal,
    });

    if (response.status === 401) {
      removeToken();
      window.location.assign('/login');
      throw new Error('登录状态已失效，请重新登录');
    }
    if (!response.ok) {
      throw new Error(response.status === 503 ? 'AI 服务暂时不可用，请稍后重试' : 'AI 请求失败，请稍后重试');
    }

    await readEventStream(response, handlers);
  },

  async createSession(context): Promise<AISession> {
    return {
      id: crypto.randomUUID(),
      messages: [],
      context,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
  },

  async getSession(sessionId: string): Promise<AISession> {
    return {
      id: sessionId,
      messages: [],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
  },

  async clearSession(_sessionId: string): Promise<void> {
    // Stub
  },

  async analyzeSearchIntent(query: string): Promise<AISearchIntent> {
    return {
      originalQuery: query,
      normalizedQuery: query.trim().toLowerCase(),
      keywords: query.trim().split(/\s+/),
      confidence: 0.5,
    };
  },

  async getRecommendations(_productId?: string, _limit?: number): Promise<AIRecommendation[]> {
    return [];
  },
};