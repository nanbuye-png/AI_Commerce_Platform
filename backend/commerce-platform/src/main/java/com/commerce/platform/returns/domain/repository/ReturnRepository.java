package com.commerce.platform.returns.domain.repository;

import com.commerce.platform.returns.domain.aggregate.ReturnRequest;

import java.util.List;
import java.util.Optional;

public interface ReturnRepository {
    ReturnRequest save(ReturnRequest returnRequest);
    Optional<ReturnRequest> findById(Long id);
    List<ReturnRequest> findByOrderId(Long orderId);
    List<ReturnRequest> findByUserId(Long userId);
}