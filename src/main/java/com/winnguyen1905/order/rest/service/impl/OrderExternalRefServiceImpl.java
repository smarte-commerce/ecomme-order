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

import com.winnguyen1905.order.model.request.CreateExternalRefRequest;
import com.winnguyen1905.order.model.response.OrderExternalRefResponse;
import com.winnguyen1905.order.persistance.entity.EOrder;
import com.winnguyen1905.order.persistance.entity.EOrderExternalRef;
import com.winnguyen1905.order.persistance.repository.OrderExternalRefRepository;
import com.winnguyen1905.order.persistance.repository.OrderRepository;
import com.winnguyen1905.order.rest.service.OrderExternalRefService;
import com.winnguyen1905.order.secure.BaseException;
import com.winnguyen1905.order.util.OrderMapper;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderExternalRefServiceImpl implements OrderExternalRefService {
    private final OrderExternalRefRepository orderExternalRefRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderExternalRefResponse createExternalRef(UUID orderId, CreateExternalRefRequest request) {
        // Get the order
        EOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        
        // Check if external reference already exists for this service and order
        boolean refExists = orderExternalRefRepository.findAll().stream()
                .anyMatch(ref -> ref.getOrder().getId().equals(orderId) 
                        && ref.getServiceName().equals(request.getServiceName())
                        && ref.getRefType().equals(request.getRefType()));
        
        if (refExists) {
            throw new BaseException("External reference already exists for service: " + request.getServiceName() 
                    + " and ref type: " + request.getRefType());
        }
        
        // Create external reference entity
        EOrderExternalRef externalRef = orderMapper.toOrderExternalRefEntity(request, order);
        
        // Save external reference
        externalRef = orderExternalRefRepository.save(externalRef);
        
        log.info("Created external reference with ID: {} for order: {}, service: {}", 
                externalRef.getId(), orderId, request.getServiceName());
        
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
    public Page<OrderExternalRefResponse> getExternalRefsByServiceName(String serviceName, Pageable pageable) {
        // This would typically involve a custom query in the repository with pagination
        List<OrderExternalRefResponse> allRefs = orderExternalRefRepository.findAll().stream()
                .filter(ref -> ref.getServiceName().equals(serviceName))
                .map(orderMapper::toOrderExternalRefResponse)
                .collect(Collectors.toList());
        
        // Implement simple pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allRefs.size());
        
        if (start > allRefs.size()) {
            return new PageImpl<>(List.of(), pageable, allRefs.size());
        }
        
        return new PageImpl<>(
                allRefs.subList(start, end),
                pageable,
                allRefs.size());
    }

    @Override
    @Transactional
    public OrderExternalRefResponse updateExternalRefStatus(UUID id, String status) {
        EOrderExternalRef externalRef = orderExternalRefRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("External reference not found with id: " + id));
        
        externalRef.setStatus(status);
        externalRef = orderExternalRefRepository.save(externalRef);
        
        log.info("Updated external reference status. ID: {}, New status: {}", id, status);
        
        return orderMapper.toOrderExternalRefResponse(externalRef);
    }

    @Override
    @Transactional
    public void deleteExternalRef(UUID id) {
        if (!orderExternalRefRepository.existsById(id)) {
            throw new EntityNotFoundException("External reference not found with id: " + id);
        }
        
        orderExternalRefRepository.deleteById(id);
        log.info("Deleted external reference with ID: {}", id);
    }
    
    @Override
    public OrderExternalRefResponse findOrderByExternalReference(String externalId, String serviceName) {
        // Find the external reference by external ID and service name
        EOrderExternalRef externalRef = orderExternalRefRepository.findAll().stream()
                .filter(ref -> ref.getExternalId().equals(externalId) 
                        && ref.getServiceName().equals(serviceName))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("External reference not found with externalId: " 
                        + externalId + " and serviceName: " + serviceName));
        
        return orderMapper.toOrderExternalRefResponse(externalRef);
    }
    
    @Override
    @Transactional
    public OrderExternalRefResponse syncOrderWithExternalSystem(UUID orderId, String serviceName) {
        // Find the external reference by order ID and service name
        EOrderExternalRef externalRef = orderExternalRefRepository.findAll().stream()
                .filter(ref -> ref.getOrder().getId().equals(orderId) 
                        && ref.getServiceName().equals(serviceName))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("External reference not found for orderId: " 
                        + orderId + " and serviceName: " + serviceName));
        
        // In a real application, this would call the external service API to get the latest status
        // For now, we'll just simulate a status update
        String newStatus = "SYNCHRONIZED";
        externalRef.setStatus(newStatus);
        externalRef = orderExternalRefRepository.save(externalRef);
        
        log.info("Synchronized order: {} with external system: {}, new status: {}", 
                orderId, serviceName, newStatus);
        
        return orderMapper.toOrderExternalRefResponse(externalRef);
    }
    
    @Override
    @Transactional
    public List<OrderExternalRefResponse> batchCreateExternalRefs(UUID orderId, List<CreateExternalRefRequest> requests) {
        // Get the order
        EOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        
        List<EOrderExternalRef> createdRefs = new ArrayList<>();
        
        for (CreateExternalRefRequest request : requests) {
            // Check if external reference already exists for this service and order
            boolean refExists = orderExternalRefRepository.findAll().stream()
                    .anyMatch(ref -> ref.getOrder().getId().equals(orderId) 
                            && ref.getServiceName().equals(request.getServiceName())
                            && ref.getRefType().equals(request.getRefType()));
            
            if (!refExists) {
                // Create external reference entity
                EOrderExternalRef externalRef = orderMapper.toOrderExternalRefEntity(request, order);
                
                // Save external reference
                externalRef = orderExternalRefRepository.save(externalRef);
                createdRefs.add(externalRef);
                
                log.info("Created external reference with ID: {} for order: {}, service: {}", 
                        externalRef.getId(), orderId, request.getServiceName());
            } else {
                log.warn("External reference already exists for service: {} and ref type: {}", 
                        request.getServiceName(), request.getRefType());
            }
        }
        
        return createdRefs.stream()
                .map(orderMapper::toOrderExternalRefResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Object> getCustomerServiceInfo(UUID orderId) {
        // Get all external references for the order
        List<EOrderExternalRef> externalRefs = orderExternalRefRepository.findAll().stream()
                .filter(ref -> ref.getOrder().getId().equals(orderId))
                .collect(Collectors.toList());
        
        Map<String, Object> customerServiceInfo = new HashMap<>();
        
        // Example: Extract tracking information
        externalRefs.stream()
                .filter(ref -> "SHIPPING".equals(ref.getRefType()))
                .forEach(ref -> {
                    Map<String, String> trackingInfo = new HashMap<>();
                    trackingInfo.put("trackingNumber", ref.getExternalId());
                    trackingInfo.put("status", ref.getStatus());
                    trackingInfo.put("updatedAt", ref.getUpdatedDate().toString());
                    
                    customerServiceInfo.put(ref.getServiceName(), trackingInfo);
                });
        
        // Example: Extract payment information
        externalRefs.stream()
                .filter(ref -> "PAYMENT".equals(ref.getRefType()))
                .forEach(ref -> {
                    Map<String, String> paymentInfo = new HashMap<>();
                    paymentInfo.put("transactionId", ref.getExternalId());
                    paymentInfo.put("status", ref.getStatus());
                    
                    customerServiceInfo.put(ref.getServiceName(), paymentInfo);
                });
        
        // Example: Extract support ticket information
        externalRefs.stream()
                .filter(ref -> "SUPPORT".equals(ref.getRefType()))
                .forEach(ref -> {
                    Map<String, String> supportInfo = new HashMap<>();
                    supportInfo.put("ticketId", ref.getExternalId());
                    supportInfo.put("status", ref.getStatus());
                    
                    customerServiceInfo.put(ref.getServiceName(), supportInfo);
                });
        
        return customerServiceInfo;
    }
    
    @Override
    @Transactional
    public OrderExternalRefResponse linkVendorReference(UUID orderId, Long vendorId, String vendorOrderId, 
            Map<String, Object> additionalInfo) {
        // Get the order
        EOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        
        // Check if vendor reference already exists for this order and vendor
        boolean refExists = orderExternalRefRepository.findAll().stream()
                .anyMatch(ref -> ref.getOrder().getId().equals(orderId) 
                        && ref.getServiceName().equals("VENDOR_" + vendorId)
                        && ref.getRefType().equals("VENDOR_ORDER"));
        
        if (refExists) {
            throw new BaseException("Vendor reference already exists for vendor: " + vendorId);
        }
        
        // Create vendor reference request
        CreateExternalRefRequest request = CreateExternalRefRequest.builder()
                .serviceName("VENDOR_" + vendorId)
                .externalId(vendorOrderId)
                .refType("VENDOR_ORDER")
                .status("ACTIVE")
                .build();
        
        // Create external reference entity
        EOrderExternalRef externalRef = orderMapper.toOrderExternalRefEntity(request, order);
        
        // Save external reference
        externalRef = orderExternalRefRepository.save(externalRef);
        
        log.info("Linked vendor reference for order: {}, vendor: {}, vendorOrderId: {}", 
                orderId, vendorId, vendorOrderId);
        
        return orderMapper.toOrderExternalRefResponse(externalRef);
    }
} 
