package com.winnguyen1905.order.rest.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.order.common.constant.VendorOrderStatus;
import com.winnguyen1905.order.model.CreateVendorOrderRequest;
import com.winnguyen1905.order.model.VendorOrderResponse;
import com.winnguyen1905.order.persistance.entity.EOrder;
import com.winnguyen1905.order.persistance.entity.EOrderItem;
import com.winnguyen1905.order.persistance.entity.EVendorOrder;
import com.winnguyen1905.order.persistance.repository.EOrderItemRepository;
import com.winnguyen1905.order.persistance.repository.EOrderRepository;
import com.winnguyen1905.order.persistance.repository.EVendorOrderRepository;
import com.winnguyen1905.order.rest.service.VendorOrderService;
import com.winnguyen1905.order.secure.BaseException;
import com.winnguyen1905.order.util.OrderMapper;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendorOrderServiceImpl implements VendorOrderService {
    private final EVendorOrderRepository vendorOrderRepository;
    private final EOrderRepository orderRepository;
    private final EOrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public VendorOrderResponse createVendorOrder(UUID orderId, CreateVendorOrderRequest request) {
        // Get the order
        EOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        
        // Check if vendor order for this vendor already exists for this order
        boolean vendorOrderExists = vendorOrderRepository.findAll().stream()
                .anyMatch(vo -> vo.getOrder().getId().equals(orderId) 
                        && vo.getVendorId().equals(request.getVendorId()));
        
        if (vendorOrderExists) {
            throw new BaseException("Vendor order already exists for vendor: " + request.getVendorId());
        }
        
        // Calculate subtotal of items from this vendor
        double subtotal = orderItemRepository.findAll().stream()
                .filter(item -> item.getOrder().getId().equals(orderId) 
                        && item.getVendorId().equals(request.getVendorId()))
                .mapToDouble(EOrderItem::getTotalPrice)
                .sum();
        
        if (subtotal <= 0) {
            throw new BaseException("No items found for vendor: " + request.getVendorId());
        }
        
        // Create vendor order entity
        EVendorOrder vendorOrder = orderMapper.toVendorOrderEntity(request, order, subtotal);
        
        // Save vendor order
        vendorOrder = vendorOrderRepository.save(vendorOrder);
        
        return orderMapper.toVendorOrderResponse(vendorOrder);
    }

    @Override
    public VendorOrderResponse getVendorOrderById(UUID id) {
        EVendorOrder vendorOrder = vendorOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vendor order not found with id: " + id));
        
        return orderMapper.toVendorOrderResponse(vendorOrder);
    }

    @Override
    public List<VendorOrderResponse> getVendorOrdersByOrderId(UUID orderId) {
        // This would typically involve a custom query in the repository
        return vendorOrderRepository.findAll().stream()
                .filter(vo -> vo.getOrder().getId().equals(orderId))
                .map(orderMapper::toVendorOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VendorOrderResponse> getVendorOrdersByVendorId(Long vendorId) {
        // This would typically involve a custom query in the repository
        return vendorOrderRepository.findAll().stream()
                .filter(vo -> vo.getVendorId().equals(vendorId))
                .map(orderMapper::toVendorOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VendorOrderResponse updateVendorOrderStatus(UUID id, VendorOrderStatus status) {
        EVendorOrder vendorOrder = vendorOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vendor order not found with id: " + id));
        
        // Validate status transition
        validateStatusTransition(vendorOrder.getStatus(), status);
        
        vendorOrder.setStatus(status);
        vendorOrder = vendorOrderRepository.save(vendorOrder);
        
        return orderMapper.toVendorOrderResponse(vendorOrder);
    }
    
    private void validateStatusTransition(VendorOrderStatus currentStatus, VendorOrderStatus newStatus) {
        // Implement business rules for valid status transitions
        if (currentStatus == VendorOrderStatus.CANCELLED && newStatus != VendorOrderStatus.CANCELLED) {
            throw new BaseException("Cannot change status from CANCELLED");
        }
        
        if (currentStatus == VendorOrderStatus.DELIVERED && newStatus != VendorOrderStatus.DELIVERED) {
            throw new BaseException("Cannot change status from DELIVERED");
        }
        
        // Add more validation rules as needed
    }
} 
