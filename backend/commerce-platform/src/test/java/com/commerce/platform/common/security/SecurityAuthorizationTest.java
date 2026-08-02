package com.commerce.platform.common.security;

import com.commerce.platform.inventory.controller.InventoryController;
import com.commerce.platform.inventory.controller.InventoryReservationController;
import com.commerce.platform.inventory.service.InventoryApplicationService;
import com.commerce.platform.inventory.service.InventoryReservationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {InventoryController.class, InventoryReservationController.class})
@Import({
        com.commerce.platform.common.config.SecurityConfig.class,
        JwtProperties.class,
        JwtUtil.class
})
@TestPropertySource(properties = {
        "jwt.customer-web-secret=test-customer-web-secret-with-minimum-32-bytes",
        "jwt.merchant-web-secret=test-merchant-web-secret-with-minimum-32-bytes",
        "jwt.admin-web-secret=test-admin-web-secret-with-minimum-32-bytes",
        "jwt.expiration=3600000"
})
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private InventoryApplicationService inventoryApplicationService;

    @MockBean
    private InventoryReservationService inventoryReservationService;

    @Test
    @DisplayName("CUSTOMER Token 不得读取管理库存接口")
    void customerCannotReadInventoryManagementApi() throws Exception {
        String token = jwtUtil.generateToken(1L, "customer", java.util.List.of("ROLE_CUSTOMER"),
                JwtUtil.ClientType.CUSTOMER_WEB);

        mockMvc.perform(get("/api/inventory/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("浏览器角色不得访问内部库存接口")
    void browserRoleCannotAccessInternalInventoryApi() throws Exception {
        String token = jwtUtil.generateToken(2L, "merchant", java.util.List.of("ROLE_MERCHANT"),
                JwtUtil.ClientType.MERCHANT_WEB);

        mockMvc.perform(get("/api/internal/inventory/reservations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}