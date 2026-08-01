from collections.abc import AsyncIterator
from typing import Protocol


class LlmProvider(Protocol):
    async def stream(self, message: str) -> AsyncIterator[str]: ...
