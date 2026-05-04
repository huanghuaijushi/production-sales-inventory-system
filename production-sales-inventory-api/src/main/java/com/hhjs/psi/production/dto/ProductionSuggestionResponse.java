package com.hhjs.psi.production.dto;

import java.util.List;

public record ProductionSuggestionResponse(
        Long finishedProductId,
        String finishedProductCode,
        String finishedProductName,
        String finishedProductUnit,
        Integer currentStock,
        Integer availableStock,
        Integer alertQuantity,
        Integer suggestedProductionQuantity,
        Integer maxProducibleQuantity,
        Boolean canProduceNow,
        String suggestionReason,
        List<ProductionMaterialRequirementResponse> materialRequirements
) {
}
