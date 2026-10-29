package com.commerce.platform.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatStreamRequest(
        @NotBlank @Size(max = 4000) String message,
        @JsonProperty("conversation_id") @Size(max = 128) String conversationId,
        @JsonProperty("user_id") Long userId,
        @Size(max = 32) String action,
        @JsonProperty("sku_id") Long skuId,
        @Min(1) Integer quantity,
        Boolean confirmed,
        @Size(max = 12) List<ChatHistoryMessage> history
) {
    public record ChatHistoryMessage(String role, String content) { }

    public ChatStreamRequest(String message, String conversationId) {
        this(message, conversationId, null, null, null, null, null, null);
    }
}
