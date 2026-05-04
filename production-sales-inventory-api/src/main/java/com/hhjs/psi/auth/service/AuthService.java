package com.hhjs.psi.auth.service;

import com.hhjs.psi.auth.dto.AdminLoginRequest;
import com.hhjs.psi.auth.dto.AdminProfileResponse;
import com.hhjs.psi.auth.dto.AdminRegisterRequest;
import com.hhjs.psi.auth.dto.AuthStatusResponse;
import com.hhjs.psi.auth.dto.AuthTokenResponse;
import com.hhjs.psi.auth.entity.AdminUser;
import com.hhjs.psi.auth.entity.Role;
import com.hhjs.psi.auth.repository.AdminUserRepository;
import com.hhjs.psi.auth.repository.RoleRepository;
import com.hhjs.psi.auth.security.GeneratedToken;
import com.hhjs.psi.auth.security.JwtTokenClaims;
import com.hhjs.psi.auth.security.JwtTokenProvider;
import com.hhjs.psi.auth.security.SecurityUtils;
import com.hhjs.psi.auth.security.TokenBlacklistService;
import com.hhjs.psi.common.exception.BusinessException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String BOOTSTRAP_ROLE_CODE = "SUPER_ADMIN";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).+$");

    private final AdminUserRepository adminUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthService(
            AdminUserRepository adminUserRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            TokenBlacklistService tokenBlacklistService
    ) {
        this.adminUserRepository = adminUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Transactional
    public AdminProfileResponse register(AdminRegisterRequest request) {
        String username = normalizeUsername(request.username());
        validatePasswordStrength(request.password());

        if (adminUserRepository.existsByUsername(username)) {
            throw BusinessException.conflict("Username already exists");
        }
        if (adminUserRepository.count() > 0) {
            throw BusinessException.forbidden("System has been initialized. Please create administrators from user management.");
        }

        AdminUser adminUser = AdminUser.create(
                username,
                passwordEncoder.encode(request.password()),
                request.nickname().trim()
        );
        Role bootstrapRole = roleRepository.findByCodeWithPermissions(BOOTSTRAP_ROLE_CODE)
                .orElseThrow(() -> new IllegalStateException("Default role SUPER_ADMIN is missing"));
        adminUser.addRole(bootstrapRole);
        return AdminProfileResponse.from(adminUserRepository.save(adminUser));
    }

    @Transactional
    public AuthTokenResponse login(AdminLoginRequest request) {
        String username = normalizeUsername(request.username());
        AdminUser adminUser = adminUserRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> BusinessException.unauthorized("Invalid username or password"));

        if (!adminUser.isActive()) {
            throw BusinessException.forbidden("Admin account is disabled");
        }
        if (!passwordEncoder.matches(request.password(), adminUser.getPasswordHash())) {
            throw BusinessException.unauthorized("Invalid username or password");
        }

        adminUser.recordLogin(Instant.now());
        GeneratedToken generatedToken = jwtTokenProvider.generateToken(adminUser);
        return AuthTokenResponse.bearer(
                generatedToken.value(),
                generatedToken.expiresInSeconds(),
                AdminProfileResponse.from(adminUser)
        );
    }

    @Transactional(readOnly = true)
    public AdminProfileResponse currentAdmin() {
        Long adminId = SecurityUtils.requireCurrentAdmin().id();
        AdminUser adminUser = loadActiveAdmin(adminId);
        return AdminProfileResponse.from(adminUser);
    }

    @Transactional(readOnly = true)
    public AuthStatusResponse status() {
        return SecurityUtils.currentAdmin()
                .flatMap(authenticatedAdmin -> adminUserRepository.findByIdWithRoles(authenticatedAdmin.id()))
                .filter(AdminUser::isActive)
                .map(AdminProfileResponse::from)
                .map(AuthStatusResponse::authenticated)
                .orElseGet(AuthStatusResponse::anonymous);
    }

    @Transactional
    public void logout(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        JwtTokenClaims claims = jwtTokenProvider.parseAndValidate(token);
        tokenBlacklistService.blacklist(claims.jti(), claims.expiresAt());
    }

    private AdminUser loadActiveAdmin(Long adminId) {
        AdminUser adminUser = adminUserRepository.findByIdWithRoles(adminId)
                .orElseThrow(() -> BusinessException.unauthorized("Authentication required"));
        if (!adminUser.isActive()) {
            throw BusinessException.forbidden("Admin account is disabled");
        }
        return adminUser;
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

    private String extractBearerToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw BusinessException.unauthorized("Missing " + HttpHeaders.AUTHORIZATION + " bearer token");
        }
        return authorizationHeader.substring(BEARER_PREFIX.length()).trim();
    }
}
