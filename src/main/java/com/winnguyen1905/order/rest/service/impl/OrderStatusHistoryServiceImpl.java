package com.winnguyen1905.order.rest.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

import com.winnguyen1905.order.common.constant.OrderStatus;
import com.winnguyen1905.order.model.response.OrderStatusHistoryResponse;
import com.winnguyen1905.order.model.response.StatusTransitionSummary;
import com.winnguyen1905.order.persistance.entity.EOrder;
import com.winnguyen1905.order.persistance.entity.EOrderStatusHistory;
import com.winnguyen1905.order.persistance.repository.OrderRepository;
import com.winnguyen1905.order.persistance.repository.OrderStatusHistoryRepository;
import com.winnguyen1905.order.rest.service.OrderStatusHistoryService;
import com.winnguyen1905.order.util.OrderMapper;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStatusHistoryServiceImpl implements OrderStatusHistoryService {
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public OrderStatusHistoryResponse getStatusHistoryById(UUID id) {
        EOrderStatusHistory statusHistory = orderStatusHistoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Status history not found with id: " + id));
        
        return orderMapper.toOrderStatusHistoryResponse(statusHistory);
    }

    @Override
    public List<OrderStatusHistoryResponse> getStatusHistoryByOrderId(UUID orderId) {
        // This would typically involve a custom query in the repository
        List<EOrderStatusHistory> history = orderStatusHistoryRepository.findAll().stream()
                .filter(h -> h.getOrder().getId().equals(orderId))
                .sorted((h1, h2) -> h1.getCreatedDate().compareTo(h2.getCreatedDate()))
                .collect(Collectors.toList());
        
        return history.stream()
                .map(orderMapper::toOrderStatusHistoryResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<OrderStatusHistoryResponse> getStatusHistoryByOrderIdPaginated(UUID orderId, Pageable pageable) {
        List<OrderStatusHistoryResponse> history = getStatusHistoryByOrderId(orderId);
        
        return createPage(history, pageable);
    }
    
    @Override
    @Transactional
    public OrderStatusHistoryResponse addOrderComment(UUID orderId, String comment, boolean visibleToCustomer) {
        // Get the order
        EOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        
        // Create status history entry without changing status
        EOrderStatusHistory statusHistory = EOrderStatusHistory.builder()
                .order(order)
                .oldStatus(order.getStatus())
                .newStatus(order.getStatus())
                .reason("Comment added")
                .notes(comment)
                .changedBy("System") // In a real app, this would be the current user
                .build();
        
        // Save status history entry
        statusHistory = orderStatusHistoryRepository.save(statusHistory);
        
        log.info("Added comment to order: {}", orderId);
        
        return orderMapper.toOrderStatusHistoryResponse(statusHistory);
    }
    
    @Override
    public Page<OrderStatusHistoryResponse> getStatusHistoryByDateRange(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        
        Instant startInstant = startDate != null 
                ? startDate.atZone(ZoneId.systemDefault()).toInstant() 
                : Instant.EPOCH;
        
        Instant endInstant = endDate != null 
                ? endDate.atZone(ZoneId.systemDefault()).toInstant() 
                : Instant.now();
        
        // This would typically involve a custom query in the repository with pagination
        List<EOrderStatusHistory> history = orderStatusHistoryRepository.findAll().stream()
                .filter(h -> !h.getCreatedDate().isBefore(startInstant) && !h.getCreatedDate().isAfter(endInstant))
                .collect(Collectors.toList());
        
        List<OrderStatusHistoryResponse> responses = history.stream()
                .map(orderMapper::toOrderStatusHistoryResponse)
                .collect(Collectors.toList());
        
        return createPage(responses, pageable);
    }
    
    @Override
    public Page<OrderStatusHistoryResponse> getStatusTransitionsByUser(Long userId, Pageable pageable) {
        // This would typically involve a custom query in the repository with pagination
        // For now, we'll simulate with user ID as a string in changedBy field
        String userIdString = userId.toString();
        
        List<EOrderStatusHistory> history = orderStatusHistoryRepository.findAll().stream()
                .filter(h -> userIdString.equals(h.getChangedBy()))
                .collect(Collectors.toList());
        
        List<OrderStatusHistoryResponse> responses = history.stream()
                .map(orderMapper::toOrderStatusHistoryResponse)
                .collect(Collectors.toList());
        
        return createPage(responses, pageable);
    }
    
    @Override
    public Page<OrderStatusHistoryResponse> getStatusHistoryForVendor(Long vendorId, Pageable pageable) {
        // This would typically involve a custom query in the repository with pagination
        // For a real implementation, this would involve joining with vendor_orders table
        // For now, we'll return a mock implementation
        
        List<OrderStatusHistoryResponse> mockResponses = new ArrayList<>();
        
        // In a real app, this would be based on actual data
        return createPage(mockResponses, pageable);
    }
    
    @Override
    public Map<OrderStatus, StatusTransitionSummary> getStatusTransitionMetrics(
            LocalDateTime startDate, LocalDateTime endDate) {
        
        Instant startInstant = startDate != null 
                ? startDate.atZone(ZoneId.systemDefault()).toInstant() 
                : Instant.EPOCH;
        
        Instant endInstant = endDate != null 
                ? endDate.atZone(ZoneId.systemDefault()).toInstant() 
                : Instant.now();
        
        // Get all status history entries in the date range
        List<EOrderStatusHistory> history = orderStatusHistoryRepository.findAll().stream()
                .filter(h -> !h.getCreatedDate().isBefore(startInstant) && !h.getCreatedDate().isAfter(endInstant))
                .collect(Collectors.toList());
        
        // Group by new status
        Map<OrderStatus, List<EOrderStatusHistory>> historyByStatus = history.stream()
                .collect(Collectors.groupingBy(EOrderStatusHistory::getNewStatus));
        
        // Create metrics for each status
        Map<OrderStatus, StatusTransitionSummary> metrics = new HashMap<>();
        
        for (OrderStatus status : OrderStatus.values()) {
            List<EOrderStatusHistory> statusHistory = historyByStatus.getOrDefault(status, List.of());
            
            if (statusHistory.isEmpty()) {
                continue;
            }
            
            // Count transitions
            int transitionCount = statusHistory.size();
            
            // Find first and last transition dates
            Instant firstTransitionDate = statusHistory.stream()
                    .map(EOrderStatusHistory::getCreatedDate)
                    .min(Instant::compareTo)
                    .orElse(null);
            
            Instant lastTransitionDate = statusHistory.stream()
                    .map(EOrderStatusHistory::getCreatedDate)
                    .max(Instant::compareTo)
                    .orElse(null);
            
            // Count previous statuses
            Map<OrderStatus, Integer> previousStatusCounts = statusHistory.stream()
                    .filter(h -> h.getOldStatus() != null)
                    .collect(Collectors.groupingBy(
                            EOrderStatusHistory::getOldStatus,
                            Collectors.summingInt(h -> 1)));
            
            // Count transitions by user
            Map<String, Integer> transitionsByUser = statusHistory.stream()
                    .filter(h -> h.getChangedBy() != null)
                    .collect(Collectors.groupingBy(
                            EOrderStatusHistory::getChangedBy,
                            Collectors.summingInt(h -> 1)));
            
            // Create summary
            StatusTransitionSummary summary = StatusTransitionSummary.builder()
                    .transitionCount(transitionCount)
                    .firstTransitionDate(firstTransitionDate != null 
                            ? LocalDateTime.ofInstant(firstTransitionDate, ZoneId.systemDefault()) 
                            : null)
                    .lastTransitionDate(lastTransitionDate != null 
                            ? LocalDateTime.ofInstant(lastTransitionDate, ZoneId.systemDefault()) 
                            : null)
                    .previousStatusCounts(previousStatusCounts)
                    .transitionsByUser(transitionsByUser)
                    .build();
            
            metrics.put(status, summary);
        }
        
        return metrics;
    }
    
    @Override
    public Map<OrderStatus, Double> getAverageTimeInStatus(LocalDateTime startDate, LocalDateTime endDate) {
        // This would typically involve a custom query in the repository
        // For a real implementation, this would calculate the average time between status transitions
        
        // Get all orders with their status histories
        Map<UUID, List<EOrderStatusHistory>> historiesByOrder = orderStatusHistoryRepository.findAll().stream()
                .collect(Collectors.groupingBy(h -> h.getOrder().getId()));
        
        // Calculate average time in each status
        Map<OrderStatus, List<Duration>> durationsByStatus = new HashMap<>();
        
        for (List<EOrderStatusHistory> orderHistory : historiesByOrder.values()) {
            // Sort by creation date
            List<EOrderStatusHistory> sortedHistory = orderHistory.stream()
                    .sorted((h1, h2) -> h1.getCreatedDate().compareTo(h2.getCreatedDate()))
                    .collect(Collectors.toList());
            
            // Calculate duration for each status
            for (int i = 0; i < sortedHistory.size() - 1; i++) {
                EOrderStatusHistory current = sortedHistory.get(i);
                EOrderStatusHistory next = sortedHistory.get(i + 1);
                
                OrderStatus status = current.getNewStatus();
                Duration duration = Duration.between(current.getCreatedDate(), next.getCreatedDate());
                
                durationsByStatus.computeIfAbsent(status, k -> new ArrayList<>()).add(duration);
            }
        }
        
        // Calculate average duration for each status
        Map<OrderStatus, Double> averages = new HashMap<>();
        
        for (Map.Entry<OrderStatus, List<Duration>> entry : durationsByStatus.entrySet()) {
            OrderStatus status = entry.getKey();
            List<Duration> durations = entry.getValue();
            
            if (!durations.isEmpty()) {
                double averageHours = durations.stream()
                        .mapToDouble(d -> d.toHours())
                        .average()
                        .orElse(0.0);
                
                averages.put(status, averageHours);
            }
        }
        
        return averages;
    }
    
    @Override
    public List<OrderStatusHistoryResponse> getCustomerVisibleStatusHistory(UUID orderId) {
        // This would typically involve a custom query in the repository
        // For now, we'll filter out entries with private notes or certain status changes
        
        List<EOrderStatusHistory> history = orderStatusHistoryRepository.findAll().stream()
                .filter(h -> h.getOrder().getId().equals(orderId))
                // Example filter: Include only customer-relevant status changes
                .filter(h -> isCustomerRelevantStatusChange(h))
                .sorted((h1, h2) -> h1.getCreatedDate().compareTo(h2.getCreatedDate()))
                .collect(Collectors.toList());
        
        return history.stream()
                .map(h -> {
                    // Create a customer-friendly version of the status history
                    OrderStatusHistoryResponse response = orderMapper.toOrderStatusHistoryResponse(h);
                    
                    // Sanitize notes if needed (remove internal information)
                    if (h.getNotes() != null && h.getNotes().contains("[INTERNAL]")) {
                        response.setNotes(null);
                    }
                    
                    return response;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Determine if a status change is relevant for customers
     */
    private boolean isCustomerRelevantStatusChange(EOrderStatusHistory history) {
        // Example: Only show certain status transitions to customers
        if (history.getNewStatus() == OrderStatus.PENDING ||
                history.getNewStatus() == OrderStatus.CONFIRMED ||
                history.getNewStatus() == OrderStatus.SHIPPED ||
                history.getNewStatus() == OrderStatus.DELIVERED ||
                history.getNewStatus() == OrderStatus.CANCELLED) {
            return true;
        }
        
        // Hide internal processing statuses
        if (history.getNewStatus() == OrderStatus.PROCESSING) {
            return false;
        }
        
        // Default: show the status change
        return true;
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
} 
