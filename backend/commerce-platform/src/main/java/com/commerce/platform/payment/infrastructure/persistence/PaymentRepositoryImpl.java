package com.commerce.platform.payment.infrastructure.persistence;

import com.commerce.platform.payment.domain.aggregate.Payment;
import com.commerce.platform.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = toEntity(payment);
        PaymentEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Payment> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId).map(this::toDomain);
    }

    @Override
    public Optional<Payment> findByPaymentNo(String paymentNo) {
        return jpaRepository.findByPaymentNo(paymentNo).map(this::toDomain);
    }

    PaymentEntity toEntity(Payment domain) {
        PaymentEntity entity = new PaymentEntity();
        if (domain.getId() != null) entity.setId(domain.getId());
        entity.setOrderId(domain.getOrderId());
        entity.setUserId(domain.getUserId());
        entity.setAmount(domain.getAmount());
        entity.setStatus(domain.getStatus());
        entity.setPaymentNo(domain.getPaymentNo());
        entity.setTransactionNo(domain.getTransactionNo());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setPaidAt(domain.getPaidAt());
        entity.setFailedAt(domain.getFailedAt());
        return entity;
    }

    Payment toDomain(PaymentEntity entity) {
        return Payment.restore(
                entity.getId(), entity.getOrderId(), entity.getUserId(),
                entity.getAmount(), entity.getStatus(), entity.getPaymentNo(),
                entity.getTransactionNo(), entity.getCreatedAt(),
                entity.getPaidAt(), entity.getFailedAt());
    }
}