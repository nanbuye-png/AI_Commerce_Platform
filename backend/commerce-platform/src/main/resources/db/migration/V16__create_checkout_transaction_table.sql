-- ============================================================
-- V16: Create checkout_transaction table
-- Description: Create checkout_transaction table for
--              Checkout Saga transaction consistency.
-- ============================================================

CREATE TABLE IF NOT EXISTS checkout_transaction (
    id              BIGSERIAL                   PRIMARY KEY,
    checkout_no     VARCHAR(64)                 NOT NULL,
    user_id         BIGINT                      NOT NULL,
    cart_id         BIGINT                      NOT NULL,
    status          VARCHAR(20)                 NOT NULL DEFAULT 'INIT',
    order_no        VARCHAR(64),
    fail_reason     VARCHAR(500),
    created_time    TIMESTAMP WITHOUT TIME ZONE,
    updated_time    TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT uk_checkout_transaction_checkout_no UNIQUE (checkout_no)
);

CREATE INDEX IF NOT EXISTS idx_checkout_transaction_checkout_no
    ON checkout_transaction (checkout_no);

CREATE INDEX IF NOT EXISTS idx_checkout_transaction_user_id
    ON checkout_transaction (user_id);

CREATE INDEX IF NOT EXISTS idx_checkout_transaction_order_no
    ON checkout_transaction (order_no);