package com.hhjs.psi.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RoleUpsertRequest(
        @NotBlank(message = "Role code is required")
        @Size(max = 64, message = "Role code length must be less than or equal to 64")
        String code,

        @NotBlank(message = "Role name is required")
        @Size(max = 80, message = "Role name length must be less than or equal to 80")
        String name,

        @Size(max = 255, message = "Role description length must be less than or equal to 255")
        String description,

        @NotNull(message = "Enabled flag is required")
        Boolean enabled,

        Integer sortOrder,

        List<String> permissionCodes
) {
}
