package com.winnguyen1905.order.rest.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.order.common.constant.OrderStatus;
import com.winnguyen1905.order.model.CreateOrderRequest;
import com.winnguyen1905.order.model.OrderResponse;
import com.winnguyen1905.order.persistance.entity.EOrder;
import com.winnguyen1905.order.persistance.entity.EOrderItem;
import com.winnguyen1905.order.persistance.entity.EOrderStatusHistory;
import com.winnguyen1905.order.persistance.repository.EOrderItemRepository;
import com.winnguyen1905.order.persistance.repository.EOrderRepository;
import com.winnguyen1905.order.persistance.repository.EOrderStatusHistoryRepository;
import com.winnguyen1905.order.rest.service.OrderService;
import com.winnguyen1905.order.secure.BaseException;
import com.winnguyen1905.order.util.OrderMapper;
import com.winnguyen1905.order.util.OrderNumberGenerator;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final EOrderRepository orderRepository;
    private final EOrderItemRepository orderItemRepository;
    private final EOrderStatusHistoryRepository orderStatusHistoryRepository;
    private final OrderMapper orderMapper;
    private final OrderNumberGenerator orderNumberGenerator;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // Create order entity
        EOrder order = orderMapper.toOrderEntity(request);
        
        // Generate order number
        order.setOrderNumber(orderNumberGenerator.generateOrderNumber());
        
        // Save order to get ID
        order = orderRepository.save(order);
        
        // Create order items
        List<EOrderItem> orderItems = request.getItems().stream()
                .map(itemRequest -> orderMapper.toOrderItemEntity(itemRequest, order))
                .collect(Collectors.toList());
        
        // Save order items
        orderItems = orderItemRepository.saveAll(orderItems);
        
        // Calculate order amounts
        double subtotal = orderItems.stream()
                .mapToDouble(EOrderItem::getTotalPrice)
                .sum();
        
        // For simplicity, we're setting default values for discount, tax and shipping
        // In a real system, these would be calculated based on business rules
        double discountAmount = 0.0;
        double taxAmount = subtotal * 0.1; // 10% tax for example
        double shippingAmount = 10.0; // Flat shipping rate for example
        double totalAmount = subtotal - discountAmount + taxAmount + shippingAmount;
        
        // Update order with calculated amounts
        order.setSubtotal(subtotal);
        order.setDiscountAmount(discountAmount);
        order.setTaxAmount(taxAmount);
        order.setShippingAmount(shippingAmount);
        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);
        
        // Save updated order
        order = orderRepository.save(order);
        
        // Create initial status history
        createStatusHistory(order, null, OrderStatus.PENDING, "Order created");
        
        return orderMapper.toOrderResponse(order);
    }

    @Override
    public OrderResponse getOrderById(UUID id) {
        EOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        
        return orderMapper.toOrderResponse(order);
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByCustomerId(Long customerId) {
        // This would typically involve a custom query in the repository
        return orderRepository.findAll().stream()
                .filter(order -> order.getCustomerId().equals(customerId))
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        // This would typically involve a custom query in the repository
        return orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == status)
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(UUID id, OrderStatus status, String reason) {
        EOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        
        // Check if status transition is valid
        validateStatusTransition(order.getStatus(), status);
        
        // Update order status
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(status);
        order = orderRepository.save(order);
        
        // Create status history entry
        createStatusHistory(order, oldStatus, status, reason);
        
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(UUID id, String reason) {
        return updateOrderStatus(id, OrderStatus.CANCELLED, reason);
    }

    @Override
    @Transactional
    public void deleteOrder(UUID id) {
        if (!orderRepository.existsById(id)) {
            throw new EntityNotFoundException("Order not found with id: " + id);
        }
        
        orderRepository.deleteById(id);
    }
    
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Implement business rules for valid status transitions
        // For example, can't change from DELIVERED to PROCESSING
        if (currentStatus == OrderStatus.CANCELLED && newStatus != OrderStatus.CANCELLED) {
            throw new BaseException("Cannot change status from CANCELLED");
        }
        
        if (currentStatus == OrderStatus.DELIVERED && 
                (newStatus != OrderStatus.DELIVERED && newStatus != OrderStatus.REFUNDED)) {
            throw new BaseException("Order is already delivered, can only change to REFUNDED");
        }
        
        // Add more validation rules as needed
    }
    
    private void createStatusHistory(EOrder order, OrderStatus oldStatus, OrderStatus newStatus, String reason) {
        EOrderStatusHistory statusHistory = EOrderStatusHistory.builder()
                .order(order)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .reason(reason)
                .build();
        
        orderStatusHistoryRepository.save(statusHistory);
    }
} 
