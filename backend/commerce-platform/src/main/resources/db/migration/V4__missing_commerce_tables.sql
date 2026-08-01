-- Tables introduced after the original database snapshot.
-- Kept in a forward-only migration so existing installations remain compatible.

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(256) NOT NULL,
    sku_name VARCHAR(256) NOT NULL,
    sku_code VARCHAR(64) NOT NULL,
    price NUMERIC(18, 2) NOT NULL,
    original_price NUMERIC(18, 2),
    image VARCHAR(512),
    quantity INTEGER NOT NULL,
    subtotal NUMERIC(18, 2) NOT NULL,
    weight NUMERIC(10, 3) DEFAULT 0,
    created_time TIMESTAMP WITHOUT TIME ZONE,
    updated_time TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_sku_id ON order_items(sku_id);

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
    updated_time TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT uk_order_addresses_order_id UNIQUE (order_id),
    CONSTRAINT fk_order_addresses_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE IF NOT EXISTS order_operation_logs (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL,
    operator_id BIGINT,
    operator_type VARCHAR(20) NOT NULL,
    operation_type VARCHAR(32) NOT NULL,
    reason VARCHAR(500),
    created_time TIMESTAMP WITHOUT TIME ZONE,
    updated_time TIMESTAMP WITHOUT TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_log_order_no ON order_operation_logs(order_no);
CREATE INDEX IF NOT EXISTS idx_log_created_time ON order_operation_logs(created_time);

CREATE TABLE IF NOT EXISTS return_request (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    refund_id BIGINT,
    reason VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    approved_at TIMESTAMP WITHOUT TIME ZONE,
    completed_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_return_request_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_return_request_refund FOREIGN KEY (refund_id) REFERENCES refund(id)
);

CREATE INDEX IF NOT EXISTS idx_return_request_order_id ON return_request(order_id);
CREATE INDEX IF NOT EXISTS idx_return_request_user_id ON return_request(user_id);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id)
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
);