import re
from decimal import Decimal, InvalidOperation

from app.models.chat import ProductSearchIntent


class ProductSearchIntentParser:
    _SEARCH_MARKERS = ("找", "搜索", "推荐", "商品", "买", "看看", "有没有")
    _PRODUCT_TERMS = (
        "耳机", "手机", "电脑", "笔记本", "键盘", "鼠标", "相机", "手表",
        "衣服", "鞋", "包", "护肤", "家具", "咖啡", "茶", "零食",
    )

    def parse(self, message: str) -> ProductSearchIntent | None:
        normalized = message.strip()
        if not normalized or not self._is_product_search(normalized):
            return None

        min_price, max_price = self._extract_price_range(normalized)
        keyword = next((term for term in self._PRODUCT_TERMS if term in normalized), None)
        if keyword is None:
            keyword = self._fallback_keyword(normalized)
        if keyword is None and min_price is None and max_price is None:
            return None

        return ProductSearchIntent(
            keyword=keyword,
            min_price=float(min_price) if min_price is not None else None,
            max_price=float(max_price) if max_price is not None else None,
        )

    def _is_product_search(self, message: str) -> bool:
        return any(marker in message for marker in self._SEARCH_MARKERS) or any(
            term in message for term in self._PRODUCT_TERMS
        )

    @staticmethod
    def _extract_price_range(message: str) -> tuple[Decimal | None, Decimal | None]:
        range_match = re.search(r"(\d+(?:\.\d+)?)\s*(?:元)?\s*(?:到|至|[-~～])\s*(\d+(?:\.\d+)?)", message)
        if range_match:
            first = ProductSearchIntentParser._decimal(range_match.group(1))
            second = ProductSearchIntentParser._decimal(range_match.group(2))
            return min(first, second), max(first, second)

        max_match = re.search(r"(\d+(?:\.\d+)?)\s*(?:元)?\s*(?:以内|以下|左右)", message)
        if max_match:
            return None, ProductSearchIntentParser._decimal(max_match.group(1))

        min_match = re.search(r"(\d+(?:\.\d+)?)\s*(?:元)?\s*(?:以上|起)", message)
        if min_match:
            return ProductSearchIntentParser._decimal(min_match.group(1)), None
        return None, None

    @staticmethod
    def _fallback_keyword(message: str) -> str | None:
        cleaned = re.sub(r"\d+(?:\.\d+)?\s*(?:元)?\s*(?:到|至|[-~～])?", " ", message)
        for word in ("请", "帮我", "给我", "找", "搜索", "推荐", "商品", "买", "看看", "有没有", "以内", "以下", "以上", "左右"):
            cleaned = cleaned.replace(word, " ")
        cleaned = re.sub(r"[，。！？,.!?\s]+", "", cleaned)
        return cleaned[:40] or None

    @staticmethod
    def _decimal(value: str) -> Decimal:
        try:
            return Decimal(value)
        except InvalidOperation as exc:
            raise ValueError("invalid price") from exc