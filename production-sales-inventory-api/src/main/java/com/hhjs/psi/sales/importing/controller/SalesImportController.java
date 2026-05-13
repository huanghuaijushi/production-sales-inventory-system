package com.hhjs.psi.sales.importing.controller;

import com.hhjs.psi.common.dto.PageResponse;
import com.hhjs.psi.sales.importing.dto.ApplyMappingRequest;
import com.hhjs.psi.sales.importing.dto.ExternalOrderEditRequest;
import com.hhjs.psi.sales.importing.dto.ExternalOrderItemRawResponse;
import com.hhjs.psi.sales.importing.dto.ExternalOrderRawResponse;
import com.hhjs.psi.sales.importing.dto.ImportedOrderResponse;
import com.hhjs.psi.sales.importing.dto.MatchRequest;
import com.hhjs.psi.sales.importing.dto.OrderImportBatchResponse;
import com.hhjs.psi.sales.importing.dto.PddExcelImportRequest;
import com.hhjs.psi.sales.importing.dto.TextImportRequest;
import com.hhjs.psi.sales.importing.service.OrderImportService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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
@RequestMapping("/api/v1/sales/imports")
public class SalesImportController {

    private final OrderImportService orderImportService;

    public SalesImportController(OrderImportService orderImportService) {
        this.orderImportService = orderImportService;
    }

    @GetMapping
    public PageResponse<OrderImportBatchResponse> getBatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<OrderImportBatchResponse> result = orderImportService.getBatches(page, size);
        return PageResponse.from(result);
    }

    @GetMapping("/{batchId}")
    public OrderImportBatchResponse getBatch(@PathVariable Long batchId) {
        return orderImportService.getBatch(batchId);
    }

    @GetMapping("/{batchId}/orders/{orderId}")
    public ExternalOrderRawResponse getImportOrder(@PathVariable Long batchId, @PathVariable Long orderId) {
        return orderImportService.getImportOrder(orderId);
    }

    @GetMapping("/{batchId}/unmapped")
    public List<ExternalOrderItemRawResponse> getUnmappedItems(@PathVariable Long batchId) {
        return orderImportService.getUnmappedItems(batchId);
    }

    @GetMapping("/{batchId}/result")
    public OrderImportBatchResponse getBatchResult(@PathVariable Long batchId) {
        return orderImportService.getBatchResult(batchId);
    }

    @PostMapping("/{batchId}/apply-mapping")
    public List<ExternalOrderItemRawResponse> applyMapping(@PathVariable Long batchId) {
        return orderImportService.applyMapping(new ApplyMappingRequest(batchId));
    }

    @PostMapping("/{batchId}/match")
    public ExternalOrderItemRawResponse manualMatch(@PathVariable Long batchId, @Valid @RequestBody MatchRequest request) {
        return orderImportService.manualMatch(request);
    }

    @PostMapping("/{batchId}/confirm")
    public ImportedOrderResponse confirmBatch(@PathVariable Long batchId) {
        return orderImportService.confirmBatch(batchId);
    }

    @PostMapping("/text")
    public OrderImportBatchResponse importText(@Valid @RequestBody TextImportRequest request) {
        return orderImportService.importText(request);
    }

    @PostMapping("/wechat-text")
    public OrderImportBatchResponse importWechatText(@Valid @RequestBody TextImportRequest request) {
        return orderImportService.importText(request);
    }

    @PostMapping("/pdd-excel")
    public OrderImportBatchResponse importPddExcel(@Valid @RequestBody PddExcelImportRequest request) {
        return orderImportService.importPddExcelText(request);
    }

    @PostMapping("/{batchId}/parse")
    public OrderImportBatchResponse rematch(@PathVariable Long batchId) {
        return orderImportService.rematchBatch(batchId);
    }

    @PutMapping("/{batchId}/orders/{externalOrderId}")
    public OrderImportBatchResponse updateExternalOrder(
            @PathVariable Long batchId,
            @PathVariable Long externalOrderId,
            @Valid @RequestBody ExternalOrderEditRequest request
    ) {
        return orderImportService.updateExternalOrder(batchId, externalOrderId, request);
    }
}
