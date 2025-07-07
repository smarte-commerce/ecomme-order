package com.winnguyen1905.order.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request class for creating a new order item
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderItemRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Vendor ID is required")
    private Long vendorId;
    
    @NotNull(message = "Product name is required")
    private String productName;
    
    @NotNull(message = "SKU is required")
    private String sku;
    
    private String description;
    
    @NotNull(message = "Unit price is required")
    @Min(value = 0, message = "Unit price must be non-negative")
    private Double unitPrice;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    private String imageUrl;
    
    private String notes;
} 
