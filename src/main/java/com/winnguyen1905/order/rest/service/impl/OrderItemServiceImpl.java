package com.winnguyen1905.order.rest.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.order.common.constant.OrderItemStatus;
import com.winnguyen1905.order.model.OrderItemResponse;
import com.winnguyen1905.order.persistance.entity.EOrderItem;
import com.winnguyen1905.order.persistance.repository.EOrderItemRepository;
import com.winnguyen1905.order.rest.service.OrderItemService;
import com.winnguyen1905.order.secure.BaseException;
import com.winnguyen1905.order.util.OrderMapper;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {
    private final EOrderItemRepository orderItemRepository;
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
    @Transactional
    public OrderItemResponse updateOrderItemStatus(UUID id, OrderItemStatus status) {
        EOrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order item not found with id: " + id));
        
        orderItem.setStatus(status);
        orderItem = orderItemRepository.save(orderItem);
        
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
        
        // In a real application, we would also need to update the order totals
        
        return orderMapper.toOrderItemResponse(orderItem);
    }

    @Override
    @Transactional
    public void deleteOrderItem(UUID id) {
        if (!orderItemRepository.existsById(id)) {
            throw new EntityNotFoundException("Order item not found with id: " + id);
        }
        
        orderItemRepository.deleteById(id);
        
        // In a real application, we would also need to update the order totals
    }
} 
