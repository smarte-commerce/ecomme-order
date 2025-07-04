package com.winnguyen1905.order.rest.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.winnguyen1905.order.model.CreateExternalRefRequest;
import com.winnguyen1905.order.model.OrderExternalRefResponse;

public interface OrderExternalRefService {
    /**
     * Create an external reference for an order
     * 
     * @param orderId Order ID
     * @param request External reference creation request
     * @return Created external reference
     */
    OrderExternalRefResponse createExternalRef(UUID orderId, CreateExternalRefRequest request);
    
    /**
     * Get external reference by ID
     * 
     * @param id External reference ID
     * @return External reference response
     */
    OrderExternalRefResponse getExternalRefById(UUID id);
    
    /**
     * Get all external references for an order
     * 
     * @param orderId Order ID
     * @return List of external references
     */
    List<OrderExternalRefResponse> getExternalRefsByOrderId(UUID orderId);
    
    /**
     * Get external references by service name with pagination
     * 
     * @param serviceName Service name
     * @param pageable Pagination information
     * @return Page of external references
     */
    Page<OrderExternalRefResponse> getExternalRefsByServiceName(String serviceName, Pageable pageable);
    
    /**
     * Update external reference status
     * 
     * @param id External reference ID
     * @param status New status
     * @return Updated external reference
     */
    OrderExternalRefResponse updateExternalRefStatus(UUID id, String status);
    
    /**
     * Delete external reference
     * 
     * @param id External reference ID
     */
    void deleteExternalRef(UUID id);
    
    /**
     * Get external references by service name (non-paginated)
     * 
     * @param serviceName Service name
     * @return List of external references
     * @deprecated Use getExternalRefsByServiceName(String, Pageable) instead
     */
    @Deprecated
    List<OrderExternalRefResponse> getExternalRefsByServiceName(String serviceName);
} 
