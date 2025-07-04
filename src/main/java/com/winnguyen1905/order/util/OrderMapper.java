package com.winnguyen1905.order.util;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.winnguyen1905.order.common.constant.DiscountAppliesTo;
import com.winnguyen1905.order.common.constant.DiscountType;
import com.winnguyen1905.order.common.constant.OrderItemStatus;
import com.winnguyen1905.order.common.constant.OrderStatus;
import com.winnguyen1905.order.common.constant.VendorOrderStatus;
import com.winnguyen1905.order.model.CreateDiscountRequest;
import com.winnguyen1905.order.model.CreateExternalRefRequest;
import com.winnguyen1905.order.model.CreateOrderRequest;
import com.winnguyen1905.order.model.CreateVendorOrderRequest;
import com.winnguyen1905.order.model.OrderDiscountResponse;
import com.winnguyen1905.order.model.OrderExternalRefResponse;
import com.winnguyen1905.order.model.OrderItemResponse;
import com.winnguyen1905.order.model.OrderResponse;
import com.winnguyen1905.order.model.OrderStatusHistoryResponse;
import com.winnguyen1905.order.model.VendorOrderResponse;
import com.winnguyen1905.order.persistance.entity.EOrder;
import com.winnguyen1905.order.persistance.entity.EOrderDiscount;
import com.winnguyen1905.order.persistance.entity.EOrderExternalRef;
import com.winnguyen1905.order.persistance.entity.EOrderItem;
import com.winnguyen1905.order.persistance.entity.EOrderStatusHistory;
import com.winnguyen1905.order.persistance.entity.EVendorOrder;

@Component
public class OrderMapper {
    
    public EOrder toOrderEntity(CreateOrderRequest request) {
        return EOrder.builder()
                .customerId(request.getCustomerId())
                .shippingAddress(request.getShippingAddress())
                .billingAddress(request.getBillingAddress())
                .estimatedDeliveryDate(request.getEstimatedDeliveryDate())
                .specialInstructions(request.getSpecialInstructions())
                .status(OrderStatus.PENDING)
                .subtotal(0.0)
                .discountAmount(0.0)
                .taxAmount(0.0)
                .shippingAmount(0.0)
                .totalAmount(0.0)
                .build();
    }
    
    public EOrderItem toOrderItemEntity(CreateOrderRequest.OrderItemRequest request, EOrder order) {
        double totalPrice = request.getQuantity() * request.getUnitPrice();
        
        return EOrderItem.builder()
                .order(order)
                .productId(request.getProductId())
                .vendorId(request.getVendorId())
                .productName(request.getProductName())
                .productSku(request.getProductSku())
                .productCategory(request.getProductCategory())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .totalPrice(totalPrice)
                .weight(request.getWeight())
                .dimensions(request.getDimensions())
                .taxCategory(request.getTaxCategory())
                .status(OrderItemStatus.PENDING)
                .build();
    }
    
    public EOrderDiscount toOrderDiscountEntity(CreateDiscountRequest request, EOrder order) {
        // Calculate discount amount based on type and value
        double discountAmount = calculateDiscountAmount(request.getDiscountType(), 
                request.getDiscountValue(), order.getSubtotal());
        
        return EOrderDiscount.builder()
                .order(order)
                .discountCode(request.getDiscountCode())
                .discountName(request.getDiscountName())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .discountAmount(discountAmount)
                .appliesTo(request.getAppliesTo() != null ? request.getAppliesTo() : DiscountAppliesTo.ORDER)
                .build();
    }
    
    private double calculateDiscountAmount(DiscountType type, double value, double subtotal) {
        if (type == DiscountType.PERCENTAGE) {
            // Cap percentage at 100%
            double percentage = Math.min(value, 100.0);
            return subtotal * (percentage / 100.0);
        } else {
            // For fixed amount, don't allow more than the subtotal
            return Math.min(value, subtotal);
        }
    }
    
    public OrderResponse toOrderResponse(EOrder order) {
        List<OrderItemResponse> itemResponses = null;
        
        if (order.getOrderItems() != null) {
            itemResponses = order.getOrderItems().stream()
                    .map(this::toOrderItemResponse)
                    .collect(Collectors.toList());
        }
        
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .orderNumber(order.getOrderNumber())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .taxAmount(order.getTaxAmount())
                .shippingAmount(order.getShippingAmount())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .billingAddress(order.getBillingAddress())
                .estimatedDeliveryDate(order.getEstimatedDeliveryDate())
                .specialInstructions(order.getSpecialInstructions())
                .orderItems(itemResponses)
                .createdDate(order.getCreatedDate())
                .updatedDate(order.getUpdatedDate())
                .build();
    }
    
    public OrderItemResponse toOrderItemResponse(EOrderItem orderItem) {
        return OrderItemResponse.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProductId())
                .vendorId(orderItem.getVendorId())
                .productName(orderItem.getProductName())
                .productSku(orderItem.getProductSku())
                .productCategory(orderItem.getProductCategory())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .totalPrice(orderItem.getTotalPrice())
                .weight(orderItem.getWeight())
                .dimensions(orderItem.getDimensions())
                .taxCategory(orderItem.getTaxCategory())
                .status(orderItem.getStatus())
                .createdDate(orderItem.getCreatedDate())
                .updatedDate(orderItem.getUpdatedDate())
                .build();
    }
    
    public OrderDiscountResponse toOrderDiscountResponse(EOrderDiscount orderDiscount) {
        return OrderDiscountResponse.builder()
                .id(orderDiscount.getId())
                .orderId(orderDiscount.getOrder().getId())
                .discountCode(orderDiscount.getDiscountCode())
                .discountName(orderDiscount.getDiscountName())
                .discountType(orderDiscount.getDiscountType())
                .discountValue(orderDiscount.getDiscountValue())
                .discountAmount(orderDiscount.getDiscountAmount())
                .appliesTo(orderDiscount.getAppliesTo())
                .createdDate(orderDiscount.getCreatedDate())
                .updatedDate(orderDiscount.getUpdatedDate())
                .build();
    }
    
    public OrderStatusHistoryResponse toOrderStatusHistoryResponse(EOrderStatusHistory statusHistory) {
        return OrderStatusHistoryResponse.builder()
                .id(statusHistory.getId())
                .orderId(statusHistory.getOrder().getId())
                .oldStatus(statusHistory.getOldStatus())
                .newStatus(statusHistory.getNewStatus())
                .reason(statusHistory.getReason())
                .notes(statusHistory.getNotes())
                .changedBy(statusHistory.getChangedBy())
                .createdDate(statusHistory.getCreatedDate())
                .build();
    }
    
    public EVendorOrder toVendorOrderEntity(CreateVendorOrderRequest request, EOrder order, double subtotal) {
        double commissionAmount = subtotal * (request.getCommissionRate() / 100.0);
        double vendorPayout = subtotal - commissionAmount;
        
        return EVendorOrder.builder()
                .order(order)
                .vendorId(request.getVendorId())
                .vendorOrderNumber(generateVendorOrderNumber(order.getOrderNumber(), request.getVendorId()))
                .subtotal(subtotal)
                .commissionRate(request.getCommissionRate())
                .commissionAmount(commissionAmount)
                .vendorPayout(vendorPayout)
                .status(VendorOrderStatus.PENDING)
                .build();
    }
    
    private String generateVendorOrderNumber(String orderNumber, Long vendorId) {
        return String.format("%s-V%d", orderNumber, vendorId);
    }
    
    public VendorOrderResponse toVendorOrderResponse(EVendorOrder vendorOrder) {
        return VendorOrderResponse.builder()
                .id(vendorOrder.getId())
                .orderId(vendorOrder.getOrder().getId())
                .vendorId(vendorOrder.getVendorId())
                .vendorOrderNumber(vendorOrder.getVendorOrderNumber())
                .subtotal(vendorOrder.getSubtotal())
                .commissionRate(vendorOrder.getCommissionRate())
                .commissionAmount(vendorOrder.getCommissionAmount())
                .vendorPayout(vendorOrder.getVendorPayout())
                .status(vendorOrder.getStatus())
                .createdDate(vendorOrder.getCreatedDate())
                .updatedDate(vendorOrder.getUpdatedDate())
                .build();
    }
    
    public EOrderExternalRef toOrderExternalRefEntity(CreateExternalRefRequest request, EOrder order) {
        return EOrderExternalRef.builder()
                .order(order)
                .serviceName(request.getServiceName())
                .externalId(request.getExternalId())
                .refType(request.getRefType())
                .status(request.getStatus())
                .build();
    }
    
    public OrderExternalRefResponse toOrderExternalRefResponse(EOrderExternalRef orderExternalRef) {
        return OrderExternalRefResponse.builder()
                .id(orderExternalRef.getId())
                .orderId(orderExternalRef.getOrder().getId())
                .serviceName(orderExternalRef.getServiceName())
                .externalId(orderExternalRef.getExternalId())
                .refType(orderExternalRef.getRefType())
                .status(orderExternalRef.getStatus())
                .createdDate(orderExternalRef.getCreatedDate())
                .updatedDate(orderExternalRef.getUpdatedDate())
                .build();
    }
} 
