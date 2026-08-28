from typing import Any

import httpx

from app import config
from app.models.chat import ProductSearchIntent


class CommerceToolError(RuntimeError):
    pass


class CommerceTool:
    def __init__(self, client: httpx.AsyncClient | None = None) -> None:
        self._client = client

    async def search_products(
        self, intent: ProductSearchIntent, *, request_id: str | None = None
    ) -> dict[str, Any]:
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

        try:
            if self._client is not None:
                response = await self._client.get(
                    "/api/internal/ai/products/search",
                    params=params,
                    headers=self._headers(request_id),
                )
            else:
                async with httpx.AsyncClient(
                    base_url=config.COMMERCE_CORE_BASE_URL,
                    timeout=config.COMMERCE_CORE_TIMEOUT_SECONDS,
                ) as client:
                    response = await client.get(
                        "/api/internal/ai/products/search",
                        params=params,
                        headers=self._headers(request_id),
                    )
            response.raise_for_status()
            payload = response.json()
        except (httpx.HTTPError, ValueError) as exc:
            raise CommerceToolError("commerce product search failed") from exc

        data = payload.get("data") if isinstance(payload, dict) else None
        if not isinstance(data, dict) or not isinstance(data.get("items"), list):
            raise CommerceToolError("commerce product search returned an invalid payload")
        return data

    async def add_to_cart(
        self,
        *,
        user_id: int,
        sku_id: int,
        quantity: int,
        request_id: str | None = None,
    ) -> dict[str, Any]:
        payload = {"userId": user_id, "skuId": sku_id, "quantity": quantity}

        try:
            if self._client is not None:
                response = await self._client.post(
                    "/api/internal/ai/cart/items",
                    json=payload,
                    headers=self._headers(request_id),
                )
            else:
                async with httpx.AsyncClient(
                    base_url=config.COMMERCE_CORE_BASE_URL,
                    timeout=config.COMMERCE_CORE_TIMEOUT_SECONDS,
                ) as client:
                    response = await client.post(
                        "/api/internal/ai/cart/items",
                        json=payload,
                        headers=self._headers(request_id),
                    )
            response.raise_for_status()
            result = response.json()
        except (httpx.HTTPError, ValueError) as exc:
            raise CommerceToolError("commerce add-to-cart failed") from exc

        data = result.get("data") if isinstance(result, dict) else None
        if not isinstance(data, dict) or not isinstance(data.get("items"), list):
            raise CommerceToolError("commerce add-to-cart returned an invalid payload")
        return data

    @staticmethod
    def _headers(request_id: str | None) -> dict[str, str]:
        headers = {"X-Internal-Token": config.INTERNAL_API_TOKEN}
        if request_id:
            headers["X-Request-Id"] = request_id
        return headers
