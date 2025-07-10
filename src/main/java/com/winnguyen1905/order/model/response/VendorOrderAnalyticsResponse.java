package com.winnguyen1905.order.model.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.winnguyen1905.order.common.constant.VendorOrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response class containing analytics data for vendor orders
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorOrderAnalyticsResponse {
    private Long vendorId;
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Order statistics
    private int totalOrders;
    private double totalOrderValue;
    private double averageOrderValue;
    
    // Status breakdown
    private Map<VendorOrderStatus, Integer> ordersByStatus;
    
    // Performance metrics
    private double fulfillmentRate;
    private double cancelationRate;
    private double returnRate;
    
    // Time metrics
    private double averageFulfillmentTimeInHours;
    private double averageTimeToAcceptInHours;
    
    // Product metrics
    private List<ProductPerformance> topSellingProducts;
    
    // Customer metrics
    private int uniqueCustomers;
    private double averageCustomerSatisfactionScore;
    
    // Trend data
    private Map<LocalDate, Integer> orderCountByDay;
    private Map<LocalDate, Double> revenueByDay;
    
    /**
     * Inner class for product performance metrics
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductPerformance {
        private Long productId;
        private String productName;
        private int quantitySold;
        private double totalRevenue;
        private double averagePrice;
    }
} 
