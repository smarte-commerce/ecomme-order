package com.winnguyen1905.order.rest.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.order.model.CreateExternalRefRequest;
import com.winnguyen1905.order.model.OrderExternalRefResponse;
import com.winnguyen1905.order.persistance.entity.EOrder;
import com.winnguyen1905.order.persistance.entity.EOrderExternalRef;
import com.winnguyen1905.order.persistance.repository.EOrderExternalRefRepository;
import com.winnguyen1905.order.persistance.repository.EOrderRepository;
import com.winnguyen1905.order.rest.service.OrderExternalRefService;
import com.winnguyen1905.order.util.OrderMapper;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderExternalRefServiceImpl implements OrderExternalRefService {
    private final EOrderExternalRefRepository orderExternalRefRepository;
    private final EOrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderExternalRefResponse createExternalRef(UUID orderId, CreateExternalRefRequest request) {
        // Get the order
        EOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        
        // Create external reference entity
        EOrderExternalRef externalRef = orderMapper.toOrderExternalRefEntity(request, order);
        
        // Save external reference
        externalRef = orderExternalRefRepository.save(externalRef);
        
        return orderMapper.toOrderExternalRefResponse(externalRef);
    }

    @Override
    public OrderExternalRefResponse getExternalRefById(UUID id) {
        EOrderExternalRef externalRef = orderExternalRefRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("External reference not found with id: " + id));
        
        return orderMapper.toOrderExternalRefResponse(externalRef);
    }

    @Override
    public List<OrderExternalRefResponse> getExternalRefsByOrderId(UUID orderId) {
        // This would typically involve a custom query in the repository
        return orderExternalRefRepository.findAll().stream()
                .filter(ref -> ref.getOrder().getId().equals(orderId))
                .map(orderMapper::toOrderExternalRefResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderExternalRefResponse> getExternalRefsByServiceName(String serviceName) {
        // This would typically involve a custom query in the repository
        return orderExternalRefRepository.findAll().stream()
                .filter(ref -> ref.getServiceName().equals(serviceName))
                .map(orderMapper::toOrderExternalRefResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderExternalRefResponse updateExternalRefStatus(UUID id, String status) {
        EOrderExternalRef externalRef = orderExternalRefRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("External reference not found with id: " + id));
        
        externalRef.setStatus(status);
        externalRef = orderExternalRefRepository.save(externalRef);
        
        return orderMapper.toOrderExternalRefResponse(externalRef);
    }

    @Override
    @Transactional
    public void deleteExternalRef(UUID id) {
        if (!orderExternalRefRepository.existsById(id)) {
            throw new EntityNotFoundException("External reference not found with id: " + id);
        }
        
        orderExternalRefRepository.deleteById(id);
    }
} 
