package com.commerce.platform.order.service;

import com.commerce.platform.common.exception.BusinessException;
import com.commerce.platform.order.domain.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantOrderApplicationServiceSecurityTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDomainService orderDomainService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void shouldAlwaysScopeOrderDetailByMerchantId() {
        MerchantOrderApplicationService service = new MerchantOrderApplicationService(
                orderRepository, orderDomainService, eventPublisher);
        when(orderRepository.findByMerchantIdAndOrderNo(200L, "ORDER-OTHER"))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> service.getMerchantOrderDetail(200L, "ORDER-OTHER"));

        verify(orderRepository).findByMerchantIdAndOrderNo(200L, "ORDER-OTHER");
    }
}