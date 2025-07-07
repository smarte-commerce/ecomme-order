package com.winnguyen1905.order.rest.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.winnguyen1905.order.model.request.CreateDiscountRequest;
import com.winnguyen1905.order.model.response.OrderDiscountResponse;

/**
 * Service for managing order discounts
 * Supports both customer and administrative operations for discounts
 */
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
    
    /**
     * Validate discount code for an order
     * Used by customers before applying a discount
     * 
     * @param orderId Order ID
     * @param discountCode Discount code to validate
     * @return true if discount code is valid for this order
     */
    boolean validateDiscountCode(UUID orderId, String discountCode);
    
    /**
     * Get available discounts for a customer
     * Shows customer what discounts they can apply to their orders
     * 
     * @param customerId Customer ID
     * @param pageable Pagination information
     * @return Page of available discount responses
     */
    Page<OrderDiscountResponse> getAvailableDiscountsForCustomer(Long customerId, Pageable pageable);
    
    /**
     * Apply automatic discounts to an order
     * System-generated discounts based on order properties or promotions
     * 
     * @param orderId Order ID
     * @return List of applied automatic discounts
     */
    List<OrderDiscountResponse> applyAutomaticDiscounts(UUID orderId);
    
    /**
     * Get total discount amount for an order
     * 
     * @param orderId Order ID
     * @return Total discount amount
     */
    double calculateTotalDiscountAmount(UUID orderId);
    
    /**
     * Get discount usage statistics for vendor analytics
     * Helps vendors understand which discounts are being used with their products
     * 
     * @param vendorId Vendor ID
     * @param startDate Start date (optional)
     * @param endDate End date (optional)
     * @return Map of discount code to usage count
     */
    List<DiscountUsageStats> getDiscountUsageStatsByVendor(Long vendorId, String startDate, String endDate);
    
    /**
     * Inner class for discount usage statistics
     */
    class DiscountUsageStats {
        private String discountCode;
        private int usageCount;
        private double totalDiscountAmount;
        
        // Getters and setters
        public String getDiscountCode() {
            return discountCode;
        }
        
        public void setDiscountCode(String discountCode) {
            this.discountCode = discountCode;
        }
        
        public int getUsageCount() {
            return usageCount;
        }
        
        public void setUsageCount(int usageCount) {
            this.usageCount = usageCount;
        }
        
        public double getTotalDiscountAmount() {
            return totalDiscountAmount;
        }
        
        public void setTotalDiscountAmount(double totalDiscountAmount) {
            this.totalDiscountAmount = totalDiscountAmount;
        }
    }
} 
