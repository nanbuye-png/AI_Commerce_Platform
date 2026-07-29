package com.commerce.platform.shipping.domain.event;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ShipmentCreatedEvent {
    private final Long shipmentId;
    private final Long fulfillmentId;
    private final Long packingTaskId;
    private final LocalDateTime occurredOn;

    public ShipmentCreatedEvent(Long shipmentId, Long fulfillmentId, Long packingTaskId) {
        this.shipmentId = shipmentId;
        this.fulfillmentId = fulfillmentId;
        this.packingTaskId = packingTaskId;
        this.occurredOn = LocalDateTime.now();
    }
}