package com.hhjs.psi.auth.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UserRoleUpdateRequest(
        @NotEmpty(message = "Role codes cannot be empty")
        List<String> roleCodes
) {
}
