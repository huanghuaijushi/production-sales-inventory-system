package com.hhjs.psi.inventory.dto;

import java.time.LocalDate;

public record ExpiringBatchItem(
        Long batchId,
        Long productId,
        String productName,
        String batchNo,
        LocalDate expiryDate,
        Integer daysToExpire,
        Integer availableQuantity,
        String productUnit
) {
}
