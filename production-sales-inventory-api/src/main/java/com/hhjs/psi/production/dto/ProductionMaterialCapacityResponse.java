package com.hhjs.psi.production.dto;

import java.math.BigDecimal;

public record ProductionMaterialCapacityResponse(
        Long materialProductId,
        String materialProductCode,
        String materialProductName,
        String materialProductUnit,
        BigDecimal quantityPerUnit,
        BigDecimal lossRate,
        Integer availableQuantity,
        Integer maxSupportQuantity
) {
}
