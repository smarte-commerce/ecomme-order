package com.winnguyen1905.order.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVendorOrderRequest {
    @NotNull(message = "Vendor ID is required")
    private Long vendorId;
    
    @NotNull(message = "Commission rate is required")
    @Min(value = 0, message = "Commission rate cannot be negative")
    @Max(value = 100, message = "Commission rate cannot exceed 100%")
    private Double commissionRate;
} 
