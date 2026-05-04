package com.hhjs.psi.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminCreateRequest(
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9_]{3,50}$")
        String username,

        @NotBlank
        @Size(max = 80)
        String nickname,

        @NotBlank
        @Size(min = 8, max = 72)
        String password
) {
}
