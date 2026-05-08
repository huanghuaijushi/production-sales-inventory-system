package com.hhjs.psi.auth.dto;

import java.util.List;

public record PermissionGroupResponse(
        String module,
        List<PermissionOptionResponse> permissions
) {
}
