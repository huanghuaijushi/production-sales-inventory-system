package com.hhjs.psi.production.dto;

public record ProductionMaterialPlanResponse(
        Long id,
        Long materialProductId,
        String materialProductCode,
        String materialProductName,
        String materialProductUnit,
        Integer requiredQuantity,
        Integer issuedQuantity
) {
}
