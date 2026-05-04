package com.hhjs.psi.auth.dto;

import com.hhjs.psi.auth.entity.AdminStatus;
import com.hhjs.psi.auth.entity.AdminUser;

import java.time.Instant;
import java.util.List;

public record AdminProfileResponse(
        Long id,
        String username,
        String nickname,
        List<String> roles,
        List<String> permissions,
        AdminStatus status,
        Instant lastLoginAt,
        Instant createdAt
) {

    public static AdminProfileResponse from(AdminUser adminUser) {
        return new AdminProfileResponse(
                adminUser.getId(),
                adminUser.getUsername(),
                adminUser.getNickname(),
                List.copyOf(adminUser.getRoleCodes()),
                List.copyOf(adminUser.getPermissionCodes()),
                adminUser.getStatus(),
                adminUser.getLastLoginAt(),
                adminUser.getCreatedAt()
        );
    }
}
