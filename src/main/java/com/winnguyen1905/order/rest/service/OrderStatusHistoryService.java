package com.winnguyen1905.order.rest.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.winnguyen1905.order.common.constant.OrderStatus;
import com.winnguyen1905.order.model.response.OrderStatusHistoryResponse;
import com.winnguyen1905.order.model.response.StatusTransitionSummary;

/**
 * Service for managing order status history
 * Provides tracking and analytics for order status changes
 */
public interface OrderStatusHistoryService {
    /**
     * Get status history entry by ID
     * 
     * @param id Status history ID
     * @return Status history response
     */
    OrderStatusHistoryResponse getStatusHistoryById(UUID id);
    
    /**
     * Get all status history entries for an order
     * 
     * @param orderId Order ID
     * @return List of status history entries
     */
    List<OrderStatusHistoryResponse> getStatusHistoryByOrderId(UUID orderId);
    
    /**
     * Get status history entries for an order with pagination
     * 
     * @param orderId Order ID
     * @param pageable Pagination information
     * @return Page of status history entries
     */
    Page<OrderStatusHistoryResponse> getStatusHistoryByOrderIdPaginated(UUID orderId, Pageable pageable);
    
    /**
     * Add a comment to an order's status history
     * Allows adding notes without changing status
     * 
     * @param orderId Order ID
     * @param comment Comment text
     * @param visibleToCustomer Whether the comment is visible to the customer
     * @return Created status history entry
     */
    OrderStatusHistoryResponse addOrderComment(UUID orderId, String comment, boolean visibleToCustomer);
    
    /**
     * Get status history by date range
     * Useful for auditing and reporting
     * 
     * @param startDate Start date
     * @param endDate End date
     * @param pageable Pagination information
     * @return Page of status history entries
     */
    Page<OrderStatusHistoryResponse> getStatusHistoryByDateRange(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Get status transitions by user
     * Tracks which users are making status changes
     * 
     * @param userId User ID
     * @param pageable Pagination information
     * @return Page of status history entries
     */
    Page<OrderStatusHistoryResponse> getStatusTransitionsByUser(Long userId, Pageable pageable);
    
    /**
     * Get status history for a vendor's orders
     * Allows vendors to track status changes for their orders
     * 
     * @param vendorId Vendor ID
     * @param pageable Pagination information
     * @return Page of status history entries
     */
    Page<OrderStatusHistoryResponse> getStatusHistoryForVendor(Long vendorId, Pageable pageable);
    
    /**
     * Get status transition metrics
     * Provides analytics about order status transitions
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Map with status transition metrics
     */
    Map<OrderStatus, StatusTransitionSummary> getStatusTransitionMetrics(
            LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get average time in each status
     * Useful for performance monitoring and optimization
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Map of status to average time in hours
     */
    Map<OrderStatus, Double> getAverageTimeInStatus(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get customer-visible status history for an order
     * Filtered version of status history for customer display
     * 
     * @param orderId Order ID
     * @return List of customer-visible status history entries
     */
    List<OrderStatusHistoryResponse> getCustomerVisibleStatusHistory(UUID orderId);
} 
