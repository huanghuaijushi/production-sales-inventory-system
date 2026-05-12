package com.hhjs.psi.production.dto;

public record ProductionOrderStepResponse(
        Long id,
        String stepCode,
        String stepName,
        Integer sortOrder,
        Boolean allowLoss,
        Integer completedQuantity,
        Integer lossQuantity
) {
}
