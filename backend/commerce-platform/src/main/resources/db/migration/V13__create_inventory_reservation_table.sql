-- ============================================================
-- V13: Create inventory_reservation table
-- Description: Create the inventory reservation table for
--              tracking order-inventory locking relationships.
-- ============================================================

CREATE TABLE IF NOT EXISTS inventory_reservation (
    id              BIGSERIAL       PRIMARY KEY,
    order_no        VARCHAR(32)     NOT NULL,
    sku_id          BIGINT          NOT NULL,
    quantity        INTEGER         NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'LOCKED',
    created_time    TIMESTAMP WITHOUT TIME ZONE,
    updated_time    TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT uk_inv_reservation_order_sku UNIQUE (order_no, sku_id)
);

-- 安全创建索引：先检查列是否存在（兼容已存在的旧表）
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory_reservation' AND column_name = 'order_no') THEN
        CREATE INDEX IF NOT EXISTS idx_inv_reservation_order_no ON inventory_reservation (order_no);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory_reservation' AND column_name = 'sku_id') THEN
        CREATE INDEX IF NOT EXISTS idx_inv_reservation_sku_id ON inventory_reservation (sku_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory_reservation' AND column_name = 'status') THEN
        CREATE INDEX IF NOT EXISTS idx_inv_reservation_status ON inventory_reservation (status);
    END IF;
END
$$;