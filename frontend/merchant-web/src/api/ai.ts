/**
 * 商家端 AI 商品助手 API 客户端
 * 通过后端网关 /api/merchant/ai/chat/stream 调用 AI 服务（SSE 流式）
 */
import { getToken, removeToken } from '../utils/token';

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '');

export interface MerchantAIHandlers {
  onToken: (content: string) => void;
  onMeta?: (meta: MerchantAIMeta) => void;
  onDone?: (result: { conversationId: string; messageId: string }) => void;
}

export interface MerchantAIMeta {
  type: 'product_search' | 'product_search_error';
  message?: string;
  query?: unknown;
  products?: unknown[];
  total?: number;
}

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

function isMetaEventData(value: unknown): value is MerchantAIMeta {
  if (!value || typeof value !== 'object') return false;
  const data = value as Record<string, unknown>;
  if (data.type === 'product_search_error') return typeof data.message === 'string';
  return data.type === 'product_search'
    && typeof data.total === 'number'
    && Array.isArray(data.products);
}

function dispatchEventFrame(frame: string, handlers: MerchantAIHandlers): void {
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
  if (eventName === 'meta' && isMetaEventData(data)) {
    handlers.onMeta?.(data);
    return;
  }
  if (eventName === 'error' && isErrorEventData(data)) {
    throw new Error(data.message);
  }
  if (eventName === 'done' && isDoneEventData(data)) {
    handlers.onDone?.({ conversationId: data.conversation_id, messageId: data.message_id });
  }
}

async function readEventStream(response: Response, handlers: MerchantAIHandlers): Promise<void> {
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

export const merchantAiApi = {
  /**
   * 发送消息给 AI 商品助手（SSE 流式）
   */
  async streamChat(message: string, conversationId: string | undefined, handlers: MerchantAIHandlers, signal?: AbortSignal): Promise<void> {
    const token = getToken();
    if (!token) throw new Error('请先登录后使用 AI 商品助手');

    const response = await fetch(`${API_BASE_URL}/api/merchant/ai/chat/stream`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
        Accept: 'text/event-stream',
      },
      body: JSON.stringify({ message, conversation_id: conversationId }),
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
};