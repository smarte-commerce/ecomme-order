package com.winnguyen1905.order.model.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response class containing performance metrics for vendors
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorPerformanceResponse {
    private Long vendorId;
    private String vendorName;
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Overall performance score (0-100)
    private int overallPerformanceScore;
    
    // Key performance indicators
    private double onTimeDeliveryRate;
    private double orderAccuracyRate;
    private double customerSatisfactionScore;
    private double fulfillmentRate;
    private double cancellationRate;
    private double returnRate;
    
    // Time metrics
    private double averageProcessingTimeInHours;
    private double averageShippingTimeInHours;
    private double averageTimeToAcceptInHours;
    
    // Trend data for performance over time
    private Map<LocalDate, Integer> performanceScoreByDay;
    private Map<LocalDate, Double> onTimeDeliveryByDay;
    
    // Comparison to marketplace average
    private double marketplaceAveragePerformanceScore;
    private double performancePercentile;
    
    // Improvement areas
    private List<ImprovementArea> suggestedImprovementAreas;
    
    /**
     * Inner class for improvement areas
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImprovementArea {
        private String area;
        private String description;
        private int priority;
        private double currentScore;
        private double targetScore;
    }
} 
