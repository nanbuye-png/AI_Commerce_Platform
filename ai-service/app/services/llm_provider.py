from collections.abc import AsyncIterator
from typing import Mapping, Protocol


LlmContext = Mapping[str, object]


class LlmProvider(Protocol):
    async def stream(
        self,
        message: str,
        context: LlmContext | None = None,
    ) -> AsyncIterator[str]: ...
