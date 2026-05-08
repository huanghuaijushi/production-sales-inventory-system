package com.hhjs.psi.auth.controller;

import com.hhjs.psi.auth.dto.PermissionOptionResponse;
import com.hhjs.psi.auth.dto.RoleOptionResponse;
import com.hhjs.psi.auth.dto.SysUserCreateRequest;
import com.hhjs.psi.auth.dto.SysUserPasswordResetRequest;
import com.hhjs.psi.auth.dto.SysUserProfileResponse;
import com.hhjs.psi.auth.dto.UserRoleUpdateRequest;
import com.hhjs.psi.auth.service.SysUserManagementService;
import com.hhjs.psi.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sys-users")
public class SysUserManagementController {

    private final SysUserManagementService sysUserManagementService;

    public SysUserManagementController(SysUserManagementService sysUserManagementService) {
        this.sysUserManagementService = sysUserManagementService;
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('auth:user:view')")
    @GetMapping
    public ApiResponse<Page<SysUserProfileResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String query
    ) {
        return ApiResponse.ok(sysUserManagementService.getUsers(page, size, query));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('auth:user:view')")
    @GetMapping("/role-options")
    public ApiResponse<List<RoleOptionResponse>> getRoleOptions() {
        return ApiResponse.ok(sysUserManagementService.getRoleOptions());
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('auth:user:view')")
    @GetMapping("/permission-options")
    public ApiResponse<List<PermissionOptionResponse>> getPermissionOptions() {
        return ApiResponse.ok(sysUserManagementService.getPermissionOptions());
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('auth:user:create')")
    @PostMapping
    public ApiResponse<SysUserProfileResponse> createUser(@Valid @RequestBody SysUserCreateRequest request) {
        return ApiResponse.ok(sysUserManagementService.createUser(request));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('auth:user:update')")
    @PatchMapping("/{userId}/roles")
    public ApiResponse<SysUserProfileResponse> updateUserRoles(
            @PathVariable Long userId,
            @Valid @RequestBody UserRoleUpdateRequest request
    ) {
        return ApiResponse.ok(sysUserManagementService.updateUserRoles(userId, request));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('auth:user:update')")
    @PatchMapping("/{userId}/activate")
    public ApiResponse<SysUserProfileResponse> activateUser(@PathVariable Long userId) {
        return ApiResponse.ok(sysUserManagementService.activateUser(userId));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('auth:user:disable')")
    @PatchMapping("/{userId}/disable")
    public ApiResponse<SysUserProfileResponse> disableUser(@PathVariable Long userId) {
        return ApiResponse.ok(sysUserManagementService.disableUser(userId));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('auth:user:update')")
    @PatchMapping("/{userId}/password")
    public ApiResponse<SysUserProfileResponse> resetPassword(
            @PathVariable Long userId,
            @Valid @RequestBody SysUserPasswordResetRequest request
    ) {
        return ApiResponse.ok(sysUserManagementService.resetPassword(userId, request));
    }
}
