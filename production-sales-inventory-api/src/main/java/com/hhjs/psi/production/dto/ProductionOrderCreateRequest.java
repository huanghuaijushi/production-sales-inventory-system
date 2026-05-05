package com.hhjs.psi.production.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProductionOrderCreateRequest(
        @NotNull(message = "Product is required")
        Long productId,
        @NotNull(message = "Planned quantity is required")
        @Positive(message = "Planned quantity must be greater than 0")
        Integer plannedQuantity,
        @Size(max = 64, message = "Batch number must be at most 64 characters")
        String batchNo,
        LocalDate plannedDate,
        @Size(max = 500, message = "Remark must be at most 500 characters")
        String remark
) {
}
