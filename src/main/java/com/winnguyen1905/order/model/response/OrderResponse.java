package com.winnguyen1905.order.model.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.winnguyen1905.order.common.constant.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private UUID id;
    private Long customerId;
    private String orderNumber;
    private Double subtotal;
    private Double discountAmount;
    private Double taxAmount;
    private Double shippingAmount;
    private Double totalAmount;
    private Double paidAmount;
    private Double amountToBePaid;
    private OrderStatus status;
    private String shippingAddress;
    private String billingAddress;
    private LocalDate estimatedDeliveryDate;
    private String specialInstructions;
    private List<OrderItemResponse> orderItems;
    private Instant createdDate;
    private Instant updatedDate;
} 
