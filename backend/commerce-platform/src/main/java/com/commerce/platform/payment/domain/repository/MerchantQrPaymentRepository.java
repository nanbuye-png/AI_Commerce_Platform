package com.commerce.platform.payment.domain.repository;

import com.commerce.platform.payment.domain.entity.MerchantQrPayment;
import com.commerce.platform.payment.domain.entity.MerchantQrPaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 商户二维码收款流水仓储
 */
@Repository
public interface MerchantQrPaymentRepository extends JpaRepository<MerchantQrPayment, Long> {

    Optional<MerchantQrPayment> findByQrTokenAndStatus(String qrToken, MerchantQrPaymentStatus status);

    Optional<MerchantQrPayment> findByOrderNoAndStatus(String orderNo, MerchantQrPaymentStatus status);

    List<MerchantQrPayment> findByStatusAndExpireTimeBefore(MerchantQrPaymentStatus status, LocalDateTime now);
}