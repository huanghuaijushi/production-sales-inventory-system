package com.hhjs.psi.auth.dto;

import java.util.List;

public record RoleDetailResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean enabled,
        Integer sortOrder,
        List<String> permissionCodes
) {
}
