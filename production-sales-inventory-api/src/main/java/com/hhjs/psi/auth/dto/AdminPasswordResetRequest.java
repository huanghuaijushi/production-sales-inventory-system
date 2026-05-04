package com.hhjs.psi.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminPasswordResetRequest(
        @NotBlank
        @Size(min = 8, max = 72)
        String password
) {
}
