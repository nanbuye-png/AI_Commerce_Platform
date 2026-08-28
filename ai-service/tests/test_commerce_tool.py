import httpx
from app.services.commerce_tool import CommerceTool, CommerceToolError


async def _add_to_cart_returns_cart_data(monkeypatch) -> None:
    monkeypatch.setattr("app.services.commerce_tool.config.INTERNAL_API_TOKEN", "secret")

    async def handler(request: httpx.Request) -> httpx.Response:
        assert request.method == "POST"
        assert request.url.path == "/api/internal/ai/cart/items"
        assert request.headers["X-Internal-Token"] == "secret"
        import json

        assert json.loads(request.content) == {"userId": 3, "skuId": 8, "quantity": 2}
        return httpx.Response(200, json={"data": {"items": [{"skuId": 8, "quantity": 2}]}})

    async with httpx.AsyncClient(
        transport=httpx.MockTransport(handler), base_url="http://commerce.test"
    ) as client:
        result = await CommerceTool(client).add_to_cart(user_id=3, sku_id=8, quantity=2)

    assert result["items"][0]["skuId"] == 8


async def _add_to_cart_raises_for_invalid_payload() -> None:
    async def handler(_request: httpx.Request) -> httpx.Response:
        return httpx.Response(200, json={"data": {"id": 1}})

    async with httpx.AsyncClient(
        transport=httpx.MockTransport(handler), base_url="http://commerce.test"
    ) as client:
        try:
            await CommerceTool(client).add_to_cart(user_id=3, sku_id=8, quantity=2)
        except CommerceToolError as exc:
            assert "invalid payload" in str(exc)
        else:
            raise AssertionError("expected CommerceToolError")


async def _add_to_cart_wraps_http_failures() -> None:
    async def handler(_request: httpx.Request) -> httpx.Response:
        return httpx.Response(503, json={"message": "service unavailable"})

    async with httpx.AsyncClient(
        transport=httpx.MockTransport(handler), base_url="http://commerce.test"
    ) as client:
        try:
            await CommerceTool(client).add_to_cart(user_id=3, sku_id=8, quantity=2)
        except CommerceToolError as exc:
            assert "add-to-cart failed" in str(exc)
        else:
            raise AssertionError("expected CommerceToolError")


def test_add_to_cart_returns_cart_data(monkeypatch) -> None:
    import asyncio

    asyncio.run(_add_to_cart_returns_cart_data(monkeypatch))


def test_add_to_cart_raises_for_invalid_payload() -> None:
    import asyncio

    asyncio.run(_add_to_cart_raises_for_invalid_payload())


def test_add_to_cart_wraps_http_failures() -> None:
    import asyncio

    asyncio.run(_add_to_cart_wraps_http_failures())
