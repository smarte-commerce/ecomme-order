package com.winnguyen1905.order.model.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotNull(message = "Shipping address is required")
    private String shippingAddress;
    
    private String billingAddress;
    
    private LocalDate estimatedDeliveryDate;
    
    private String specialInstructions;
    
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        @NotNull(message = "Vendor ID is required")
        private Long vendorId;
        
        @NotBlank(message = "Product name is required")
        private String productName;
        
        private String productSku;
        
        private String productCategory;
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
        
        @NotNull(message = "Unit price is required")
        @Min(value = 0, message = "Unit price cannot be negative")
        private Double unitPrice;
        
        private Double weight;
        
        private String dimensions;
        
        private String taxCategory;
    }
} 
