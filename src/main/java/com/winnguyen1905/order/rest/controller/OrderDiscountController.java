package com.winnguyen1905.order.rest.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.winnguyen1905.order.model.response.OrderDiscountResponse;
import com.winnguyen1905.order.rest.service.OrderDiscountService;
import com.winnguyen1905.order.secure.RestResponse;
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
@RequestMapping("/api/order-discounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Discounts", description = "APIs for managing discounts applied to orders")
public class OrderDiscountController {
  private final OrderDiscountService orderDiscountService;

  @Operation(summary = "Apply discount to order", description = "Applies a discount to an existing order using a discount code or amount", tags = {
      "Customer Operations", "Admin Operations" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Discount applied successfully", content = @Content(schema = @Schema(implementation = OrderDiscountResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid discount data or discount already applied"),
      @ApiResponse(responseCode = "404", description = "Order not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping("/orders/{orderId}")
  public ResponseEntity<OrderDiscountResponse> applyDiscount(
      @Parameter(description = "Order ID", required = true) @PathVariable UUID orderId,
      @Valid @RequestBody CreateDiscountRequest request) {

    log.info("Applying discount to order: {}, code: {}", orderId, request.getDiscountCode());
    OrderDiscountResponse response = orderDiscountService.applyDiscount(orderId, request);
    log.info("Discount applied with ID: {}", response.getId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(response);
  }

  @Operation(summary = "Get discount by ID", description = "Retrieves a discount by its unique identifier", tags = {
      "Admin Operations", "Customer Operations" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Discount retrieved successfully", content = @Content(schema = @Schema(implementation = OrderDiscountResponse.class))),
      @ApiResponse(responseCode = "404", description = "Discount not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}")
  public ResponseEntity<OrderDiscountResponse> getDiscountById(
      @Parameter(description = "Discount ID", required = true) @PathVariable UUID id) {
    log.info("Retrieving discount with ID: {}", id);
    OrderDiscountResponse response = orderDiscountService.getDiscountById(id);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get discounts by order ID", description = "Retrieves all discounts applied to a specific order", tags = {
      "Admin Operations", "Customer Operations" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Order discounts retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Order not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/orders/{orderId}")
  public ResponseEntity<List<OrderDiscountResponse>> getDiscountsByOrderId(
      @Parameter(description = "Order ID", required = true) @PathVariable UUID orderId) {
    log.info("Retrieving discounts for order: {}", orderId);
    List<OrderDiscountResponse> response = orderDiscountService.getDiscountsByOrderId(orderId);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Remove discount", description = "Removes a discount from an order", tags = {
      "Admin Operations" })
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Discount removed successfully"),
      @ApiResponse(responseCode = "404", description = "Discount not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> removeDiscount(
      @Parameter(description = "Discount ID", required = true) @PathVariable UUID id) {
    log.info("Removing discount with ID: {}", id);
    orderDiscountService.removeDiscount(id);
    return ResponseEntity.ok(null);
  }
}
