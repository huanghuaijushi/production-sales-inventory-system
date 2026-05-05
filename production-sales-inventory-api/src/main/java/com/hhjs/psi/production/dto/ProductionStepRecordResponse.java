package com.hhjs.psi.production.dto;

import com.hhjs.psi.production.entity.ProductionStepType;

import java.time.Instant;

public record ProductionStepRecordResponse(
        Long id,
        ProductionStepType stepType,
        Integer completedQuantity,
        Integer lossQuantity,
        String lossReason,
        String operatorName,
        Instant createdAt
) {
}
