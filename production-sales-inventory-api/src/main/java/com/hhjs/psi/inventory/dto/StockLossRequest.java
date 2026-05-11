package com.hhjs.psi.inventory.dto;

import com.hhjs.psi.inventory.entity.StockRecordSubType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record StockLossRequest(
        @NotNull(message = "Product is required")
        Long productId,
        @NotNull(message = "Batch is required")
        Long batchId,
        @NotNull(message = "Loss type is required")
        StockRecordSubType lossType,
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than 0")
        Integer quantity,
        @Size(max = 500, message = "Remark must be at most 500 characters")
        String remark
) {
}
