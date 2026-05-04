package com.hhjs.psi.auth.service;

import com.hhjs.psi.auth.dto.AdminCreateRequest;
import com.hhjs.psi.auth.dto.AdminPasswordResetRequest;
import com.hhjs.psi.auth.dto.AdminProfileResponse;
import com.hhjs.psi.auth.entity.AdminUser;
import com.hhjs.psi.auth.entity.Role;
import com.hhjs.psi.auth.repository.AdminUserRepository;
import com.hhjs.psi.auth.repository.RoleRepository;
import com.hhjs.psi.auth.security.SecurityUtils;
import com.hhjs.psi.common.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class AdminUserManagementService {

    private static final String DEFAULT_ROLE_CODE = "SUPER_ADMIN";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).+$");

    private final AdminUserRepository adminUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserManagementService(
            AdminUserRepository adminUserRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.adminUserRepository = adminUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<AdminProfileResponse> getAdmins(int page, int size, String query) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        String normalizedQuery = query == null ? "" : query.trim();
        return adminUserRepository.searchWithRoles(normalizedQuery, pageable)
                .map(AdminProfileResponse::from);
    }

    @Transactional
    public AdminProfileResponse createAdmin(AdminCreateRequest request) {
        String username = normalizeUsername(request.username());
        validatePasswordStrength(request.password());

        if (adminUserRepository.existsByUsername(username)) {
            throw BusinessException.conflict("Username already exists");
        }

        AdminUser adminUser = AdminUser.create(
                username,
                passwordEncoder.encode(request.password()),
                request.nickname().trim()
        );
        adminUser.addRole(loadDefaultRole());
        return AdminProfileResponse.from(adminUserRepository.save(adminUser));
    }

    @Transactional
    public AdminProfileResponse activateAdmin(Long adminId) {
        AdminUser adminUser = loadAdmin(adminId);
        adminUser.activate();
        return AdminProfileResponse.from(adminUser);
    }

    @Transactional
    public AdminProfileResponse disableAdmin(Long adminId) {
        Long currentAdminId = SecurityUtils.requireCurrentAdmin().id();
        if (currentAdminId.equals(adminId)) {
            throw BusinessException.badRequest("Cannot disable current admin");
        }

        AdminUser adminUser = loadAdmin(adminId);
        adminUser.disable();
        return AdminProfileResponse.from(adminUser);
    }

    @Transactional
    public AdminProfileResponse resetPassword(Long adminId, AdminPasswordResetRequest request) {
        validatePasswordStrength(request.password());
        AdminUser adminUser = loadAdmin(adminId);
        adminUser.updatePasswordHash(passwordEncoder.encode(request.password()));
        return AdminProfileResponse.from(adminUser);
    }

    private AdminUser loadAdmin(Long adminId) {
        return adminUserRepository.findByIdWithRoles(adminId)
                .orElseThrow(() -> BusinessException.badRequest("Admin user does not exist: " + adminId));
    }

    private Role loadDefaultRole() {
        return roleRepository.findByCodeWithPermissions(DEFAULT_ROLE_CODE)
                .orElseThrow(() -> new IllegalStateException("Default role SUPER_ADMIN is missing"));
    }

    private String normalizeUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw BusinessException.badRequest("Username is required");
        }
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private void validatePasswordStrength(String password) {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw BusinessException.badRequest("Password must contain at least one letter and one number");
        }
    }
}
