"""
AI 用量统计接口
内部接口，供 commerce-platform 转发给 admin 端
"""
from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse

from app.core.internal_auth import require_internal_token
from app.services.usage_tracker import usage_tracker

router = APIRouter()


@router.get(
    "/internal/ai/stats",
    dependencies=[Depends(require_internal_token)],
    response_class=JSONResponse,
)
async def get_usage_stats() -> JSONResponse:
    """返回 AI 调用统计快照"""
    return JSONResponse(content={"code": 0, "message": "success", "data": usage_tracker.snapshot()})