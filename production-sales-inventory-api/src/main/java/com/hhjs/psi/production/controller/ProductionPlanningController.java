package com.hhjs.psi.production.controller;

import com.hhjs.psi.common.dto.ApiResponse;
import com.hhjs.psi.production.dto.BomItemRequest;
import com.hhjs.psi.production.dto.BomItemResponse;
import com.hhjs.psi.production.dto.ProductionCapacityResponse;
import com.hhjs.psi.production.dto.ProductionInboundRequest;
import com.hhjs.psi.production.dto.ProductionMaterialIssueRequest;
import com.hhjs.psi.production.dto.ProductionOrderCreateRequest;
import com.hhjs.psi.production.dto.ProductionOrderDetailResponse;
import com.hhjs.psi.production.dto.ProductionOrderSummaryResponse;
import com.hhjs.psi.production.dto.ProductionStepRecordRequest;
import com.hhjs.psi.production.dto.ProductionSuggestionResponse;
import com.hhjs.psi.production.dto.PurchaseSuggestionGroupResponse;
import com.hhjs.psi.production.dto.SupplierMaterialRequest;
import com.hhjs.psi.production.dto.SupplierMaterialResponse;
import com.hhjs.psi.production.service.ProductionPlanningService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/production")
public class ProductionPlanningController {

    private final ProductionPlanningService productionPlanningService;

    public ProductionPlanningController(ProductionPlanningService productionPlanningService) {
        this.productionPlanningService = productionPlanningService;
    }

    @GetMapping("/orders")
    public ApiResponse<List<ProductionOrderSummaryResponse>> getProductionOrders() {
        return ApiResponse.ok(productionPlanningService.getProductionOrders());
    }

    @GetMapping("/orders/{productionOrderId}")
    public ApiResponse<ProductionOrderDetailResponse> getProductionOrder(@PathVariable Long productionOrderId) {
        return ApiResponse.ok(productionPlanningService.getProductionOrder(productionOrderId));
    }

    @PostMapping("/orders")
    public ApiResponse<ProductionOrderDetailResponse> createProductionOrder(
            @Valid @RequestBody ProductionOrderCreateRequest request
    ) {
        return ApiResponse.ok(productionPlanningService.createProductionOrder(request));
    }

    @PostMapping("/orders/{productionOrderId}/start")
    public ApiResponse<ProductionOrderDetailResponse> startProductionOrder(@PathVariable Long productionOrderId) {
        return ApiResponse.ok(productionPlanningService.startProductionOrder(productionOrderId));
    }

    @PostMapping("/orders/{productionOrderId}/materials/issue")
    public ApiResponse<ProductionOrderDetailResponse> issueProductionMaterial(
            @PathVariable Long productionOrderId,
            @Valid @RequestBody ProductionMaterialIssueRequest request
    ) {
        return ApiResponse.ok(productionPlanningService.issueProductionMaterial(productionOrderId, request));
    }

    @PostMapping("/orders/{productionOrderId}/steps")
    public ApiResponse<ProductionOrderDetailResponse> recordProductionStep(
            @PathVariable Long productionOrderId,
            @Valid @RequestBody ProductionStepRecordRequest request
    ) {
        return ApiResponse.ok(productionPlanningService.recordProductionStep(productionOrderId, request));
    }

    @PostMapping("/orders/{productionOrderId}/inbound")
    public ApiResponse<ProductionOrderDetailResponse> inboundProduction(
            @PathVariable Long productionOrderId,
            @Valid @RequestBody ProductionInboundRequest request
    ) {
        return ApiResponse.ok(productionPlanningService.inboundProduction(productionOrderId, request));
    }

    @PostMapping("/orders/{productionOrderId}/cancel")
    public ApiResponse<ProductionOrderDetailResponse> cancelProductionOrder(@PathVariable Long productionOrderId) {
        return ApiResponse.ok(productionPlanningService.cancelProductionOrder(productionOrderId));
    }

    @GetMapping("/bom")
    public ApiResponse<List<BomItemResponse>> getBomItems() {
        return ApiResponse.ok(productionPlanningService.getBomItems());
    }

    @PostMapping("/bom")
    public ApiResponse<BomItemResponse> createBomItem(@Valid @RequestBody BomItemRequest request) {
        return ApiResponse.ok(productionPlanningService.createBomItem(request));
    }

    @PutMapping("/bom/{bomItemId}")
    public ApiResponse<BomItemResponse> updateBomItem(
            @PathVariable Long bomItemId,
            @Valid @RequestBody BomItemRequest request
    ) {
        return ApiResponse.ok(productionPlanningService.updateBomItem(bomItemId, request));
    }

    @DeleteMapping("/bom/{bomItemId}")
    public ApiResponse<Void> deleteBomItem(@PathVariable Long bomItemId) {
        productionPlanningService.deleteBomItem(bomItemId);
        return ApiResponse.ok();
    }

    @GetMapping("/supplier-materials")
    public ApiResponse<List<SupplierMaterialResponse>> getSupplierMaterials() {
        return ApiResponse.ok(productionPlanningService.getSupplierMaterials());
    }

    @PostMapping("/supplier-materials")
    public ApiResponse<SupplierMaterialResponse> createSupplierMaterial(@Valid @RequestBody SupplierMaterialRequest request) {
        return ApiResponse.ok(productionPlanningService.createSupplierMaterial(request));
    }

    @PutMapping("/supplier-materials/{supplierMaterialId}")
    public ApiResponse<SupplierMaterialResponse> updateSupplierMaterial(
            @PathVariable Long supplierMaterialId,
            @Valid @RequestBody SupplierMaterialRequest request
    ) {
        return ApiResponse.ok(productionPlanningService.updateSupplierMaterial(supplierMaterialId, request));
    }

    @DeleteMapping("/supplier-materials/{supplierMaterialId}")
    public ApiResponse<Void> deleteSupplierMaterial(@PathVariable Long supplierMaterialId) {
        productionPlanningService.deleteSupplierMaterial(supplierMaterialId);
        return ApiResponse.ok();
    }

    @GetMapping("/capacity")
    public ApiResponse<List<ProductionCapacityResponse>> getProductionCapacity() {
        return ApiResponse.ok(productionPlanningService.getProductionCapacity());
    }

    @GetMapping("/suggestions")
    public ApiResponse<List<ProductionSuggestionResponse>> getProductionSuggestions() {
        return ApiResponse.ok(productionPlanningService.getProductionSuggestions());
    }

    @GetMapping("/purchase-suggestions")
    public ApiResponse<List<PurchaseSuggestionGroupResponse>> getPurchaseSuggestionGroups() {
        return ApiResponse.ok(productionPlanningService.getPurchaseSuggestionGroups());
    }
}
