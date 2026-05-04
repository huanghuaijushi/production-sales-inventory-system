package com.hhjs.psi.auth.controller;

import com.hhjs.psi.auth.dto.AdminCreateRequest;
import com.hhjs.psi.auth.dto.AdminPasswordResetRequest;
import com.hhjs.psi.auth.dto.AdminProfileResponse;
import com.hhjs.psi.auth.service.AdminUserManagementService;
import com.hhjs.psi.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin-users")
public class AdminUserManagementController {

    private final AdminUserManagementService adminUserManagementService;

    public AdminUserManagementController(AdminUserManagementService adminUserManagementService) {
        this.adminUserManagementService = adminUserManagementService;
    }

    @GetMapping
    public ApiResponse<Page<AdminProfileResponse>> getAdmins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String query
    ) {
        return ApiResponse.ok(adminUserManagementService.getAdmins(page, size, query));
    }

    @PostMapping
    public ApiResponse<AdminProfileResponse> createAdmin(@Valid @RequestBody AdminCreateRequest request) {
        return ApiResponse.ok(adminUserManagementService.createAdmin(request));
    }

    @PatchMapping("/{adminId}/activate")
    public ApiResponse<AdminProfileResponse> activateAdmin(@PathVariable Long adminId) {
        return ApiResponse.ok(adminUserManagementService.activateAdmin(adminId));
    }

    @PatchMapping("/{adminId}/disable")
    public ApiResponse<AdminProfileResponse> disableAdmin(@PathVariable Long adminId) {
        return ApiResponse.ok(adminUserManagementService.disableAdmin(adminId));
    }

    @PatchMapping("/{adminId}/password")
    public ApiResponse<AdminProfileResponse> resetPassword(
            @PathVariable Long adminId,
            @Valid @RequestBody AdminPasswordResetRequest request
    ) {
        return ApiResponse.ok(adminUserManagementService.resetPassword(adminId, request));
    }
}
