package com.winnguyen1905.order.model.response;

import java.time.Instant;
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
public class OrderStatusHistoryResponse {
    private UUID id;
    private UUID orderId;
    private OrderStatus oldStatus;
    private OrderStatus newStatus;
    private String reason;
    private String notes;
    private String changedBy;
    private Instant createdDate;
} 
