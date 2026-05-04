package com.hhjs.psi.production.dto;

import java.util.List;

public record ProductionCapacityResponse(
        Long finishedProductId,
        String finishedProductCode,
        String finishedProductName,
        String finishedProductUnit,
        Integer currentStock,
        Integer availableStock,
        Integer alertQuantity,
        Boolean hasBom,
        Integer maxProducibleQuantity,
        String bottleneckMaterialName,
        List<ProductionMaterialCapacityResponse> materials
) {
}
