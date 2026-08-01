from app.models.chat import ProductSearchIntent
from app.services.product_search_intent_parser import ProductSearchIntentParser


parser = ProductSearchIntentParser()


def test_parses_keyword_and_price_range() -> None:
    intent = parser.parse("帮我找500到1000元的耳机")
    assert intent is not None
    assert intent.keyword == "耳机"
    assert intent.min_price == 500
    assert intent.max_price == 1000


def test_parses_upper_price_bound() -> None:
    intent = parser.parse("推荐1000元以内的键盘")
    assert intent is not None
    assert intent.keyword == "键盘"
    assert intent.min_price is None
    assert intent.max_price == 1000


def test_parses_generic_clothing_term() -> None:
    intent = parser.parse("我想买服装，有推荐的吗")
    assert intent is not None
    assert intent.keyword == "服装"


def test_normalizes_clothing_synonym() -> None:
    """用户说'衣服'应归一化为'服装'，命中服装分类下的商品"""
    intent = parser.parse("我想购买一些衣服")
    assert intent is not None
    assert intent.keyword == "服装"


def test_normalizes_shoe_synonym() -> None:
    """用户说'鞋子'应归一化为'鞋'"""
    intent = parser.parse("推荐一下鞋子")
    assert intent is not None
    assert intent.keyword == "鞋"


def test_parses_generic_computer_term() -> None:
    intent = parser.parse("电脑有推荐的吗")
    assert intent is not None
    assert intent.keyword == "电脑"


def test_parses_hot_recommendation() -> None:
    """热门推荐：不关心具体品类，按销量排序推荐"""
    intent = parser.parse("推荐几款当前热销的商品")
    assert intent is not None
    assert intent.sort_by == "salesCount"
    assert intent.keyword is None


def test_parses_hot_with_category() -> None:
    """'热销的鞋子' → 关键鞋子 + 按销量排序"""
    intent = parser.parse("推荐几款热销的鞋子")
    assert intent is not None
    assert intent.keyword == "鞋"
    assert intent.sort_by == "salesCount"


def test_parses_more_products() -> None:
    """'更多/换一批' → 翻到第 2 页，支持继续推荐"""
    intent = parser.parse("再推荐一些服装")
    assert intent is not None
    assert intent.keyword == "服装"
    assert intent.page == 2


def test_ignores_general_conversation() -> None:
    assert parser.parse("你好，今天怎么样？") is None