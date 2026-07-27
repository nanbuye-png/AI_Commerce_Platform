-- ============================================================
-- V12: Create inventory table
-- Description: Create the inventory table for Inventory Domain.
-- ============================================================

CREATE TABLE IF NOT EXISTS inventory (
    id              BIGSERIAL       PRIMARY KEY,
    product_id      BIGINT          NOT NULL,
    sku_id          BIGINT          NOT NULL,
    available_stock INTEGER         NOT NULL DEFAULT 0,
    locked_stock    INTEGER         NOT NULL DEFAULT 0,
    sold_stock      INTEGER         NOT NULL DEFAULT 0,
    status          VARCHAR(20)     NOT NULL DEFAULT 'AVAILABLE',
    created_time    TIMESTAMP WITHOUT TIME ZONE,
    updated_time    TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT uk_inventory_sku_id UNIQUE (sku_id)
);

-- 安全创建索引：先检查列是否存在（兼容已存在的旧表）
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inventory' AND column_name = 'product_id') THEN
        CREATE INDEX IF NOT EXISTS idx_inventory_product_id ON inventory (product_id);
    END IF;
END
$$;