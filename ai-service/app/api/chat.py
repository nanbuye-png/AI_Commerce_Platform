import json
from collections.abc import AsyncIterator
from uuid import uuid4

from fastapi import APIRouter, Depends
from fastapi.responses import StreamingResponse

from app.core.internal_auth import require_internal_token
from app.models.chat import ChatStreamRequest
from app.services.llm_provider import LlmProvider
from app.services.mock_llm_provider import MockLlmProvider

router = APIRouter()
provider: LlmProvider = MockLlmProvider()


def _sse(event: str, data: dict[str, object]) -> str:
    payload = json.dumps(data, ensure_ascii=False, separators=(",", ":"))
    return f"event: {event}\ndata: {payload}\n\n"


async def _stream_chat(request: ChatStreamRequest) -> AsyncIterator[str]:
    conversation_id = request.conversation_id or f"conv_{uuid4().hex}"
    message_id = f"msg_{uuid4().hex}"

    async for token in provider.stream(request.message):
        yield _sse("message", {"type": "token", "content": token})

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
