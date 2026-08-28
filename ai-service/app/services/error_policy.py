"""Stable public errors that never expose upstream response details."""


def public_error_message(component: str) -> str:
    messages = {
        "product_search": "商品搜索暂时不可用，请稍后再试。",
        "add_to_cart": "商品暂时无法加入购物车，请稍后再试。",
        "provider": "AI 生成暂时不可用，请稍后再试。",
        "invalid_action": "缺少有效的加购参数。",
    }
    return messages.get(component, "服务暂时不可用，请稍后再试。")
