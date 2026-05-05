package com.hhjs.psi.production.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProductionInboundRequest(
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than 0")
        Integer quantity,
        LocalDate productionDate,
        LocalDate expiryDate,
        @Size(max = 500, message = "Remark must be at most 500 characters")
        String remark
) {
}
