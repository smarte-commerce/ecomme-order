package com.winnguyen1905.order.model.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.winnguyen1905.order.common.constant.OrderItemStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response class for order item with additional vendor-specific details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemWithVendorDetailsResponse {
    private UUID id;
    private UUID orderId;
    private String orderNumber;
    private Long productId;
    private String productName;
    private String sku;
    private String description;
    private double unitPrice;
    private int quantity;
    private double totalPrice;
    private OrderItemStatus status;
    private String imageUrl;
    
    // Customer information
    private Long customerId;
    private String customerName;
    private String customerEmail;
    
    // Shipping information
    private String shippingAddress;
    private String shippingCity;
    private String shippingState;
    private String shippingZip;
    private String shippingCountry;
    
    // Vendor information
    private Long vendorId;
    private String vendorName;
    private String vendorOrderNumber;
    private String vendorNotes;
    
    // Fulfillment information
    private LocalDateTime fulfillmentDate;
    private String trackingNumber;
    private String carrierName;
    
    // Timing information
    private LocalDateTime orderDate;
    private LocalDateTime statusUpdateDate;
} 
