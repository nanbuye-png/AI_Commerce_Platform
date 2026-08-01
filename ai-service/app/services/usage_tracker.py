"""
AI Usage 追踪模块
内存级统计 AI 调用次数、成功失败、token 用量等
（生产环境可替换为 Redis/数据库持久化）
"""
import threading
import time
from collections import defaultdict


class UsageTracker:
    """线程安全的 AI 调用统计追踪器"""

    def __init__(self) -> None:
        self._lock = threading.Lock()
        self._started_at = time.time()
        self._total_calls = 0
        self._total_tokens = 0
        self._succeeded = 0
        self._failed = 0
        self._calls_by_minute: dict[int, int] = defaultdict(int)
        self._calls_by_scenario: dict[str, int] = defaultdict(int)

    def record_call(self, *, succeeded: bool, tokens: int = 0, scenario: str = "chat") -> None:
        """记录一次 AI 调用"""
        with self._lock:
            self._total_calls += 1
            self._total_tokens += max(0, tokens)
            if succeeded:
                self._succeeded += 1
            else:
                self._failed += 1
            minute_key = int(time.time() // 60)
            self._calls_by_minute[minute_key] += 1
            self._calls_by_scenario[scenario] += 1

    def snapshot(self) -> dict[str, object]:
        """返回当前统计快照"""
        with self._lock:
            now = int(time.time())
            current_minute = now // 60
            # 最近 60 分钟每分钟调用量
            recent_minutes = {
                (current_minute - offset) * 60: self._calls_by_minute[current_minute - offset]
                for offset in range(59, -1, -1)
                if (current_minute - offset) in self._calls_by_minute
            }
            return {
                "total_calls": self._total_calls,
                "total_tokens": self._total_tokens,
                "succeeded": self._succeeded,
                "failed": self._failed,
                "success_rate": round(self._succeeded / self._total_calls, 4) if self._total_calls else 0.0,
                "started_at": self._started_at,
                "uptime_seconds": int(now - self._started_at),
                "recent_calls_per_minute": {
                    str(int(k)): v for k, v in sorted(recent_minutes.items())
                },
                "calls_by_scenario": dict(self._calls_by_scenario),
            }


# 全局单例
usage_tracker = UsageTracker()