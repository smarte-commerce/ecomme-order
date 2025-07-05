package com.winnguyen1905.order.model.response;

import java.time.Instant;
import java.util.UUID;

import com.winnguyen1905.order.common.constant.DiscountAppliesTo;
import com.winnguyen1905.order.common.constant.DiscountType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDiscountResponse {
    private UUID id;
    private UUID orderId;
    private String discountCode;
    private String discountName;
    private DiscountType discountType;
    private Double discountValue;
    private Double discountAmount;
    private DiscountAppliesTo appliesTo;
    private Instant createdDate;
    private Instant updatedDate;
} 
