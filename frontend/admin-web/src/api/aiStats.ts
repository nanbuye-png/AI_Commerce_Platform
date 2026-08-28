import request from './request';

export interface Result<T> {
  code: number;
  message: string;
  data: T;
}

export interface AiStatsVO {
  provider: string;
  model: string;
  token_accounting: 'estimated';
  total_calls: number;
  total_tokens: number;
  estimated_input_tokens: number;
  estimated_output_tokens: number;
  succeeded: number;
  failed: number;
  success_rate: number;
  avg_ttft_ms: number;
  p95_ttft_ms: number;
  avg_latency_ms: number;
  p95_latency_ms: number;
  started_at: number;
  uptime_seconds: number;
  recent_calls_per_minute?: Record<string, number>;
  calls_by_scenario?: Record<string, number>;
  tool_stats?: Record<string, {
    calls: number;
    succeeded: number;
    failed: number;
    success_rate: number;
    avg_latency_ms: number;
    p95_latency_ms: number;
  }>;
}

export const aiStatsApi = {
  /** 获取 AI 调用统计（实时） */
  getStats: (): Promise<Result<AiStatsVO>> =>
    request.get<Result<AiStatsVO>, Result<AiStatsVO>>('/api/admin/ai/stats'),
};
