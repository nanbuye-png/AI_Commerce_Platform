-- ============================================================
-- V2: Create Product Domain Tables
-- Description: Create tables for Product Domain (category, product,
--              product_image, product_spec, product_sku)
-- ============================================================

-- ============================================================
-- 1. category — 商品分类
-- ============================================================
CREATE TABLE IF NOT EXISTS category (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT NOT NULL DEFAULT 0,
    category_name VARCHAR(64) NOT NULL,
    sort INTEGER NOT NULL DEFAULT 0,
    level INTEGER NOT NULL DEFAULT 1,
    created_time TIMESTAMP WITHOUT TIME ZONE,
    updated_time TIMESTAMP WITHOUT TIME ZONE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_category_parent_id ON category(parent_id);
CREATE INDEX IF NOT EXISTS idx_category_level ON category(level);

-- ============================================================
-- 2. product — 商品 SPU（聚合根）
-- ============================================================
CREATE TABLE IF NOT EXISTS product (
    id BIGSERIAL PRIMARY KEY,
    product_code VARCHAR(64) NOT NULL UNIQUE,
    merchant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    product_name VARCHAR(256) NOT NULL,
    description TEXT,
    brand VARCHAR(64),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    sales_count INTEGER NOT NULL DEFAULT 0,
    created_time TIMESTAMP WITHOUT TIME ZONE,
    updated_time TIMESTAMP WITHOUT TIME ZONE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_product_merchant_id ON product(merchant_id);
CREATE INDEX IF NOT EXISTS idx_product_store_id ON product(store_id);
CREATE INDEX IF NOT EXISTS idx_product_category_id ON product(category_id);
CREATE INDEX IF NOT EXISTS idx_product_merchant_status ON product(merchant_id, status);
CREATE INDEX IF NOT EXISTS idx_product_status_created ON product(status, created_time);

-- ============================================================
-- 3. product_image — 商品图片
-- ============================================================
CREATE TABLE IF NOT EXISTS product_image (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    image_type VARCHAR(20) NOT NULL,
    url VARCHAR(512) NOT NULL,
    sort INTEGER NOT NULL DEFAULT 0,
    is_cover BOOLEAN NOT NULL DEFAULT FALSE,
    created_time TIMESTAMP WITHOUT TIME ZONE,
    updated_time TIMESTAMP WITHOUT TIME ZONE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_product_image_product_id ON product_image(product_id);
CREATE INDEX IF NOT EXISTS idx_product_image_cover ON product_image(product_id, is_cover);

-- ============================================================
-- 4. product_spec — 商品规格模板
-- ============================================================
CREATE TABLE IF NOT EXISTS product_spec (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    spec_name VARCHAR(64) NOT NULL,
    spec_values JSON NOT NULL,
    sort INTEGER NOT NULL DEFAULT 0,
    created_time TIMESTAMP WITHOUT TIME ZONE,
    updated_time TIMESTAMP WITHOUT TIME ZONE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_product_spec_product_id ON product_spec(product_id);

-- ============================================================
-- 5. product_sku — 商品 SKU
-- ============================================================
CREATE TABLE IF NOT EXISTS product_sku (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    sku_code VARCHAR(64) NOT NULL UNIQUE,
    attributes_json JSON NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    original_price DECIMAL(12, 2),
    weight DECIMAL(10, 3) DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    sales_count INTEGER NOT NULL DEFAULT 0,
    created_time TIMESTAMP WITHOUT TIME ZONE,
    updated_time TIMESTAMP WITHOUT TIME ZONE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_product_sku_product_id ON product_sku(product_id);
CREATE INDEX IF NOT EXISTS idx_product_sku_status ON product_sku(product_id, status);