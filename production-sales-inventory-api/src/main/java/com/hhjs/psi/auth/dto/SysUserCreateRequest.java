package com.hhjs.psi.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SysUserCreateRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username length must be between 3 and 50")
        @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Username can only contain letters, numbers and underscores")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password length must be between 8 and 72")
        String password,

        @NotBlank(message = "Nickname is required")
        @Size(max = 80, message = "Nickname length must be less than or equal to 80")
        String nickname
) {
}
