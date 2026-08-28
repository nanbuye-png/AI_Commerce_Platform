"""
AI 用量统计接口
内部接口，供 commerce-platform 转发给 admin 端
"""
from fastapi import APIRouter, Depends, Header
from fastapi.responses import JSONResponse
from uuid import UUID, uuid4

from app.core.internal_auth import require_internal_token
from app.services.usage_tracker import usage_tracker

router = APIRouter()


def _request_id(value: str | None) -> str:
    if value:
        try:
            return str(UUID(value))
        except ValueError:
            pass
    return str(uuid4())


@router.get(
    "/internal/ai/stats",
    dependencies=[Depends(require_internal_token)],
    response_class=JSONResponse,
)
async def get_usage_stats(x_request_id: str | None = Header(default=None, alias="X-Request-Id")) -> JSONResponse:
    """返回 AI 调用统计快照"""
    request_id = _request_id(x_request_id)
    return JSONResponse(
        content={"code": 0, "message": "success", "data": usage_tracker.snapshot()},
        headers={"X-Request-Id": request_id},
    )
