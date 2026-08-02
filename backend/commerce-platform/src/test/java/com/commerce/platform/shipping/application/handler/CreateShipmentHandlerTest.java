package com.commerce.platform.shipping.application.handler;

import com.commerce.platform.shipping.application.command.CreateShipmentCommand;
import com.commerce.platform.shipping.domain.aggregate.Shipment;
import com.commerce.platform.shipping.domain.event.ShipmentCreatedEvent;
import com.commerce.platform.shipping.domain.repository.ShipmentRepository;
import com.commerce.platform.shipping.domain.service.ShippingDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateShipmentHandler 测试")
class CreateShipmentHandlerTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ShippingDomainService domainService;
    private CreateShipmentHandler handler;

    @BeforeEach
    void setUp() {
        domainService = new ShippingDomainService(shipmentRepository);
        handler = new CreateShipmentHandler(domainService, shipmentRepository, eventPublisher);
    }

    @Test
    @DisplayName("处理创建配送单命令应保存并发布事件")
    void shouldSaveAndPublishEvent() {
        when(shipmentRepository.findByFulfillmentId(100L)).thenReturn(Optional.empty());
        when(shipmentRepository.save(any(Shipment.class)))
                .thenAnswer(inv -> { Shipment s = inv.getArgument(0); s.setId(10L); return s; });

        CreateShipmentCommand cmd = new CreateShipmentCommand(100L, 50L, "SF");
        Shipment result = handler.handle(cmd);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(100L, result.getFulfillmentId());

        verify(shipmentRepository).save(any(Shipment.class));
        ArgumentCaptor<ShipmentCreatedEvent> captor = ArgumentCaptor.forClass(ShipmentCreatedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(10L, captor.getValue().getShipmentId());
        assertEquals(100L, captor.getValue().getFulfillmentId());
    }
}