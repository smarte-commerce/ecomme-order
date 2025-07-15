package com.winnguyen1905.order.model.request;

import jakarta.validation.constraints.NotBlank;
import com.winnguyen1905.order.secure.AbstractModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorShippingUpdateRequest implements AbstractModel {
    
    @NotBlank(message = "Tracking number is required")
    private String trackingNumber;
    
    private String carrier;
    
    private String estimatedDeliveryDate;
    
    @Builder.Default
    private boolean notifyCustomer = true;
    
    private String notes;
} 
