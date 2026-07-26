-- ============================================================
-- V5: Create Order Domain Tables
-- Description: Create tables for Order Domain (orders,
--              order_items, order_addresses)
-- ============================================================

-- ============================================================
-- 1. orders — 订单主表（聚合根）
-- 采用三状态模型：order_status / payment_status / shipping_status
-- 金额使用 DECIMAL(18,2) 保证精度
-- ============================================================
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL,
    buyer_id BIGINT NOT NULL,
    merchant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    order_status VARCHAR(20) NOT NULL DEFAULT 'PENDING_PAYMENT',
    payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    shipping_status VARCHAR(20) NOT NULL DEFAULT 'UNSHIPPED',
    total_amount DECIMAL(18, 2) NOT NULL,
    product_amount DECIMAL(18, 2) NOT NULL,
    freight_amount DECIMAL(18, 2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(18, 2) NOT NULL DEFAULT 0,
    pay_amount DECIMAL(18, 2) NOT NULL,
    buyer_remark VARCHAR(500),
    merchant_remark VARCHAR(500),
    payment_time TIMESTAMP WITHOUT TIME ZONE,
    shipping_time TIMESTAMP WITHOUT TIME ZONE,
    completed_time TIMESTAMP WITHOUT TIME ZONE,
    cancelled_time TIMESTAMP WITHOUT TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_time TIMESTAMP WITHOUT TIME ZONE,
    updated_time TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT uk_order_no UNIQUE (order_no)
);

CREATE INDEX IF NOT EXISTS idx_order_buyer_id ON orders(buyer_id);
CREATE INDEX IF NOT EXISTS idx_order_merchant_id ON orders(merchant_id);
CREATE INDEX IF NOT EXISTS idx_order_status ON orders(order_status);
CREATE INDEX IF NOT EXISTS idx_order_created_time ON orders(created_time);
CREATE INDEX IF NOT EXISTS idx_order_buyer_status_created ON orders(buyer_id, order_status, created_time);
CREATE INDEX IF NOT EXISTS idx_order_merchant_status_created ON orders(merchant_id, order_status, created_time);

-- ============================================================
-- 2. order_items — 订单条目表
-- 保存下单时的商品快照信息：商品名称、SKU 名称、价格、图片等
-- 创建后不可修改
-- ============================================================
CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(256) NOT NULL,
    sku_name VARCHAR(256) NOT NULL,
    sku_code VARCHAR(64) NOT NULL,
    price DECIMAL(18, 2) NOT NULL,
    original_price DECIMAL(18, 2),
    image VARCHAR(512),
    quantity INTEGER NOT NULL,
    subtotal DECIMAL(18, 2) NOT NULL,
    weight DECIMAL(10, 3) DEFAULT 0,
    created_time TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_order_items_order_id FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_sku_id ON order_items(sku_id);

-- ============================================================
-- 3. order_addresses — 订单收货地址表
-- 保存下单时的收货地址快照
-- 一个订单仅有一条收货地址（order_id 唯一）
-- ============================================================
CREATE TABLE IF NOT EXISTS order_addresses (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    receiver VARCHAR(64) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    province VARCHAR(32) NOT NULL,
    city VARCHAR(32) NOT NULL,
    district VARCHAR(32) NOT NULL,
    detail_address VARCHAR(256) NOT NULL,
    postal_code VARCHAR(10),
    created_time TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT uk_order_address_order_id UNIQUE (order_id),
    CONSTRAINT fk_order_addresses_order_id FOREIGN KEY (order_id) REFERENCES orders(id)
);