-- ============================================================
-- V3: Create Inventory Domain Tables
-- Description: Create tables for Inventory Domain (inventory,
--              inventory_reservation, inventory_movement)
-- ============================================================

-- ============================================================
-- 1. inventory — 库存（聚合根）
-- 采用三字段模型：available_stock / reserved_stock / total_stock
-- 约束：total_stock = available_stock + reserved_stock（应用层保证）
-- ============================================================
CREATE TABLE IF NOT EXISTS inventory (
    id BIGSERIAL PRIMARY KEY,
    product_sku_id BIGINT NOT NULL,
    available_stock INTEGER NOT NULL DEFAULT 0,
    reserved_stock INTEGER NOT NULL DEFAULT 0,
    total_stock INTEGER NOT NULL DEFAULT 0,
    low_stock_threshold INTEGER NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_time TIMESTAMP WITHOUT TIME ZONE,
    updated_time TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT uk_inventory_sku_id UNIQUE (product_sku_id)
);

CREATE INDEX IF NOT EXISTS idx_inventory_low_stock ON inventory(low_stock_threshold, available_stock);

-- ============================================================
-- 2. inventory_reservation — 库存预占
-- 独立表设计，不依赖 inventory 单一字段记录所有锁定关系
-- 支持：一个订单多个 SKU、超时释放、支付成功、售后释放
-- ============================================================
CREATE TABLE IF NOT EXISTS inventory_reservation (
    id BIGSERIAL PRIMARY KEY,
    reservation_no VARCHAR(64) NOT NULL,
    inventory_id BIGINT NOT NULL,
    product_sku_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expire_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_time TIMESTAMP WITHOUT TIME ZONE,
    updated_time TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT uk_reservation_no UNIQUE (reservation_no)
);

CREATE INDEX IF NOT EXISTS idx_reservation_inventory_id ON inventory_reservation(inventory_id);
CREATE INDEX IF NOT EXISTS idx_reservation_sku_id ON inventory_reservation(product_sku_id);
CREATE INDEX IF NOT EXISTS idx_reservation_order_id ON inventory_reservation(order_id);
CREATE INDEX IF NOT EXISTS idx_reservation_status_expired ON inventory_reservation(status, expire_time);
CREATE INDEX IF NOT EXISTS idx_reservation_created_time ON inventory_reservation(created_time);

-- ============================================================
-- 3. inventory_movement — 库存流水
-- Append-Only 模式：仅 INSERT，不 UPDATE，不 DELETE
-- 记录变动前后三字段快照，用于审计和对账
-- ============================================================
CREATE TABLE IF NOT EXISTS inventory_movement (
    id BIGSERIAL PRIMARY KEY,
    movement_no VARCHAR(64) NOT NULL,
    product_sku_id BIGINT NOT NULL,
    inventory_id BIGINT NOT NULL,
    movement_type VARCHAR(20) NOT NULL,
    quantity INTEGER NOT NULL,
    before_available INTEGER NOT NULL,
    after_available INTEGER NOT NULL,
    operator_id BIGINT,
    business_id VARCHAR(64),
    remark VARCHAR(256),
    created_time TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT uk_movement_no UNIQUE (movement_no)
);

CREATE INDEX IF NOT EXISTS idx_movement_sku_id ON inventory_movement(product_sku_id);
CREATE INDEX IF NOT EXISTS idx_movement_inventory_id ON inventory_movement(inventory_id);
CREATE INDEX IF NOT EXISTS idx_movement_type ON inventory_movement(movement_type);
CREATE INDEX IF NOT EXISTS idx_movement_business_id ON inventory_movement(business_id);
CREATE INDEX IF NOT EXISTS idx_movement_sku_created ON inventory_movement(product_sku_id, created_time);
CREATE INDEX IF NOT EXISTS idx_movement_created_time ON inventory_movement(created_time);