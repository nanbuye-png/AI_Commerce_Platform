package com.commerce.platform.fulfillment.domain.service;

import com.commerce.platform.fulfillment.domain.aggregate.Fulfillment;
import com.commerce.platform.fulfillment.domain.repository.FulfillmentRepository;
import com.commerce.platform.fulfillment.domain.valueobject.FulfillmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * FulfillmentDomainService 测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentDomainService 测试")
class FulfillmentDomainServiceTest {

    @Mock
    private FulfillmentRepository fulfillmentRepository;

    private FulfillmentDomainService domainService;

    @BeforeEach
    void setUp() {
        domainService = new FulfillmentDomainService(fulfillmentRepository);
    }

    @Test
    @DisplayName("创建履约单应返回 PENDING 状态的履约单")
    void shouldCreateFulfillmentSuccessfully() {
        when(fulfillmentRepository.existsByOrderId(1L)).thenReturn(false);

        Fulfillment result = domainService.createFulfillment(1L, 100L);

        assertNotNull(result);
        assertEquals(1L, result.getOrderId());
        assertEquals(100L, result.getMerchantId());
        assertEquals(FulfillmentStatus.PENDING, result.getStatus());

        verify(fulfillmentRepository).existsByOrderId(1L);
    }

    @Test
    @DisplayName("订单已有履约单时应抛出异常")
    void shouldThrowExceptionWhenOrderAlreadyHasFulfillment() {
        when(fulfillmentRepository.existsByOrderId(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> domainService.createFulfillment(1L, 100L));

        verify(fulfillmentRepository).existsByOrderId(1L);
    }

    @Test
    @DisplayName("canCreateFulfillment 应正确返回校验结果")
    void shouldCheckCanCreateFulfillment() {
        when(fulfillmentRepository.existsByOrderId(1L)).thenReturn(false);
        when(fulfillmentRepository.existsByOrderId(2L)).thenReturn(true);

        assertTrue(domainService.canCreateFulfillment(1L));
        assertFalse(domainService.canCreateFulfillment(2L));
    }
}