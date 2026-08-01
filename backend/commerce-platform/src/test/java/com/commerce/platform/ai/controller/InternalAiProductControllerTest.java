package com.commerce.platform.ai.controller;

import com.commerce.platform.ai.config.AiGatewayConfig;
import com.commerce.platform.common.config.SecurityConfig;
import com.commerce.platform.common.security.JwtProperties;
import com.commerce.platform.common.security.JwtUtil;
import com.commerce.platform.product.dto.customer.ProductCardResponse;
import com.commerce.platform.product.service.CustomerProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalAiProductController.class)
@Import({SecurityConfig.class, AiGatewayConfig.class,
        JwtProperties.class, JwtUtil.class})
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
class InternalAiProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerProductService customerProductService;

    @Test
    void returnsProductsForValidInternalToken() throws Exception {
        ProductCardResponse product = new ProductCardResponse();
        product.setId(42L);
        product.setProductName("降噪耳机");
        product.setMinPrice(new BigDecimal("899.00"));
        when(customerProductService.listProducts(argThat(request ->
                "耳机".equals(request.getKeyword())
                        && new BigDecimal("500").equals(request.getMinPrice())
                        && new BigDecimal("1000").equals(request.getMaxPrice())
                        && request.getSize() == 6
        ))).thenReturn(new PageImpl<>(List.of(product), PageRequest.of(0, 6), 1));

        mockMvc.perform(get("/api/internal/ai/products/search")
                        .header("X-Internal-Token", "test-internal-token")
                        .queryParam("keyword", "耳机")
                        .queryParam("minPrice", "500")
                        .queryParam("maxPrice", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value(42))
                .andExpect(jsonPath("$.data.items[0].productName").value("降噪耳机"))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void rejectsMissingInternalToken() throws Exception {
        mockMvc.perform(get("/api/internal/ai/products/search"))
                .andExpect(status().isForbidden());

        verify(customerProductService, never()).listProducts(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsInvalidInternalToken() throws Exception {
        mockMvc.perform(get("/api/internal/ai/products/search")
                        .header("X-Internal-Token", "wrong-token"))
                .andExpect(status().isForbidden());

        verify(customerProductService, never()).listProducts(org.mockito.ArgumentMatchers.any());
    }
}