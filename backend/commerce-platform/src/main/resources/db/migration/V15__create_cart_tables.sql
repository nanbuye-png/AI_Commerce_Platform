-- ============================================================
-- V15: Create cart tables
-- Description: Create cart and cart_item tables for Cart Domain.
-- ============================================================

CREATE TABLE IF NOT EXISTS cart (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    created_time    TIMESTAMP WITHOUT TIME ZONE,
    updated_time    TIMESTAMP WITHOUT TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_cart_user_id ON cart (user_id);

CREATE TABLE IF NOT EXISTS cart_item (
    id              BIGSERIAL       PRIMARY KEY,
    cart_id         BIGINT          NOT NULL,
    product_id      BIGINT          NOT NULL,
    sku_id          BIGINT          NOT NULL,
    product_name    VARCHAR(200),
    product_image   VARCHAR(500),
    price           DECIMAL(10,2)   NOT NULL,
    quantity        INTEGER         NOT NULL,
    selected        BOOLEAN         NOT NULL DEFAULT TRUE,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_time    TIMESTAMP WITHOUT TIME ZONE,
    updated_time    TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT uk_cart_item_sku UNIQUE (cart_id, sku_id)
);