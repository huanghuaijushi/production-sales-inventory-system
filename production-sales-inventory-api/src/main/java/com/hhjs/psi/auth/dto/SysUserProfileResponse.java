package com.hhjs.psi.auth.dto;

import com.hhjs.psi.auth.entity.AdminStatus;
import com.hhjs.psi.auth.entity.SysUser;

import java.time.Instant;
import java.util.List;

public record SysUserProfileResponse(
        Long id,
        String username,
        String nickname,
        List<String> roles,
        List<String> permissions,
        AdminStatus status,
        Instant lastLoginAt,
        Instant createdAt
) {

    public static SysUserProfileResponse from(SysUser sysUser) {
        return new SysUserProfileResponse(
                sysUser.getId(),
                sysUser.getUsername(),
                sysUser.getNickname(),
                List.copyOf(sysUser.getRoleCodes()),
                List.copyOf(sysUser.getPermissionCodes()),
                sysUser.getStatus(),
                sysUser.getLastLoginAt(),
                sysUser.getCreatedAt()
        );
    }
}
