package com.hhjs.psi.sales.importing.dto;

import com.hhjs.psi.sales.importing.entity.OrderImportBatch;

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
        List<ExternalOrderRawResponse> orders
) {
    public static OrderImportBatchResponse from(OrderImportBatch batch, List<ExternalOrderRawResponse> orders) {
        return new OrderImportBatchResponse(
                batch.getId(),
                batch.getBatchNo(),
                batch.getChannel().getId(),
                batch.getChannel().getName(),
                batch.getSourceType().name(),
                batch.getFileName(),
                batch.getTotalCount(),
                batch.getParsedCount(),
                batch.getReadyCount(),
                batch.getConvertedCount(),
                batch.getErrorCount(),
                batch.getStatus().name(),
                batch.getOperatorName(),
                batch.getCreatedAt(),
                batch.getUpdatedAt(),
                orders
        );
    }
}
