/* ============================================================
   AI Client — Stub Implementation
   Implements AIService interface. No actual LLM calls.
   ============================================================ */

import type { AIService, AIResponse, AISession, AISearchIntent, AIRecommendation } from './aiTypes';

/**
 * AI 客户端 — 桩实现
 *
 * TODO: 替换为实际 AI Service 调用
 * - 替换 sendMessage 为真实 API 调用
 * - 实现流式响应 (SSE / WebSocket)
 * - 添加错误重试逻辑
 */
export const aiClient: AIService = {
  async sendMessage(_sessionId: string, _message: string): Promise<AIResponse> {
    // Stub: return empty response
    await new Promise((resolve) => setTimeout(resolve, 300));
    return {
      message: {
        id: crypto.randomUUID(),
        role: 'assistant',
        content: '您好！我是 AI 购物助手，可以帮您推荐商品、比较价格、提供购买建议。请问有什么可以帮您的吗？',
        createdAt: new Date().toISOString(),
      },
      suggestions: [
        '推荐几款热销手机',
        '帮我挑选礼物',
        '有什么优惠活动吗',
      ],
    };
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