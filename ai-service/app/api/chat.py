import json
import inspect
import logging
import time
from collections.abc import AsyncIterator
from uuid import UUID, uuid4

from fastapi import APIRouter, Depends, Header
from fastapi.responses import StreamingResponse

from app.core.internal_auth import require_internal_token
from app.models.chat import ChatStreamRequest
from app.services.action_policy import decide_add_to_cart
from app.services.commerce_tool import CommerceTool, CommerceToolError
from app.services.error_policy import public_error_message
from app.services.llm_provider import LlmProvider
from app.services.llm_provider_factory import create_llm_provider
from app.services.product_search_intent_parser import ProductSearchIntentParser
from app.services.usage_tracker import usage_tracker

router = APIRouter()
provider: LlmProvider = create_llm_provider()
intent_parser = ProductSearchIntentParser()
commerce_tool = CommerceTool()
logger = logging.getLogger(__name__)


async def _search_products(intent, request_id: str):
    method = commerce_tool.search_products
    if "request_id" in inspect.signature(method).parameters:
        return await method(intent, request_id=request_id)
    return await method(intent)


async def _add_to_cart(*, user_id: int, sku_id: int, quantity: int, request_id: str):
    method = commerce_tool.add_to_cart
    kwargs = {"user_id": user_id, "sku_id": sku_id, "quantity": quantity}
    if "request_id" in inspect.signature(method).parameters:
        kwargs["request_id"] = request_id
    return await method(**kwargs)


def _sse(event: str, data: dict[str, object]) -> str:
    payload = json.dumps(data, ensure_ascii=False, separators=(",", ":"))
    return f"event: {event}\ndata: {payload}\n\n"


def _request_id(value: str | None) -> str:
    if value:
        try:
            return str(UUID(value))
        except ValueError:
            pass
    return str(uuid4())


async def _stream_chat(request: ChatStreamRequest, request_id: str) -> AsyncIterator[str]:
    started = time.perf_counter()
    first_token_at: float | None = None
    output_chars = 0
    succeeded = False
    error_type: str | None = None
    scenario = "add_to_cart" if request.action == "add_to_cart" else "chat"
    conversation_id = request.conversation_id or f"conv_{uuid4().hex}"
    message_id = f"msg_{uuid4().hex}"
    llm_context: dict[str, object] = {}

    try:
        action_decision = decide_add_to_cart(
            action=request.action,
            user_id=request.user_id,
            sku_id=request.sku_id,
            quantity=request.quantity,
            confirmed=request.confirmed,
        )
        if action_decision == "invalid":
            error_type = "invalid_action"
            yield _sse(
                "error",
                {"type": "tool_error", "message": public_error_message("invalid_action")},
            )
            return
        if action_decision == "confirm":
            confirmation = {
                "type": "add_to_cart_confirmation",
                "sku_id": request.sku_id,
                "quantity": request.quantity,
            }
            llm_context["add_to_cart"] = {"confirmation_required": True, **confirmation}
            yield _sse("meta", confirmation)
        elif action_decision == "execute":
            tool_started = time.perf_counter()
            try:
                cart = await _add_to_cart(
                    user_id=request.user_id,
                    sku_id=request.sku_id,
                    quantity=request.quantity,
                    request_id=request_id,
                )
                usage_tracker.record_tool(
                    "add_to_cart",
                    succeeded=True,
                    latency_ms=(time.perf_counter() - tool_started) * 1000,
                )
                result = {
                    "type": "add_to_cart_result",
                    "success": True,
                    "sku_id": request.sku_id,
                    "quantity": request.quantity,
                    "cart": cart,
                }
                llm_context["add_to_cart"] = result
                yield _sse("meta", result)
            except CommerceToolError:
                usage_tracker.record_tool(
                    "add_to_cart",
                    succeeded=False,
                    latency_ms=(time.perf_counter() - tool_started) * 1000,
                )
                result = {
                    "type": "add_to_cart_result",
                    "success": False,
                    "sku_id": request.sku_id,
                    "quantity": request.quantity,
                    "message": public_error_message("add_to_cart"),
                }
                llm_context["add_to_cart"] = result
                yield _sse("meta", result)

        intent = intent_parser.parse(request.message)
        if intent is not None:
            scenario = "product_search" if scenario == "chat" else scenario
            tool_started = time.perf_counter()
            try:
                result = await _search_products(intent, request_id)
                usage_tracker.record_tool(
                    "product_search",
                    succeeded=True,
                    latency_ms=(time.perf_counter() - tool_started) * 1000,
                )
                product_search = {
                    "query": intent.model_dump(exclude_none=True),
                    "products": result["items"],
                    "total": result.get("total", len(result["items"])),
                }
                llm_context["product_search"] = product_search
                yield _sse("meta", {"type": "product_search", **product_search})
            except CommerceToolError:
                usage_tracker.record_tool(
                    "product_search",
                    succeeded=False,
                    latency_ms=(time.perf_counter() - tool_started) * 1000,
                )
                yield _sse(
                    "meta",
                    {"type": "product_search_error", "message": public_error_message("product_search")},
                )

        try:
            async for token in provider.stream(request.message, llm_context or None):
                if first_token_at is None:
                    first_token_at = time.perf_counter()
                output_chars += len(token)
                yield _sse("message", {"type": "token", "content": token})
        except Exception:
            error_type = "provider_error"
            yield _sse(
                "error",
                {"type": "stream_error", "message": public_error_message("provider")},
            )
            return

        succeeded = True
        yield _sse("done", {"conversation_id": conversation_id, "message_id": message_id})
    finally:
        finished = time.perf_counter()
        usage_tracker.record_call(
            succeeded=succeeded,
            estimated_input_tokens=max(1, len(request.message)),
            estimated_output_tokens=output_chars,
            ttft_ms=(first_token_at - started) * 1000 if first_token_at is not None else None,
            latency_ms=(finished - started) * 1000,
            scenario=scenario,
        )
        logger.info(
            "ai_stream_completed request_id=%s conversation_id=%s scenario=%s "
            "succeeded=%s latency_ms=%.2f error_type=%s",
            request_id,
            conversation_id,
            scenario,
            succeeded,
            (finished - started) * 1000,
            error_type or "none",
        )


@router.post(
    "/internal/ai/chat/stream",
    dependencies=[Depends(require_internal_token)],
    response_class=StreamingResponse,
)
async def stream_chat(
    request: ChatStreamRequest,
    x_request_id: str | None = Header(default=None, alias="X-Request-Id"),
) -> StreamingResponse:
    request_id = _request_id(x_request_id)
    return StreamingResponse(
        _stream_chat(request, request_id),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "X-Accel-Buffering": "no",
            "X-Request-Id": request_id,
        },
    )
