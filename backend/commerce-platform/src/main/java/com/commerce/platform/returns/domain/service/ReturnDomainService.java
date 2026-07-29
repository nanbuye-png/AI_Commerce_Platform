package com.commerce.platform.returns.domain.service;

import com.commerce.platform.returns.domain.aggregate.ReturnRequest;
import com.commerce.platform.returns.domain.valueobject.ReturnReason;
import org.springframework.stereotype.Service;

@Service
public class ReturnDomainService {

    public ReturnRequest createReturn(Long orderId, Long userId, ReturnReason reason) {
        return ReturnRequest.create(orderId, userId, reason);
    }
}