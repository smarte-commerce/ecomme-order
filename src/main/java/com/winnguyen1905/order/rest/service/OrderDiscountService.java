package com.winnguyen1905.order.rest.service;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.order.model.CreateDiscountRequest;
import com.winnguyen1905.order.model.OrderDiscountResponse;

public interface OrderDiscountService {
    /**
     * Apply a discount to an order
     * 
     * @param orderId Order ID
     * @param request Discount request
     * @return Applied discount response
     */
    OrderDiscountResponse applyDiscount(UUID orderId, CreateDiscountRequest request);
    
    /**
     * Get discount by ID
     * 
     * @param id Discount ID
     * @return Discount response
     */
    OrderDiscountResponse getDiscountById(UUID id);
    
    /**
     * Get all discounts for an order
     * 
     * @param orderId Order ID
     * @return List of discounts
     */
    List<OrderDiscountResponse> getDiscountsByOrderId(UUID orderId);
    
    /**
     * Remove discount from order
     * 
     * @param discountId Discount ID
     */
    void removeDiscount(UUID discountId);
} 
