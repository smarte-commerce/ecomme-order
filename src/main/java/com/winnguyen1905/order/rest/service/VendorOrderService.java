package com.winnguyen1905.order.rest.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.winnguyen1905.order.common.constant.VendorOrderStatus;
import com.winnguyen1905.order.model.request.CreateVendorOrderRequest;
import com.winnguyen1905.order.model.request.VendorShippingUpdateRequest;
import com.winnguyen1905.order.model.response.VendorOrderAnalyticsResponse;
import com.winnguyen1905.order.model.response.VendorOrderResponse;
import com.winnguyen1905.order.model.response.VendorPerformanceResponse;

/**
 * Service for managing vendor-specific orders
 * Supports operations for vendors to manage their portion of customer orders
 */
public interface VendorOrderService {
    /**
     * Create a vendor order for an existing order
     * 
     * @param orderId Order ID
     * @param request Vendor order request
     * @return Created vendor order
     */
    VendorOrderResponse createVendorOrder(UUID orderId, CreateVendorOrderRequest request);
    
    /**
     * Get vendor order by ID
     * 
     * @param id Vendor order ID
     * @return Vendor order response
     */
    VendorOrderResponse getVendorOrderById(UUID id);
    
    /**
     * Get all vendor orders for an order
     * 
     * @param orderId Order ID
     * @return List of vendor orders
     */
    List<VendorOrderResponse> getVendorOrdersByOrderId(UUID orderId);
    
    /**
     * Get all vendor orders for a vendor with pagination
     * 
     * @param vendorId Vendor ID
     * @param pageable Pagination information
     * @return Page of vendor orders
     */
    Page<VendorOrderResponse> getVendorOrdersByVendorId(Long vendorId, Pageable pageable);
    
    /**
     * Update vendor order status
     * 
     * @param id Vendor order ID
     * @param status New status
     * @return Updated vendor order
     */
    VendorOrderResponse updateVendorOrderStatus(UUID id, VendorOrderStatus status);
    
    /**
     * Get all vendor orders with a specific status with pagination
     * 
     * @param status Vendor order status
     * @param pageable Pagination information
     * @return Page of vendor orders
     */
    Page<VendorOrderResponse> getVendorOrdersByStatus(VendorOrderStatus status, Pageable pageable);
    
    /**
     * Get all vendor orders for a vendor by status with pagination
     * Allows vendors to filter their orders by status
     * 
     * @param vendorId Vendor ID
     * @param status Vendor order status
     * @param pageable Pagination information
     * @return Page of vendor orders
     */
    Page<VendorOrderResponse> getVendorOrdersByVendorIdAndStatus(
            Long vendorId, VendorOrderStatus status, Pageable pageable);
    
    /**
     * Search vendor orders by various criteria
     * Flexible search for vendor order management
     * 
     * @param vendorId Vendor ID
     * @param orderNumber Order number (optional)
     * @param status Order status (optional)
     * @param startDate Start date (optional)
     * @param endDate End date (optional)
     * @param minAmount Minimum order amount (optional)
     * @param maxAmount Maximum order amount (optional)
     * @param pageable Pagination information
     * @return Page of vendor orders matching the criteria
     */
    Page<VendorOrderResponse> searchVendorOrders(
            Long vendorId, String orderNumber, VendorOrderStatus status,
            LocalDate startDate, LocalDate endDate, 
            Double minAmount, Double maxAmount, Pageable pageable);
    
    /**
     * Update shipping information for a vendor order
     * Allows vendors to provide tracking and shipping details
     * 
     * @param id Vendor order ID
     * @param request Shipping update request
     * @return Updated vendor order
     */
    VendorOrderResponse updateShippingInfo(UUID id, VendorShippingUpdateRequest request);
    
    /**
     * Accept a vendor order
     * Vendor confirms they will fulfill the order
     * 
     * @param id Vendor order ID
     * @param estimatedShippingDate Estimated shipping date
     * @param notes Acceptance notes
     * @return Updated vendor order
     */
    VendorOrderResponse acceptVendorOrder(UUID id, LocalDate estimatedShippingDate, String notes);
    
    /**
     * Reject a vendor order
     * Vendor declines to fulfill the order
     * 
     * @param id Vendor order ID
     * @param reason Rejection reason
     * @return Updated vendor order
     */
    VendorOrderResponse rejectVendorOrder(UUID id, String reason);
    
    /**
     * Get vendor order analytics
     * Provides vendors with analytics about their orders
     * 
     * @param vendorId Vendor ID
     * @param startDate Start date
     * @param endDate End date
     * @return Vendor order analytics
     */
    VendorOrderAnalyticsResponse getVendorOrderAnalytics(Long vendorId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get vendor performance metrics
     * Provides vendors with metrics about their performance
     * 
     * @param vendorId Vendor ID
     * @param startDate Start date
     * @param endDate End date
     * @return Vendor performance metrics
     */
    VendorPerformanceResponse getVendorPerformance(Long vendorId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get pending vendor orders count
     * Quick way for vendors to see how many orders need attention
     * 
     * @param vendorId Vendor ID
     * @return Count of pending vendor orders
     */
    Map<VendorOrderStatus, Integer> getVendorOrdersCountByStatus(Long vendorId);
    
    /**
     * Get all vendor orders for a vendor (non-paginated)
     * 
     * @param vendorId Vendor ID
     * @return List of vendor orders
     * @deprecated Use getVendorOrdersByVendorId(Long, Pageable) instead
     */
    @Deprecated
    List<VendorOrderResponse> getVendorOrdersByVendorId(Long vendorId);
} 
