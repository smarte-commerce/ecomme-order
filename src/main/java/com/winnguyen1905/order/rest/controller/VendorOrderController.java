package com.winnguyen1905.order.rest.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.winnguyen1905.order.common.constant.VendorOrderStatus;
import com.winnguyen1905.order.model.request.CreateVendorOrderRequest;
import com.winnguyen1905.order.model.response.VendorOrderResponse;
import com.winnguyen1905.order.rest.service.VendorOrderService;
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
@RequestMapping("/api/vendor-orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vendor Order Management", description = "APIs for managing vendor-specific orders and fulfillment")
public class VendorOrderController {
    private final VendorOrderService vendorOrderService;
    
    @Operation(
        summary = "Create vendor order", 
        description = "Creates a vendor-specific order linked to a main order",
        tags = {"Admin Operations", "Vendor Operations"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Vendor order created successfully", 
                content = @Content(schema = @Schema(implementation = VendorOrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data or vendor order already exists"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/orders/{orderId}")
    public ResponseEntity<RestResponse<VendorOrderResponse>> createVendorOrder(
            @Parameter(description = "Parent order ID", required = true) @PathVariable UUID orderId,
            @Valid @RequestBody CreateVendorOrderRequest request) {
        
        log.info("Creating vendor order for order: {}, vendor: {}", orderId, request.getVendorId());
        VendorOrderResponse response = vendorOrderService.createVendorOrder(orderId, request);
        log.info("Vendor order created with ID: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Vendor order created successfully", response));
    }
    
    @Operation(
        summary = "Get vendor order by ID", 
        description = "Retrieves a vendor order by its unique identifier",
        tags = {"Vendor Operations", "Admin Operations"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vendor order retrieved successfully", 
                content = @Content(schema = @Schema(implementation = VendorOrderResponse.class))),
        @ApiResponse(responseCode = "404", description = "Vendor order not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<VendorOrderResponse>> getVendorOrderById(
            @Parameter(description = "Vendor order ID", required = true) @PathVariable UUID id) {
        log.info("Retrieving vendor order with ID: {}", id);
        VendorOrderResponse response = vendorOrderService.getVendorOrderById(id);
        return ResponseEntity.ok(ResponseUtil.success("Vendor order retrieved successfully", response));
    }
    
    @Operation(
        summary = "Get vendor orders by order ID", 
        description = "Retrieves all vendor orders associated with a specific customer order",
        tags = {"Admin Operations", "Customer Operations"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vendor orders retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<RestResponse<List<VendorOrderResponse>>> getVendorOrdersByOrderId(
            @Parameter(description = "Parent order ID", required = true) @PathVariable UUID orderId) {
        log.info("Retrieving vendor orders for order: {}", orderId);
        List<VendorOrderResponse> response = vendorOrderService.getVendorOrdersByOrderId(orderId);
        return ResponseEntity.ok(ResponseUtil.success("Order vendor orders retrieved successfully", response));
    }
    
    @Operation(
        summary = "Get vendor orders by vendor ID", 
        description = "Retrieves all orders for a specific vendor with pagination",
        tags = {"Vendor Operations"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vendor orders retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/vendors/{vendorId}")
    public ResponseEntity<RestResponse<Page<VendorOrderResponse>>> getVendorOrdersByVendorId(
            @Parameter(description = "Vendor ID", required = true) @PathVariable Long vendorId,
            @Parameter(description = "Pagination parameters") 
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Retrieving orders for vendor: {} with pagination: {}", vendorId, pageable);
        Page<VendorOrderResponse> response = vendorOrderService.getVendorOrdersByVendorId(vendorId, pageable);
        return ResponseEntity.ok(ResponseUtil.success("Vendor orders retrieved successfully", response));
    }
    
    @Operation(
        summary = "Update vendor order status", 
        description = "Updates the status of a vendor order (e.g., processing, shipped)",
        tags = {"Vendor Operations"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vendor order status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Vendor order not found"),
        @ApiResponse(responseCode = "400", description = "Invalid status or invalid status transition"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<RestResponse<VendorOrderResponse>> updateVendorOrderStatus(
            @Parameter(description = "Vendor order ID", required = true) @PathVariable UUID id,
            @Parameter(description = "New vendor order status", required = true) @RequestParam VendorOrderStatus status) {
        
        log.info("Updating vendor order status. ID: {}, New status: {}", id, status);
        VendorOrderResponse response = vendorOrderService.updateVendorOrderStatus(id, status);
        return ResponseEntity.ok(ResponseUtil.success("Vendor order status updated successfully", response));
    }
    
    @Operation(
        summary = "Get vendor orders by status", 
        description = "Retrieves all vendor orders with a specific status with pagination",
        tags = {"Vendor Operations", "Admin Operations"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vendor orders retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<RestResponse<Page<VendorOrderResponse>>> getVendorOrdersByStatus(
            @Parameter(description = "Vendor order status", required = true) @PathVariable VendorOrderStatus status,
            @Parameter(description = "Pagination parameters") 
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Retrieving vendor orders with status: {} with pagination: {}", status, pageable);
        Page<VendorOrderResponse> response = vendorOrderService.getVendorOrdersByStatus(status, pageable);
        return ResponseEntity.ok(ResponseUtil.success("Vendor orders with status retrieved successfully", response));
    }
} 
