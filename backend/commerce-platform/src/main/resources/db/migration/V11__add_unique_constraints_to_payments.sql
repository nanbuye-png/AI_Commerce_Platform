-- 支付表约束增强
-- 添加缺失的唯一约束和索引

-- transaction_no 应该唯一（第三方交易号不能重复）
-- 但由于部分记录可能为 NULL，使用 partial unique index
DROP INDEX IF EXISTS idx_transaction_no;
CREATE UNIQUE INDEX IF NOT EXISTS idx_transaction_no ON payments(transaction_no) WHERE transaction_no IS NOT NULL;

-- 确保 payment_no 唯一索引存在
DROP INDEX IF EXISTS idx_payment_no;
CREATE UNIQUE INDEX IF NOT EXISTS idx_payment_no ON payments(payment_no);