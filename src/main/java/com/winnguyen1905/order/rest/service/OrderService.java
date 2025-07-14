package com.winnguyen1905.order.rest.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.winnguyen1905.order.model.request.CreateOrderRequest;
import com.winnguyen1905.order.model.response.OrderResponse;
import com.winnguyen1905.order.model.response.OrderSummaryResponse;
import com.winnguyen1905.order.common.constant.OrderStatus;
import com.winnguyen1905.order.secure.TAccountRequest;

/**
 * Service for managing orders
 * Supports operations for customers, vendors, and administrators
 */
public interface OrderService {
    /**
     * Create a new order
     * 
     * @param request Order creation request
     * @param accountRequest Account request
     * @return Created order response
     */ 
    OrderResponse createOrder(CreateOrderRequest request, TAccountRequest accountRequest);
    
    /**
     * Get order by ID
     * 
     * @param id Order ID
     * @return Order response
     */
    OrderResponse getOrderById(UUID id);
    
    /**
     * Get all orders with pagination
     *
     * @param pageable Pagination information
     * @return Page of orders
     */
    Page<OrderResponse> getAllOrders(Pageable pageable);
    
    /**
     * Get orders by customer ID with pagination
     * 
     * @param customerId Customer ID
     * @param pageable Pagination information
     * @return Page of orders for the customer
     */
    Page<OrderResponse> getOrdersByCustomerId(Long customerId, Pageable pageable);
    
    /**
     * Get orders by status with pagination
     * 
     * @param status Order status
     * @param pageable Pagination information
     * @return Page of orders with the specified status
     */
    Page<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable);
    
    /**
     * Update order status
     * 
     * @param id Order ID
     * @param status New status
     * @param reason Reason for status change
     * @return Updated order response
     */
    OrderResponse updateOrderStatus(UUID id, OrderStatus status, String reason);
    
    /**
     * Cancel order
     * 
     * @param id Order ID
     * @param reason Cancellation reason
     * @return Cancelled order response
     */
    OrderResponse cancelOrder(UUID id, String reason);
    
    /**
     * Delete order by ID
     * 
     * @param id Order ID
     */
    void deleteOrder(UUID id);

    // Payment-related methods

    /**
     * Update order payment amounts
     * 
     * @param orderId Order ID
     * @param paidAmount Amount that has been paid
     * @param amountToBePaid Amount remaining to be paid
     */
    void updateOrderPaymentAmounts(UUID orderId, BigDecimal paidAmount, BigDecimal amountToBePaid);

    /**
     * Mark order as paid
     * Sets paidAmount to the specified amount and amountToBePaid to 0
     * 
     * @param orderId Order ID
     * @param paidAmount Amount that was paid
     */
    void markOrderAsPaid(UUID orderId, BigDecimal paidAmount);

    /**
     * Mark order as unpaid
     * Sets paidAmount to 0 and amountToBePaid to totalAmount
     * 
     * @param orderId Order ID
     */
    void markOrderAsUnpaid(UUID orderId);
    
    /**
     * Get orders that contain products from a specific vendor
     * Useful for vendors to view all orders containing their products
     * 
     * @param vendorId Vendor ID
     * @param pageable Pagination information
     * @return Page of orders containing products from the vendor
     */
    Page<OrderResponse> getOrdersContainingVendorProducts(Long vendorId, Pageable pageable);
    
    /**
     * Search orders by various criteria
     * Flexible search for customer service and administrative purposes
     * 
     * @param customerId Customer ID (optional)
     * @param orderNumber Order number (optional)
     * @param status Order status (optional)
     * @param startDate Start date (optional)
     * @param endDate End date (optional)
     * @param minAmount Minimum order amount (optional)
     * @param maxAmount Maximum order amount (optional)
     * @param pageable Pagination information
     * @return Page of orders matching the criteria
     */
    Page<OrderResponse> searchOrders(Long customerId, String orderNumber, OrderStatus status,
                                   LocalDate startDate, LocalDate endDate, 
                                   Double minAmount, Double maxAmount, Pageable pageable);
    
    /**
     * Get customer order history summary
     * Provides analytics for customers about their ordering habits
     * 
     * @param customerId Customer ID
     * @return Order summary for the customer
     */
    OrderSummaryResponse getCustomerOrderSummary(Long customerId);
    
    /**
     * Request return or refund for an order
     * Customer-initiated returns or refunds
     * 
     * @param orderId Order ID
     * @param reason Return/refund reason
     * @param itemIds List of item IDs to return (optional, if empty all items are returned)
     * @return Updated order with return initiated
     */
    OrderResponse requestReturnOrRefund(UUID orderId, String reason, List<UUID> itemIds);
    
    /**
     * Process return or refund for an order
     * Vendor or admin processing of return/refund requests
     * 
     * @param orderId Order ID
     * @param approved Whether the return/refund is approved
     * @param refundAmount Amount to refund (if approved)
     * @param notes Processing notes
     * @return Updated order with processed return/refund
     */
    OrderResponse processReturnOrRefund(UUID orderId, boolean approved, Double refundAmount, String notes);
    
    /**
     * Generate invoice for an order
     * Creates a formal invoice document for an order
     * 
     * @param orderId Order ID
     * @return Path to generated invoice file
     */
    String generateInvoice(UUID orderId);
    
    /**
     * Get sales report for vendors
     * Provides vendors with analytics about their sales
     * 
     * @param vendorId Vendor ID
     * @param startDate Start date
     * @param endDate End date
     * @return Map with sales metrics
     */
    Map<String, Object> getVendorSalesReport(Long vendorId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get all orders (non-paginated, use with caution)
     * 
     * @return List of all orders
     * @deprecated Use getAllOrders(Pageable pageable) instead
     */
    @Deprecated
    List<OrderResponse> getAllOrders();
}
