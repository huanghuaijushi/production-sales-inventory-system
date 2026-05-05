package com.hhjs.psi.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InventoryValueTrendItemResponse(
        LocalDate date,
        String label,
        BigDecimal totalAmount,
        BigDecimal rawMaterialInboundAmount,
        BigDecimal rawMaterialUsageAmount,
        BigDecimal finishedProductInboundAmount,
        BigDecimal finishedProductSalesAmount
) {
}
