package com.hhjs.psi.auth.dto;

public record PermissionOptionResponse(
        Long id,
        String code,
        String name,
        String module,
        String description
) {
}
