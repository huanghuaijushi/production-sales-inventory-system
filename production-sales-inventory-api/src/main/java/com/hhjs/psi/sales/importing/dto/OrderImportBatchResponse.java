package com.hhjs.psi.sales.importing.dto;

import com.hhjs.psi.sales.importing.entity.OrderImportBatch;
import com.hhjs.psi.sales.importing.entity.ImportSourceType;

import java.time.Instant;
import java.util.List;

public record OrderImportBatchResponse(
        Long id,
        String batchNo,
        Long channelId,
        String channelName,
        String sourceType,
        String fileName,
        Integer totalCount,
        Integer parsedCount,
        Integer readyCount,
        Integer convertedCount,
        Integer errorCount,
        String status,
        String operatorName,
        Instant createdAt,
        Instant updatedAt,
        List<ExternalOrderRawResponse> orders,
        Integer matchedItems,
        Integer unmatchedItems,
        List<SkuSummary> skuSummaries,
        List<ExternalOrderItemRawResponse> items
) {
    public static OrderImportBatchResponse from(OrderImportBatch batch) {
        return from(batch, null);
    }

    public static OrderImportBatchResponse from(OrderImportBatch batch, List<ExternalOrderRawResponse> orders) {
        return new OrderImportBatchResponse(
                batch.getId(),
                batch.getBatchNo(),
                batch.getChannel() != null ? batch.getChannel().getId() : null,
                batch.getChannel() != null ? batch.getChannel().getName() : null,
                batch.getSourceType() != null ? batch.getSourceType().name() : null,
                batch.getFileName(),
                batch.getTotalCount(),
                batch.getParsedCount(),
                batch.getReadyCount(),
                batch.getConvertedCount(),
                batch.getErrorCount(),
                batch.getStatus() != null ? batch.getStatus().name() : null,
                batch.getOperatorName(),
                batch.getCreatedAt(),
                batch.getUpdatedAt(),
                orders,
                null,
                null,
                null,
                null
        );
    }

    public static OrderImportBatchResponse forMatchResult(OrderImportBatch batch,
                                                          Integer totalItems,
                                                          Integer matchedItems,
                                                          Integer unmatchedItems,
                                                          List<SkuSummary> skuSummaries,
                                                          List<ExternalOrderItemRawResponse> items) {
        return new OrderImportBatchResponse(
                batch.getId(),
                batch.getBatchNo(),
                batch.getChannel() != null ? batch.getChannel().getId() : null,
                batch.getChannel() != null ? batch.getChannel().getName() : null,
                batch.getSourceType() != null ? batch.getSourceType().name() : null,
                batch.getFileName(),
                batch.getTotalCount(),
                batch.getParsedCount(),
                batch.getReadyCount(),
                batch.getConvertedCount(),
                batch.getErrorCount(),
                batch.getStatus() != null ? batch.getStatus().name() : null,
                batch.getOperatorName(),
                batch.getCreatedAt(),
                batch.getUpdatedAt(),
                null,
                matchedItems,
                unmatchedItems,
                skuSummaries,
                items
        );
    }

    public record SkuSummary(
            Long skuId,
            String skuName,
            String skuCode,
            String skuSpecName,
            String unit,
            Integer totalQuantity,
            java.math.BigDecimal totalAmount
    ) {}
}
