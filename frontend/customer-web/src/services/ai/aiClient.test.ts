import { describe, expect, it, vi } from 'vitest';

import { setToken } from '../../utils/token';
import { aiClient, dispatchEventFrame, readEventStream } from './aiClient';
import type { AIStreamHandlers } from './aiTypes';


function handlers() {
  return {
    onToken: vi.fn(),
    onMeta: vi.fn(),
    onDone: vi.fn(),
  } satisfies AIStreamHandlers;
}


function streamResponse(chunks: string[]): Response {
  const encoder = new TextEncoder();
  return new Response(new ReadableStream({
    start(controller) {
      chunks.forEach((chunk) => controller.enqueue(encoder.encode(chunk)));
      controller.close();
    },
  }), { status: 200, headers: { 'Content-Type': 'text/event-stream' } });
}


describe('AI SSE client', () => {
  it('parses frames split across network chunks', async () => {
    const callbacks = handlers();
    await readEventStream(streamResponse([
      'event: message\ndata: {"type":"token","content":"你',
      '好"}\n\nevent: done\ndata: {"conversation_id":"conv_1",',
      '"message_id":"msg_1"}\n\n',
    ]), callbacks);

    expect(callbacks.onToken).toHaveBeenCalledWith('你好');
    expect(callbacks.onDone).toHaveBeenCalledWith({ conversationId: 'conv_1', messageId: 'msg_1' });
  });

  it('dispatches product and add-to-cart metadata', () => {
    const callbacks = handlers();
    dispatchEventFrame(
      'event: meta\ndata: {"type":"product_search","query":{"keyword":"耳机"},"products":[],"total":0}',
      callbacks,
    );
    dispatchEventFrame(
      'event: meta\ndata: {"type":"add_to_cart_confirmation","sku_id":42,"quantity":1}',
      callbacks,
    );
    dispatchEventFrame(
      'event: meta\ndata: {"type":"add_to_cart_result","success":true,"sku_id":42,"quantity":1}',
      callbacks,
    );

    expect(callbacks.onMeta).toHaveBeenCalledTimes(3);
    expect(callbacks.onMeta).toHaveBeenLastCalledWith(expect.objectContaining({
      type: 'add_to_cart_result',
      success: true,
    }));
  });

  it('rejects invalid stream JSON', () => {
    expect(() => dispatchEventFrame('event: message\ndata: not-json', handlers()))
      .toThrow('AI 服务返回了无效的流式数据');
  });

  it('does not call the API when no JWT exists', async () => {
    const fetchMock = vi.fn();
    vi.stubGlobal('fetch', fetchMock);

    await expect(aiClient.sendMessage('conv', '推荐耳机', handlers()))
      .rejects.toThrow('请先登录后使用 AI 购物助手');
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it('clears an expired JWT and reports re-login', async () => {
    setToken('expired-token');
    const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 401 }));
    vi.stubGlobal('fetch', fetchMock);

    await expect(aiClient.sendMessage('conv', '推荐耳机', handlers()))
      .rejects.toThrow('登录状态已失效，请重新登录');
    expect(localStorage.getItem('customer_token')).toBeNull();
  });

  it('sends a request id and confirmed action payload', async () => {
    setToken('valid-token');
    const fetchMock = vi.fn().mockResolvedValue(streamResponse([
      'event: done\ndata: {"conversation_id":"conv_1","message_id":"msg_1"}\n\n',
    ]));
    vi.stubGlobal('fetch', fetchMock);

    await aiClient.sendMessage('conv_1', '确认加入购物车', handlers(), {
      action: 'add_to_cart',
      skuId: 42,
      quantity: 2,
      confirmed: true,
    });

    const [, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect(init.headers).toEqual(expect.objectContaining({
      'X-Request-Id': expect.any(String),
      Authorization: 'Bearer valid-token',
    }));
    expect(JSON.parse(String(init.body))).toEqual(expect.objectContaining({
      action: 'add_to_cart',
      sku_id: 42,
      quantity: 2,
      confirmed: true,
    }));
  });
});
