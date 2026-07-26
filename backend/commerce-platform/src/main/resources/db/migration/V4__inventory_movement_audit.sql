-- ============================================================
-- V4: Inventory Movement Audit Enhancement
-- Description: Add audit fields to inventory_movement table
--              (source_type, source_id, reason_code, before_reserved,
--               after_reserved, operator_name)
-- ============================================================

ALTER TABLE inventory_movement
    ADD COLUMN IF NOT EXISTS source_type VARCHAR(20),
    ADD COLUMN IF NOT EXISTS source_id VARCHAR(64),
    ADD COLUMN IF NOT EXISTS reason_code VARCHAR(30),
    ADD COLUMN IF NOT EXISTS before_reserved INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS after_reserved INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS operator_name VARCHAR(64);

CREATE INDEX IF NOT EXISTS idx_movement_source_type ON inventory_movement(source_type);
CREATE INDEX IF NOT EXISTS idx_movement_reason_code ON inventory_movement(reason_code);
CREATE INDEX IF NOT EXISTS idx_movement_source_id ON inventory_movement(source_id);