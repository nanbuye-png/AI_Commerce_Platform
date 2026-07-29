package com.commerce.platform.shipping.application.handler;

import com.commerce.platform.shipping.application.command.CreateShipmentCommand;
import com.commerce.platform.shipping.domain.aggregate.Shipment;
import com.commerce.platform.shipping.domain.event.ShipmentCreatedEvent;
import com.commerce.platform.shipping.domain.repository.ShipmentRepository;
import com.commerce.platform.shipping.domain.service.ShippingDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateShipmentHandler {

    private final ShippingDomainService shippingDomainService;
    private final ShipmentRepository shipmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    public Shipment handle(CreateShipmentCommand command) {
        log.info("开始创建配送单: fulfillmentId={}", command.getFulfillmentId());

        Shipment shipment = shippingDomainService.createShipment(
                command.getFulfillmentId(), command.getPackingTaskId(), command.getCarrier());
        Shipment saved = shipmentRepository.save(shipment);

        ShipmentCreatedEvent event = new ShipmentCreatedEvent(
                saved.getId(), saved.getFulfillmentId(), saved.getPackingTaskId());
        eventPublisher.publishEvent(event);

        log.info("配送单创建成功: shipmentId={}, fulfillmentId={}", saved.getId(), saved.getFulfillmentId());
        return saved;
    }
}