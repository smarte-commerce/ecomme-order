package com.winnguyen1905.order.rest.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.order.common.constant.OrderItemStatus;
import com.winnguyen1905.order.model.request.CreateOrderItemRequest;
import com.winnguyen1905.order.model.request.UpdateOrderItemRequest;
import com.winnguyen1905.order.model.response.OrderItemResponse;
import com.winnguyen1905.order.model.response.OrderItemWithVendorDetailsResponse;
import com.winnguyen1905.order.persistance.entity.EOrder;
import com.winnguyen1905.order.persistance.entity.EOrderItem;
import com.winnguyen1905.order.persistance.repository.OrderItemRepository;
import com.winnguyen1905.order.persistance.repository.OrderRepository;
import com.winnguyen1905.order.rest.service.OrderItemService;
import com.winnguyen1905.order.secure.BaseException;
import com.winnguyen1905.order.util.OrderMapper;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderItemServiceImpl implements OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public OrderItemResponse getOrderItemById(UUID id) {
        EOrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order item not found with id: " + id));
        
        return orderMapper.toOrderItemResponse(orderItem);
    }

    @Override
    public List<OrderItemResponse> getOrderItemsByOrderId(UUID orderId) {
        // This would typically involve a custom query in the repository
        return orderItemRepository.findAll().stream()
                .filter(item -> item.getOrder().getId().equals(orderId))
                .map(orderMapper::toOrderItemResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<OrderItemResponse> getOrderItemsByOrderId(UUID orderId, Pageable pageable) {
        // This would typically involve a custom query in the repository with pagination
        List<OrderItemResponse> allItems = getOrderItemsByOrderId(orderId);
        
        return createPage(allItems, pageable);
    }

    @Override
    @Transactional
    public OrderItemResponse updateOrderItemStatus(UUID id, OrderItemStatus status) {
        EOrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order item not found with id: " + id));
        
        // Validate status transition
        validateStatusTransition(orderItem.getStatus(), status);
        
        orderItem.setStatus(status);
        orderItem = orderItemRepository.save(orderItem);
        
        log.info("Updated order item status. ID: {}, New status: {}", id, status);
        
        return orderMapper.toOrderItemResponse(orderItem);
    }

    @Override
    @Transactional
    public OrderItemResponse updateOrderItemQuantity(UUID id, int quantity) {
        if (quantity <= 0) {
            throw new BaseException("Quantity must be greater than zero");
        }
        
        EOrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order item not found with id: " + id));
        
        // Update quantity and recalculate total price
        orderItem.setQuantity(quantity);
        orderItem.setTotalPrice(orderItem.getUnitPrice() * quantity);
        
        orderItem = orderItemRepository.save(orderItem);
        
        // Update the order subtotal and total
        updateOrderTotals(orderItem.getOrder());
        
        log.info("Updated order item quantity. ID: {}, New quantity: {}", id, quantity);
        
        return orderMapper.toOrderItemResponse(orderItem);
    }

    @Override
    @Transactional
    public void deleteOrderItem(UUID id) {
        EOrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order item not found with id: " + id));
        
        EOrder order = orderItem.getOrder();
        
        orderItemRepository.deleteById(id);
        
        // Update the order subtotal and total
        updateOrderTotals(order);
        
        log.info("Deleted order item with ID: {}", id);
    }
    
    @Override
    @Transactional
    public OrderItemResponse addItemToOrder(UUID orderId, CreateOrderItemRequest request) {
        // Get the order
        EOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        
        // Create order item entity
        EOrderItem orderItem = EOrderItem.builder()
                .order(order)
                .productId(request.getProductId())
                .vendorId(request.getVendorId())
                .productName(request.getProductName())
                .productSku(request.getSku())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .totalPrice(request.getUnitPrice() * request.getQuantity())
                .status(OrderItemStatus.PENDING)
                .build();
        
        // Save order item
        orderItem = orderItemRepository.save(orderItem);
        
        // Update the order subtotal and total
        updateOrderTotals(order);
        
        log.info("Added item to order: {}, product: {}", orderId, request.getProductName());
        
        return orderMapper.toOrderItemResponse(orderItem);
    }
    
    @Override
    @Transactional
    public OrderItemResponse updateOrderItem(UUID id, UpdateOrderItemRequest request) {
        EOrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order item not found with id: " + id));
        
        // Update fields if provided in the request
        if (request.getProductName() != null) {
            orderItem.setProductName(request.getProductName());
        }
        
        if (request.getSku() != null) {
            orderItem.setProductSku(request.getSku());
        }
        
        if (request.getStatus() != null) {
            validateStatusTransition(orderItem.getStatus(), request.getStatus());
            orderItem.setStatus(request.getStatus());
        }
        
        boolean updateTotals = false;
        
        if (request.getUnitPrice() != null) {
            orderItem.setUnitPrice(request.getUnitPrice());
            updateTotals = true;
        }
        
        if (request.getQuantity() != null) {
            if (request.getQuantity() <= 0) {
                throw new BaseException("Quantity must be greater than zero");
            }
            orderItem.setQuantity(request.getQuantity());
            updateTotals = true;
        }
        
        // Recalculate total price if quantity or unit price has changed
        if (updateTotals) {
            orderItem.setTotalPrice(orderItem.getUnitPrice() * orderItem.getQuantity());
        }
        
        // Save order item
        orderItem = orderItemRepository.save(orderItem);
        
        // Update the order subtotal and total if needed
        if (updateTotals) {
            updateOrderTotals(orderItem.getOrder());
        }
        
        log.info("Updated order item. ID: {}", id);
        
        return orderMapper.toOrderItemResponse(orderItem);
    }
    
    @Override
    public Page<OrderItemResponse> getOrderItemsByVendorId(Long vendorId, Pageable pageable) {
        // This would typically involve a custom query in the repository with pagination
        List<OrderItemResponse> allItems = orderItemRepository.findAll().stream()
                .filter(item -> item.getVendorId().equals(vendorId))
                .map(orderMapper::toOrderItemResponse)
                .collect(Collectors.toList());
        
        return createPage(allItems, pageable);
    }
    
    @Override
    public Page<OrderItemResponse> getOrderItemsByProductId(Long productId, Pageable pageable) {
        // This would typically involve a custom query in the repository with pagination
        List<OrderItemResponse> allItems = orderItemRepository.findAll().stream()
                .filter(item -> item.getProductId().equals(productId))
                .map(orderMapper::toOrderItemResponse)
                .collect(Collectors.toList());
        
        return createPage(allItems, pageable);
    }
    
    @Override
    public Page<OrderItemResponse> getOrderItemsByStatus(OrderItemStatus status, Pageable pageable) {
        // This would typically involve a custom query in the repository with pagination
        List<OrderItemResponse> allItems = orderItemRepository.findAll().stream()
                .filter(item -> item.getStatus() == status)
                .map(orderMapper::toOrderItemResponse)
                .collect(Collectors.toList());
        
        return createPage(allItems, pageable);
    }
    
    @Override
    public Page<OrderItemWithVendorDetailsResponse> getVendorOrderItemsWithDetails(
            Long vendorId, OrderItemStatus status, Pageable pageable) {
        // This would typically involve a custom query in the repository with pagination
        
        // First, get all order items for the vendor with the specified status (or any status if null)
        List<EOrderItem> items = orderItemRepository.findAll().stream()
                .filter(item -> item.getVendorId().equals(vendorId) 
                        && (status == null || item.getStatus() == status))
                .collect(Collectors.toList());
        
        // Convert to detailed response objects with vendor information
        List<OrderItemWithVendorDetailsResponse> detailedItems = items.stream()
                .map(item -> {
                    EOrder order = item.getOrder();
                    
                    // In a real application, this would fetch additional information from a vendor service
                    // and a customer service
                    
                    return OrderItemWithVendorDetailsResponse.builder()
                            .id(item.getId())
                            .orderId(order.getId())
                            .orderNumber(order.getOrderNumber())
                            .productId(item.getProductId())
                            .productName(item.getProductName())
                            .sku(item.getProductSku())
                            .unitPrice(item.getUnitPrice())
                            .quantity(item.getQuantity())
                            .totalPrice(item.getTotalPrice())
                            .status(item.getStatus())
                            
                            // Customer information (would be fetched from customer service)
                            .customerId(order.getCustomerId())
                            .customerName("Customer Name") // Mock data
                            .customerEmail("customer@example.com") // Mock data
                            
                            // Shipping information (from order)
                            .shippingAddress(order.getShippingAddress())
                            
                            // Vendor information
                            .vendorId(item.getVendorId())
                            .vendorName("Vendor Name") // Mock data
                            
                            // Timing information
                            .orderDate(order.getCreatedDate().atZone(java.time.ZoneOffset.UTC).toLocalDateTime())
                            .statusUpdateDate(item.getUpdatedDate().atZone(java.time.ZoneOffset.UTC).toLocalDateTime())
                            
                            .build();
                })
                .collect(Collectors.toList());
        
        return createPage(detailedItems, pageable);
    }
    
    @Override
    @Transactional
    public Map<UUID, Boolean> bulkUpdateOrderItemStatus(List<UUID> itemIds, OrderItemStatus status) {
        Map<UUID, Boolean> results = new HashMap<>();
        
        for (UUID itemId : itemIds) {
            try {
                EOrderItem orderItem = orderItemRepository.findById(itemId)
                        .orElseThrow(() -> new EntityNotFoundException("Order item not found with id: " + itemId));
                
                // Validate status transition
                validateStatusTransition(orderItem.getStatus(), status);
                
                orderItem.setStatus(status);
                orderItemRepository.save(orderItem);
                
                results.put(itemId, true);
                log.info("Updated order item status. ID: {}, New status: {}", itemId, status);
            } catch (Exception e) {
                results.put(itemId, false);
                log.error("Failed to update order item status. ID: {}, Error: {}", itemId, e.getMessage());
            }
        }
        
        return results;
    }
    
    @Override
    public Map<Long, Integer> getVendorSalesByProduct(Long vendorId) {
        // This would typically involve a custom query in the repository
        
        // Get all order items for the vendor
        List<EOrderItem> items = orderItemRepository.findAll().stream()
                .filter(item -> item.getVendorId().equals(vendorId))
                .collect(Collectors.toList());
        
        // Group by product ID and sum quantities
        Map<Long, Integer> salesByProduct = new HashMap<>();
        
        for (EOrderItem item : items) {
            Long productId = item.getProductId();
            int quantity = item.getQuantity();
            
            salesByProduct.put(productId, salesByProduct.getOrDefault(productId, 0) + quantity);
        }
        
        return salesByProduct;
    }
    
    /**
     * Create a Page object from a list with pagination
     */
    private <T> Page<T> createPage(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        
        if (start > list.size()) {
            return new PageImpl<>(List.of(), pageable, list.size());
        }
        
        return new PageImpl<>(
                list.subList(start, end),
                pageable,
                list.size());
    }
    
    /**
     * Update the order subtotal and total
     */
    private void updateOrderTotals(EOrder order) {
        // Calculate subtotal from all order items
        double subtotal = orderItemRepository.findAll().stream()
                .filter(item -> item.getOrder().getId().equals(order.getId()))
                .mapToDouble(EOrderItem::getTotalPrice)
                .sum();
        
        // Update order
        order.setSubtotal(subtotal);
        
        // Recalculate total amount
        double totalAmount = subtotal - order.getDiscountAmount() 
                + order.getTaxAmount() + order.getShippingAmount();
        order.setTotalAmount(totalAmount);
        
        // Save order
        orderRepository.save(order);
        
        log.info("Updated order totals. Order ID: {}, Subtotal: {}, Total: {}", 
                order.getId(), subtotal, totalAmount);
    }
    
    /**
     * Validate order item status transition
     */
    private void validateStatusTransition(OrderItemStatus currentStatus, OrderItemStatus newStatus) {
        // Implement business rules for valid status transitions
        if (currentStatus == OrderItemStatus.CANCELLED && newStatus != OrderItemStatus.CANCELLED) {
            throw new BaseException("Cannot change status from CANCELLED");
        }
        
        if (currentStatus == OrderItemStatus.DELIVERED && 
                (newStatus != OrderItemStatus.DELIVERED && newStatus != OrderItemStatus.REFUNDED)) {
            throw new BaseException("Item is already delivered, can only change to REFUNDED");
        }
        
        // Add more validation rules as needed
    }
} 
