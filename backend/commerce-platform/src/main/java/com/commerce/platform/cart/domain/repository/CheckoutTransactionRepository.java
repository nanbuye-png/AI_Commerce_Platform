package com.commerce.platform.cart.domain.repository;

import com.commerce.platform.cart.domain.entity.CheckoutTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 结算交易记录 Repository
 */
@Repository
public interface CheckoutTransactionRepository extends JpaRepository<CheckoutTransaction, Long> {

    Optional<CheckoutTransaction> findByCheckoutNo(String checkoutNo);

    Optional<CheckoutTransaction> findByOrderNo(String orderNo);
}