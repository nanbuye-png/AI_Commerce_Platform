import json
from collections.abc import AsyncIterator

import httpx

from app.services.llm_provider import LlmContext


class LlmProviderError(RuntimeError):
    pass


class OpenAiCompatibleLlmProvider:
    def __init__(
        self,
        *,
        api_key: str,
        model: str,
        base_url: str,
        timeout_seconds: float,
        temperature: float,
        max_tokens: int,
        context_max_chars: int,
        client: httpx.AsyncClient | None = None,
    ) -> None:
        if not api_key.strip():
            raise ValueError("AI_LLM_API_KEY is required for openai-compatible provider")
        if not model.strip():
            raise ValueError("AI_DEFAULT_MODEL is required for openai-compatible provider")
        if not base_url.strip():
            raise ValueError("AI_LLM_BASE_URL is required for openai-compatible provider")
        if timeout_seconds <= 0:
            raise ValueError("AI_LLM_TIMEOUT_SECONDS must be greater than zero")
        if max_tokens <= 0:
            raise ValueError("AI_LLM_MAX_TOKENS must be greater than zero")
        if context_max_chars <= 0:
            raise ValueError("AI_LLM_CONTEXT_MAX_CHARS must be greater than zero")

        self._model = model
        self._temperature = temperature
        self._max_tokens = max_tokens
        self._context_max_chars = context_max_chars
        self._owns_client = client is None
        self._client = client or httpx.AsyncClient(
            base_url=base_url,
            timeout=timeout_seconds,
            headers={
                "Authorization": f"Bearer {api_key}",
                "Content-Type": "application/json",
                "Accept": "text/event-stream",
            },
        )

    async def stream(
        self,
        message: str,
        context: LlmContext | None = None,
    ) -> AsyncIterator[str]:
        messages: list[dict[str, str]] = []
        if context:
            serialized_context = json.dumps(
                context,
                ensure_ascii=False,
                separators=(",", ":"),
                default=str,
            )[: self._context_max_chars]
            messages.append(
                {
                    "role": "system",
                    "content": (
                        "你是电商平台的智能购物助手。请严格基于下面的可信商品数据回答用户。\n"
                        "规则：\n"
                        "1. 如果商品列表为空，明确告知暂时没有符合条件的商品，并建议用户放宽条件。\n"
                        "2. 如果有商品，按价格、销量或用户需求推荐最合适的几件，并简述名称、价格和推荐理由。\n"
                        "3. 不要编造商品、价格、库存或评分。\n"
                        "4. 回答使用简洁自然的中文。\n"
                        f"{serialized_context}"
                    ),
                }
            )
        messages.append({"role": "user", "content": message})

        payload = {
            "model": self._model,
            "messages": messages,
            "stream": True,
            "temperature": self._temperature,
            "max_tokens": self._max_tokens,
        }

        try:
            async with self._client.stream(
                "POST", "/chat/completions", json=payload
            ) as response:
                response.raise_for_status()
                async for line in response.aiter_lines():
                    if line.removeprefix("data:").strip() == "[DONE]":
                        break
                    token = self._parse_sse_line(line)
                    if token is not None:
                        yield token
        except httpx.HTTPStatusError as exc:
            # 保留 HTTP 状态码（如 401/402/429），但不暴露上游响应体中的敏感信息
            raise LlmProviderError(
                f"LLM provider stream failed (HTTP {exc.response.status_code})"
            ) from exc
        except (httpx.HTTPError, ValueError, TypeError, KeyError) as exc:
            raise LlmProviderError("LLM provider stream failed") from exc

    async def aclose(self) -> None:
        if self._owns_client:
            await self._client.aclose()

    @staticmethod
    def _parse_sse_line(line: str) -> str | None:
        if not line.startswith("data:"):
            return None

        data = line.removeprefix("data:").strip()
        if not data or data == "[DONE]":
            return None

        payload = json.loads(data)
        choices = payload.get("choices", [])
        if not isinstance(choices, list):
            return None

        for choice in choices:
            if not isinstance(choice, dict):
                continue
            delta = choice.get("delta")
            if not isinstance(delta, dict):
                continue
            # 仅返回正式回复内容（delta.content）
            # DeepSeek 等模型把思考过程放在 delta.reasoning_content，
            # 该内容不应展示给用户，这里统一过滤，只产出最终回答。
            content = delta.get("content")
            if isinstance(content, str) and content:
                return content
        return None
