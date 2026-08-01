import request from './request';

export interface Result<T> {
  code: number;
  message: string;
  data: T;
}

export interface AiStatsVO {
  total_calls: number;
  total_tokens: number;
  succeeded: number;
  failed: number;
  success_rate: number;
  started_at: number;
  uptime_seconds: number;
  recent_calls_per_minute?: Record<string, number>;
  calls_by_scenario?: Record<string, number>;
}

export const aiStatsApi = {
  /** 获取 AI 调用统计（实时） */
  getStats: (): Promise<Result<AiStatsVO>> =>
    request.get<Result<AiStatsVO>, Result<AiStatsVO>>('/api/admin/ai/stats'),
};