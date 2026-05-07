package com.hhjs.psi.inventory.dto;

import com.hhjs.psi.inventory.entity.StockRecordSubType;
import com.hhjs.psi.inventory.entity.StockRecordType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StockOperationRequest(
        @NotNull(message = "Product is required")
        Long productId,
        StockRecordType type,
        @NotNull(message = "Operation subtype is required")
        StockRecordSubType subType,
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than 0")
        Integer quantity,
        Long relatedOrderId,
        BigDecimal businessUnitPrice,
        Long batchId,
        @Size(max = 64, message = "Batch number must be at most 64 characters")
        String batchNo,
        LocalDate productionDate,
        LocalDate expiryDate,
        @Size(max = 500, message = "Remark must be at most 500 characters")
        String remark
) {
}
