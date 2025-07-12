package com.winnguyen1905.order.rest.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.winnguyen1905.order.model.request.CreateExternalRefRequest;
import com.winnguyen1905.order.model.response.OrderExternalRefResponse;
import com.winnguyen1905.order.rest.service.OrderExternalRefService;
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
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/order-external-refs")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "External References", description = "APIs for managing external references to orders for integration with other services")
public class OrderExternalRefController {
  private final OrderExternalRefService orderExternalRefService;

  @Operation(
      summary = "Create external reference", 
      description = "Creates an external reference for an order with the provided service information",
      tags = {"Integration Operations", "Admin Operations"}
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "External reference created successfully", 
              content = @Content(schema = @Schema(implementation = OrderExternalRefResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "404", description = "Order not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping("/orders/{orderId}")
  public ResponseEntity<OrderExternalRefResponse> createExternalRef(
      @Parameter(description = "Order ID", required = true) @PathVariable UUID orderId,
      @Valid @RequestBody CreateExternalRefRequest request) {

    log.info("Creating external reference for order: {}, service: {}",
        orderId, request.getServiceName());
    OrderExternalRefResponse response = orderExternalRefService.createExternalRef(orderId, request);
    log.info("External reference created with ID: {}", response.getId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(response);
  }

  @Operation(
      summary = "Get external reference by ID", 
      description = "Retrieves an external reference by its unique identifier",
      tags = {"Integration Operations", "Admin Operations"}
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "External reference retrieved successfully", 
              content = @Content(schema = @Schema(implementation = OrderExternalRefResponse.class))),
      @ApiResponse(responseCode = "404", description = "External reference not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}")
  public ResponseEntity<OrderExternalRefResponse> getExternalRefById(
      @Parameter(description = "External reference ID", required = true) @PathVariable UUID id) {
    log.info("Retrieving external reference with ID: {}", id);
    OrderExternalRefResponse response = orderExternalRefService.getExternalRefById(id);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get external references by order ID", 
      description = "Retrieves all external references for a specific order",
      tags = {"Integration Operations", "Admin Operations", "Vendor Operations"}
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "External references retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Order not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/orders/{orderId}")
  public ResponseEntity<List<OrderExternalRefResponse>> getExternalRefsByOrderId(
      @Parameter(description = "Order ID", required = true) @PathVariable UUID orderId) {

    log.info("Retrieving external references for order: {}", orderId);
    List<OrderExternalRefResponse> response = orderExternalRefService.getExternalRefsByOrderId(orderId);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get external references by service name", 
      description = "Retrieves all external references for a specific service with pagination",
      tags = {"Integration Operations", "Admin Operations"}
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "External references retrieved successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid service name"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/services/{serviceName}")
  public ResponseEntity<Page<OrderExternalRefResponse>> getExternalRefsByServiceName(
      @Parameter(description = "Service name", required = true) @PathVariable @NotBlank String serviceName,
      @Parameter(description = "Pagination parameters") @PageableDefault(size = 20) Pageable pageable) {

    log.info("Retrieving external references for service: {}", serviceName);
    Page<OrderExternalRefResponse> response = orderExternalRefService.getExternalRefsByServiceName(serviceName,
        pageable);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Update external reference status", 
      description = "Updates the status of an existing external reference",
      tags = {"Integration Operations"}
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "External reference status updated successfully"),
      @ApiResponse(responseCode = "404", description = "External reference not found"),
      @ApiResponse(responseCode = "400", description = "Invalid status"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PatchMapping("/{id}/status")
    public ResponseEntity<OrderExternalRefResponse> updateExternalRefStatus(
      @Parameter(description = "External reference ID", required = true) @PathVariable UUID id,
      @Parameter(description = "New status", required = true) @RequestParam @NotBlank String status) {

    log.info("Updating external reference status. ID: {}, New status: {}", id, status);
    OrderExternalRefResponse response = orderExternalRefService.updateExternalRefStatus(id, status);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Delete external reference", 
      description = "Deletes an external reference by its ID",
      tags = {"Integration Operations", "Admin Operations"}
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "External reference deleted successfully"),
      @ApiResponse(responseCode = "404", description = "External reference not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteExternalRef(
      @Parameter(description = "External reference ID", required = true) @PathVariable UUID id) {
    log.info("Deleting external reference with ID: {}", id);
    orderExternalRefService.deleteExternalRef(id);
    return ResponseEntity.ok(null);
  }
}
