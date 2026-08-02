-- ============================================================
-- V8__customer_profile_features.sql
-- C 端个人中心功能表：收货地址、优惠券、收藏夹、浏览历史
-- 同时为 product_sku 补充库存种子数据（便于演示库存校验）
--
-- 注意：所有实体继承 BaseEntity（id/created_time/updated_time），
-- 建表时已包含 created_time / updated_time 字段。
-- ============================================================

-- 收货地址表
CREATE TABLE IF NOT EXISTS user_address (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    receiver VARCHAR(64) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    province VARCHAR(32),
    city VARCHAR(32),
    district VARCHAR(32),
    detail_address VARCHAR(256),
    postal_code VARCHAR(10),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_time TIMESTAMP WITHOUT TIME ZONE,
    updated_time TIMESTAMP WITHOUT TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_user_address_user_id ON user_address(user_id);

-- 用户优惠券表
CREATE TABLE IF NOT EXISTS user_coupon (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coupon_name VARCHAR(128) NOT NULL,
    coupon_type VARCHAR(20) NOT NULL,
    discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    min_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'UNUSED',
    expire_time TIMESTAMP WITHOUT TIME ZONE,
    created_time TIMESTAMP WITHOUT TIME ZONE,
    updated_time TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT user_coupon_status_check CHECK (status IN ('UNUSED','USED','EXPIRED'))
);

-- 收藏夹表
CREATE TABLE IF NOT EXISTS user_favorite (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(256),
    product_image VARCHAR(512),
    price NUMERIC(12,2),
    created_time TIMESTAMP WITHOUT TIME ZONE,
    updated_time TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT uk_user_favorite UNIQUE (user_id, product_id)
);
CREATE INDEX IF NOT EXISTS idx_user_favorite_user_id ON user_favorite(user_id);

-- 浏览历史表（含 BaseEntity 的 created_time / updated_time）
CREATE TABLE IF NOT EXISTS browse_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(256),
    product_image VARCHAR(512),
    price NUMERIC(12,2),
    viewed_time TIMESTAMP WITHOUT TIME ZONE,
    created_time TIMESTAMP WITHOUT TIME ZONE,
    updated_time TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT uk_browse_history UNIQUE (user_id, product_id)
);
CREATE INDEX IF NOT EXISTS idx_browse_history_user_id ON browse_history(user_id);

-- 库存种子数据：为部分 SKU 配置库存，便于验证库存校验逻辑
INSERT INTO public.inventory (product_id, sku_id, available_stock, locked_stock, reserved_stock, sold_stock, status, created_time, updated_time)
SELECT p.id, s.id, 10, 0, 0, 0, 'AVAILABLE', NOW(), NOW()
FROM public.product_sku s
JOIN public.product p ON p.id = s.product_id
WHERE s.id BETWEEN 20001 AND 20050
ON CONFLICT DO NOTHING;

-- 将 SKU-BOOK-001（20001，星耀轻薄本 Pro 14）库存调低为 1，用于演示"库存=1、购买2"场景
UPDATE public.inventory SET available_stock = 1 WHERE sku_id = 20001;

-- 将 SKU-PHONE-001（20006，旗舰影像手机 Pro）库存调低为 3
UPDATE public.inventory SET available_stock = 3 WHERE sku_id = 20006;

-- ============================================================
-- Rollback 提示:
--   DROP TABLE IF EXISTS user_address, user_coupon, user_favorite, browse_history;
-- ============================================================