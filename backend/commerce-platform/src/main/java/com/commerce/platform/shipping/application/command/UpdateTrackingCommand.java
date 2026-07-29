package com.commerce.platform.shipping.application.command;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateTrackingCommand {
    @NotNull private final Long shipmentId;
    @NotNull private final String location;
    @NotNull private final String description;
    @NotNull private final String status;

    public UpdateTrackingCommand(Long shipmentId, String location, String description, String status) {
        this.shipmentId = shipmentId;
        this.location = location;
        this.description = description;
        this.status = status;
    }
}