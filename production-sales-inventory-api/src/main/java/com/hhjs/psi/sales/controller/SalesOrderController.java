package com.hhjs.psi.sales.controller;

import com.hhjs.psi.common.dto.ApiResponse;
import com.hhjs.psi.common.dto.PageResponse;
import com.hhjs.psi.sales.dto.SalesOrderRequest;
import com.hhjs.psi.sales.dto.SalesOrderResponse;
import com.hhjs.psi.sales.service.SalesOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sales/orders")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    public SalesOrderController(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    @GetMapping
    public ApiResponse<PageResponse<SalesOrderResponse>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false, defaultValue = "all") String status
    ) {
        return ApiResponse.ok(PageResponse.from(salesOrderService.getOrders(page, size, query, status)));
    }

    @PostMapping
    public ApiResponse<SalesOrderResponse> createOrder(@Valid @RequestBody SalesOrderRequest request) {
        return ApiResponse.ok(salesOrderService.createOrder(request));
    }

    @PutMapping("/{orderId}")
    public ApiResponse<SalesOrderResponse> updateOrder(@PathVariable Long orderId, @Valid @RequestBody SalesOrderRequest request) {
        return ApiResponse.ok(salesOrderService.updateOrder(orderId, request));
    }

    @PostMapping("/{orderId}/ship")
    public ApiResponse<SalesOrderResponse> shipOrder(@PathVariable Long orderId) {
        return ApiResponse.ok(salesOrderService.shipOrder(orderId));
    }

    @PostMapping("/{orderId}/complete")
    public ApiResponse<SalesOrderResponse> completeOrder(@PathVariable Long orderId) {
        return ApiResponse.ok(salesOrderService.completeOrder(orderId));
    }

    @PostMapping("/{orderId}/cancel")
    public ApiResponse<SalesOrderResponse> cancelOrder(@PathVariable Long orderId) {
        return ApiResponse.ok(salesOrderService.cancelOrder(orderId));
    }
}
