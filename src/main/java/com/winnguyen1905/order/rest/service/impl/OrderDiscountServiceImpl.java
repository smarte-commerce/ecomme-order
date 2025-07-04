package com.winnguyen1905.order.rest.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.order.model.CreateDiscountRequest;
import com.winnguyen1905.order.model.OrderDiscountResponse;
import com.winnguyen1905.order.persistance.entity.EOrder;
import com.winnguyen1905.order.persistance.entity.EOrderDiscount;
import com.winnguyen1905.order.persistance.repository.EOrderDiscountRepository;
import com.winnguyen1905.order.persistance.repository.EOrderRepository;
import com.winnguyen1905.order.rest.service.OrderDiscountService;
import com.winnguyen1905.order.secure.BaseException;
import com.winnguyen1905.order.util.OrderMapper;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderDiscountServiceImpl implements OrderDiscountService {
    private final EOrderDiscountRepository orderDiscountRepository;
    private final EOrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderDiscountResponse applyDiscount(UUID orderId, CreateDiscountRequest request) {
        // Get the order
        EOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        
        // Check if discount code already applied to this order
        boolean discountExists = orderDiscountRepository.findAll().stream()
                .anyMatch(discount -> discount.getOrder().getId().equals(orderId) 
                        && discount.getDiscountCode().equals(request.getDiscountCode()));
        
        if (discountExists) {
            throw new BaseException("Discount code already applied to this order: " + request.getDiscountCode());
        }
        
        // Create order discount entity
        EOrderDiscount orderDiscount = orderMapper.toOrderDiscountEntity(request, order);
        
        // Save discount
        orderDiscount = orderDiscountRepository.save(orderDiscount);
        
        // Update order discount amount
        updateOrderDiscountAmount(order);
        
        return orderMapper.toOrderDiscountResponse(orderDiscount);
    }

    @Override
    public OrderDiscountResponse getDiscountById(UUID id) {
        EOrderDiscount orderDiscount = orderDiscountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Discount not found with id: " + id));
        
        return orderMapper.toOrderDiscountResponse(orderDiscount);
    }

    @Override
    public List<OrderDiscountResponse> getDiscountsByOrderId(UUID orderId) {
        // This would typically involve a custom query in the repository
        return orderDiscountRepository.findAll().stream()
                .filter(discount -> discount.getOrder().getId().equals(orderId))
                .map(orderMapper::toOrderDiscountResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeDiscount(UUID discountId) {
        EOrderDiscount orderDiscount = orderDiscountRepository.findById(discountId)
                .orElseThrow(() -> new EntityNotFoundException("Discount not found with id: " + discountId));
        
        EOrder order = orderDiscount.getOrder();
        
        // Delete the discount
        orderDiscountRepository.deleteById(discountId);
        
        // Update order discount amount
        updateOrderDiscountAmount(order);
    }
    
    /**
     * Update the order's total discount amount
     * 
     * @param order Order to update
     */
    private void updateOrderDiscountAmount(EOrder order) {
        // Calculate total discount amount
        double totalDiscount = orderDiscountRepository.findAll().stream()
                .filter(discount -> discount.getOrder().getId().equals(order.getId()))
                .mapToDouble(EOrderDiscount::getDiscountAmount)
                .sum();
        
        // Update order
        order.setDiscountAmount(totalDiscount);
        
        // Recalculate total amount
        double totalAmount = order.getSubtotal() - totalDiscount + order.getTaxAmount() + order.getShippingAmount();
        order.setTotalAmount(totalAmount);
        
        // Save order
        orderRepository.save(order);
    }
} 
