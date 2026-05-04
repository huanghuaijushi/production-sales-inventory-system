package com.hhjs.psi.production.dto;

import java.math.BigDecimal;

public record BomItemResponse(
        Long id,
        Long finishedProductId,
        String finishedProductCode,
        String finishedProductName,
        Long materialProductId,
        String materialProductCode,
        String materialProductName,
        String materialProductUnit,
        BigDecimal quantityPerUnit,
        BigDecimal lossRate
) {
}
