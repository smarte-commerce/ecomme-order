package com.winnguyen1905.order.rest.service.impl;

import java.math.BigDecimal;
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

import com.winnguyen1905.order.common.constant.OrderItemStatus;
import com.winnguyen1905.order.common.constant.OrderStatus;
import com.winnguyen1905.order.model.request.CreateOrderRequest;
import com.winnguyen1905.order.model.response.OrderItemResponse;
import com.winnguyen1905.order.model.response.OrderResponse;
import com.winnguyen1905.order.model.response.OrderSummaryResponse;
import com.winnguyen1905.order.persistance.entity.EOrder;
import com.winnguyen1905.order.persistance.entity.EOrderItem;
import com.winnguyen1905.order.persistance.entity.EOrderStatusHistory;
import com.winnguyen1905.order.persistance.repository.OrderDiscountRepository;
import com.winnguyen1905.order.persistance.repository.OrderItemRepository;
import com.winnguyen1905.order.persistance.repository.OrderRepository;
import com.winnguyen1905.order.persistance.repository.OrderStatusHistoryRepository;
import com.winnguyen1905.order.rest.service.OrderService;
import com.winnguyen1905.order.secure.BaseException;
import com.winnguyen1905.order.secure.TAccountRequest;
import com.winnguyen1905.order.util.OrderMapper;
import com.winnguyen1905.order.util.OrderNumberGenerator;
import com.winnguyen1905.order.messaging.OrderEventProducer;
import com.winnguyen1905.order.model.event.OrderCreatedEvent;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final OrderStatusHistoryRepository orderStatusHistoryRepository;
  private final OrderDiscountRepository orderDiscountRepository;
  private final OrderMapper orderMapper;
  private final OrderNumberGenerator orderNumberGenerator;
  private final OrderEventProducer orderEventProducer;

  @Override
  @Transactional
  public OrderResponse createOrder(CreateOrderRequest request, TAccountRequest accountRequest) {
    // Create minimal order entity for SAGA pattern
    EOrder order = EOrder.builder()
        .orderNumber(orderNumberGenerator.generateOrderNumber())
        .status(OrderStatus.PENDING)
        .customerId(accountRequest.id().getMostSignificantBits()) // Convert UUID to Long
        .shippingAddress(request.getShippingAddress())
        .billingAddress(request.getBillingAddress())
        .estimatedDeliveryDate(request.getEstimatedDeliveryDate())
        .specialInstructions(request.getSpecialInstructions())
        .build();

    // Initialize amounts as 0 - will be calculated in orchestrator
    order.setSubtotal(0.0);
    order.setDiscountAmount(0.0);
    order.setTaxAmount(0.0);
    order.setShippingAmount(0.0);
    order.setTotalAmount(0.0);

    // Initialize payment amounts from request
    order.setPaidAmount(request.getPaidAmount() != null ? request.getPaidAmount() : 0.0);
    order.setAmountToBePaid(request.getAmountToBePaid() != null ? request.getAmountToBePaid() : 0.0);

    // Save order to get ID
    EOrder savedOrder = orderRepository.save(order);

    // Create initial status history
    createStatusHistory(savedOrder, null, OrderStatus.PENDING, "Order created - pending orchestration");

    // Create SAGA ID and publish OrderCreated event
    UUID sagaId = UUID.randomUUID();

    // Convert request to event structure
    List<OrderCreatedEvent.CheckoutItem> eventCheckoutItems = request.getCheckoutItems().stream()
        .map(checkoutItem -> OrderCreatedEvent.CheckoutItem.builder()
            .shopId(checkoutItem.getShopId())
            .notes(checkoutItem.getNotes())
            .shopProductDiscountId(checkoutItem.getShopProductDiscountId())
            .items(checkoutItem.getItems().stream()
                .map(item -> OrderCreatedEvent.OrderItem.builder()
                    .productId(item.getProductId())
                    .variantId(item.getVariantId())
                    .productSku(item.getProductSku())
                    .quantity(item.getQuantity())
                    .weight(item.getWeight())
                    .dimensions(item.getDimensions())
                    .taxCategory(item.getTaxCategory())
                    .build())
                .collect(Collectors.toList()))
            .build())
        .collect(Collectors.toList());

    OrderCreatedEvent event = OrderCreatedEvent.builder()
        .eventId(UUID.randomUUID())
        .sagaId(sagaId)
        .orderId(savedOrder.getId())
        .eventType("OrderCreated")
        .timestamp(Instant.now())
        .retryCount(0)
        .correlationId(sagaId)
        .customerId(accountRequest.id().getMostSignificantBits())
        .orderNumber(savedOrder.getOrderNumber())
        .paymentMethod(request.getPaymentMethod())
        .currency(request.getCurrency())
        .shippingAddress(request.getShippingAddress())
        .billingAddress(request.getBillingAddress())
        .checkoutItems(eventCheckoutItems)
        .shippingDiscountId(request.getShippingDiscountId())
        .globalProductDiscountId(request.getGlobalProductDiscountId())
        .build();

    // Publish event to Kafka
    orderEventProducer.publishOrderCreated(event);

    log.info("Created order with ID: {}, number: {}, sagaId: {}",
        savedOrder.getId(), savedOrder.getOrderNumber(), sagaId);

    return orderMapper.toOrderResponse(savedOrder);
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
  public Page<OrderResponse> getAllOrders(Pageable pageable) {
    // This would typically involve a custom query in the repository with pagination
    List<OrderResponse> allOrders = getAllOrders();

    return createPage(allOrders, pageable);
  }

  public List<OrderResponse> getOrdersByCustomerId(Long customerId) {
    // This would typically involve a custom query in the repository
    return orderRepository.findAll().stream()
        .filter(order -> order.getCustomerId().equals(customerId))
        .map(orderMapper::toOrderResponse)
        .collect(Collectors.toList());
  }

  @Override
  public Page<OrderResponse> getOrdersByCustomerId(Long customerId, Pageable pageable) {
    // Get all orders for the customer
    List<EOrder> customerOrders = orderRepository.findAll().stream()
        .filter(order -> order.getCustomerId().equals(customerId))
        .collect(Collectors.toList());

    List<OrderResponse> responses = customerOrders.stream()
        .map(orderMapper::toOrderResponse)
        .collect(Collectors.toList());

    return createPage(responses, pageable);
  }

  public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
    // This would typically involve a custom query in the repository
    return orderRepository.findAll().stream()
        .filter(order -> order.getStatus() == status)
        .map(orderMapper::toOrderResponse)
        .collect(Collectors.toList());
  }

  @Override
  public Page<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
    // This would typically involve a custom query in the repository with pagination
    List<OrderResponse> statusOrders = getOrdersByStatus(status);

    return createPage(statusOrders, pageable);
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

    // Update order items status if needed
    updateOrderItemsStatus(order, status);

    log.info("Updated order status. Order ID: {}, Old status: {}, New status: {}",
        id, oldStatus, status);

    return orderMapper.toOrderResponse(order);
  }

  @Override
  @Transactional
  public OrderResponse cancelOrder(UUID id, String reason) {
    EOrder order = orderRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));

    // Check if the order can be cancelled
    if (order.getStatus() == OrderStatus.DELIVERED ||
        order.getStatus() == OrderStatus.SHIPPED) {
      throw new BaseException("Cannot cancel order that is already shipped or delivered");
    }

    return updateOrderStatus(id, OrderStatus.CANCELLED, reason);
  }

  @Override
  @Transactional
  public void deleteOrder(UUID id) {
    EOrder order = orderRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));

    orderRepository.delete(order);
    log.info("Deleted order with ID: {}", id);
  }

  // Payment-related methods implementation

  @Override
  @Transactional
  public void updateOrderPaymentAmounts(UUID orderId, BigDecimal paidAmount, BigDecimal amountToBePaid) {
    EOrder order = orderRepository.findById(orderId)
        .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

    order.setPaidAmount(paidAmount.doubleValue());
    order.setAmountToBePaid(amountToBePaid.doubleValue());
    
    orderRepository.save(order);

    // Create status history for payment update
    String reason = String.format("Payment amounts updated - Paid: $%.2f, To be paid: $%.2f", 
        paidAmount.doubleValue(), amountToBePaid.doubleValue());
    createStatusHistory(order, order.getStatus(), order.getStatus(), reason);

    log.info("Updated payment amounts for order {}. Paid: {}, To be paid: {}", 
        orderId, paidAmount, amountToBePaid);
  }

  @Override
  @Transactional
  public void markOrderAsPaid(UUID orderId, BigDecimal paidAmount) {
    EOrder order = orderRepository.findById(orderId)
        .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

    order.setPaidAmount(paidAmount.doubleValue());
    order.setAmountToBePaid(0.0);
    
    orderRepository.save(order);

    // Create status history for payment completion
    String reason = String.format("Order marked as paid - Amount: $%.2f", paidAmount.doubleValue());
    createStatusHistory(order, order.getStatus(), order.getStatus(), reason);

    log.info("Marked order {} as paid with amount: {}", orderId, paidAmount);
  }

  @Override
  @Transactional
  public void markOrderAsUnpaid(UUID orderId) {
    EOrder order = orderRepository.findById(orderId)
        .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

    order.setPaidAmount(0.0);
    order.setAmountToBePaid(order.getTotalAmount());
    
    orderRepository.save(order);

    // Create status history for payment reset
    String reason = String.format("Order marked as unpaid - Amount to be paid: $%.2f", order.getTotalAmount());
    createStatusHistory(order, order.getStatus(), order.getStatus(), reason);

    log.info("Marked order {} as unpaid. Amount to be paid: {}", orderId, order.getTotalAmount());
  }

  @Override
  public Page<OrderResponse> getOrdersContainingVendorProducts(Long vendorId, Pageable pageable) {
    // Get all order items for the vendor
    List<EOrderItem> vendorItems = orderItemRepository.findAll().stream()
        .filter(item -> item.getVendorId().equals(vendorId))
        .collect(Collectors.toList());

    // Get unique order IDs
    List<UUID> orderIds = vendorItems.stream()
        .map(item -> item.getOrder().getId())
        .distinct()
        .collect(Collectors.toList());

    // Get orders
    List<EOrder> orders = orderRepository.findAll().stream()
        .filter(order -> orderIds.contains(order.getId()))
        .collect(Collectors.toList());

    List<OrderResponse> orderResponses = orders.stream()
        .map(orderMapper::toOrderResponse)
        .collect(Collectors.toList());

    return createPage(orderResponses, pageable);
  }

  @Override
  public Page<OrderResponse> searchOrders(Long customerId, String orderNumber, OrderStatus status,
      LocalDate startDate, LocalDate endDate, Double minAmount, Double maxAmount, Pageable pageable) {

    // This would typically involve a custom query in the repository with pagination
    // For now, we'll filter the orders in memory

    List<EOrder> allOrders = orderRepository.findAll();

    // Apply filters
    List<EOrder> filteredOrders = allOrders.stream()
        .filter(order -> customerId == null || order.getCustomerId().equals(customerId))
        .filter(order -> orderNumber == null || order.getOrderNumber().contains(orderNumber))
        .filter(order -> status == null || order.getStatus() == status)
        .filter(order -> startDate == null ||
            order.getCreatedDate().atZone(java.time.ZoneId.systemDefault()).toLocalDate().isAfter(startDate) ||
            order.getCreatedDate().atZone(java.time.ZoneId.systemDefault()).toLocalDate().isEqual(startDate))
        .filter(order -> endDate == null ||
            order.getCreatedDate().atZone(java.time.ZoneId.systemDefault()).toLocalDate().isBefore(endDate) ||
            order.getCreatedDate().atZone(java.time.ZoneId.systemDefault()).toLocalDate().isEqual(endDate))
        .filter(order -> minAmount == null || order.getTotalAmount() >= minAmount)
        .filter(order -> maxAmount == null || order.getTotalAmount() <= maxAmount)
        .collect(Collectors.toList());

    List<OrderResponse> orderResponses = filteredOrders.stream()
        .map(orderMapper::toOrderResponse)
        .collect(Collectors.toList());

    return createPage(orderResponses, pageable);
  }

  @Override
  public OrderSummaryResponse getCustomerOrderSummary(Long customerId) {
    // Get all orders for the customer
    List<EOrder> customerOrders = orderRepository.findAll().stream()
        .filter(order -> order.getCustomerId().equals(customerId))
        .collect(Collectors.toList());

    if (customerOrders.isEmpty()) {
      throw new EntityNotFoundException("No orders found for customer: " + customerId);
    }

    // Calculate summary metrics
    int totalOrders = customerOrders.size();

    double totalSpent = customerOrders.stream()
        .mapToDouble(EOrder::getTotalAmount)
        .sum();

    double averageOrderValue = totalSpent / totalOrders;

    java.time.Instant firstOrderDate = customerOrders.stream()
        .map(EOrder::getCreatedDate)
        .min(java.time.Instant::compareTo)
        .orElse(null);

    java.time.Instant lastOrderDate = customerOrders.stream()
        .map(EOrder::getCreatedDate)
        .max(java.time.Instant::compareTo)
        .orElse(null);

    // Count orders by status
    Map<String, Integer> ordersByStatus = new HashMap<>();
    for (EOrder order : customerOrders) {
      String status = order.getStatus().toString();
      ordersByStatus.put(status, ordersByStatus.getOrDefault(status, 0) + 1);
    }

    // Get all order items for the customer
    List<EOrderItem> customerItems = new ArrayList<>();
    for (EOrder order : customerOrders) {
      List<EOrderItem> items = orderItemRepository.findAll().stream()
          .filter(item -> item.getOrder().getId().equals(order.getId()))
          .collect(Collectors.toList());
      customerItems.addAll(items);
    }

    // Most ordered products (top 5)
    Map<String, Integer> productCounts = new HashMap<>();
    for (EOrderItem item : customerItems) {
      String productName = item.getProductName();
      productCounts.put(productName, productCounts.getOrDefault(productName, 0) + item.getQuantity());
    }

    List<String> mostOrderedProducts = productCounts.entrySet().stream()
        .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
        .limit(5)
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());

    // Most ordered categories (top 3)
    Map<String, Integer> categoryCounts = new HashMap<>();
    for (EOrderItem item : customerItems) {
      if (item.getProductCategory() != null) {
        String category = item.getProductCategory();
        categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + item.getQuantity());
      }
    }

    List<String> mostOrderedCategories = categoryCounts.entrySet().stream()
        .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
        .limit(3)
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());

    // Create and return summary
    return OrderSummaryResponse.builder()
        .customerId(customerId)
        .totalOrders(totalOrders)
        .totalSpent(totalSpent)
        .averageOrderValue(averageOrderValue)
        .firstOrderDate(firstOrderDate != null
            ? java.time.LocalDateTime.ofInstant(firstOrderDate, java.time.ZoneId.systemDefault())
            : null)
        .lastOrderDate(lastOrderDate != null
            ? java.time.LocalDateTime.ofInstant(lastOrderDate, java.time.ZoneId.systemDefault())
            : null)
        .ordersByStatus(ordersByStatus)
        .mostOrderedCategories(mostOrderedCategories)
        .mostOrderedProducts(mostOrderedProducts)
        .build();
  }

  @Override
  @Transactional
  public OrderResponse requestReturnOrRefund(UUID orderId, String reason, List<UUID> itemIds) {
    EOrder order = orderRepository.findById(orderId)
        .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

    // Check if the order is eligible for return/refund
    if (order.getStatus() != OrderStatus.DELIVERED) {
      throw new BaseException("Only delivered orders can be returned or refunded");
    }

    // If item IDs are provided, update their status
    if (itemIds != null && !itemIds.isEmpty()) {
      for (UUID itemId : itemIds) {
        EOrderItem item = orderItemRepository.findById(itemId)
            .orElseThrow(() -> new EntityNotFoundException("Order item not found with id: " + itemId));

        if (!item.getOrder().getId().equals(orderId)) {
          throw new BaseException("Item does not belong to the specified order");
        }

        item.setStatus(OrderItemStatus.REFUNDED);
        orderItemRepository.save(item);
      }
    } else {
      // If no item IDs provided, update all items
      List<EOrderItem> items = orderItemRepository.findAll().stream()
          .filter(item -> item.getOrder().getId().equals(orderId))
          .collect(Collectors.toList());

      for (EOrderItem item : items) {
        item.setStatus(OrderItemStatus.REFUNDED);
        orderItemRepository.save(item);
      }
    }

    // Update order status
    OrderStatus oldStatus = order.getStatus();
    order.setStatus(OrderStatus.REFUNDED);
    order = orderRepository.save(order);

    // Create status history entry
    createStatusHistory(order, oldStatus, OrderStatus.REFUNDED, reason);

    log.info("Return/refund requested for order: {}", orderId);

    return orderMapper.toOrderResponse(order);
  }

  @Override
  @Transactional
  public OrderResponse processReturnOrRefund(UUID orderId, boolean approved, Double refundAmount, String notes) {
    EOrder order = orderRepository.findById(orderId)
        .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

    // Check if the order status is appropriate for processing a return/refund
    if (order.getStatus() != OrderStatus.REFUNDED) {
      throw new BaseException("Order is not in a status that can be processed for return/refund");
    }

    // Update order status based on approval
    OrderStatus oldStatus = order.getStatus();
    OrderStatus newStatus = approved ? OrderStatus.REFUNDED : OrderStatus.DELIVERED;
    order.setStatus(newStatus);

    // Update order items status
    List<EOrderItem> items = orderItemRepository.findAll().stream()
        .filter(item -> item.getOrder().getId().equals(orderId))
        .filter(item -> item.getStatus() == OrderItemStatus.REFUNDED)
        .collect(Collectors.toList());

    for (EOrderItem item : items) {
      OrderItemStatus newItemStatus = approved ? OrderItemStatus.REFUNDED : OrderItemStatus.DELIVERED;
      item.setStatus(newItemStatus);
      orderItemRepository.save(item);
    }

    // Save order
    order = orderRepository.save(order);

    // Create status history entry
    String reason = approved
        ? "Return/refund approved. Refund amount: $" + refundAmount
        : "Return/refund rejected. " + notes;
    createStatusHistory(order, oldStatus, newStatus, reason);

    log.info("Processed return/refund for order: {}, approved: {}", orderId, approved);

    return orderMapper.toOrderResponse(order);
  }

  @Override
  public String generateInvoice(UUID orderId) {
    EOrder order = orderRepository.findById(orderId)
        .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

    // In a real application, this would generate a PDF invoice and return its path
    // For now, we'll just return a mock path

    String invoicePath = "/invoices/" + order.getOrderNumber() + ".pdf";

    log.info("Generated invoice for order: {}, path: {}", orderId, invoicePath);

    return invoicePath;
  }

  @Override
  public Map<String, Object> getVendorSalesReport(Long vendorId, LocalDate startDate, LocalDate endDate) {
    // Get all order items for the vendor
    List<EOrderItem> vendorItems = orderItemRepository.findAll().stream()
        .filter(item -> item.getVendorId().equals(vendorId))
        .collect(Collectors.toList());

    // Filter by date range
    java.time.Instant startInstant = startDate != null
        ? startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
        : java.time.Instant.EPOCH;

    java.time.Instant endInstant = endDate != null
        ? endDate.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
        : java.time.Instant.now();

    List<EOrderItem> filteredItems = vendorItems.stream()
        .filter(item -> {
          java.time.Instant itemDate = item.getCreatedDate();
          return !itemDate.isBefore(startInstant) && !itemDate.isAfter(endInstant);
        })
        .collect(Collectors.toList());

    // Calculate sales metrics
    List<UUID> uniqueOrderIds = filteredItems.stream()
        .map(item -> item.getOrder().getId())
        .distinct()
        .collect(Collectors.toList());
    int totalOrders = uniqueOrderIds.size();

    int totalItemsSold = filteredItems.stream()
        .mapToInt(EOrderItem::getQuantity)
        .sum();

    double totalSales = filteredItems.stream()
        .mapToDouble(EOrderItem::getTotalPrice)
        .sum();

    double averageOrderValue = totalOrders > 0 ? totalSales / totalOrders : 0;

    // Sales by product
    Map<String, Integer> salesByProduct = new HashMap<>();
    for (EOrderItem item : filteredItems) {
      String productName = item.getProductName();
      salesByProduct.put(productName, salesByProduct.getOrDefault(productName, 0) + item.getQuantity());
    }

    // Sales by category
    Map<String, Double> salesByCategory = new HashMap<>();
    for (EOrderItem item : filteredItems) {
      if (item.getProductCategory() != null) {
        String category = item.getProductCategory();
        salesByCategory.put(category, salesByCategory.getOrDefault(category, 0.0) + item.getTotalPrice());
      }
    }

    // Sales by day
    Map<String, Double> salesByDay = new HashMap<>();
    for (EOrderItem item : filteredItems) {
      String day = item.getCreatedDate().atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString();
      salesByDay.put(day, salesByDay.getOrDefault(day, 0.0) + item.getTotalPrice());
    }

    // Create report
    Map<String, Object> report = new HashMap<>();
    report.put("vendorId", vendorId);
    report.put("startDate", startDate);
    report.put("endDate", endDate);
    report.put("totalOrders", totalOrders);
    report.put("totalItemsSold", totalItemsSold);
    report.put("totalSales", totalSales);
    report.put("averageOrderValue", averageOrderValue);
    report.put("salesByProduct", salesByProduct);
    report.put("salesByCategory", salesByCategory);
    report.put("salesByDay", salesByDay);

    return report;
  }

  private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
    // Implement business rules for valid status transitions
    if (currentStatus == OrderStatus.CANCELLED && newStatus != OrderStatus.CANCELLED) {
      throw new BaseException("Cannot change status from CANCELLED");
    }

    if (currentStatus == OrderStatus.DELIVERED &&
        (newStatus != OrderStatus.DELIVERED &&
            newStatus != OrderStatus.REFUNDED)) {
      throw new BaseException("Order is already delivered, can only change to REFUNDED");
    }

    // Additional validation rules
    switch (currentStatus) {
      case PENDING:
        // Pending can transition to confirmed, processing, or cancelled
        if (newStatus != OrderStatus.CONFIRMED &&
            newStatus != OrderStatus.PROCESSING &&
            newStatus != OrderStatus.CANCELLED) {
          throw new BaseException("Invalid status transition from PENDING to " + newStatus);
        }
        break;

      case CONFIRMED:
        // Confirmed can transition to processing or cancelled
        if (newStatus != OrderStatus.PROCESSING &&
            newStatus != OrderStatus.CANCELLED) {
          throw new BaseException("Invalid status transition from CONFIRMED to " + newStatus);
        }
        break;

      case PROCESSING:
        // Processing can transition to shipped or cancelled
        if (newStatus != OrderStatus.SHIPPED &&
            newStatus != OrderStatus.CANCELLED) {
          throw new BaseException("Invalid status transition from PROCESSING to " + newStatus);
        }
        break;

      case SHIPPED:
        // Shipped can only transition to delivered
        if (newStatus != OrderStatus.DELIVERED) {
          throw new BaseException("Invalid status transition from SHIPPED to " + newStatus);
        }
        break;

      case REFUNDED:
        // These are terminal states
        throw new BaseException("Cannot change status from " + currentStatus);

      default:
        // No specific validation for other statuses
        break;
    }
  }

  private void createStatusHistory(EOrder order, OrderStatus oldStatus, OrderStatus newStatus, String reason) {
    EOrderStatusHistory statusHistory = EOrderStatusHistory.builder()
        .order(order)
        .oldStatus(oldStatus)
        .newStatus(newStatus)
        .reason(reason)
        .changedBy("SYSTEM") // This would come from security context in real implementation
        .build();

    orderStatusHistoryRepository.save(statusHistory);
  }

  private void updateOrderItemsStatus(EOrder order, OrderStatus orderStatus) {
    // Convert order status to appropriate item status
    OrderItemStatus itemStatus = mapOrderStatusToItemStatus(orderStatus);
    
    if (itemStatus != null) {
      List<EOrderItem> orderItems = orderItemRepository.findAll().stream()
          .filter(item -> item.getOrder().getId().equals(order.getId()))
          .collect(Collectors.toList());
      
      orderItems.forEach(item -> {
        item.setStatus(itemStatus);
        orderItemRepository.save(item);
      });
    }
  }

  private OrderItemStatus mapOrderStatusToItemStatus(OrderStatus orderStatus) {
    return switch (orderStatus) {
      case PENDING -> OrderItemStatus.PENDING;
      case CONFIRMED -> OrderItemStatus.CONFIRMED;
      case PROCESSING -> OrderItemStatus.PROCESSING;
      case SHIPPED -> OrderItemStatus.SHIPPED;
      case DELIVERED -> OrderItemStatus.DELIVERED;
      case CANCELLED -> OrderItemStatus.CANCELLED;
      default -> null;
    };
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
