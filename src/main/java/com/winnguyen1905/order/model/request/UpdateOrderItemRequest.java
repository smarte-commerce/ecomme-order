package com.winnguyen1905.order.model.request;

import com.winnguyen1905.order.common.constant.OrderItemStatus;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request class for updating an existing order item
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderItemRequest {
    private String productName;
    
    private String sku;
    
    private String description;
    
    @Min(value = 0, message = "Unit price must be non-negative")
    private Double unitPrice;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    private OrderItemStatus status;
    
    private String imageUrl;
    
    private String notes;
} 
