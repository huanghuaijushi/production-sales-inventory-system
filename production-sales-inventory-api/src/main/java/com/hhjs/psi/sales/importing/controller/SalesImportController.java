package com.hhjs.psi.sales.importing.controller;

import com.hhjs.psi.common.dto.ApiResponse;
import com.hhjs.psi.common.dto.PageResponse;
import com.hhjs.psi.sales.importing.dto.ChannelProductMappingRequest;
import com.hhjs.psi.sales.importing.dto.ChannelProductMappingResponse;
import com.hhjs.psi.sales.importing.dto.ExternalOrderEditRequest;
import com.hhjs.psi.sales.importing.dto.OrderImportBatchResponse;
import com.hhjs.psi.sales.importing.dto.PddExcelImportRequest;
import com.hhjs.psi.sales.importing.dto.SalesChannelConfigRequest;
import com.hhjs.psi.sales.importing.dto.SalesChannelConfigResponse;
import com.hhjs.psi.sales.importing.dto.TextImportRequest;
import com.hhjs.psi.sales.importing.service.ChannelProductMappingService;
import com.hhjs.psi.sales.importing.service.OrderImportService;
import com.hhjs.psi.sales.importing.service.SalesChannelConfigService;
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
@RequestMapping("/api/v1/sales")
public class SalesImportController {

    private final SalesChannelConfigService channelService;
    private final ChannelProductMappingService mappingService;
    private final OrderImportService importService;

    public SalesImportController(SalesChannelConfigService channelService, ChannelProductMappingService mappingService, OrderImportService importService) {
        this.channelService = channelService;
        this.mappingService = mappingService;
        this.importService = importService;
    }

    @GetMapping("/channels")
    public ApiResponse<List<SalesChannelConfigResponse>> getChannels(@RequestParam(defaultValue = "false") boolean enabledOnly) {
        return ApiResponse.ok(channelService.getChannels(enabledOnly));
    }

    @PostMapping("/channels")
    public ApiResponse<SalesChannelConfigResponse> createChannel(@Valid @RequestBody SalesChannelConfigRequest request) {
        return ApiResponse.ok(channelService.create(request));
    }

    @PutMapping("/channels/{id}")
    public ApiResponse<SalesChannelConfigResponse> updateChannel(@PathVariable Long id, @Valid @RequestBody SalesChannelConfigRequest request) {
        return ApiResponse.ok(channelService.update(id, request));
    }

    @GetMapping("/product-mappings")
    public ApiResponse<List<ChannelProductMappingResponse>> getMappings(@RequestParam(required = false) Long channelId) {
        return ApiResponse.ok(mappingService.getMappings(channelId));
    }

    @PostMapping("/product-mappings")
    public ApiResponse<ChannelProductMappingResponse> createMapping(@Valid @RequestBody ChannelProductMappingRequest request) {
        return ApiResponse.ok(mappingService.create(request));
    }

    @PutMapping("/product-mappings/{id}")
    public ApiResponse<ChannelProductMappingResponse> updateMapping(@PathVariable Long id, @Valid @RequestBody ChannelProductMappingRequest request) {
        return ApiResponse.ok(mappingService.update(id, request));
    }

    @GetMapping("/imports")
    public ApiResponse<PageResponse<OrderImportBatchResponse>> getBatches(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(PageResponse.from(importService.getBatches(page, size)));
    }

    @GetMapping("/imports/{batchId}")
    public ApiResponse<OrderImportBatchResponse> getBatch(@PathVariable Long batchId) {
        return ApiResponse.ok(importService.getBatch(batchId));
    }

    @PostMapping("/imports/text")
    public ApiResponse<OrderImportBatchResponse> importText(@Valid @RequestBody TextImportRequest request) {
        return ApiResponse.ok(importService.importText(request));
    }

    @PostMapping("/imports/wechat-text")
    public ApiResponse<OrderImportBatchResponse> importWechatText(@Valid @RequestBody TextImportRequest request) {
        return ApiResponse.ok(importService.importText(request));
    }

    @PostMapping("/imports/pdd-excel")
    public ApiResponse<OrderImportBatchResponse> importPddExcel(@Valid @RequestBody PddExcelImportRequest request) {
        return ApiResponse.ok(importService.importPddExcelText(request));
    }

    @PostMapping("/imports/{batchId}/parse")
    public ApiResponse<OrderImportBatchResponse> rematchBatch(@PathVariable Long batchId) {
        return ApiResponse.ok(importService.rematchBatch(batchId));
    }

    @PutMapping("/imports/{batchId}/orders/{externalOrderId}")
    public ApiResponse<OrderImportBatchResponse> updateExternalOrder(@PathVariable Long batchId, @PathVariable Long externalOrderId, @Valid @RequestBody ExternalOrderEditRequest request) {
        return ApiResponse.ok(importService.updateExternalOrder(batchId, externalOrderId, request));
    }

    @PostMapping("/imports/{batchId}/confirm")
    public ApiResponse<OrderImportBatchResponse> confirmBatch(@PathVariable Long batchId) {
        return ApiResponse.ok(importService.confirmBatch(batchId));
    }
}
