package com.hhjs.psi.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockCheckRequest(
        @NotNull(message = "Product is required")
        Long productId,
        @NotNull(message = "Batch is required")
        Long batchId,
        @NotNull(message = "Actual quantity is required")
        @Min(value = 0, message = "Actual quantity must be greater than or equal to 0")
        Integer actualQuantity,
        @Size(max = 500, message = "Remark must be at most 500 characters")
        String remark
) {
}
