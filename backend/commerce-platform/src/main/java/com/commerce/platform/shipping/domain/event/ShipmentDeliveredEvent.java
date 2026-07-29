package com.commerce.platform.shipping.domain.event;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ShipmentDeliveredEvent {
    private final Long shipmentId;
    private final Long fulfillmentId;
    private final LocalDateTime occurredOn;

    public ShipmentDeliveredEvent(Long shipmentId, Long fulfillmentId) {
        this.shipmentId = shipmentId;
        this.fulfillmentId = fulfillmentId;
        this.occurredOn = LocalDateTime.now();
    }
}