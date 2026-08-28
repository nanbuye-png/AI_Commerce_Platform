from app.services.usage_tracker import UsageTracker


def test_usage_tracker_reports_latency_tokens_and_tools(monkeypatch) -> None:
    monkeypatch.setattr("app.services.usage_tracker.config.LLM_PROVIDER", "mock")
    monkeypatch.setattr("app.services.usage_tracker.config.DEFAULT_MODEL", "demo-model")
    tracker = UsageTracker()
    tracker.record_call(
        succeeded=True,
        estimated_input_tokens=10,
        estimated_output_tokens=20,
        ttft_ms=12.5,
        latency_ms=80.0,
        scenario="product_search",
    )
    tracker.record_call(
        succeeded=False,
        estimated_input_tokens=5,
        latency_ms=120.0,
        scenario="chat",
    )
    tracker.record_tool("product_search", succeeded=True, latency_ms=30.0)
    tracker.record_tool("product_search", succeeded=False, latency_ms=50.0)

    snapshot = tracker.snapshot()

    assert snapshot["provider"] == "mock"
    assert snapshot["model"] == "demo-model"
    assert snapshot["token_accounting"] == "estimated"
    assert snapshot["total_calls"] == 2
    assert snapshot["total_tokens"] == 35
    assert snapshot["success_rate"] == 0.5
    assert snapshot["avg_latency_ms"] == 100.0
    assert snapshot["p95_latency_ms"] == 120.0
    assert snapshot["tool_stats"]["product_search"]["success_rate"] == 0.5
