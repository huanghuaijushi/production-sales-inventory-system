package com.hhjs.psi.inventory.controller;

import com.hhjs.psi.common.dto.ApiResponse;
import com.hhjs.psi.common.dto.PageResponse;
import com.hhjs.psi.inventory.dto.BusinessFlowTrendItemResponse;
import com.hhjs.psi.inventory.dto.InventoryDashboardResponse;
import com.hhjs.psi.inventory.dto.InventoryValueTrendItemResponse;
import com.hhjs.psi.inventory.dto.MobileHomeResponse;
import com.hhjs.psi.inventory.dto.StockBatchResponse;
import com.hhjs.psi.inventory.dto.StockCheckOrderResponse;
import com.hhjs.psi.inventory.dto.StockCheckRequest;
import com.hhjs.psi.inventory.dto.StockItemResponse;
import com.hhjs.psi.inventory.dto.StockLossRequest;
import com.hhjs.psi.inventory.dto.StockOperationRequest;
import com.hhjs.psi.inventory.dto.StockRecordResponse;
import com.hhjs.psi.inventory.dto.StockTrendItemResponse;
import com.hhjs.psi.inventory.dto.StockUpdateRequest;
import com.hhjs.psi.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<InventoryDashboardResponse> getDashboard() {
        return ApiResponse.ok(inventoryService.getDashboard());
    }

    @GetMapping("/mobile/home")
    public ApiResponse<MobileHomeResponse> getMobileHome() {
        return ApiResponse.ok(inventoryService.getMobileHome());
    }

    @GetMapping("/trends")
    public ApiResponse<List<StockTrendItemResponse>> getStockTrend(
            @RequestParam(defaultValue = "7") int days
    ) {
        return ApiResponse.ok(inventoryService.getStockTrend(days));
    }

    @GetMapping("/business-flow-trends")
    public ApiResponse<List<BusinessFlowTrendItemResponse>> getBusinessFlowTrend(
            @RequestParam(defaultValue = "7") int days
    ) {
        return ApiResponse.ok(inventoryService.getBusinessFlowTrend(days));
    }

    @GetMapping("/value-trends")
    public ApiResponse<List<InventoryValueTrendItemResponse>> getInventoryValueTrend(
            @RequestParam(defaultValue = "7") int days
    ) {
        return ApiResponse.ok(inventoryService.getInventoryValueTrend(days));
    }

    @GetMapping("/stocks")
    public ApiResponse<PageResponse<StockItemResponse>> getAllStocks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.ok(PageResponse.from(inventoryService.getAllStocks(page, size, query, category, status)));
    }

    @GetMapping("/stocks/product/{productId}")
    public ApiResponse<StockItemResponse> getStockByProductId(@PathVariable Long productId) {
        return ApiResponse.ok(inventoryService.getStockByProductId(productId));
    }

    @GetMapping("/batches/product/{productId}")
    public ApiResponse<List<StockBatchResponse>> getBatchesByProductId(@PathVariable Long productId) {
        return ApiResponse.ok(inventoryService.getBatchesByProductId(productId));
    }

    @GetMapping("/batches")
    public ApiResponse<PageResponse<StockBatchResponse>> getAvailableBatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) Integer expiringWithinDays,
            @RequestParam(defaultValue = "true") boolean availableOnly
    ) {
        return ApiResponse.ok(PageResponse.from(
                inventoryService.getAvailableBatches(page, size, expiringWithinDays, availableOnly)
        ));
    }

    @PostMapping("/batches/backfill")
    public ApiResponse<Integer> backfillInboundBatches() {
        return ApiResponse.ok(inventoryService.backfillInboundBatches());
    }

    @PutMapping("/stocks/{stockId}")
    public ApiResponse<StockItemResponse> updateStock(
            @PathVariable Long stockId,
            @Valid @RequestBody StockUpdateRequest request
    ) {
        return ApiResponse.ok(inventoryService.updateStock(stockId, request));
    }

    @PostMapping("/inbound")
    public ApiResponse<StockRecordResponse> inbound(@Valid @RequestBody StockOperationRequest request) {
        return ApiResponse.ok(inventoryService.inbound(request));
    }

    @PostMapping("/outbound")
    public ApiResponse<StockRecordResponse> outbound(@Valid @RequestBody StockOperationRequest request) {
        return ApiResponse.ok(inventoryService.outbound(request));
    }

    @PostMapping("/loss")
    public ApiResponse<StockRecordResponse> reportLoss(@Valid @RequestBody StockLossRequest request) {
        return ApiResponse.ok(inventoryService.reportLoss(request));
    }

    @PostMapping("/check")
    public ApiResponse<StockCheckOrderResponse> quickCheck(@Valid @RequestBody StockCheckRequest request) {
        return ApiResponse.ok(inventoryService.quickCheck(request));
    }

    @GetMapping("/check-orders")
    public ApiResponse<PageResponse<StockCheckOrderResponse>> getStockCheckOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(PageResponse.from(inventoryService.getStockCheckOrders(page, size)));
    }

    @GetMapping("/records")
    public ApiResponse<PageResponse<StockRecordResponse>> getStockRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query
    ) {
        return ApiResponse.ok(PageResponse.from(inventoryService.getStockRecords(page, size, query)));
    }

    @GetMapping("/records/product/{productId}")
    public ApiResponse<List<StockRecordResponse>> getStockRecordsByProduct(@PathVariable Long productId) {
        return ApiResponse.ok(inventoryService.getStockRecordsByProduct(productId));
    }
}
