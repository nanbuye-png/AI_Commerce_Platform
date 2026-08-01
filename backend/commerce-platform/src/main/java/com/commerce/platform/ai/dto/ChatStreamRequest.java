package com.commerce.platform.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatStreamRequest(
        @NotBlank @Size(max = 4000) String message,
        @JsonProperty("conversation_id") @Size(max = 128) String conversationId
) {
}