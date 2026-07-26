package com.commerce.platform.order.service;

import com.commerce.platform.order.domain.entity.Order;
import com.commerce.platform.order.domain.entity.OrderAddress;
import com.commerce.platform.order.domain.entity.OrderItem;
import com.commerce.platform.order.domain.enums.OrderStatus;
import com.commerce.platform.order.domain.enums.PaymentStatus;
import com.commerce.platform.order.domain.enums.ShippingStatus;
import com.commerce.platform.order.dto.request.CreateOrderItemRequest;
import com.commerce.platform.order.dto.request.CreateOrderRequest;
import com.commerce.platform.order.dto.response.OrderAddressVO;
import com.commerce.platform.order.dto.response.OrderItemVO;
import com.commerce.platform.order.dto.response.OrderVO;
import com.commerce.platform.order.util.OrderNoGenerator;
import com.commerce.platform.product.entity.ProductSku;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单领域服务
 * <p>
 * 负责订单领域核心逻辑：创建订单聚合、金额计算、状态初始化、可执行操作计算。
 * 不依赖外部基础设施和跨域服务。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class OrderDomainService {

    private final OrderNoGenerator orderNoGenerator;

    /**
     * 创建订单聚合
     */
    public Order createOrder(CreateOrderRequest request,
                             java.util.Map<Long, ProductSku> skuInfoMap,
                             Long buyerId, Long merchantId, Long storeId) {

        List<OrderItem> items = new ArrayList<>();
        BigDecimal productAmount = BigDecimal.ZERO;

        for (CreateOrderItemRequest itemReq : request.getItems()) {
            ProductSku sku = skuInfoMap.get(itemReq.getSkuId());
            if (sku == null) continue;

            BigDecimal price = sku.getPrice();
            int quantity = itemReq.getQuantity();
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, RoundingMode.HALF_UP);

            OrderItem item = OrderItem.builder()
                    .skuId(sku.getId())
                    .productId(sku.getProduct().getId())
                    .productName(sku.getProduct().getProductName())
                    .skuName(sku.getAttributesJson())
                    .skuCode(sku.getSkuCode())
                    .price(price)
                    .originalPrice(sku.getOriginalPrice())
                    .image(null)
                    .quantity(quantity)
                    .subtotal(subtotal)
                    .weight(sku.getWeight())
                    .build();

            items.add(item);
            productAmount = productAmount.add(subtotal);
        }

        BigDecimal freightAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = productAmount.add(freightAmount).subtract(discountAmount);
        BigDecimal payAmount = totalAmount;

        Order order = Order.builder()
                .orderNo(orderNoGenerator.generate())
                .buyerId(buyerId)
                .merchantId(merchantId)
                .storeId(storeId)
                .orderStatus(OrderStatus.PENDING_PAYMENT)
                .paymentStatus(PaymentStatus.UNPAID)
                .shippingStatus(ShippingStatus.UNSHIPPED)
                .totalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP))
                .productAmount(productAmount.setScale(2, RoundingMode.HALF_UP))
                .freightAmount(freightAmount.setScale(2, RoundingMode.HALF_UP))
                .discountAmount(discountAmount.setScale(2, RoundingMode.HALF_UP))
                .payAmount(payAmount.setScale(2, RoundingMode.HALF_UP))
                .buyerRemark(request.getRemark())
                .build();

        items.forEach(order::addItem);
        return order;
    }

    /**
     * 设置订单收货地址快照
     */
    public void setOrderAddress(Order order, String receiver, String phone,
                                String province, String city, String district,
                                String detailAddress, String postalCode) {
        OrderAddress address = OrderAddress.builder()
                .receiver(receiver)
                .phone(phone)
                .province(province)
                .city(city)
                .district(district)
                .detailAddress(detailAddress)
                .postalCode(postalCode)
                .build();
        order.setAddress(address);
    }

    /**
     * 构建订单 VO（含可执行操作计算）
     */
    public OrderVO buildOrderVO(Order order) {
        OrderVO.OrderVOBuilder builder = OrderVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .buyerId(order.getBuyerId())
                .merchantId(order.getMerchantId())
                .storeId(order.getStoreId())
                .orderStatus(order.getOrderStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .shippingStatus(order.getShippingStatus().name())
                .totalAmount(order.getTotalAmount())
                .productAmount(order.getProductAmount())
                .freightAmount(order.getFreightAmount())
                .discountAmount(order.getDiscountAmount())
                .payAmount(order.getPayAmount())
                .buyerRemark(order.getBuyerRemark())
                .merchantRemark(order.getMerchantRemark())
                .paymentTime(order.getPaymentTime())
                .shippingTime(order.getShippingTime())
                .completedTime(order.getCompletedTime())
                .cancelledTime(order.getCancelledTime())
                .createdTime(order.getCreatedTime());

        // 计算可执行操作
        OrderStatus status = order.getOrderStatus();
        builder.canPay(status == OrderStatus.PENDING_PAYMENT);
        builder.canCancel(status == OrderStatus.PENDING_PAYMENT || status == OrderStatus.PAID);
        builder.canConfirm(status == OrderStatus.SHIPPED);
        builder.canRefund(status == OrderStatus.PAID || status == OrderStatus.PROCESSING
                || status == OrderStatus.SHIPPED || status == OrderStatus.COMPLETED);
        builder.displayStatus(getDisplayStatus(order));

        // 构建 orderItemVOs
        if (order.getItems() != null) {
            List<OrderItemVO> itemVOs = order.getItems().stream()
                    .map(item -> OrderItemVO.builder()
                            .id(item.getId())
                            .skuId(item.getSkuId())
                            .productId(item.getProductId())
                            .productName(item.getProductName())
                            .skuName(item.getSkuName())
                            .skuCode(item.getSkuCode())
                            .price(item.getPrice())
                            .originalPrice(item.getOriginalPrice())
                            .image(item.getImage())
                            .quantity(item.getQuantity())
                            .subtotal(item.getSubtotal())
                            .build())
                    .collect(Collectors.toList());
            builder.items(itemVOs);
        }

        // 构建 orderAddressVO
        if (order.getAddress() != null) {
            OrderAddress addr = order.getAddress();
            builder.address(OrderAddressVO.builder()
                    .id(addr.getId())
                    .receiver(addr.getReceiver())
                    .phone(addr.getPhone())
                    .province(addr.getProvince())
                    .city(addr.getCity())
                    .district(addr.getDistrict())
                    .detailAddress(addr.getDetailAddress())
                    .postalCode(addr.getPostalCode())
                    .build());
        }

        return builder.build();
    }

    /**
     * 获取显示状态
     */
    public String getDisplayStatus(Order order) {
        switch (order.getOrderStatus()) {
            case PENDING_PAYMENT: return "待支付";
            case PAID: return "已支付";
            case PROCESSING: return "处理中";
            case SHIPPED: return "已发货";
            case COMPLETED: return "已完成";
            case CANCELLED: return "已取消";
            case REFUNDING: return "退款中";
            case REFUNDED: return "已退款";
            case CLOSED: return "已关闭";
            default: return order.getOrderStatus().name();
        }
    }
}