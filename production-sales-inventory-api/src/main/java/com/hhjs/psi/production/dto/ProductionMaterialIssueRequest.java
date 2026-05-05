package com.hhjs.psi.production.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProductionMaterialIssueRequest(
        @NotNull(message = "Material plan is required")
        Long materialPlanId,
        @NotNull(message = "Batch is required")
        Long batchId,
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than 0")
        Integer quantity,
        @Size(max = 500, message = "Remark must be at most 500 characters")
        String remark
) {
}
