import importlib
import json

from fastapi.testclient import TestClient


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
    response = _client(monkeypatch).post(
        "/api/v1/internal/ai/chat/stream",
        headers={"X-Internal-Token": "test-internal-token"},
        json={"message": "推荐耳机", "conversation_id": "conv_existing"},
    )

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
