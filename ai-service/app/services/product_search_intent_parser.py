import re
from decimal import Decimal, InvalidOperation

from app.models.chat import ProductSearchIntent


class ProductSearchIntentParser:
    _SEARCH_MARKERS = ("找", "搜索", "推荐", "商品", "买", "看看", "有没有", "想要", "想买", "咨询")
    _PRODUCT_TERMS = (
        # 电子产品
        "耳机", "手机", "电脑", "笔记本", "键盘", "鼠标", "相机", "手表",
        "平板", "显示器", "充电器", "音箱", "游戏机", "路由",
        # 服装（更具体的词排在前面，避免被短词先命中）
        "衣服", "服装", "裤子", "裙子", "连衣裙", "T恤", "衬衫", "外套", "大衣",
        "夹克", "卫衣", "毛衣", "羽绒服", "风衣", "西装", "牛仔裤", "运动鞋",
        "皮鞋", "靴子", "凉鞋", "半身裙", "打底裤", "短裤", "背心", "内衣",
        "睡衣", "帽子", "围巾", "手套", "袜子", "鞋",
        # 箱包
        "包", "背包", "双肩包", "手提包", "钱包", "行李箱", "旅行箱",
        # 家居
        "家具", "沙发", "床垫", "床头柜", "衣柜", "书桌", "餐桌", "椅子", "柜子",
        "四件套", "被子", "枕头", "窗帘", "地毯", "厨具", "咖啡", "灯具", "收纳",
        "床", "锅", "餐具", "杯",
        # 图书
        "图书", "小说", "教材", "绘本", "漫画", "书",
        # 运动
        "运动", "健身", "哑铃", "瑜伽", "跑步机", "自行车", "露营", "帐篷",
        "篮球", "足球", "羽毛球", "乒乓球", "泳衣", "泳镜", "球",
        # 美妆
        "护肤", "面霜", "精华", "眼霜", "面膜", "口红", "眼影", "粉底",
        "保湿", "防晒", "香水", "洗发水", "洗面奶", "彩妆", "腮红", "眉笔",
        # 其他常见
        "零食", "饮料", "茶", "奶粉", "玩具", "文具", "宠物", "数码", "家电",
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
        for word in ("请", "帮我", "给我", "找", "搜索", "推荐", "商品", "买", "看看", "有没有",
                     "想要", "想买", "咨询", "以内", "以下", "以上", "左右", "推荐一下", "有没有推荐"):
            cleaned = cleaned.replace(word, " ")
        cleaned = re.sub(r"[，。！？,.!?\s]+", "", cleaned)
        return cleaned[:40] or None

    @staticmethod
    def _decimal(value: str) -> Decimal:
        try:
            return Decimal(value)
        except InvalidOperation as exc:
            raise ValueError("invalid price") from exc