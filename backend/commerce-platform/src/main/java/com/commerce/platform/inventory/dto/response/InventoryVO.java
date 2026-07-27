package com.commerce.platform.inventory.dto.response;

import com.commerce.platform.inventory.domain.enums.InventoryStatus;
import lombok.Data;

/**
 * 库存信息 VO
 */
@Data
public class InventoryVO {

    private Long id;
    private Long productId;
    private Long skuId;
    private Integer availableStock;
    private Integer lockedStock;
    private Integer soldStock;
    private String status;
}