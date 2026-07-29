-- ============================================================
-- V3__inventory_schema_alignment.sql
-- Sprint 20 Step 3A — Inventory Schema Alignment
-- 
-- 目的：
--   1. 合并 locked_stock → reserved_stock（两列语义完全等价）
--   2. 标记 locked_stock 为废弃
--   3. 为 InventoryStockEntity 补充 status 字段列定义（确保与 Inventory Entity 一致）
--
-- 风险级别: 🟡 MEDIUM
-- 前提条件: 所有写 locked_stock 的代码已迁移到 InventoryStockRepository
-- ============================================================

-- Step 1: 数据对账（执行前手动检查）
-- 检查 locked_stock 和 reserved_stock 是否存在非零值并存的情况
-- 正常情况应该是：只有一列有值，另一列为 0
-- SELECT sku_id, locked_stock, reserved_stock 
-- FROM inventory 
-- WHERE locked_stock > 0 AND reserved_stock > 0;
-- 如果有结果 → 说明两套架构同时操作了同一个 SKU，需要人工裁决

-- Step 2: 合并 locked_stock 到 reserved_stock
UPDATE inventory 
SET reserved_stock = reserved_stock + locked_stock 
WHERE locked_stock > 0;

-- Step 3: 将 locked_stock 置零（不删除列，避免破坏 Hibernate 对旧 Entity 的兼容）
UPDATE inventory SET locked_stock = 0;

-- Step 4: 添加列注释，标记废弃状态
COMMENT ON COLUMN inventory.locked_stock IS 'DEPRECATED by Sprint 20 Step 3A: 已合并到 reserved_stock。将在 Phase 3 DROP。';
COMMENT ON COLUMN inventory.reserved_stock IS '预占库存（含原 locked_stock 合并值）';

-- Step 5: 确保 status 列有默认值（兼容 InventoryStockEntity 未来增加 status 映射）
-- InventoryStockEntity 当前不映射 status 列，但为了与 Inventory Entity 兼容，确保无 NULL
UPDATE inventory SET status = 'AVAILABLE' WHERE status IS NULL;

-- ============================================================
-- Rollback 方案:
-- 
-- 如果迁移后发现问题，执行以下 SQL 回滚：
--
-- -- 回滚 Step 2: 恢复 locked_stock 值
-- UPDATE inventory 
-- SET locked_stock = reserved_stock 
-- WHERE locked_stock = 0 AND reserved_stock > 0;
--
-- -- 回滚 Step 3: 恢复 reserved_stock 值
-- UPDATE inventory SET reserved_stock = 0 WHERE locked_stock > 0;
--
-- -- 回滚注释
-- COMMENT ON COLUMN inventory.locked_stock IS NULL;
-- COMMENT ON COLUMN inventory.reserved_stock IS NULL;
-- ============================================================