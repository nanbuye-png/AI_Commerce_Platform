from typing import Any

import httpx

from app import config
from app.models.chat import ProductSearchIntent


class CommerceToolError(RuntimeError):
    pass


class CommerceTool:
    def __init__(self, client: httpx.AsyncClient | None = None) -> None:
        self._client = client

    async def search_products(self, intent: ProductSearchIntent) -> dict[str, Any]:
        params = {
            "keyword": intent.keyword,
            "categoryId": intent.category_id,
            "minPrice": intent.min_price,
            "maxPrice": intent.max_price,
            "page": intent.page,
            "pageSize": intent.page_size,
            "sortBy": intent.sort_by,
        }
        params = {key: value for key, value in params.items() if value is not None}
        headers = {"X-Internal-Token": config.INTERNAL_API_TOKEN}

        try:
            if self._client is not None:
                response = await self._client.get(
                    "/api/internal/ai/products/search", params=params, headers=headers
                )
            else:
                async with httpx.AsyncClient(
                    base_url=config.COMMERCE_CORE_BASE_URL,
                    timeout=config.COMMERCE_CORE_TIMEOUT_SECONDS,
                ) as client:
                    response = await client.get(
                        "/api/internal/ai/products/search", params=params, headers=headers
                    )
            response.raise_for_status()
            payload = response.json()
        except (httpx.HTTPError, ValueError) as exc:
            raise CommerceToolError("commerce product search failed") from exc

        data = payload.get("data") if isinstance(payload, dict) else None
        if not isinstance(data, dict) or not isinstance(data.get("items"), list):
            raise CommerceToolError("commerce product search returned an invalid payload")
        return data