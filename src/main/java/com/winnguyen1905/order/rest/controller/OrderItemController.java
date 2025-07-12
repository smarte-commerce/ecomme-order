package com.winnguyen1905.order.rest.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.winnguyen1905.order.common.constant.OrderItemStatus;
import com.winnguyen1905.order.model.response.OrderItemResponse;
import com.winnguyen1905.order.rest.service.OrderItemService;
import com.winnguyen1905.order.secure.RestResponse;
import com.winnguyen1905.order.util.ResponseUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/order-items")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Items", description = "APIs for managing individual items within orders")
public class OrderItemController {
  private final OrderItemService orderItemService;

  @Operation(summary = "Get order item by ID", description = "Retrieves a specific order item by its unique identifier", tags = {
      "Customer Operations", "Vendor Operations", "Admin Operations" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Order item retrieved successfully", content = @Content(schema = @Schema(implementation = OrderItemResponse.class))),
      @ApiResponse(responseCode = "404", description = "Order item not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}")
  public ResponseEntity<OrderItemResponse> getOrderItemById(
      @Parameter(description = "Order item ID", required = true) @PathVariable UUID id) {
    log.info("Retrieving order item with ID: {}", id);
    OrderItemResponse response = orderItemService.getOrderItemById(id);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get order items by order ID", description = "Retrieves all items belonging to a specific order with pagination", tags = {
      "Customer Operations", "Vendor Operations", "Admin Operations" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Order items retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Order not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/order/{orderId}")
  public ResponseEntity<Page<OrderItemResponse>> getOrderItemsByOrderId(
      @Parameter(description = "Order ID", required = true) @PathVariable UUID orderId,
      @Parameter(description = "Pagination parameters") @PageableDefault(size = 20) Pageable pageable) {
    log.info("Retrieving order items for order: {} with pagination: {}", orderId, pageable);
    Page<OrderItemResponse> response = orderItemService.getOrderItemsByOrderId(orderId, pageable);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Update order item status", description = "Updates the status of a specific order item", tags = {
      "Vendor Operations", "Admin Operations" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Order item status updated successfully"),
      @ApiResponse(responseCode = "404", description = "Order item not found"),
      @ApiResponse(responseCode = "400", description = "Invalid status"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PatchMapping("/{id}/status")
  public ResponseEntity<OrderItemResponse> updateOrderItemStatus(
      @Parameter(description = "Order item ID", required = true) @PathVariable UUID id,
      @Parameter(description = "New status", required = true) @RequestParam OrderItemStatus status) {
    log.info("Updating order item status. ID: {}, New status: {}", id, status);
    OrderItemResponse response = orderItemService.updateOrderItemStatus(id, status);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Update order item quantity", description = "Updates the quantity of a specific order item and recalculates price", tags = {
      "Customer Operations", "Admin Operations" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Order item quantity updated successfully"),
      @ApiResponse(responseCode = "404", description = "Order item not found"),
      @ApiResponse(responseCode = "400", description = "Invalid quantity"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PatchMapping("/{id}/quantity")
  public ResponseEntity<OrderItemResponse> updateOrderItemQuantity(
      @Parameter(description = "Order item ID", required = true) @PathVariable UUID id,
      @Parameter(description = "New quantity (must be greater than 0)", required = true) @RequestParam int quantity) {
    log.info("Updating order item quantity. ID: {}, New quantity: {}", id, quantity);
    OrderItemResponse response = orderItemService.updateOrderItemQuantity(id, quantity);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Delete order item", description = "Removes an item from an order", tags = {
      "Admin Operations" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Order item deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Order item not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteOrderItem(
      @Parameter(description = "Order item ID", required = true) @PathVariable UUID id) {
    log.info("Deleting order item with ID: {}", id);
    orderItemService.deleteOrderItem(id);
    return ResponseEntity.ok(null);
  }

  /**
   * Legacy endpoint for backward compatibility
   */
  @Deprecated
  @Operation(summary = "Get all order items (non-paginated)", description = "Legacy endpoint - use paginated version instead", deprecated = true, tags = {
      "Admin Operations" })
  @GetMapping("/list/order/{orderId}")
  public ResponseEntity<List<OrderItemResponse>> getOrderItemsByOrderIdNonPaginated(
      @Parameter(description = "Order ID", required = true) @PathVariable UUID orderId) {
    log.warn("Using deprecated non-paginated endpoint to retrieve order items");
    List<OrderItemResponse> response = orderItemService.getOrderItemsByOrderId(orderId);
    return ResponseEntity.ok(response);
  }
}
