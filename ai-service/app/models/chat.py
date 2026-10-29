from typing import Literal

from pydantic import BaseModel, Field


class ChatHistoryMessage(BaseModel):
    role: Literal['user', 'assistant']
    content: str = Field(min_length=1, max_length=2000)


class ChatStreamRequest(BaseModel):
    message: str = Field(min_length=1, max_length=4000)
    conversation_id: str | None = Field(default=None, min_length=1, max_length=128)
    user_id: int | None = Field(default=None, ge=1)
    action: str | None = Field(default=None, max_length=32)
    sku_id: int | None = Field(default=None, ge=1)
    quantity: int | None = Field(default=None, ge=1, le=20)
    confirmed: bool = False
    history: list[ChatHistoryMessage] = Field(default_factory=list, max_length=12)


class ProductSearchIntent(BaseModel):
    keyword: str | None = None
    category_id: int | None = None
    min_price: float | None = None
    max_price: float | None = None
    page: int = Field(default=1, ge=1, le=100)
    page_size: int = Field(default=6, ge=1, le=20)
    sort_by: str | None = None
