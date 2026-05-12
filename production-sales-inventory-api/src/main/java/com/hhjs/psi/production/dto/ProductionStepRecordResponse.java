package com.hhjs.psi.production.dto;

import java.time.Instant;

public record ProductionStepRecordResponse(
        Long id,
        String stepType,
        String stepName,
        Integer completedQuantity,
        Integer lossQuantity,
        String lossReason,
        String operatorName,
        Instant createdAt
) {
}
