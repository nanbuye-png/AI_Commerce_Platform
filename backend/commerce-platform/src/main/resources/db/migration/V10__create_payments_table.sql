-- 支付表
-- Payment Domain 聚合根
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    payment_no VARCHAR(32) NOT NULL,
    order_no VARCHAR(32) NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    transaction_no VARCHAR(64),
    paid_time TIMESTAMP,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_payment_no ON payments(payment_no);
CREATE INDEX IF NOT EXISTS idx_order_no ON payments(order_no);
CREATE INDEX IF NOT EXISTS idx_transaction_no ON payments(transaction_no);