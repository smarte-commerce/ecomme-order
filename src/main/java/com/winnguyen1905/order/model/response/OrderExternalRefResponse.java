package com.winnguyen1905.order.model.response;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderExternalRefResponse {
    private UUID id;
    private UUID orderId;
    private String serviceName;
    private String externalId;
    private String refType;
    private String status;
    private Instant createdDate;
    private Instant updatedDate;
} 
