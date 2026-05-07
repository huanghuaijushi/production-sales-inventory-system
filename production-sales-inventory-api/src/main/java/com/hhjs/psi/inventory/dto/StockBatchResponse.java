package com.hhjs.psi.inventory.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record StockBatchResponse(
        Long id,
        Long productId,
        String productCode,
        String productName,
        String productUnit,
        String batchNo,
        LocalDate productionDate,
        LocalDate expiryDate,
        Integer quantity,
        Integer availableQuantity,
        BigDecimal unitCost,
        String remark,
        Instant createdAt,
        Instant updatedAt
) {
}
