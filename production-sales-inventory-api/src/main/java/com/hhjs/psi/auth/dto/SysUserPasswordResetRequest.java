package com.hhjs.psi.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SysUserPasswordResetRequest(
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password length must be between 8 and 72")
        String password
) {
}
