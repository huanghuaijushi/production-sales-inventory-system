package com.hhjs.psi.auth.service;

import com.hhjs.psi.auth.dto.PermissionGroupResponse;
import com.hhjs.psi.auth.dto.PermissionOptionResponse;
import com.hhjs.psi.auth.dto.RoleDetailResponse;
import com.hhjs.psi.auth.dto.RoleOptionResponse;
import com.hhjs.psi.auth.dto.RoleUpsertRequest;
import com.hhjs.psi.auth.entity.Permission;
import com.hhjs.psi.auth.entity.Role;
import com.hhjs.psi.auth.repository.PermissionRepository;
import com.hhjs.psi.auth.repository.RoleRepository;
import com.hhjs.psi.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoleManagementService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleManagementService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional(readOnly = true)
    public List<RoleOptionResponse> getRoles() {
        return roleRepository.findAll().stream()
                .sorted(Comparator.comparing(Role::getSortOrder).thenComparing(Role::getCode))
                .map(role -> new RoleOptionResponse(role.getId(), role.getCode(), role.getName(), role.getDescription()))
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleDetailResponse getRole(Long roleId) {
        return toDetail(loadRole(roleId));
    }

    @Transactional(readOnly = true)
    public List<PermissionGroupResponse> getPermissionGroups() {
        return permissionRepository.findAll().stream()
                .sorted(Comparator.comparing(Permission::getModule).thenComparing(Permission::getSortOrder).thenComparing(Permission::getCode))
                .collect(Collectors.groupingBy(
                        Permission::getModule,
                        java.util.LinkedHashMap::new,
                        Collectors.mapping(this::toPermissionOption, Collectors.toList())
                ))
                .entrySet()
                .stream()
                .map(entry -> new PermissionGroupResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Transactional
    public RoleDetailResponse createRole(RoleUpsertRequest request) {
        String code = normalizeCode(request.code());
        if (roleRepository.findByCode(code).isPresent()) {
            throw BusinessException.conflict("Role code already exists");
        }
        Role role = Role.create(code, request.name().trim(), request.description(), request.enabled(), normalizeSortOrder(request.sortOrder()));
        assignPermissions(role, request.permissionCodes());
        return toDetail(roleRepository.save(role));
    }

    @Transactional
    public RoleDetailResponse updateRole(Long roleId, RoleUpsertRequest request) {
        Role role = loadRole(roleId);
        assignBasicFields(role, request);
        assignPermissions(role, request.permissionCodes());
        return toDetail(role);
    }

    @Transactional
    public RoleDetailResponse updatePermissions(Long roleId, List<String> permissionCodes) {
        Role role = loadRole(roleId);
        assignPermissions(role, permissionCodes);
        return toDetail(role);
    }

    private void assignBasicFields(Role role, RoleUpsertRequest request) {
        role.updateProfile(normalizeCode(request.code()), request.name().trim(), request.description(), request.enabled(), normalizeSortOrder(request.sortOrder()));
    }

    private void assignPermissions(Role role, List<String> permissionCodes) {
        role.getPermissions().clear();
        if (permissionCodes == null) {
            return;
        }
        permissionCodes.stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .map(permissionRepository::findByCode)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .forEach(role.getPermissions()::add);
    }

    private Role loadRole(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> BusinessException.badRequest("Role does not exist: " + roleId));
    }

    private RoleDetailResponse toDetail(Role role) {
        return new RoleDetailResponse(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.isEnabled(),
                role.getSortOrder(),
                role.getPermissions().stream()
                        .map(Permission::getCode)
                        .sorted()
                        .toList()
        );
    }

    private PermissionOptionResponse toPermissionOption(Permission permission) {
        return new PermissionOptionResponse(permission.getId(), permission.getCode(), permission.getName(), permission.getModule(), permission.getDescription());
    }

    private String normalizeCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw BusinessException.badRequest("Role code is required");
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private int normalizeSortOrder(Integer sortOrder) {
        return sortOrder == null ? 0 : Math.max(sortOrder, 0);
    }

}
