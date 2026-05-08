package com.hhjs.psi.purchase.controller;

import com.hhjs.psi.common.dto.ApiResponse;
import com.hhjs.psi.common.dto.PageResponse;
import com.hhjs.psi.purchase.dto.PurchaseOrderRequest;
import com.hhjs.psi.purchase.dto.PurchaseOrderResponse;
import com.hhjs.psi.purchase.service.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('purchase:view')")
    @GetMapping
    public ApiResponse<PageResponse<PurchaseOrderResponse>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.ok(PageResponse.from(purchaseOrderService.getOrders(page, size, query, status)));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('purchase:create')")
    @PostMapping
    public ApiResponse<PurchaseOrderResponse> createOrder(@Valid @RequestBody PurchaseOrderRequest request) {
        return ApiResponse.ok(purchaseOrderService.createOrder(request));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('purchase:update')")
    @PutMapping("/{orderId}")
    public ApiResponse<PurchaseOrderResponse> updateOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody PurchaseOrderRequest request
    ) {
        return ApiResponse.ok(purchaseOrderService.updateOrder(orderId, request));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('purchase:inbound')")
    @PostMapping("/{orderId}/inbound")
    public ApiResponse<PurchaseOrderResponse> inbound(@PathVariable Long orderId) {
        return ApiResponse.ok(purchaseOrderService.inbound(orderId));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('purchase:cancel')")
    @PostMapping("/{orderId}/cancel")
    public ApiResponse<PurchaseOrderResponse> cancel(@PathVariable Long orderId) {
        return ApiResponse.ok(purchaseOrderService.cancel(orderId));
    }
}
