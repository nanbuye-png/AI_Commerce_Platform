import asyncio
import json

import httpx
import pytest

from app.services.openai_compatible_llm_provider import (
    LlmProviderError,
    OpenAiCompatibleLlmProvider,
)


def _provider(handler, **overrides) -> OpenAiCompatibleLlmProvider:
    client = httpx.AsyncClient(
        base_url="https://llm.example/v1",
        transport=httpx.MockTransport(handler),
    )
    options = {
        "api_key": "secret-key",
        "model": "test-model",
        "base_url": "https://llm.example/v1",
        "timeout_seconds": 30,
        "temperature": 0.1,
        "max_tokens": 256,
        "context_max_chars": 1000,
        "client": client,
    }
    options.update(overrides)
    return OpenAiCompatibleLlmProvider(**options)


def test_stream_posts_openai_compatible_request_and_yields_content() -> None:
    captured = {}

    def handler(request: httpx.Request) -> httpx.Response:
        captured["path"] = request.url.path
        captured["payload"] = json.loads(request.content)
        body = (
            'data: {"choices":[{"delta":{"role":"assistant"}}]}\n\n'
            'data: {"choices":[{"delta":{"content":"找到"}}]}\n\n'
            ': keep-alive\n\n'
            'data: {"choices":[{"delta":{"content":"耳机"}}]}\n\n'
            'data: [DONE]\n\n'
            'data: {"choices":[{"delta":{"content":"ignored"}}]}\n\n'
        )
        return httpx.Response(
            200,
            headers={"content-type": "text/event-stream"},
            content=body.encode(),
        )

    async def run_test() -> tuple[OpenAiCompatibleLlmProvider, list[str]]:
        provider = _provider(handler)
        tokens = [
            token
            async for token in provider.stream(
                "推荐耳机",
                {"product_search": {"products": [{"id": 1, "name": "耳机"}]}},
            )
        ]
        return provider, tokens

    provider, tokens = asyncio.run(run_test())

    assert tokens == ["找到", "耳机"]
    assert captured["path"] == "/v1/chat/completions"
    assert captured["payload"]["model"] == "test-model"
    assert captured["payload"]["stream"] is True
    assert captured["payload"]["messages"][-1] == {
        "role": "user",
        "content": "推荐耳机",
    }
    assert '"id":1' in captured["payload"]["messages"][0]["content"]
    asyncio.run(provider._client.aclose())


def test_stream_maps_upstream_http_error_without_exposing_response() -> None:
    def handler(_: httpx.Request) -> httpx.Response:
        return httpx.Response(401, json={"error": {"message": "secret details"}})

    async def run_test() -> None:
        provider = _provider(handler)
        try:
            _ = [token async for token in provider.stream("hello")]
        finally:
            await provider._client.aclose()

    with pytest.raises(LlmProviderError, match="LLM provider stream failed") as exc_info:
        asyncio.run(run_test())

    assert "secret details" not in str(exc_info.value)


def test_provider_requires_api_key() -> None:
    with pytest.raises(ValueError, match="AI_LLM_API_KEY"):
        OpenAiCompatibleLlmProvider(
            api_key="",
            model="test-model",
            base_url="https://llm.example/v1",
            timeout_seconds=30,
            temperature=0.1,
            max_tokens=256,
            context_max_chars=1000,
        )