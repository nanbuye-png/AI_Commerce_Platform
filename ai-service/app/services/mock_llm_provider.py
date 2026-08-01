import asyncio
from collections.abc import AsyncIterator
from typing import Mapping

from app.services.llm_provider import LlmContext


class MockLlmProvider:
    """Deterministic provider used until a production LLM adapter is configured."""

    async def stream(
        self,
        message: str,
        context: LlmContext | None = None,
    ) -> AsyncIterator[str]:
        response = self._response(message, context)
        for token in response:
            await asyncio.sleep(0)
            yield token

    @staticmethod
    def _response(message: str, context: LlmContext | None) -> str:
        product_search = context.get("product_search") if context else None
        if not isinstance(product_search, Mapping):
            return f"收到您的需求：{message}。我会根据预算和使用场景为您整理商品建议。"

        products = product_search.get("products")
        if not isinstance(products, list) or not products:
            return "暂未找到符合条件的商品，建议放宽关键词或价格范围后再试。"

        names = [
            str(product["productName"])
            for product in products[:3]
            if isinstance(product, Mapping) and product.get("productName")
        ]
        if not names:
            return f"已找到 {len(products)} 件符合条件的商品，请查看商品卡片了解详情。"
        return f"已找到 {len(products)} 件符合条件的商品，优先可以看看：{'、'.join(names)}。"
