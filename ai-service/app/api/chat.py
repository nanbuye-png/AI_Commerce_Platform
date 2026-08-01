import json
from collections.abc import AsyncIterator
from uuid import uuid4

from fastapi import APIRouter, Depends
from fastapi.responses import StreamingResponse

from app.core.internal_auth import require_internal_token
from app.models.chat import ChatStreamRequest
from app.services.commerce_tool import CommerceTool, CommerceToolError
from app.services.llm_provider import LlmProvider
from app.services.llm_provider_factory import create_llm_provider
from app.services.product_search_intent_parser import ProductSearchIntentParser

router = APIRouter()
provider: LlmProvider = create_llm_provider()
intent_parser = ProductSearchIntentParser()
commerce_tool = CommerceTool()


def _sse(event: str, data: dict[str, object]) -> str:
    payload = json.dumps(data, ensure_ascii=False, separators=(",", ":"))
    return f"event: {event}\ndata: {payload}\n\n"


async def _stream_chat(request: ChatStreamRequest) -> AsyncIterator[str]:
    conversation_id = request.conversation_id or f"conv_{uuid4().hex}"
    message_id = f"msg_{uuid4().hex}"
    llm_context: dict[str, object] = {}

    intent = intent_parser.parse(request.message)
    if intent is not None:
        try:
            result = await commerce_tool.search_products(intent)
            product_search = {
                "query": intent.model_dump(exclude_none=True),
                "products": result["items"],
                "total": result.get("total", len(result["items"])),
            }
            llm_context["product_search"] = product_search
            yield _sse(
                "meta",
                {
                    "type": "product_search",
                    **product_search,
                },
            )
        except CommerceToolError:
            yield _sse(
                "meta",
                {
                    "type": "product_search_error",
                    "message": "商品搜索暂时不可用，请稍后再试。",
                },
            )

    try:
        async for token in provider.stream(request.message, llm_context or None):
            yield _sse("message", {"type": "token", "content": token})
    except Exception:
        yield _sse(
            "error",
            {"type": "stream_error", "message": "AI 生成暂时不可用，请稍后再试。"},
        )
        return

    yield _sse(
        "done",
        {"conversation_id": conversation_id, "message_id": message_id},
    )


@router.post(
    "/internal/ai/chat/stream",
    dependencies=[Depends(require_internal_token)],
    response_class=StreamingResponse,
)
async def stream_chat(request: ChatStreamRequest) -> StreamingResponse:
    return StreamingResponse(
        _stream_chat(request),
        media_type="text/event-stream",
        headers={"Cache-Control": "no-cache", "X-Accel-Buffering": "no"},
    )
