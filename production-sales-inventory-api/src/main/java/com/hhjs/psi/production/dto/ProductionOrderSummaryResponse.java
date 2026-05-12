package com.hhjs.psi.production.dto;

import com.hhjs.psi.production.entity.ProductionOrderStatus;

import java.time.Instant;
import java.time.LocalDate;

public record ProductionOrderSummaryResponse(
        Long id,
        String orderNo,
        String batchNo,
        Long productId,
        String productCode,
        String productName,
        String productUnit,
        Integer plannedQuantity,
        Integer completedQuantity,
        Integer inboundQuantity,
        Integer lossQuantity,
        String currentStep,
        ProductionOrderStatus status,
        LocalDate plannedDate,
        Instant startedAt,
        Instant completedAt,
        String operatorName,
        String remark,
        Instant createdAt
) {
}
