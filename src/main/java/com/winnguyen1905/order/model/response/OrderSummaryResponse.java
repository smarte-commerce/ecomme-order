package com.winnguyen1905.order.model.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response class containing order summary information for a customer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryResponse {
    private Long customerId;
    private int totalOrders;
    private double totalSpent;
    private double averageOrderValue;
    private LocalDateTime firstOrderDate;
    private LocalDateTime lastOrderDate;
    private Map<String, Integer> ordersByStatus;
    private List<String> mostOrderedCategories;
    private List<String> mostOrderedProducts;
    private List<String> frequentlyUsedPaymentMethods;
} 
