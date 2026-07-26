package com.commerce.platform.product.repository;

import com.commerce.platform.product.entity.ProductAuditRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 商品审核记录 Repository
 */
@Repository
public interface ProductAuditRecordRepository extends JpaRepository<ProductAuditRecord, Long> {
}