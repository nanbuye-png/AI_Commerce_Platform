/* ============================================================
   AI Service — Type Definitions
   ============================================================ */

/** AI 对话角色 */
export type AIMessageRole = 'user' | 'assistant' | 'system';

/** AI 消息 */
export interface AIMessage {
  readonly id: string;
  readonly role: AIMessageRole;
  readonly content: string;
  readonly createdAt: string;
  readonly metadata?: {
    readonly productId?: string;
    readonly actionType?: AIActionType;
    readonly confidence?: number;
    readonly productSearch?: AIProductSearchMetadata;
    readonly addToCart?: AIAddToCartMetadata;
  };
}

export interface AIProductSearchProduct {
  readonly id: number;
  readonly productName: string;
  readonly description?: string | null;
  readonly brand?: string | null;
  readonly categoryId?: number | null;
  readonly categoryName?: string | null;
  readonly minPrice?: number | null;
  readonly maxPrice?: number | null;
  readonly defaultSkuId?: number | null;
  readonly defaultSkuPrice?: number | null;
  readonly defaultSkuStock?: number | null;
  readonly coverImage?: string | null;
  readonly salesCount?: number | null;
  readonly createdTime?: string | null;
}

export interface AIProductSearchQuery {
  readonly keyword?: string;
  readonly category_id?: number;
  readonly min_price?: number;
  readonly max_price?: number;
  readonly page_size: number;
}

export interface AIProductSearchMetadata {
  readonly query?: AIProductSearchQuery;
  readonly products: AIProductSearchProduct[];
  readonly total: number;
  readonly error?: string;
}

export interface AIAddToCartMetadata {
  readonly type: 'add_to_cart_confirmation' | 'add_to_cart_result';
  readonly skuId?: number;
  readonly quantity?: number;
  readonly success?: boolean;
  readonly message?: string;
}

export interface AISendMessageOptions {
  readonly action?: 'add_to_cart';
  readonly skuId?: number;
  readonly quantity?: number;
  readonly confirmed?: boolean;
}

export type AIStreamMeta =
  | {
      readonly type: 'product_search';
      readonly query: AIProductSearchQuery;
      readonly products: AIProductSearchProduct[];
      readonly total: number;
    }
  | {
      readonly type: 'product_search_error';
      readonly message: string;
    }
  | {
      readonly type: 'add_to_cart_confirmation';
      readonly sku_id: number;
      readonly quantity: number;
    }
  | {
      readonly type: 'add_to_cart_result';
      readonly sku_id: number;
      readonly quantity: number;
      readonly success: boolean;
      readonly message?: string;
      readonly cart?: {
        readonly items?: unknown[];
      };
    };

/** AI 动作类型 */
export type AIActionType =
  | 'product_search'
  | 'product_recommend'
  | 'product_compare'
  | 'outfit_match'
  | 'price_analysis'
  | 'purchase_advice'
  | 'order_inquiry'
  | 'general_chat';

/** AI 对话会话 */
export interface AISession {
  readonly id: string;
  readonly messages: AIMessage[];
  readonly context?: {
    readonly currentProductId?: string;
    readonly currentCategoryId?: string;
    readonly userQuery?: string;
  };
  readonly createdAt: string;
  readonly updatedAt: string;
}

/** AI 搜索意图 */
export interface AISearchIntent {
  readonly originalQuery: string;
  readonly normalizedQuery: string;
  readonly category?: string;
  readonly brand?: string;
  readonly minPrice?: number;
  readonly maxPrice?: number;
  readonly attributes?: Record<string, string>;
  readonly keywords: string[];
  readonly confidence: number;
}

/** AI 回答 */
export interface AIResponse {
  readonly message: AIMessage;
  readonly actions?: AIActionType[];
  readonly suggestions?: string[];
}

/** AI 流式响应完成信息 */
export interface AIStreamResult {
  readonly conversationId: string;
  readonly messageId: string;
}

/** AI 流式响应回调 */
export interface AIStreamHandlers {
  readonly onToken: (content: string) => void;
  readonly onMeta: (meta: AIStreamMeta) => void;
  readonly onDone: (result: AIStreamResult) => void;
}

/** AI 服务接口 */
export interface AIService {
  /** 发送聊天消息 */
  sendMessage(
    sessionId: string,
    message: string,
    handlers: AIStreamHandlers,
    options?: AISendMessageOptions,
    signal?: AbortSignal,
  ): Promise<void>;
  /** 创建新会话 */
  createSession(context?: AISession['context']): Promise<AISession>;
  /** 获取会话历史 */
  getSession(sessionId: string): Promise<AISession>;
  /** 清除会话 */
  clearSession(sessionId: string): Promise<void>;
  /** 分析搜索意图 */
  analyzeSearchIntent(query: string): Promise<AISearchIntent>;
}

/** AI Service Key */
export const AI_SERVICE_KEY = 'aiService';
