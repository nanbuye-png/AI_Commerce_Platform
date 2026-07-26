-- ============================================================
-- V8: Create product_audit_record table
-- Description: ProductAuditRecord entity exists but the table
--              was never created in any previous migration.
-- ============================================================

CREATE TABLE IF NOT EXISTS product_audit_record (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL,
    before_status VARCHAR(20) NOT NULL,
    after_status VARCHAR(20) NOT NULL,
    audit_remark VARCHAR(500),
    created_time TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_product_id ON product_audit_record(product_id);
CREATE INDEX IF NOT EXISTS idx_audit_created ON product_audit_record(created_time);