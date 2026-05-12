package com.hhjs.psi.production.dto;

public record ProductionRouteStepResponse(
        Long id,
        Long productId,
        String productCode,
        String productName,
        String stepCode,
        String stepName,
        Integer sortOrder,
        Boolean allowLoss,
        Boolean enabled
) {
}
