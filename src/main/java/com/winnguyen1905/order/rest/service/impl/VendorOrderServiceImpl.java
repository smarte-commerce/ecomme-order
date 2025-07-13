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

import com.winnguyen1905.order.common.constant.OrderItemStatus;
import com.winnguyen1905.order.common.constant.VendorOrderStatus;
import com.winnguyen1905.order.model.request.CreateVendorOrderRequest;
import com.winnguyen1905.order.model.request.VendorShippingUpdateRequest;
import com.winnguyen1905.order.model.response.VendorOrderAnalyticsResponse;
import com.winnguyen1905.order.model.response.VendorOrderResponse;
import com.winnguyen1905.order.model.response.VendorPerformanceResponse;
import com.winnguyen1905.order.persistance.entity.EOrder;
import com.winnguyen1905.order.persistance.entity.EOrderItem;
import com.winnguyen1905.order.persistance.entity.EVendorOrder;
import com.winnguyen1905.order.persistance.repository.OrderItemRepository;
import com.winnguyen1905.order.persistance.repository.OrderRepository;
import com.winnguyen1905.order.persistance.repository.VendorOrderRepository;
import com.winnguyen1905.order.rest.service.VendorOrderService;
import com.winnguyen1905.order.secure.BaseException;
import com.winnguyen1905.order.util.OrderMapper;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorOrderServiceImpl implements VendorOrderService {
  private final VendorOrderRepository vendorOrderRepository;
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
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

    // Calculate commission amount and vendor payout
    double commissionAmount = subtotal * (request.getCommissionRate() / 100.0);
    double vendorPayout = subtotal - commissionAmount;

    vendorOrder.setCommissionAmount(commissionAmount);
    vendorOrder.setVendorPayout(vendorPayout);
    vendorOrder.setStatus(VendorOrderStatus.PENDING);

    // Generate vendor order number
    vendorOrder.setVendorOrderNumber("VO-" + order.getOrderNumber() + "-" + request.getVendorId());

    // Save vendor order
    vendorOrder = vendorOrderRepository.save(vendorOrder);

    log.info("Created vendor order with ID: {}, for vendor: {}, order: {}",
        vendorOrder.getId(), request.getVendorId(), orderId);

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
    List<EVendorOrder> vendorOrders = vendorOrderRepository.findAll().stream()
        .filter(vo -> vo.getOrder().getId().equals(orderId))
        .collect(Collectors.toList());

    return vendorOrders.stream()
        .map(orderMapper::toVendorOrderResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Deprecated
  public List<VendorOrderResponse> getVendorOrdersByVendorId(Long vendorId) {
    // This would typically involve a custom query in the repository
    List<EVendorOrder> vendorOrders = vendorOrderRepository.findAll().stream()
        .filter(vo -> vo.getVendorId().equals(vendorId))
        .collect(Collectors.toList());

    return vendorOrders.stream()
        .map(orderMapper::toVendorOrderResponse)
        .collect(Collectors.toList());
  }

  @Override
  public Page<VendorOrderResponse> getVendorOrdersByVendorId(Long vendorId, Pageable pageable) {
    // In a real application, this would use a repository method with native
    // pagination
    List<EVendorOrder> vendorOrders = vendorOrderRepository.findAll().stream()
        .filter(vo -> vo.getVendorId().equals(vendorId))
        .collect(Collectors.toList());

    List<VendorOrderResponse> responses = vendorOrders.stream()
        .map(orderMapper::toVendorOrderResponse)
        .collect(Collectors.toList());

    return createPage(responses, pageable);
  }

  @Override
  public Page<VendorOrderResponse> getVendorOrdersByStatus(VendorOrderStatus status, Pageable pageable) {
    // In a real application, this would use a repository method with native
    // pagination
    List<EVendorOrder> vendorOrders = vendorOrderRepository.findAll().stream()
        .filter(vo -> vo.getStatus() == status)
        .collect(Collectors.toList());

    List<VendorOrderResponse> responses = vendorOrders.stream()
        .map(orderMapper::toVendorOrderResponse)
        .collect(Collectors.toList());

    return createPage(responses, pageable);
  }

  @Override
  @Transactional
  public VendorOrderResponse updateVendorOrderStatus(UUID id, VendorOrderStatus status) {
    EVendorOrder vendorOrder = vendorOrderRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Vendor order not found with id: " + id));

    // Validate status transition
    validateStatusTransition(vendorOrder.getStatus(), status);

    VendorOrderStatus oldStatus = vendorOrder.getStatus();
    vendorOrder.setStatus(status);
    EVendorOrder savedVendorOrder = vendorOrderRepository.save(vendorOrder);

    // Update order items status if needed
    updateOrderItemsStatus(savedVendorOrder, status);

    log.info("Updated vendor order status. ID: {}, Old status: {}, New status: {}",
        id, oldStatus, status);

    return orderMapper.toVendorOrderResponse(savedVendorOrder);
  }

  @Override
  public Page<VendorOrderResponse> getVendorOrdersByVendorIdAndStatus(
      Long vendorId, VendorOrderStatus status, Pageable pageable) {
    // In a real application, this would use a repository method with native
    // pagination
    List<EVendorOrder> vendorOrders = vendorOrderRepository.findAll().stream()
        .filter(vo -> vo.getVendorId().equals(vendorId) && vo.getStatus() == status)
        .collect(Collectors.toList());

    List<VendorOrderResponse> responses = vendorOrders.stream()
        .map(orderMapper::toVendorOrderResponse)
        .collect(Collectors.toList());

    return createPage(responses, pageable);
  }

  @Override
  public Page<VendorOrderResponse> searchVendorOrders(
      Long vendorId, String orderNumber, VendorOrderStatus status,
      LocalDate startDate, LocalDate endDate,
      Double minAmount, Double maxAmount, Pageable pageable) {

    // This would typically involve a custom query in the repository with pagination
    // For now, we'll filter the vendor orders in memory

    List<EVendorOrder> allVendorOrders = vendorOrderRepository.findAll();

    // Apply filters
    List<EVendorOrder> filteredOrders = allVendorOrders.stream()
        .filter(vo -> vendorId == null || vo.getVendorId().equals(vendorId))
        .filter(vo -> orderNumber == null ||
            vo.getVendorOrderNumber().contains(orderNumber) ||
            vo.getOrder().getOrderNumber().contains(orderNumber))
        .filter(vo -> status == null || vo.getStatus() == status)
        .filter(vo -> startDate == null ||
            vo.getCreatedDate().atZone(java.time.ZoneId.systemDefault()).toLocalDate().isAfter(startDate) ||
            vo.getCreatedDate().atZone(java.time.ZoneId.systemDefault()).toLocalDate().isEqual(startDate))
        .filter(vo -> endDate == null ||
            vo.getCreatedDate().atZone(java.time.ZoneId.systemDefault()).toLocalDate().isBefore(endDate) ||
            vo.getCreatedDate().atZone(java.time.ZoneId.systemDefault()).toLocalDate().isEqual(endDate))
        .filter(vo -> minAmount == null || vo.getSubtotal() >= minAmount)
        .filter(vo -> maxAmount == null || vo.getSubtotal() <= maxAmount)
        .collect(Collectors.toList());

    List<VendorOrderResponse> responses = filteredOrders.stream()
        .map(orderMapper::toVendorOrderResponse)
        .collect(Collectors.toList());

    return createPage(responses, pageable);
  }

  @Override
  @Transactional
  public VendorOrderResponse updateShippingInfo(UUID id, VendorShippingUpdateRequest request) {
    EVendorOrder vendorOrder = vendorOrderRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Vendor order not found with id: " + id));

    // Validate vendor order status
    if (vendorOrder.getStatus() != VendorOrderStatus.PROCESSING &&
        vendorOrder.getStatus() != VendorOrderStatus.CONFIRMED) {
      throw new BaseException("Cannot update shipping info for vendor order with status: " +
          vendorOrder.getStatus());
    }

    // In a real application, we would store shipping info in a separate entity
    // For now, we'll update the vendor order status
    vendorOrder.setStatus(VendorOrderStatus.SHIPPED);
    EVendorOrder savedVendorOrder = vendorOrderRepository.save(vendorOrder);

    // Update order items status
    List<EOrderItem> orderItems = orderItemRepository.findAll().stream()
        .filter(item -> item.getOrder().getId().equals(savedVendorOrder.getOrder().getId()) &&
            item.getVendorId().equals(savedVendorOrder.getVendorId()))
        .collect(Collectors.toList());

    for (EOrderItem item : orderItems) {
      item.setStatus(OrderItemStatus.SHIPPED);
      orderItemRepository.save(item);
    }

    // If customer notification is requested, send notification (in a real app)
    if (request.isNotifyCustomer()) {
      // In a real application, this would call a notification service
      log.info("Notification sent to customer for order: {}, tracking: {}",
          savedVendorOrder.getOrder().getId(), request.getTrackingNumber());
    }

    log.info("Updated shipping info for vendor order: {}, tracking: {}",
        id, request.getTrackingNumber());

    return orderMapper.toVendorOrderResponse(savedVendorOrder);
  }

  @Override
  @Transactional
  public VendorOrderResponse acceptVendorOrder(UUID id, LocalDate estimatedShippingDate, String notes) {
    EVendorOrder vendorOrder = vendorOrderRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Vendor order not found with id: " + id));

    // Validate vendor order status
    if (vendorOrder.getStatus() != VendorOrderStatus.PENDING) {
      throw new BaseException("Can only accept vendor orders with PENDING status");
    }

    // Update vendor order status
    vendorOrder.setStatus(VendorOrderStatus.CONFIRMED);
    EVendorOrder savedVendorOrder = vendorOrderRepository.save(vendorOrder);

    // Update order items status
    List<EOrderItem> orderItems = orderItemRepository.findAll().stream()
        .filter(item -> item.getOrder().getId().equals(savedVendorOrder.getOrder().getId()) &&
            item.getVendorId().equals(savedVendorOrder.getVendorId()))
        .collect(Collectors.toList());

    for (EOrderItem item : orderItems) {
      item.setStatus(OrderItemStatus.CONFIRMED);
      orderItemRepository.save(item);
    }

    log.info("Vendor order accepted: {}, estimated shipping date: {}",
        id, estimatedShippingDate);

    return orderMapper.toVendorOrderResponse(savedVendorOrder);
  }

  @Override
  @Transactional
  public VendorOrderResponse rejectVendorOrder(UUID id, String reason) {
    EVendorOrder vendorOrder = vendorOrderRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Vendor order not found with id: " + id));

    // Validate vendor order status
    if (vendorOrder.getStatus() != VendorOrderStatus.PENDING) {
      throw new BaseException("Can only reject vendor orders with PENDING status");
    }

    // Update vendor order status
    vendorOrder.setStatus(VendorOrderStatus.CANCELLED);
    EVendorOrder savedVendorOrder = vendorOrderRepository.save(vendorOrder);

    // Update order items status
    List<EOrderItem> orderItems = orderItemRepository.findAll().stream()
        .filter(item -> item.getOrder().getId().equals(savedVendorOrder.getOrder().getId()) &&
            item.getVendorId().equals(savedVendorOrder.getVendorId()))
        .collect(Collectors.toList());

    for (EOrderItem item : orderItems) {
      item.setStatus(OrderItemStatus.CANCELLED);
      orderItemRepository.save(item);
    }

    log.info("Vendor order rejected: {}, reason: {}", id, reason);

    return orderMapper.toVendorOrderResponse(savedVendorOrder);
  }

  @Override
  public VendorOrderAnalyticsResponse getVendorOrderAnalytics(Long vendorId, LocalDate startDate, LocalDate endDate) {
    // Get all vendor orders for the vendor
    List<EVendorOrder> vendorOrders = vendorOrderRepository.findAll().stream()
        .filter(vo -> vo.getVendorId().equals(vendorId))
        .collect(Collectors.toList());

    // Filter by date range
    java.time.Instant startInstant = startDate != null
        ? startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
        : java.time.Instant.EPOCH;

    java.time.Instant endInstant = endDate != null
        ? endDate.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
        : java.time.Instant.now();

    List<EVendorOrder> filteredOrders = vendorOrders.stream()
        .filter(vo -> {
          java.time.Instant orderDate = vo.getCreatedDate();
          return !orderDate.isBefore(startInstant) && !orderDate.isAfter(endInstant);
        })
        .collect(Collectors.toList());

    // Calculate analytics
    int totalOrders = filteredOrders.size();

    double totalOrderValue = filteredOrders.stream()
        .mapToDouble(EVendorOrder::getSubtotal)
        .sum();

    double averageOrderValue = totalOrders > 0 ? totalOrderValue / totalOrders : 0;

    // Orders by status
    Map<VendorOrderStatus, Integer> ordersByStatus = new HashMap<>();
    for (EVendorOrder vo : filteredOrders) {
      ordersByStatus.put(vo.getStatus(), ordersByStatus.getOrDefault(vo.getStatus(), 0) + 1);
    }

    // Performance metrics
    double fulfillmentRate = calculateRate(filteredOrders, VendorOrderStatus.DELIVERED);
    double cancelationRate = calculateRate(filteredOrders, VendorOrderStatus.CANCELLED);

    // Time metrics (in a real app, this would involve more complex calculations)
    double averageFulfillmentTimeInHours = 48.0; // Mock value
    double averageTimeToAcceptInHours = 2.5; // Mock value

    // Product metrics
    List<VendorOrderAnalyticsResponse.ProductPerformance> topSellingProducts = getTopSellingProducts(vendorId,
        startInstant, endInstant);

    // Order count by day
    Map<LocalDate, Integer> orderCountByDay = new HashMap<>();
    Map<LocalDate, Double> revenueByDay = new HashMap<>();

    for (EVendorOrder vo : filteredOrders) {
      LocalDate orderDate = vo.getCreatedDate()
          .atZone(java.time.ZoneId.systemDefault())
          .toLocalDate();

      orderCountByDay.put(orderDate, orderCountByDay.getOrDefault(orderDate, 0) + 1);
      revenueByDay.put(orderDate, revenueByDay.getOrDefault(orderDate, 0.0) + vo.getSubtotal());
    }

    // Build and return analytics response
    return VendorOrderAnalyticsResponse.builder()
        .vendorId(vendorId)
        .startDate(startDate)
        .endDate(endDate)
        .totalOrders(totalOrders)
        .totalOrderValue(totalOrderValue)
        .averageOrderValue(averageOrderValue)
        .ordersByStatus(ordersByStatus)
        .fulfillmentRate(fulfillmentRate)
        .cancelationRate(cancelationRate)
        .averageFulfillmentTimeInHours(averageFulfillmentTimeInHours)
        .averageTimeToAcceptInHours(averageTimeToAcceptInHours)
        .topSellingProducts(topSellingProducts)
        .uniqueCustomers(getUniqueCustomersCount(filteredOrders))
        .orderCountByDay(orderCountByDay)
        .revenueByDay(revenueByDay)
        .build();
  }

  @Override
  public VendorPerformanceResponse getVendorPerformance(Long vendorId, LocalDate startDate, LocalDate endDate) {
    // In a real application, this would calculate performance metrics based on
    // orders, customer reviews, etc.
    // For now, we'll return mock data

    // Performance metrics
    int overallPerformanceScore = 85;
    double onTimeDeliveryRate = 0.92;
    double orderAccuracyRate = 0.98;
    double customerSatisfactionScore = 4.7;
    double fulfillmentRate = 0.95;
    double cancellationRate = 0.03;
    double returnRate = 0.02;

    // Time metrics
    double averageProcessingTimeInHours = 4.5;
    double averageShippingTimeInHours = 48.0;
    double averageTimeToAcceptInHours = 2.5;

    // Marketplace averages
    double marketplaceAveragePerformanceScore = 75.0;
    double performancePercentile = 90.0; // Top 10%

    // Improvement areas
    List<VendorPerformanceResponse.ImprovementArea> improvementAreas = new ArrayList<>();
    improvementAreas.add(new VendorPerformanceResponse.ImprovementArea(
        "Processing Time",
        "Reduce time between order acceptance and shipping",
        1,
        4.5,
        3.5));

    improvementAreas.add(new VendorPerformanceResponse.ImprovementArea(
        "Order Acceptance",
        "Improve time to accept new orders",
        2,
        2.5,
        1.5));

    // Performance over time (mock data)
    Map<LocalDate, Integer> performanceScoreByDay = new HashMap<>();
    Map<LocalDate, Double> onTimeDeliveryByDay = new HashMap<>();

    LocalDate currentDate = startDate != null ? startDate : LocalDate.now().minusDays(30);
    LocalDate endDateValue = endDate != null ? endDate : LocalDate.now();

    while (!currentDate.isAfter(endDateValue)) {
      performanceScoreByDay.put(currentDate, 75 + (int) (Math.random() * 20));
      onTimeDeliveryByDay.put(currentDate, 0.85 + (Math.random() * 0.15));
      currentDate = currentDate.plusDays(1);
    }

    // Build and return performance response
    return VendorPerformanceResponse.builder()
        .vendorId(vendorId)
        .vendorName("Vendor Name") // In a real app, this would come from a vendor service
        .startDate(startDate)
        .endDate(endDate)
        .overallPerformanceScore(overallPerformanceScore)
        .onTimeDeliveryRate(onTimeDeliveryRate)
        .orderAccuracyRate(orderAccuracyRate)
        .customerSatisfactionScore(customerSatisfactionScore)
        .fulfillmentRate(fulfillmentRate)
        .cancellationRate(cancellationRate)
        .returnRate(returnRate)
        .averageProcessingTimeInHours(averageProcessingTimeInHours)
        .averageShippingTimeInHours(averageShippingTimeInHours)
        .averageTimeToAcceptInHours(averageTimeToAcceptInHours)
        .performanceScoreByDay(performanceScoreByDay)
        .onTimeDeliveryByDay(onTimeDeliveryByDay)
        .marketplaceAveragePerformanceScore(marketplaceAveragePerformanceScore)
        .performancePercentile(performancePercentile)
        .suggestedImprovementAreas(improvementAreas)
        .build();
  }

  @Override
  public Map<VendorOrderStatus, Integer> getVendorOrdersCountByStatus(Long vendorId) {
    // Get all vendor orders for the vendor
    List<EVendorOrder> vendorOrders = vendorOrderRepository.findAll().stream()
        .filter(vo -> vo.getVendorId().equals(vendorId))
        .collect(Collectors.toList());

    // Count orders by status
    Map<VendorOrderStatus, Integer> countByStatus = new HashMap<>();

    for (EVendorOrder vo : vendorOrders) {
      countByStatus.put(vo.getStatus(), countByStatus.getOrDefault(vo.getStatus(), 0) + 1);
    }

    // Ensure all statuses are represented
    for (VendorOrderStatus status : VendorOrderStatus.values()) {
      countByStatus.putIfAbsent(status, 0);
    }

    return countByStatus;
  }

  private void validateStatusTransition(VendorOrderStatus currentStatus, VendorOrderStatus newStatus) {
    // Implement business rules for valid status transitions
    if (currentStatus == VendorOrderStatus.CANCELLED && newStatus != VendorOrderStatus.CANCELLED) {
      throw new BaseException("Cannot change status from CANCELLED");
    }

    if (currentStatus == VendorOrderStatus.DELIVERED && newStatus != VendorOrderStatus.DELIVERED) {
      throw new BaseException("Cannot change status from DELIVERED");
    }

    // Additional validation rules
    switch (currentStatus) {
      case PENDING:
        // Pending can transition to confirmed or cancelled
        if (newStatus != VendorOrderStatus.CONFIRMED && newStatus != VendorOrderStatus.CANCELLED) {
          throw new BaseException("Invalid status transition from PENDING to " + newStatus);
        }
        break;

      case CONFIRMED:
        // Confirmed can transition to processing or cancelled
        if (newStatus != VendorOrderStatus.PROCESSING && newStatus != VendorOrderStatus.CANCELLED) {
          throw new BaseException("Invalid status transition from CONFIRMED to " + newStatus);
        }
        break;

      case PROCESSING:
        // Processing can transition to shipped or cancelled
        if (newStatus != VendorOrderStatus.SHIPPED && newStatus != VendorOrderStatus.CANCELLED) {
          throw new BaseException("Invalid status transition from PROCESSING to " + newStatus);
        }
        break;

      case SHIPPED:
        // Shipped can only transition to delivered
        if (newStatus != VendorOrderStatus.DELIVERED) {
          throw new BaseException("Invalid status transition from SHIPPED to " + newStatus);
        }
        break;

      default:
        // No specific validation for other statuses
        break;
    }
  }

  private void updateOrderItemsStatus(EVendorOrder vendorOrder, VendorOrderStatus status) {
    // Map vendor order status to item status
    OrderItemStatus itemStatus = null;

    switch (status) {
      case PENDING:
        itemStatus = OrderItemStatus.PENDING;
        break;

      case CONFIRMED:
        itemStatus = OrderItemStatus.CONFIRMED;
        break;

      case PROCESSING:
        itemStatus = OrderItemStatus.PROCESSING;
        break;

      case SHIPPED:
        itemStatus = OrderItemStatus.SHIPPED;
        break;

      case DELIVERED:
        itemStatus = OrderItemStatus.DELIVERED;
        break;

      case CANCELLED:
        itemStatus = OrderItemStatus.CANCELLED;
        break;

      default:
        // No specific mapping for other statuses
        return;
    }

    if (itemStatus != null) {
      // Get all items for the vendor in this order
      List<EOrderItem> items = orderItemRepository.findAll().stream()
          .filter(item -> item.getOrder().getId().equals(vendorOrder.getOrder().getId()) &&
              item.getVendorId().equals(vendorOrder.getVendorId()))
          .collect(Collectors.toList());

      // Update item status
      for (EOrderItem item : items) {
        item.setStatus(itemStatus);
        orderItemRepository.save(item);
      }
    }
  }

  private double calculateRate(List<EVendorOrder> orders, VendorOrderStatus status) {
    if (orders.isEmpty()) {
      return 0.0;
    }

    long count = orders.stream()
        .filter(vo -> vo.getStatus() == status)
        .count();

    return (double) count / orders.size();
  }

  private List<VendorOrderAnalyticsResponse.ProductPerformance> getTopSellingProducts(
      Long vendorId, java.time.Instant startDate, java.time.Instant endDate) {
    // Get all order items for the vendor
    List<EOrderItem> items = orderItemRepository.findAll().stream()
        .filter(item -> item.getVendorId().equals(vendorId))
        .filter(item -> {
          java.time.Instant itemDate = item.getCreatedDate();
          return !itemDate.isBefore(startDate) && !itemDate.isAfter(endDate);
        })
        .collect(Collectors.toList());

    // Group by product and calculate metrics
    Map<Long, VendorOrderAnalyticsResponse.ProductPerformance> productPerformance = new HashMap<>();

    for (EOrderItem item : items) {
      Long productId = item.getProductId();
      String productName = item.getProductName();
      int quantity = item.getQuantity();
      double totalPrice = item.getTotalPrice();

      if (productPerformance.containsKey(productId)) {
        VendorOrderAnalyticsResponse.ProductPerformance performance = productPerformance.get(productId);
        performance.setQuantitySold(performance.getQuantitySold() + quantity);
        performance.setTotalRevenue(performance.getTotalRevenue() + totalPrice);
      } else {
        VendorOrderAnalyticsResponse.ProductPerformance performance = new VendorOrderAnalyticsResponse.ProductPerformance(
            productId,
            productName,
            quantity,
            totalPrice,
            item.getUnitPrice());
        productPerformance.put(productId, performance);
      }
    }

    // Calculate average price
    for (VendorOrderAnalyticsResponse.ProductPerformance performance : productPerformance.values()) {
      if (performance.getQuantitySold() > 0) {
        performance.setAveragePrice(performance.getTotalRevenue() / performance.getQuantitySold());
      }
    }

    // Get top selling products by quantity
    return productPerformance.values().stream()
        .sorted((p1, p2) -> Integer.compare(p2.getQuantitySold(), p1.getQuantitySold()))
        .limit(5)
        .collect(Collectors.toList());
  }

  private int getUniqueCustomersCount(List<EVendorOrder> orders) {
    return (int) orders.stream()
        .map(vo -> vo.getOrder().getCustomerId())
        .distinct()
        .count();
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
