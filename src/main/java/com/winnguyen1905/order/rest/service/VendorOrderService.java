package com.winnguyen1905.order.rest.service;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.order.common.constant.VendorOrderStatus;
import com.winnguyen1905.order.model.CreateVendorOrderRequest;
import com.winnguyen1905.order.model.VendorOrderResponse;

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
     * Get all vendor orders for a vendor
     * 
     * @param vendorId Vendor ID
     * @return List of vendor orders
     */
    List<VendorOrderResponse> getVendorOrdersByVendorId(Long vendorId);
    
    /**
     * Update vendor order status
     * 
     * @param id Vendor order ID
     * @param status New status
     * @return Updated vendor order
     */
    VendorOrderResponse updateVendorOrderStatus(UUID id, VendorOrderStatus status);
} 
