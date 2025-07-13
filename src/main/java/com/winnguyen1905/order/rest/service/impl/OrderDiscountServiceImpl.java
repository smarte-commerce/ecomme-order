package com.winnguyen1905.order.rest.service.impl;

import java.time.LocalDate;
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

import com.winnguyen1905.order.common.constant.DiscountAppliesTo;
import com.winnguyen1905.order.common.constant.DiscountType;
import com.winnguyen1905.order.model.request.CreateDiscountRequest;
import com.winnguyen1905.order.model.response.OrderDiscountResponse;
import com.winnguyen1905.order.persistance.entity.EOrder;
import com.winnguyen1905.order.persistance.entity.EOrderDiscount;
import com.winnguyen1905.order.persistance.entity.EOrderItem;
import com.winnguyen1905.order.persistance.repository.OrderDiscountRepository;
import com.winnguyen1905.order.persistance.repository.OrderItemRepository;
import com.winnguyen1905.order.persistance.repository.OrderRepository;
import com.winnguyen1905.order.rest.service.OrderDiscountService;
import com.winnguyen1905.order.secure.BaseException;
import com.winnguyen1905.order.util.OrderMapper;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderDiscountServiceImpl implements OrderDiscountService {
    private final OrderDiscountRepository orderDiscountRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
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
        
        // Validate discount code (in a real app, check against a discount database)
        if (!validateDiscountCode(orderId, request.getDiscountCode())) {
            throw new BaseException("Invalid discount code: " + request.getDiscountCode());
        }
        
        // Create order discount entity
        EOrderDiscount orderDiscount = orderMapper.toOrderDiscountEntity(request, order);
        
        // Calculate discount amount based on discount type and value
        double discountAmount = calculateDiscountAmount(order, request);
        orderDiscount.setDiscountAmount(discountAmount);
        
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
    
    @Override
    public boolean validateDiscountCode(UUID orderId, String discountCode) {
        // In a real application, this would check against a discount database
        // For now, we'll just do some basic validation
        
        // Check if the discount code is not empty
        if (discountCode == null || discountCode.trim().isEmpty()) {
            return false;
        }
        
        // Check if the order exists
        EOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        
        // Example validation rules:
        // 1. Check if the discount code is valid (format, expiry, etc.)
        boolean isValidFormat = discountCode.matches("^[A-Z0-9]{6,10}$");
        
        // 2. Check if the order meets minimum requirements for the discount
        boolean meetsMinimumAmount = order.getSubtotal() >= 10.0; // Example minimum amount
        
        // 3. Check if the discount is still active
        boolean isActive = true; // In a real app, check against a discount database
        
        return isValidFormat && meetsMinimumAmount && isActive;
    }
    
    @Override
    public Page<OrderDiscountResponse> getAvailableDiscountsForCustomer(Long customerId, Pageable pageable) {
        // In a real application, this would query a discount database for applicable discounts
        // For now, we'll return a mock list of available discounts
        
        List<OrderDiscountResponse> availableDiscounts = new ArrayList<>();
        
        // Example discount 1: 10% off entire order
        OrderDiscountResponse discount1 = OrderDiscountResponse.builder()
                .id(UUID.randomUUID())
                .discountCode("SAVE10PCT")
                .discountName("10% Off Your Order")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(10.0)
                .appliesTo(DiscountAppliesTo.ORDER)
                .build();
        
        // Example discount 2: $15 off on orders over $100
        OrderDiscountResponse discount2 = OrderDiscountResponse.builder()
                .id(UUID.randomUUID())
                .discountCode("SAVE15USD")
                .discountName("$15 Off Orders Over $100")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(15.0)
                .appliesTo(DiscountAppliesTo.ORDER)
                .build();
        
        // Example discount 3: Free shipping
        OrderDiscountResponse discount3 = OrderDiscountResponse.builder()
                .id(UUID.randomUUID())
                .discountCode("FREESHIP")
                .discountName("Free Shipping")
                .discountType(DiscountType.FREE_SHIPPING)
                .discountValue(0.0)
                .appliesTo(DiscountAppliesTo.SHIPPING)
                .build();
        
        availableDiscounts.add(discount1);
        availableDiscounts.add(discount2);
        availableDiscounts.add(discount3);
        
        // Implement simple pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), availableDiscounts.size());
        
        return new PageImpl<>(
                availableDiscounts.subList(start, end),
                pageable,
                availableDiscounts.size());
    }
    
    @Override
    @Transactional
    public List<OrderDiscountResponse> applyAutomaticDiscounts(UUID orderId) {
        // Get the order
        EOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        
        List<EOrderDiscount> appliedDiscounts = new ArrayList<>();
        
        // Example automatic discount rules:
        
        // Rule 1: If order subtotal is over $100, apply 5% discount
        if (order.getSubtotal() >= 100.0) {
            CreateDiscountRequest discountRequest = CreateDiscountRequest.builder()
                    .discountCode("AUTO5PCT")
                    .discountName("Automatic 5% Off Orders Over $100")
                    .discountType(DiscountType.PERCENTAGE)
                    .discountValue(5.0)
                    .appliesTo(DiscountAppliesTo.ORDER)
                    .build();
            
            // Check if this automatic discount already exists
            boolean discountExists = orderDiscountRepository.findAll().stream()
                    .anyMatch(discount -> discount.getOrder().getId().equals(orderId) 
                            && discount.getDiscountCode().equals(discountRequest.getDiscountCode()));
            
            if (!discountExists) {
                EOrderDiscount discount = orderMapper.toOrderDiscountEntity(discountRequest, order);
                double discountAmount = calculateDiscountAmount(order, discountRequest);
                discount.setDiscountAmount(discountAmount);
                discount = orderDiscountRepository.save(discount);
                appliedDiscounts.add(discount);
            }
        }
        
        // Rule 2: If order has more than 5 items, apply $10 discount
        List<EOrderItem> orderItems = orderItemRepository.findAll().stream()
                .filter(item -> item.getOrder().getId().equals(orderId))
                .collect(Collectors.toList());
        
        if (orderItems.size() > 5) {
            CreateDiscountRequest discountRequest = CreateDiscountRequest.builder()
                    .discountCode("AUTO10USD")
                    .discountName("Automatic $10 Off Orders With More Than 5 Items")
                    .discountType(DiscountType.FIXED_AMOUNT)
                    .discountValue(10.0)
                    .appliesTo(DiscountAppliesTo.ORDER)
                    .build();
            
            // Check if this automatic discount already exists
            boolean discountExists = orderDiscountRepository.findAll().stream()
                    .anyMatch(discount -> discount.getOrder().getId().equals(orderId) 
                            && discount.getDiscountCode().equals(discountRequest.getDiscountCode()));
            
            if (!discountExists) {
                EOrderDiscount discount = orderMapper.toOrderDiscountEntity(discountRequest, order);
                double discountAmount = calculateDiscountAmount(order, discountRequest);
                discount.setDiscountAmount(discountAmount);
                discount = orderDiscountRepository.save(discount);
                appliedDiscounts.add(discount);
            }
        }
        
        // Update order discount amount
        if (!appliedDiscounts.isEmpty()) {
            updateOrderDiscountAmount(order);
        }
        
        return appliedDiscounts.stream()
                .map(orderMapper::toOrderDiscountResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public double calculateTotalDiscountAmount(UUID orderId) {
        // Get all discounts for the order
        List<EOrderDiscount> discounts = orderDiscountRepository.findAll().stream()
                .filter(discount -> discount.getOrder().getId().equals(orderId))
                .collect(Collectors.toList());
        
        // Sum up all discount amounts
        return discounts.stream()
                .mapToDouble(EOrderDiscount::getDiscountAmount)
                .sum();
    }
    
    @Override
    public List<DiscountUsageStats> getDiscountUsageStatsByVendor(Long vendorId, String startDate, String endDate) {
        // In a real application, this would query the database for discount usage statistics
        // For now, we'll return mock statistics
        
        Map<String, DiscountUsageStats> statsMap = new HashMap<>();
        
        // Example discount usage statistics
        DiscountUsageStats stats1 = new DiscountUsageStats();
        stats1.setDiscountCode("SAVE10PCT");
        stats1.setUsageCount(25);
        stats1.setTotalDiscountAmount(250.0);
        
        DiscountUsageStats stats2 = new DiscountUsageStats();
        stats2.setDiscountCode("SAVE15USD");
        stats2.setUsageCount(15);
        stats2.setTotalDiscountAmount(225.0);
        
        DiscountUsageStats stats3 = new DiscountUsageStats();
        stats3.setDiscountCode("FREESHIP");
        stats3.setUsageCount(10);
        stats3.setTotalDiscountAmount(100.0);
        
        statsMap.put("SAVE10PCT", stats1);
        statsMap.put("SAVE15USD", stats2);
        statsMap.put("FREESHIP", stats3);
        
        return new ArrayList<>(statsMap.values());
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
    
    /**
     * Calculate discount amount based on discount type and value
     * 
     * @param order Order to apply discount to
     * @param request Discount request
     * @return Calculated discount amount
     */
    private double calculateDiscountAmount(EOrder order, CreateDiscountRequest request) {
        double discountAmount = 0.0;
        
        switch (request.getDiscountType()) {
            case PERCENTAGE:
                // Calculate percentage discount based on the applies to field
                if (request.getAppliesTo() == DiscountAppliesTo.ORDER) {
                    discountAmount = order.getSubtotal() * (request.getDiscountValue() / 100.0);
                } else if (request.getAppliesTo() == DiscountAppliesTo.SHIPPING) {
                    discountAmount = order.getShippingAmount() * (request.getDiscountValue() / 100.0);
                }
                break;
                
            case FIXED_AMOUNT:
                // Apply fixed amount discount
                discountAmount = Math.min(request.getDiscountValue(), order.getSubtotal());
                break;
                
            case FREE_SHIPPING:
                // Apply free shipping discount
                discountAmount = order.getShippingAmount();
                break;
                
            default:
                discountAmount = 0.0;
        }
        
        return discountAmount;
    }
} 
