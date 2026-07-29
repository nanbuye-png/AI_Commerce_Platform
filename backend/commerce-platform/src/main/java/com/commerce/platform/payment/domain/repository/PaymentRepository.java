package com.commerce.platform.payment.domain.repository;

import com.commerce.platform.payment.domain.aggregate.Payment;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(Long id);
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByPaymentNo(String paymentNo);

    /** @deprecated 使用 findByOrderId */
    default Optional<Payment> findByOrderNo(String orderNo) {
        try {
            return findByOrderId(Long.parseLong(orderNo));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
