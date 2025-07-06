package com.winnguyen1905.order.model.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request class for updating shipping information for a vendor order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorShippingUpdateRequest {
    @NotBlank(message = "Tracking number is required")
    private String trackingNumber;
    
    @NotBlank(message = "Carrier name is required")
    private String carrierName;
    
    @NotNull(message = "Shipping date is required")
    private LocalDate shippingDate;
    
    private LocalDate estimatedDeliveryDate;
    
    private String shippingMethod;
    
    private Double shippingCost;
    
    private String notes;
    
    private boolean notifyCustomer;
} 
