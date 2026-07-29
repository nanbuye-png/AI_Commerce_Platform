package com.commerce.platform.shipping.application.command;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateShipmentCommand {
    @NotNull private final Long fulfillmentId;
    @NotNull private final Long packingTaskId;
    @NotNull private final String carrier;

    public CreateShipmentCommand(Long fulfillmentId, Long packingTaskId, String carrier) {
        this.fulfillmentId = fulfillmentId;
        this.packingTaskId = packingTaskId;
        this.carrier = carrier;
    }
}