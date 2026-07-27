-- 订单操作日志表
-- 记录订单生命周期中的操作行为，独立于 Order Entity
CREATE TABLE IF NOT EXISTS order_operation_logs (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL,
    operator_id BIGINT,
    operator_type VARCHAR(20) NOT NULL DEFAULT 'SYSTEM',
    operation_type VARCHAR(32) NOT NULL,
    reason VARCHAR(500),
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_log_order_no ON order_operation_logs(order_no);
CREATE INDEX IF NOT EXISTS idx_log_created_time ON order_operation_logs(created_time);