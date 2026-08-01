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


def test_parses_generic_computer_term() -> None:
    intent = parser.parse("电脑有推荐的吗")
    assert intent is not None
    assert intent.keyword == "电脑"


def test_parses_shoe_term() -> None:
    intent = parser.parse("推荐一下运动鞋")
    assert intent is not None
    assert intent.keyword == "运动鞋"


def test_ignores_general_conversation() -> None:
    assert parser.parse("你好，今天怎么样？") is None