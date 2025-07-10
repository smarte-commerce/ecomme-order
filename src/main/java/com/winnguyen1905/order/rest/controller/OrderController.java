package com.winnguyen1905.order.rest.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.winnguyen1905.order.common.constant.OrderStatus;
import com.winnguyen1905.order.model.request.CreateOrderRequest;
import com.winnguyen1905.order.model.response.OrderResponse;
import com.winnguyen1905.order.rest.service.OrderService;
import com.winnguyen1905.order.secure.AccountRequest;
import com.winnguyen1905.order.secure.RestResponse;
import com.winnguyen1905.order.secure.TAccountRequest;
import com.winnguyen1905.order.util.ResponseUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Management", description = "APIs for managing orders for both customers and vendors")
public class OrderController {
  private final OrderService orderService;

  // Customer-facing endpoints

  @Operation(summary = "Create a new order", description = "Creates a new order with the provided details, including items and shipping information", tags = {
      "Customer Operations" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Order created successfully", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping
  public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request, @AccountRequest TAccountRequest accountRequest) {
    log.info("Creating new order for customer: {}", accountRequest.id());
    OrderResponse response = orderService.createOrder(request, accountRequest);
    log.info("Order created with ID: {}", response.getId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(response);
  }

  @Operation(summary = "Get order by ID", description = "Retrieves detailed order information by its unique identifier", tags = {
      "Customer Operations", "Vendor Operations" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Order retrieved successfully", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
      @ApiResponse(responseCode = "404", description = "Order not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}")
  public ResponseEntity<OrderResponse> getOrderById(
      @Parameter(description = "Order ID", required = true) @PathVariable UUID id) {
    log.info("Retrieving order with ID: {}", id);
    OrderResponse response = orderService.getOrderById(id);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get all orders with pagination", description = "Retrieves all orders with pagination support", tags = {
      "Admin Operations" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping
  public ResponseEntity<Page<OrderResponse>> getAllOrders(
      @Parameter(description = "Pagination parameters") @PageableDefault(size = 20) Pageable pageable) {
    log.info("Retrieving all orders with pagination: {}", pageable);
    Page<OrderResponse> response = orderService.getAllOrders(pageable);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get customer orders", description = "Retrieves all orders for a specific customer with pagination", tags = {
      "Customer Operations" })
  @GetMapping("/customer/{customerId}")
  public ResponseEntity<Page<OrderResponse>> getOrdersByCustomerId(
      @Parameter(description = "Customer ID", required = true) @PathVariable Long customerId,
      @Parameter(description = "Pagination parameters") @PageableDefault(size = 20) Pageable pageable) {
    log.info("Retrieving orders for customer: {}", customerId);
    Page<OrderResponse> response = orderService.getOrdersByCustomerId(customerId, pageable);
    return ResponseEntity.ok(response);
  }

  // Shared endpoints for customers, vendors, and admins

  @Operation(summary = "Get orders by status", description = "Retrieves all orders with a specific status with pagination", tags = {
      "Customer Operations", "Vendor Operations", "Admin Operations" })
  @GetMapping("/status/{status}")
  public ResponseEntity<Page<OrderResponse>> getOrdersByStatus(
      @Parameter(description = "Order status", required = true) @PathVariable OrderStatus status,
      @Parameter(description = "Pagination parameters") @PageableDefault(size = 20) Pageable pageable) {
    log.info("Retrieving orders with status: {}", status);
    Page<OrderResponse> response = orderService.getOrdersByStatus(status, pageable);
    return ResponseEntity.ok(response);
  }

  // Admin and operations endpoints

  @Operation(summary = "Update order status", description = "Updates the status of an existing order with an optional reason", tags = {
      "Admin Operations", "Vendor Operations" })
  @PatchMapping("/{id}/status")
  public ResponseEntity<OrderResponse> updateOrderStatus(
      @Parameter(description = "Order ID", required = true) @PathVariable UUID id,
      @Parameter(description = "New order status", required = true) @RequestParam OrderStatus status,
      @Parameter(description = "Reason for status change") @RequestParam(required = false) String reason) {
    log.info("Updating order status. Order ID: {}, New Status: {}, Reason: {}", id, status, reason);
    OrderResponse response = orderService.updateOrderStatus(id, status, reason);
    return ResponseEntity.ok(response);
  }

  // Customer-facing endpoints

  @Operation(summary = "Cancel order", description = "Cancels an existing order with an optional reason", tags = {
      "Customer Operations" })
  @PatchMapping("/{id}/cancel")
  public ResponseEntity<OrderResponse> cancelOrder(
      @Parameter(description = "Order ID", required = true) @PathVariable UUID id,
      @Parameter(description = "Cancellation reason") @RequestParam(required = false) String reason) {
    log.info("Cancelling order. Order ID: {}, Reason: {}", id, reason);
    OrderResponse response = orderService.cancelOrder(id, reason);
    return ResponseEntity.ok(response);
  }

  // Admin endpoints

  @Operation(summary = "Delete order", description = "Deletes an order by its ID (admin only)", tags = {
      "Admin Operations" })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteOrder(
      @Parameter(description = "Order ID", required = true) @PathVariable UUID id) {
    log.info("Deleting order with ID: {}", id);
    orderService.deleteOrder(id);
    return ResponseEntity.ok(null);
  }

  /**
   * Legacy endpoint for backward compatibility
   */
  @Deprecated
  @Operation(summary = "Get all orders (non-paginated)", description = "Legacy endpoint - use paginated version instead", deprecated = true, tags = {
      "Admin Operations" })
  @GetMapping("/list-all")
  public ResponseEntity<List<OrderResponse>> getAllOrdersNonPaginated() {
    log.warn("Using deprecated non-paginated endpoint to retrieve all orders");
    List<OrderResponse> response = orderService.getAllOrders();
    return ResponseEntity.ok(response);
  }
}
