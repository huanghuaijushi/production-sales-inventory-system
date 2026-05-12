package com.hhjs.psi.production.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductionRouteStepRequest(
        @NotNull Long productId,
        @NotBlank String stepCode,
        @NotBlank String stepName,
        @NotNull @PositiveOrZero Integer sortOrder,
        Boolean allowLoss,
        Boolean enabled
) {
}
