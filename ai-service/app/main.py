"""
AI Service 主入口
提供 AI 对话、推荐、内容生成等能力
"""
import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import SERVICE_NAME, SERVICE_HOST, SERVICE_PORT, DEBUG, API_PREFIX
from app.api.health import router as health_router
from app.api.chat import router as chat_router

app = FastAPI(
    title=SERVICE_NAME,
    version="0.1.0",
    description="AI 智能服务 - 对话、推荐、内容生成",
)

# CORS 配置
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 注册路由
app.include_router(health_router, prefix=API_PREFIX, tags=["Health"])
app.include_router(chat_router, prefix=API_PREFIX, tags=["AI Chat"])


if __name__ == "__main__":
    uvicorn.run(
        "app.main:app",
        host=SERVICE_HOST,
        port=SERVICE_PORT,
        reload=DEBUG,
    )