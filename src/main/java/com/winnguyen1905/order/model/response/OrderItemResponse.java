package com.winnguyen1905.order.model.response;

import java.time.Instant;
import java.util.UUID;

import com.winnguyen1905.order.common.constant.OrderItemStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private UUID id;
    private Long productId;
    private Long vendorId;
    private String productName;
    private String productSku;
    private String productCategory;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
    private Double weight;
    private String dimensions;
    private String taxCategory;
    private OrderItemStatus status;
    private Instant createdDate;
    private Instant updatedDate;
} 
