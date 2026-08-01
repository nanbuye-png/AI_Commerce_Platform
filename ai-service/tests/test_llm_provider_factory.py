import importlib

import pytest


def _reload_factory(monkeypatch, provider: str, api_key: str = ""):
    monkeypatch.setenv("AI_LLM_PROVIDER", provider)
    monkeypatch.setenv("AI_LLM_API_KEY", api_key)

    import app.config
    import app.services.llm_provider_factory

    importlib.reload(app.config)
    return importlib.reload(app.services.llm_provider_factory)


def test_factory_uses_mock_by_default(monkeypatch) -> None:
    factory = _reload_factory(monkeypatch, "mock")

    assert factory.create_llm_provider().__class__.__name__ == "MockLlmProvider"


def test_factory_builds_openai_compatible_provider(monkeypatch) -> None:
    factory = _reload_factory(monkeypatch, "openai-compatible", "test-key")
    provider = factory.create_llm_provider()

    try:
        assert provider.__class__.__name__ == "OpenAiCompatibleLlmProvider"
    finally:
        import asyncio

        asyncio.run(provider.aclose())


def test_factory_rejects_unknown_provider(monkeypatch) -> None:
    factory = _reload_factory(monkeypatch, "unknown")

    with pytest.raises(ValueError, match="AI_LLM_PROVIDER"):
        factory.create_llm_provider()