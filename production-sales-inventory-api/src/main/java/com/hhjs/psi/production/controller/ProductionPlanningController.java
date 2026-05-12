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
import com.hhjs.psi.production.dto.ProductionRouteStepRequest;
import com.hhjs.psi.production.dto.ProductionRouteStepResponse;
import com.hhjs.psi.production.dto.ProductionStepRecordRequest;
import com.hhjs.psi.production.dto.ProductionSuggestionResponse;
import com.hhjs.psi.production.dto.PurchaseSuggestionGroupResponse;
import com.hhjs.psi.production.dto.SupplierMaterialRequest;
import com.hhjs.psi.production.dto.SupplierMaterialResponse;
import com.hhjs.psi.production.service.ProductionPlanningService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasAuthority('*') or hasAuthority('production:view')")
    @GetMapping("/orders")
    public ApiResponse<List<ProductionOrderSummaryResponse>> getProductionOrders() {
        return ApiResponse.ok(productionPlanningService.getProductionOrders());
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('production:view')")
    @GetMapping("/orders/{productionOrderId}")
    public ApiResponse<ProductionOrderDetailResponse> getProductionOrder(@PathVariable Long productionOrderId) {
        return ApiResponse.ok(productionPlanningService.getProductionOrder(productionOrderId));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('production:create')")
    @PostMapping("/orders")
    public ApiResponse<ProductionOrderDetailResponse> createProductionOrder(
            @Valid @RequestBody ProductionOrderCreateRequest request
    ) {
        return ApiResponse.ok(productionPlanningService.createProductionOrder(request));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('production:start')")
    @PostMapping("/orders/{productionOrderId}/start")
    public ApiResponse<ProductionOrderDetailResponse> startProductionOrder(@PathVariable Long productionOrderId) {
        return ApiResponse.ok(productionPlanningService.startProductionOrder(productionOrderId));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('production:material-issue')")
    @PostMapping("/orders/{productionOrderId}/materials/issue")
    public ApiResponse<ProductionOrderDetailResponse> issueProductionMaterial(
            @PathVariable Long productionOrderId,
            @Valid @RequestBody ProductionMaterialIssueRequest request
    ) {
        return ApiResponse.ok(productionPlanningService.issueProductionMaterial(productionOrderId, request));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('production:step-report')")
    @PostMapping("/orders/{productionOrderId}/steps")
    public ApiResponse<ProductionOrderDetailResponse> recordProductionStep(
            @PathVariable Long productionOrderId,
            @Valid @RequestBody ProductionStepRecordRequest request
    ) {
        return ApiResponse.ok(productionPlanningService.recordProductionStep(productionOrderId, request));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('production:inbound')")
    @PostMapping("/orders/{productionOrderId}/inbound")
    public ApiResponse<ProductionOrderDetailResponse> inboundProduction(
            @PathVariable Long productionOrderId,
            @Valid @RequestBody ProductionInboundRequest request
    ) {
        return ApiResponse.ok(productionPlanningService.inboundProduction(productionOrderId, request));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('production:cancel')")
    @PostMapping("/orders/{productionOrderId}/cancel")
    public ApiResponse<ProductionOrderDetailResponse> cancelProductionOrder(@PathVariable Long productionOrderId) {
        return ApiResponse.ok(productionPlanningService.cancelProductionOrder(productionOrderId));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('production:view')")
    @GetMapping("/route-steps")
    public ApiResponse<List<ProductionRouteStepResponse>> getProductionRouteSteps() {
        return ApiResponse.ok(productionPlanningService.getProductionRouteSteps());
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('production:create')")
    @PostMapping("/route-steps")
    public ApiResponse<ProductionRouteStepResponse> createProductionRouteStep(
            @Valid @RequestBody ProductionRouteStepRequest request
    ) {
        return ApiResponse.ok(productionPlanningService.createProductionRouteStep(request));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('production:step-report')")
    @PutMapping("/route-steps/{routeStepId}")
    public ApiResponse<ProductionRouteStepResponse> updateProductionRouteStep(
            @PathVariable Long routeStepId,
            @Valid @RequestBody ProductionRouteStepRequest request
    ) {
        return ApiResponse.ok(productionPlanningService.updateProductionRouteStep(routeStepId, request));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('production:cancel')")
    @DeleteMapping("/route-steps/{routeStepId}")
    public ApiResponse<Void> deleteProductionRouteStep(@PathVariable Long routeStepId) {
        productionPlanningService.deleteProductionRouteStep(routeStepId);
        return ApiResponse.ok();
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('product:view')")
    @GetMapping("/bom")
    public ApiResponse<List<BomItemResponse>> getBomItems() {
        return ApiResponse.ok(productionPlanningService.getBomItems());
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('product:create')")
    @PostMapping("/bom")
    public ApiResponse<BomItemResponse> createBomItem(@Valid @RequestBody BomItemRequest request) {
        return ApiResponse.ok(productionPlanningService.createBomItem(request));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('product:update')")
    @PutMapping("/bom/{bomItemId}")
    public ApiResponse<BomItemResponse> updateBomItem(
            @PathVariable Long bomItemId,
            @Valid @RequestBody BomItemRequest request
    ) {
        return ApiResponse.ok(productionPlanningService.updateBomItem(bomItemId, request));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('product:delete')")
    @DeleteMapping("/bom/{bomItemId}")
    public ApiResponse<Void> deleteBomItem(@PathVariable Long bomItemId) {
        productionPlanningService.deleteBomItem(bomItemId);
        return ApiResponse.ok();
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('purchase:view')")
    @GetMapping("/supplier-materials")
    public ApiResponse<List<SupplierMaterialResponse>> getSupplierMaterials() {
        return ApiResponse.ok(productionPlanningService.getSupplierMaterials());
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('purchase:create')")
    @PostMapping("/supplier-materials")
    public ApiResponse<SupplierMaterialResponse> createSupplierMaterial(@Valid @RequestBody SupplierMaterialRequest request) {
        return ApiResponse.ok(productionPlanningService.createSupplierMaterial(request));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('purchase:update')")
    @PutMapping("/supplier-materials/{supplierMaterialId}")
    public ApiResponse<SupplierMaterialResponse> updateSupplierMaterial(
            @PathVariable Long supplierMaterialId,
            @Valid @RequestBody SupplierMaterialRequest request
    ) {
        return ApiResponse.ok(productionPlanningService.updateSupplierMaterial(supplierMaterialId, request));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('purchase:cancel')")
    @DeleteMapping("/supplier-materials/{supplierMaterialId}")
    public ApiResponse<Void> deleteSupplierMaterial(@PathVariable Long supplierMaterialId) {
        productionPlanningService.deleteSupplierMaterial(supplierMaterialId);
        return ApiResponse.ok();
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('production:view')")
    @GetMapping("/capacity")
    public ApiResponse<List<ProductionCapacityResponse>> getProductionCapacity() {
        return ApiResponse.ok(productionPlanningService.getProductionCapacity());
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('production:view')")
    @GetMapping("/suggestions")
    public ApiResponse<List<ProductionSuggestionResponse>> getProductionSuggestions() {
        return ApiResponse.ok(productionPlanningService.getProductionSuggestions());
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('production:view')")
    @GetMapping("/purchase-suggestions")
    public ApiResponse<List<PurchaseSuggestionGroupResponse>> getPurchaseSuggestionGroups() {
        return ApiResponse.ok(productionPlanningService.getPurchaseSuggestionGroups());
    }
}
