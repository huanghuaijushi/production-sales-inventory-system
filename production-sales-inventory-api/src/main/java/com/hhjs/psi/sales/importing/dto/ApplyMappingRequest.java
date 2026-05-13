package com.hhjs.psi.sales.importing.dto;

import jakarta.validation.constraints.NotNull;

public record ApplyMappingRequest(
        @NotNull Long batchId
) {}
