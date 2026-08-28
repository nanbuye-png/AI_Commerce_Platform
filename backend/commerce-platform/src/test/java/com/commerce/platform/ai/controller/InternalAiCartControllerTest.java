package com.commerce.platform.ai.controller;

import com.commerce.platform.ai.config.AiGatewayConfig;
import com.commerce.platform.cart.dto.response.CartVO;
import com.commerce.platform.cart.service.CartApplicationService;
import com.commerce.platform.common.config.SecurityConfig;
import com.commerce.platform.common.security.JwtProperties;
import com.commerce.platform.common.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalAiCartController.class)
@Import({SecurityConfig.class, AiGatewayConfig.class, JwtProperties.class, JwtUtil.class})
@TestPropertySource(properties = {
        "app.ai.base-url=http://localhost:8000",
        "app.ai.internal-token=test-internal-token",
        "app.ai.connect-timeout=3s",
        "app.ai.request-timeout=60s",
        "jwt.customer-web-secret=test-customer-web-secret-with-minimum-32-bytes",
        "jwt.merchant-web-secret=test-merchant-web-secret-with-minimum-32-bytes",
        "jwt.admin-web-secret=test-admin-web-secret-with-minimum-32-bytes",
        "jwt.expiration=3600000"
})
class InternalAiCartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartApplicationService cartApplicationService;

    @Test
    void addsSkuForValidInternalToken() throws Exception {
        CartVO cart = new CartVO();
        cart.setUserId(12L);
        when(cartApplicationService.addItemBySku(12L, 42L, 2)).thenReturn(cart);

        mockMvc.perform(post("/api/internal/ai/cart/items")
                        .header("X-Internal-Token", "test-internal-token")
                        .contentType("application/json")
                        .content("{\"userId\":12,\"skuId\":42,\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(12));

        verify(cartApplicationService).addItemBySku(eq(12L), eq(42L), eq(2));
    }

    @Test
    void rejectsInvalidTokenAndDoesNotCallCartService() throws Exception {
        mockMvc.perform(post("/api/internal/ai/cart/items")
                        .header("X-Internal-Token", "wrong-token")
                        .contentType("application/json")
                        .content("{\"userId\":12,\"skuId\":42,\"quantity\":2}"))
                .andExpect(status().isForbidden());

        verify(cartApplicationService, never()).addItemBySku(org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void validatesPositiveQuantity() throws Exception {
        mockMvc.perform(post("/api/internal/ai/cart/items")
                        .header("X-Internal-Token", "test-internal-token")
                        .contentType("application/json")
                        .content("{\"userId\":12,\"skuId\":42,\"quantity\":0}"))
                .andExpect(status().isBadRequest());

        verify(cartApplicationService, never()).addItemBySku(org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyInt());
    }
}
