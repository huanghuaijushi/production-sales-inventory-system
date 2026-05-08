package com.hhjs.psi.auth.service;

import com.hhjs.psi.auth.dto.AuthStatusResponse;
import com.hhjs.psi.auth.dto.AuthTokenResponse;
import com.hhjs.psi.auth.dto.SysUserLoginRequest;
import com.hhjs.psi.auth.dto.SysUserProfileResponse;
import com.hhjs.psi.auth.dto.SysUserRegisterRequest;
import com.hhjs.psi.auth.entity.Role;
import com.hhjs.psi.auth.entity.SysUser;
import com.hhjs.psi.auth.repository.RoleRepository;
import com.hhjs.psi.auth.repository.SysUserRepository;
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

    private final SysUserRepository sysUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthService(
            SysUserRepository sysUserRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            TokenBlacklistService tokenBlacklistService
    ) {
        this.sysUserRepository = sysUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Transactional
    public SysUserProfileResponse register(SysUserRegisterRequest request) {
        String username = normalizeUsername(request.username());
        validatePasswordStrength(request.password());

        if (sysUserRepository.existsByUsername(username)) {
            throw BusinessException.conflict("Username already exists");
        }
        if (sysUserRepository.count() > 0) {
            throw BusinessException.forbidden("System has been initialized. Please create administrators from user management.");
        }

        SysUser sysUser = SysUser.create(
                username,
                passwordEncoder.encode(request.password()),
                request.nickname().trim()
        );
        Role bootstrapRole = roleRepository.findByCodeWithPermissions(BOOTSTRAP_ROLE_CODE)
                .orElseThrow(() -> new IllegalStateException("Default role SUPER_ADMIN is missing"));
        sysUser.addRole(bootstrapRole);
        return SysUserProfileResponse.from(sysUserRepository.save(sysUser));
    }

    @Transactional
    public AuthTokenResponse login(SysUserLoginRequest request) {
        String username = normalizeUsername(request.username());
        SysUser sysUser = sysUserRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> BusinessException.unauthorized("Invalid username or password"));

        if (!sysUser.isActive()) {
            throw BusinessException.forbidden("Admin account is disabled");
        }
        if (!passwordEncoder.matches(request.password(), sysUser.getPasswordHash())) {
            throw BusinessException.unauthorized("Invalid username or password");
        }

        sysUser.recordLogin(Instant.now());
        GeneratedToken generatedToken = jwtTokenProvider.generateToken(sysUser);
        return AuthTokenResponse.bearer(
                generatedToken.value(),
                generatedToken.expiresInSeconds(),
                SysUserProfileResponse.from(sysUser)
        );
    }

    @Transactional(readOnly = true)
    public SysUserProfileResponse currentSysUser() {
        Long sysUserId = SecurityUtils.requireCurrentSysUser().id();
        SysUser sysUser = loadActiveSysUser(sysUserId);
        return SysUserProfileResponse.from(sysUser);
    }

    @Transactional(readOnly = true)
    public AuthStatusResponse status() {
        return SecurityUtils.currentSysUser()
                .flatMap(authenticatedSysUser -> sysUserRepository.findByIdWithRoles(authenticatedSysUser.id()))
                .filter(SysUser::isActive)
                .map(SysUserProfileResponse::from)
                .map(AuthStatusResponse::authenticated)
                .orElseGet(AuthStatusResponse::anonymous);
    }

    @Transactional
    public void logout(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        JwtTokenClaims claims = jwtTokenProvider.parseAndValidate(token);
        tokenBlacklistService.blacklist(claims.jti(), claims.expiresAt());
    }

    private SysUser loadActiveSysUser(Long sysUserId) {
        SysUser sysUser = sysUserRepository.findByIdWithRoles(sysUserId)
                .orElseThrow(() -> BusinessException.unauthorized("Authentication required"));
        if (!sysUser.isActive()) {
            throw BusinessException.forbidden("Admin account is disabled");
        }
        return sysUser;
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
