package com.winnguyen1905.order.model.response;

import java.time.LocalDateTime;
import java.util.Map;

import com.winnguyen1905.order.common.constant.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response class containing summary information about status transitions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusTransitionSummary {
    // Basic metrics
    private int transitionCount;
    private double averageTimeInHours;
    private LocalDateTime firstTransitionDate;
    private LocalDateTime lastTransitionDate;
    
    // Transition origin counts
    private Map<OrderStatus, Integer> previousStatusCounts;
    
    // Transition destination counts
    private Map<OrderStatus, Integer> nextStatusCounts;
    
    // Time metrics
    private double minTimeInHours;
    private double maxTimeInHours;
    private double medianTimeInHours;
    
    // User metrics
    private Map<String, Integer> transitionsByUser;
} 
