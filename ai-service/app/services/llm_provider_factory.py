from app import config
from app.services.llm_provider import LlmProvider
from app.services.mock_llm_provider import MockLlmProvider
from app.services.openai_compatible_llm_provider import OpenAiCompatibleLlmProvider


def create_llm_provider() -> LlmProvider:
    if config.LLM_PROVIDER == "mock":
        return MockLlmProvider()
    if config.LLM_PROVIDER == "openai-compatible":
        return OpenAiCompatibleLlmProvider(
            api_key=config.LLM_API_KEY,
            model=config.DEFAULT_MODEL,
            base_url=config.LLM_BASE_URL,
            timeout_seconds=config.LLM_TIMEOUT_SECONDS,
            temperature=config.LLM_TEMPERATURE,
            max_tokens=config.LLM_MAX_TOKENS,
            context_max_chars=config.LLM_CONTEXT_MAX_CHARS,
        )
    raise ValueError(
        "AI_LLM_PROVIDER must be one of: mock, openai-compatible"
    )