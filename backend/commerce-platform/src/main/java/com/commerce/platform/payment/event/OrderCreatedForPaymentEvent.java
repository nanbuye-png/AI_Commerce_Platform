package com.commerce.platform.payment.event;

@Deprecated
public class OrderCreatedForPaymentEvent {
    public OrderCreatedForPaymentEvent(Long paymentId, Long orderId, java.math.BigDecimal amount) {}
    public OrderCreatedForPaymentEvent(String paymentNo, Long userId, java.math.BigDecimal amount, Object ignored) {}
}