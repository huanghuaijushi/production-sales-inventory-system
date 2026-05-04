package com.hhjs.psi.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminLoginRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 50, message = "Username length must be less than or equal to 50")
        String username,

        @NotBlank(message = "Password is required")
        @Size(max = 72, message = "Password length must be less than or equal to 72")
        String password
) {
}
