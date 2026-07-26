-- ============================================================
-- V6: Fix Inventory Movement - Add missing updated_time column
-- Description: InventoryMovement extends BaseEntity which requires
--              updated_time column. V3/V4 did not include this column.
-- ============================================================

ALTER TABLE inventory_movement
    ADD COLUMN IF NOT EXISTS updated_time TIMESTAMP WITHOUT TIME ZONE;