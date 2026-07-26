package com.commerce.platform.product.service;

/**
 * 商品编码生成器接口
 * 预留用于后续实现统一编码生成策略（如雪花ID、数据库序列等）
 */
public interface ProductCodeGenerator {

    /**
     * 生成全局唯一的商品业务编码
     *
     * @return 商品编码
     */
    String generateProductCode();
}