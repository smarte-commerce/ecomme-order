package com.winnguyen1905.order.model.response;

import java.time.Instant;
import java.util.UUID;

import com.winnguyen1905.order.common.constant.VendorOrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorOrderResponse {
    private UUID id;
    private UUID orderId;
    private Long vendorId;
    private String vendorOrderNumber;
    private Double subtotal;
    private Double commissionRate;
    private Double commissionAmount;
    private Double vendorPayout;
    private VendorOrderStatus status;
    private Instant createdDate;
    private Instant updatedDate;
} 
