-- ============================================================
-- V7: Fix missing updated_time columns for order tables
-- Description: OrderAddress and OrderItem extend BaseEntity but
--              V5 did not include updated_time column.
-- Affected tables: order_addresses, order_items
-- ============================================================

ALTER TABLE order_addresses
    ADD COLUMN IF NOT EXISTS updated_time TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE order_items
    ADD COLUMN IF NOT EXISTS updated_time TIMESTAMP WITHOUT TIME ZONE;