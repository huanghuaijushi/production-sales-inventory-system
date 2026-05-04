package com.hhjs.psi.production.dto;

import java.math.BigDecimal;

public record ProductionMaterialRequirementResponse(
        Long materialProductId,
        String materialProductCode,
        String materialProductName,
        String materialProductUnit,
        Integer requiredQuantity,
        Integer availableQuantity,
        Integer onOrderQuantity,
        Integer shortageQuantity,
        Long defaultSupplierId,
        String defaultSupplierName,
        BigDecimal defaultUnitPrice,
        Integer suggestedPurchaseQuantity,
        Integer leadTimeDays,
        String suggestionReason
) {
}
