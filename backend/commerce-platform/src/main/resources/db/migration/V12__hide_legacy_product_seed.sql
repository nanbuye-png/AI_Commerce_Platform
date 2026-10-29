-- V7 inserted a public demo catalog unconditionally, including in production.
-- Match both its fixed ID and product code so merchant-created products are untouched.
-- Soft delete preserves existing order and cart references.
UPDATE public.product
SET deleted = true,
    status = 'OFF_SHELF',
    version = version + 1,
    updated_time = NOW()
WHERE id BETWEEN 10001 AND 10023
  AND merchant_id = 1
  AND store_id = 1
  AND product_code = 'P2026' || LPAD((id - 10000)::text, 5, '0')
  AND deleted = false;
