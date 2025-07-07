package com.winnguyen1905.order.rest.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.winnguyen1905.order.model.request.CreateExternalRefRequest;
import com.winnguyen1905.order.model.response.OrderExternalRefResponse;

/**
 * Service for managing external references to orders for integration with other services
 * Supports integrations for both customer-facing and vendor systems
 */
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
     * Find order by external reference
     * Allows lookup of orders by references from other systems
     * 
     * @param externalId External ID
     * @param serviceName Service name
     * @return External reference if found
     */
    OrderExternalRefResponse findOrderByExternalReference(String externalId, String serviceName);
    
    /**
     * Synchronize order status with external system
     * Updates the status of an order based on external system status
     * 
     * @param orderId Order ID
     * @param serviceName External service name
     * @return Updated external reference
     */
    OrderExternalRefResponse syncOrderWithExternalSystem(UUID orderId, String serviceName);
    
    /**
     * Batch create external references
     * Useful for integrating with multiple external systems at once
     * 
     * @param orderId Order ID
     * @param requests List of external reference creation requests
     * @return List of created external references
     */
    List<OrderExternalRefResponse> batchCreateExternalRefs(UUID orderId, List<CreateExternalRefRequest> requests);
    
    /**
     * Get customer service information for an order
     * Retrieves tracking numbers, support ticket IDs, etc. for customer service
     * 
     * @param orderId Order ID
     * @return Map of service name to service-specific information
     */
    Map<String, Object> getCustomerServiceInfo(UUID orderId);
    
    /**
     * Link vendor system reference to order
     * Creates an external reference for vendor-specific tracking
     * 
     * @param orderId Order ID
     * @param vendorId Vendor ID
     * @param vendorOrderId Vendor's internal order ID
     * @param additionalInfo Additional vendor-specific information
     * @return Created external reference
     */
    OrderExternalRefResponse linkVendorReference(UUID orderId, Long vendorId, String vendorOrderId, Map<String, Object> additionalInfo);
    
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
