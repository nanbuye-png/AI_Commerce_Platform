package com.commerce.platform.shipping.domain.service;

import com.commerce.platform.shipping.domain.aggregate.Shipment;
import com.commerce.platform.shipping.domain.repository.ShipmentRepository;
import com.commerce.platform.shipping.domain.valueobject.ShipmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShippingDomainService 测试")
class ShippingDomainServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    private ShippingDomainService domainService;

    @BeforeEach
    void setUp() {
        domainService = new ShippingDomainService(shipmentRepository);
    }

    @Test
    @DisplayName("创建配送单应返回 CREATED 状态")
    void shouldCreateShipment() {
        when(shipmentRepository.findByFulfillmentId(100L)).thenReturn(Optional.empty());

        Shipment result = domainService.createShipment(100L, 50L, "SF");

        assertNotNull(result);
        assertEquals(100L, result.getFulfillmentId());
        assertEquals(50L, result.getPackingTaskId());
        assertEquals("SF", result.getCarrier());
        assertEquals(ShipmentStatus.CREATED, result.getStatus());
    }

    @Test
    @DisplayName("履约单已有配送单时应抛出异常")
    void shouldThrowWhenFulfillmentAlreadyHasShipment() {
        when(shipmentRepository.findByFulfillmentId(100L))
                .thenReturn(Optional.of(Shipment.create(100L, 50L, "SF")));

        assertThrows(IllegalStateException.class,
                () -> domainService.createShipment(100L, 50L, "SF"));
    }
}