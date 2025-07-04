package com.winnguyen1905.order.rest.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.winnguyen1905.order.common.constant.VendorOrderStatus;
import com.winnguyen1905.order.model.CreateVendorOrderRequest;
import com.winnguyen1905.order.model.VendorOrderResponse;
import com.winnguyen1905.order.rest.service.VendorOrderService;
import com.winnguyen1905.order.secure.RestResponse;
import com.winnguyen1905.order.util.ResponseUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vendor-orders")
@RequiredArgsConstructor
public class VendorOrderController {
    private final VendorOrderService vendorOrderService;
    
    @PostMapping("/orders/{orderId}")
    public ResponseEntity<RestResponse<VendorOrderResponse>> createVendorOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody CreateVendorOrderRequest request) {
        
        VendorOrderResponse response = vendorOrderService.createVendorOrder(orderId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Vendor order created successfully", response));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<VendorOrderResponse>> getVendorOrderById(@PathVariable UUID id) {
        VendorOrderResponse response = vendorOrderService.getVendorOrderById(id);
        return ResponseEntity.ok(ResponseUtil.success("Vendor order retrieved successfully", response));
    }
    
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<RestResponse<List<VendorOrderResponse>>> getVendorOrdersByOrderId(@PathVariable UUID orderId) {
        List<VendorOrderResponse> response = vendorOrderService.getVendorOrdersByOrderId(orderId);
        return ResponseEntity.ok(ResponseUtil.success("Order vendor orders retrieved successfully", response));
    }
    
    @GetMapping("/vendors/{vendorId}")
    public ResponseEntity<RestResponse<List<VendorOrderResponse>>> getVendorOrdersByVendorId(@PathVariable Long vendorId) {
        List<VendorOrderResponse> response = vendorOrderService.getVendorOrdersByVendorId(vendorId);
        return ResponseEntity.ok(ResponseUtil.success("Vendor orders retrieved successfully", response));
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<RestResponse<VendorOrderResponse>> updateVendorOrderStatus(
            @PathVariable UUID id,
            @RequestParam VendorOrderStatus status) {
        
        VendorOrderResponse response = vendorOrderService.updateVendorOrderStatus(id, status);
        return ResponseEntity.ok(ResponseUtil.success("Vendor order status updated successfully", response));
    }
} 
