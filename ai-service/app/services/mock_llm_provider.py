import asyncio
from collections.abc import AsyncIterator


class MockLlmProvider:
    """Deterministic provider used until a production LLM adapter is configured."""

    async def stream(self, message: str) -> AsyncIterator[str]:
        response = f"收到您的需求：{message}。我会根据预算和使用场景为您整理商品建议。"
        for token in response:
            await asyncio.sleep(0)
            yield token
