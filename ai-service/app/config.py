"""
全局配置文件
集中管理 AI Service 的所有配置项
"""
import os
from pathlib import Path

# 项目根目录
BASE_DIR = Path(__file__).resolve().parent.parent

# 服务配置
SERVICE_NAME = os.getenv("AI_SERVICE_NAME", "ai-service")
SERVICE_PORT = int(os.getenv("AI_SERVICE_PORT", "8000"))
SERVICE_HOST = os.getenv("AI_SERVICE_HOST", "0.0.0.0")
DEBUG = os.getenv("AI_SERVICE_DEBUG", "false").lower() == "true"

# API 前缀
API_PREFIX = os.getenv("AI_API_PREFIX", "/api/v1")

# 模型配置
MODEL_PATH = os.getenv("AI_MODEL_PATH", str(BASE_DIR / "models"))
DEFAULT_MODEL = os.getenv("AI_DEFAULT_MODEL", "gpt-4o-mini")

# Internal service authentication. Production deployments must provide this value.
INTERNAL_API_TOKEN = os.getenv("AI_INTERNAL_API_TOKEN", "")

# 日志配置
LOG_LEVEL = os.getenv("AI_LOG_LEVEL", "INFO")
LOG_FORMAT = "%(asctime)s - %(name)s - %(levelname)s - %(message)s"