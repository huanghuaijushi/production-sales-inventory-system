package com.hhjs.psi.auth.controller;

import com.hhjs.psi.auth.dto.PermissionGroupResponse;
import com.hhjs.psi.auth.dto.PermissionOptionResponse;
import com.hhjs.psi.auth.dto.RoleDetailResponse;
import com.hhjs.psi.auth.dto.RoleOptionResponse;
import com.hhjs.psi.auth.dto.RoleUpsertRequest;
import com.hhjs.psi.auth.service.RoleManagementService;
import com.hhjs.psi.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleManagementController {

    private final RoleManagementService roleManagementService;

    public RoleManagementController(RoleManagementService roleManagementService) {
        this.roleManagementService = roleManagementService;
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('auth:role:view')")
    @GetMapping
    public ApiResponse<List<RoleOptionResponse>> getRoles() {
        return ApiResponse.ok(roleManagementService.getRoles());
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('auth:role:view')")
    @GetMapping("/{roleId}")
    public ApiResponse<RoleDetailResponse> getRole(@PathVariable Long roleId) {
        return ApiResponse.ok(roleManagementService.getRole(roleId));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('auth:role:view')")
    @GetMapping("/permission-groups")
    public ApiResponse<List<PermissionGroupResponse>> getPermissionGroups() {
        return ApiResponse.ok(roleManagementService.getPermissionGroups());
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('auth:role:create')")
    @PostMapping
    public ApiResponse<RoleDetailResponse> createRole(@Valid @RequestBody RoleUpsertRequest request) {
        return ApiResponse.ok(roleManagementService.createRole(request));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('auth:role:update')")
    @PatchMapping("/{roleId}")
    public ApiResponse<RoleDetailResponse> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody RoleUpsertRequest request
    ) {
        return ApiResponse.ok(roleManagementService.updateRole(roleId, request));
    }

    @PreAuthorize("hasAuthority('*') or hasAuthority('auth:role:update')")
    @PatchMapping("/{roleId}/permissions")
    public ApiResponse<RoleDetailResponse> updatePermissions(
            @PathVariable Long roleId,
            @RequestBody List<String> permissionCodes
    ) {
        return ApiResponse.ok(roleManagementService.updatePermissions(roleId, permissionCodes));
    }
}
