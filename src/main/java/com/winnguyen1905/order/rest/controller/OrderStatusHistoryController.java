package com.winnguyen1905.order.rest.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.winnguyen1905.order.model.response.OrderStatusHistoryResponse;
import com.winnguyen1905.order.rest.service.OrderStatusHistoryService;
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
@RequestMapping("/api/order-status-history")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Status History", description = "APIs for tracking order status changes over time")
public class OrderStatusHistoryController {
  private final OrderStatusHistoryService orderStatusHistoryService;

  @Operation(summary = "Get status history entry by ID", description = "Retrieves a specific status history entry by its unique identifier", tags = {
      "Admin Operations" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Status history retrieved successfully", content = @Content(schema = @Schema(implementation = OrderStatusHistoryResponse.class))),
      @ApiResponse(responseCode = "404", description = "Status history entry not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}")
  public ResponseEntity<RestResponse<OrderStatusHistoryResponse>> getStatusHistoryById(
      @Parameter(description = "Status history entry ID", required = true) @PathVariable UUID id) {
    log.info("Retrieving status history entry with ID: {}", id);
    OrderStatusHistoryResponse response = orderStatusHistoryService.getStatusHistoryById(id);
    return ResponseEntity.ok(ResponseUtil.success("Status history retrieved successfully", response));
  }

  @Operation(summary = "Get order status history", description = "Retrieves the complete status history for a specific order", tags = {
      "Customer Operations", "Vendor Operations", "Admin Operations" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Order status history retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Order not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/orders/{orderId}")
  public ResponseEntity<RestResponse<List<OrderStatusHistoryResponse>>> getStatusHistoryByOrderId(
      @Parameter(description = "Order ID", required = true) @PathVariable UUID orderId) {
    log.info("Retrieving status history for order: {}", orderId);
    List<OrderStatusHistoryResponse> response = orderStatusHistoryService.getStatusHistoryByOrderId(orderId);
    return ResponseEntity.ok(ResponseUtil.success("Order status history retrieved successfully", response));
  }
}
