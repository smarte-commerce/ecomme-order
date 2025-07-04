package com.winnguyen1905.order.rest.service;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.order.model.OrderStatusHistoryResponse;

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
} 
