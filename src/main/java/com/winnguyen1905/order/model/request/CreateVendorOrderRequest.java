package com.winnguyen1905.order.model.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import com.winnguyen1905.order.secure.AbstractModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVendorOrderRequest implements AbstractModel {
    
    @NotNull(message = "Vendor ID is required")
    private Long vendorId;
    
    @NotNull(message = "Commission rate is required")
    @DecimalMin(value = "0.0", message = "Commission rate must be non-negative")
    @DecimalMax(value = "100.0", message = "Commission rate cannot exceed 100%")
    private Double commissionRate;
    
    private String notes;
} 
