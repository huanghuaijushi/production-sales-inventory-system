package com.hhjs.psi.production.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductionStepRecordRequest(
        @NotNull(message = "Loss quantity is required")
        @Min(value = 0, message = "Loss quantity cannot be negative")
        Integer lossQuantity,
        @Size(max = 500, message = "Loss reason must be at most 500 characters")
        String lossReason
) {
}
