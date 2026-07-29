package com.commerce.platform.refund.domain.repository;

import com.commerce.platform.refund.domain.aggregate.Refund;

import java.util.Optional;

/**
 * 退款仓储接口
 * <p>
 * 定义退款聚合的持久化操作接口，属于 Port（出站端口）。
 * 仅定义接口，不实现业务逻辑。
 * </p>
 */
public interface RefundRepository {

    /**
     * 保存退款
     *
     * @param refund 退款聚合
     * @return 保存后的退款（含生成的ID）
     */
    Refund save(Refund refund);

    /**
     * 根据ID查询退款
     *
     * @param id 退款ID
     * @return 退款 Optional
     */
    Optional<Refund> findById(Long id);
}