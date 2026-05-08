package com.hhjs.psi.auth.service;

import com.hhjs.psi.auth.dto.PermissionGroupResponse;
import com.hhjs.psi.auth.dto.PermissionOptionResponse;
import com.hhjs.psi.auth.dto.RoleDetailResponse;
import com.hhjs.psi.auth.dto.RoleOptionResponse;
import com.hhjs.psi.auth.dto.RoleUpsertRequest;
import com.hhjs.psi.auth.dto.SysUserCreateRequest;
import com.hhjs.psi.auth.dto.SysUserPasswordResetRequest;
import com.hhjs.psi.auth.dto.SysUserProfileResponse;
import com.hhjs.psi.auth.dto.UserRoleUpdateRequest;
import com.hhjs.psi.auth.entity.Permission;
import com.hhjs.psi.auth.entity.Role;
import com.hhjs.psi.auth.entity.SysUser;
import com.hhjs.psi.auth.repository.PermissionRepository;
import com.hhjs.psi.auth.repository.RoleRepository;
import com.hhjs.psi.auth.repository.SysUserRepository;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class SysUserManagementService {

    private static final String DEFAULT_ROLE_CODE = "SUPER_ADMIN";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).+$");

    private final SysUserRepository sysUserRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    public SysUserManagementService(
            SysUserRepository sysUserRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.sysUserRepository = sysUserRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<SysUserProfileResponse> getUsers(int page, int size, String query) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        String normalizedQuery = query == null ? "" : query.trim();
        return sysUserRepository.searchWithRoles(normalizedQuery, pageable)
                .map(SysUserProfileResponse::from);
    }

    @Transactional
    public SysUserProfileResponse createUser(SysUserCreateRequest request) {
        String username = normalizeUsername(request.username());
        validatePasswordStrength(request.password());

        if (sysUserRepository.existsByUsername(username)) {
            throw BusinessException.conflict("Username already exists");
        }

        SysUser sysUser = SysUser.create(
                username,
                passwordEncoder.encode(request.password()),
                request.nickname().trim()
        );
        assignRoles(sysUser, request.roleCodes());
        if (sysUser.getRoles().isEmpty()) {
            sysUser.addRole(loadDefaultRole());
        }
        return SysUserProfileResponse.from(sysUserRepository.save(sysUser));
    }

    @Transactional
    public SysUserProfileResponse activateUser(Long userId) {
        SysUser sysUser = loadUser(userId);
        sysUser.activate();
        return SysUserProfileResponse.from(sysUser);
    }

    @Transactional
    public SysUserProfileResponse disableUser(Long userId) {
        Long currentSysUserId = SecurityUtils.requireCurrentSysUser().id();
        if (currentSysUserId.equals(userId)) {
            throw BusinessException.badRequest("Cannot disable current user");
        }

        SysUser sysUser = loadUser(userId);
        sysUser.disable();
        return SysUserProfileResponse.from(sysUser);
    }

    @Transactional
    public SysUserProfileResponse updateUserRoles(Long userId, UserRoleUpdateRequest request) {
        SysUser sysUser = loadUser(userId);
        assignRoles(sysUser, request.roleCodes());
        return SysUserProfileResponse.from(sysUser);
    }

    @Transactional
    public SysUserProfileResponse resetPassword(Long userId, SysUserPasswordResetRequest request) {
        validatePasswordStrength(request.password());
        SysUser sysUser = loadUser(userId);
        sysUser.updatePasswordHash(passwordEncoder.encode(request.password()));
        return SysUserProfileResponse.from(sysUser);
    }

    @Transactional(readOnly = true)
    public List<RoleOptionResponse> getRoleOptions() {
        return roleRepository.findAll().stream()
                .map(role -> new RoleOptionResponse(role.getId(), role.getCode(), role.getName(), role.getDescription()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PermissionOptionResponse> getPermissionOptions() {
        return permissionRepository.findAll().stream()
                .map(permission -> new PermissionOptionResponse(permission.getId(), permission.getCode(), permission.getName(), permission.getModule(), permission.getDescription()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PermissionGroupResponse> getPermissionGroups() {
        return permissionRepository.findAll().stream()
                .sorted(Comparator.comparing(Permission::getModule).thenComparing(Permission::getSortOrder).thenComparing(Permission::getCode))
                .collect(java.util.stream.Collectors.groupingBy(
                        Permission::getModule,
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.mapping(
                                permission -> new PermissionOptionResponse(permission.getId(), permission.getCode(), permission.getName(), permission.getModule(), permission.getDescription()),
                                java.util.stream.Collectors.toList()
                        )
                ))
                .entrySet()
                .stream()
                .map(entry -> new PermissionGroupResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private SysUser loadUser(Long userId) {
        return sysUserRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> BusinessException.badRequest("System user does not exist: " + userId));
    }

    private Role loadDefaultRole() {
        return roleRepository.findByCodeWithPermissions(DEFAULT_ROLE_CODE)
                .orElseThrow(() -> new IllegalStateException("Default role SUPER_ADMIN is missing"));
    }

    private void assignRoles(SysUser sysUser, List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            sysUser.addRole(loadDefaultRole());
            return;
        }
        roleCodes.stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .forEach(code -> roleRepository.findByCodeWithPermissions(code)
                        .ifPresent(sysUser::addRole));
        if (sysUser.getRoles().isEmpty()) {
            sysUser.addRole(loadDefaultRole());
        }
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
