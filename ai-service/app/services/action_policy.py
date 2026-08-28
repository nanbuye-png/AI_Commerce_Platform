"""Deterministic policy for commerce write actions."""


def decide_add_to_cart(
    *,
    action: str | None,
    user_id: int | None,
    sku_id: int | None,
    quantity: int | None,
    confirmed: bool,
) -> str:
    if action != "add_to_cart":
        return "none"
    if user_id is None or user_id < 1 or sku_id is None or sku_id < 1:
        return "invalid"
    if quantity is None or quantity < 1 or quantity > 20:
        return "invalid"
    return "execute" if confirmed else "confirm"
