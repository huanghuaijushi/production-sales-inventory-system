package com.hhjs.psi.sales.importing.dto;

import jakarta.validation.constraints.NotNull;

public record MatchRequest(
        @NotNull Long itemId,
        @NotNull Long batchId,
        @NotNull Long salesSkuId
) {}
