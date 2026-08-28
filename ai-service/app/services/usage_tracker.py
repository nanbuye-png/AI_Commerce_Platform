"""Thread-safe in-memory metrics for the AI application demo."""

import threading
import time
from collections import defaultdict, deque
from statistics import fmean

from app import config


def _percentile(values: deque[float], percentile: float) -> float:
    if not values:
        return 0.0
    ordered = sorted(values)
    index = min(len(ordered) - 1, max(0, int(round((len(ordered) - 1) * percentile))))
    return round(ordered[index], 2)


class UsageTracker:
    """Tracks request, latency, token-estimate and tool metrics."""

    def __init__(self, sample_limit: int = 1000) -> None:
        self._lock = threading.Lock()
        self._sample_limit = sample_limit
        self.reset()

    def reset(self) -> None:
        with self._lock:
            self._started_at = time.time()
            self._total_calls = 0
            self._estimated_input_tokens = 0
            self._estimated_output_tokens = 0
            self._succeeded = 0
            self._failed = 0
            self._calls_by_minute: dict[int, int] = defaultdict(int)
            self._calls_by_scenario: dict[str, int] = defaultdict(int)
            self._ttft_samples: deque[float] = deque(maxlen=self._sample_limit)
            self._latency_samples: deque[float] = deque(maxlen=self._sample_limit)
            self._tool_stats: dict[str, dict[str, object]] = defaultdict(
                lambda: {
                    "calls": 0,
                    "succeeded": 0,
                    "failed": 0,
                    "latency_samples": deque(maxlen=self._sample_limit),
                }
            )

    def record_call(
        self,
        *,
        succeeded: bool,
        estimated_input_tokens: int = 0,
        estimated_output_tokens: int = 0,
        ttft_ms: float | None = None,
        latency_ms: float = 0.0,
        scenario: str = "chat",
    ) -> None:
        with self._lock:
            self._total_calls += 1
            self._estimated_input_tokens += max(0, estimated_input_tokens)
            self._estimated_output_tokens += max(0, estimated_output_tokens)
            self._succeeded += int(succeeded)
            self._failed += int(not succeeded)
            self._calls_by_minute[int(time.time() // 60)] += 1
            self._calls_by_scenario[scenario] += 1
            if ttft_ms is not None:
                self._ttft_samples.append(max(0.0, ttft_ms))
            self._latency_samples.append(max(0.0, latency_ms))

    def record_tool(self, tool_name: str, *, succeeded: bool, latency_ms: float) -> None:
        with self._lock:
            stats = self._tool_stats[tool_name]
            stats["calls"] = int(stats["calls"]) + 1
            key = "succeeded" if succeeded else "failed"
            stats[key] = int(stats[key]) + 1
            samples = stats["latency_samples"]
            assert isinstance(samples, deque)
            samples.append(max(0.0, latency_ms))

    def snapshot(self) -> dict[str, object]:
        with self._lock:
            now = int(time.time())
            current_minute = now // 60
            recent_minutes = {
                (current_minute - offset) * 60: self._calls_by_minute[current_minute - offset]
                for offset in range(59, -1, -1)
                if (current_minute - offset) in self._calls_by_minute
            }
            tools: dict[str, object] = {}
            for name, raw in self._tool_stats.items():
                calls = int(raw["calls"])
                succeeded = int(raw["succeeded"])
                samples = raw["latency_samples"]
                assert isinstance(samples, deque)
                tools[name] = {
                    "calls": calls,
                    "succeeded": succeeded,
                    "failed": int(raw["failed"]),
                    "success_rate": round(succeeded / calls, 4) if calls else 0.0,
                    "avg_latency_ms": round(fmean(samples), 2) if samples else 0.0,
                    "p95_latency_ms": _percentile(samples, 0.95),
                }

            estimated_total = self._estimated_input_tokens + self._estimated_output_tokens
            return {
                "provider": config.LLM_PROVIDER,
                "model": config.DEFAULT_MODEL,
                "token_accounting": "estimated",
                "total_calls": self._total_calls,
                "total_tokens": estimated_total,
                "estimated_input_tokens": self._estimated_input_tokens,
                "estimated_output_tokens": self._estimated_output_tokens,
                "succeeded": self._succeeded,
                "failed": self._failed,
                "success_rate": round(self._succeeded / self._total_calls, 4) if self._total_calls else 0.0,
                "avg_ttft_ms": round(fmean(self._ttft_samples), 2) if self._ttft_samples else 0.0,
                "p95_ttft_ms": _percentile(self._ttft_samples, 0.95),
                "avg_latency_ms": round(fmean(self._latency_samples), 2) if self._latency_samples else 0.0,
                "p95_latency_ms": _percentile(self._latency_samples, 0.95),
                "started_at": self._started_at,
                "uptime_seconds": int(now - self._started_at),
                "recent_calls_per_minute": {
                    str(int(key)): value for key, value in sorted(recent_minutes.items())
                },
                "calls_by_scenario": dict(self._calls_by_scenario),
                "tool_stats": tools,
            }


usage_tracker = UsageTracker()
