import importlib
import json

from fastapi.testclient import TestClient

from app.services.commerce_tool import CommerceToolError


def _client(monkeypatch) -> TestClient:
    monkeypatch.setenv("AI_INTERNAL_API_TOKEN", "test-internal-token")

    import app.config
    import app.core.internal_auth
    import app.main

    importlib.reload(app.config)
    importlib.reload(app.core.internal_auth)
    importlib.reload(app.main)
    return TestClient(app.main.app)


def _events(response_text: str) -> list[tuple[str, dict[str, object]]]:
    events: list[tuple[str, dict[str, object]]] = []
    for block in response_text.strip().split("\n\n"):
        lines = block.splitlines()
        event = lines[0].removeprefix("event: ")
        data = json.loads(lines[1].removeprefix("data: "))
        events.append((event, data))
    return events


def test_chat_stream_rejects_missing_internal_token(monkeypatch) -> None:
    response = _client(monkeypatch).post(
        "/api/v1/internal/ai/chat/stream",
        json={"message": "推荐耳机"},
    )

    assert response.status_code == 401


def test_chat_stream_rejects_invalid_internal_token(monkeypatch) -> None:
    response = _client(monkeypatch).post(
        "/api/v1/internal/ai/chat/stream",
        headers={"X-Internal-Token": "wrong-token"},
        json={"message": "推荐耳机"},
    )

    assert response.status_code == 401


def test_chat_stream_validates_message(monkeypatch) -> None:
    response = _client(monkeypatch).post(
        "/api/v1/internal/ai/chat/stream",
        headers={"X-Internal-Token": "test-internal-token"},
        json={"message": ""},
    )

    assert response.status_code == 422


def test_chat_stream_emits_tokens_and_done_event(monkeypatch) -> None:
    client = _client(monkeypatch)
    from app.api import chat as chat_api

    original_parser = chat_api.intent_parser
    chat_api.intent_parser = ProductSearchIntentParserStub()
    try:
        response = client.post(
            "/api/v1/internal/ai/chat/stream",
            headers={"X-Internal-Token": "test-internal-token"},
            json={"message": "推荐耳机", "conversation_id": "conv_existing"},
        )
    finally:
        chat_api.intent_parser = original_parser

    assert response.status_code == 200
    assert response.headers["content-type"].startswith("text/event-stream")

    events = _events(response.text)
    assert events[0][0] == "message"
    assert events[0][1]["type"] == "token"
    assert "推荐耳机" in "".join(
        str(data["content"])
        for event, data in events
        if event == "message" and data.get("type") == "token"
    )
    assert events[-1][0] == "done"
    assert events[-1][1]["conversation_id"] == "conv_existing"
    assert str(events[-1][1]["message_id"]).startswith("msg_")


class ProductSearchIntentParserStub:
    def parse(self, message):
        return None


class StubCommerceTool:
    async def search_products(self, intent):
        return {
            "items": [{"id": 42, "productName": "降噪耳机", "minPrice": 899}],
            "total": 1,
        }


class FailingCommerceTool:
    async def search_products(self, intent):
        raise CommerceToolError("unavailable")


class CapturingProvider:
    def __init__(self):
        self.context = None

    async def stream(self, message, context=None):
        self.context = context
        yield "已找到商品"


class FailingProvider:
    async def stream(self, message, context=None):
        yield "部分回答"
        raise RuntimeError("provider unavailable")


def test_chat_stream_emits_product_search_metadata(monkeypatch) -> None:
    client = _client(monkeypatch)
    from app.api import chat as chat_api

    original_tool = chat_api.commerce_tool
    original_provider = chat_api.provider
    capturing_provider = CapturingProvider()
    chat_api.commerce_tool = StubCommerceTool()
    chat_api.provider = capturing_provider
    try:
        response = client.post(
            "/api/v1/internal/ai/chat/stream",
            headers={"X-Internal-Token": "test-internal-token"},
            json={
                "message": "帮我找500到1000元的耳机",
                "conversation_id": "conv_products",
            },
        )
        events = _events(response.text)
        assert response.status_code == 200
        assert events[0][0] == "meta"
        assert events[0][1]["type"] == "product_search"
        assert events[0][1]["products"][0]["productName"] == "降噪耳机"
        assert any(event == "message" for event, _ in events)
        assert capturing_provider.context["product_search"]["products"][0]["id"] == 42
    finally:
        chat_api.commerce_tool = original_tool
        chat_api.provider = original_provider


def test_chat_stream_keeps_tokens_when_product_search_fails(monkeypatch) -> None:
    client = _client(monkeypatch)
    from app.api import chat as chat_api

    original_tool = chat_api.commerce_tool
    chat_api.commerce_tool = FailingCommerceTool()
    try:
        response = client.post(
            "/api/v1/internal/ai/chat/stream",
            headers={"X-Internal-Token": "test-internal-token"},
            json={"message": "推荐1000元以内的耳机"},
        )
        events = _events(response.text)
        assert response.status_code == 200
        assert events[0] == (
            "meta",
            {"type": "product_search_error", "message": "商品搜索暂时不可用，请稍后再试。"},
        )
        assert any(event == "message" for event, _ in events)
        assert events[-1][0] == "done"
    finally:
        chat_api.commerce_tool = original_tool


def test_chat_stream_emits_error_event_when_provider_fails(monkeypatch) -> None:
    client = _client(monkeypatch)
    from app.api import chat as chat_api

    original_parser = chat_api.intent_parser
    original_provider = chat_api.provider
    chat_api.intent_parser = ProductSearchIntentParserStub()
    chat_api.provider = FailingProvider()
    try:
        response = client.post(
            "/api/v1/internal/ai/chat/stream",
            headers={"X-Internal-Token": "test-internal-token"},
            json={"message": "推荐耳机"},
        )
        events = _events(response.text)
        assert response.status_code == 200
        assert events[-1] == (
            "error",
            {"type": "stream_error", "message": "AI 生成暂时不可用，请稍后再试。"},
        )
        assert not any(event == "done" for event, _ in events)
    finally:
        chat_api.intent_parser = original_parser
        chat_api.provider = original_provider
