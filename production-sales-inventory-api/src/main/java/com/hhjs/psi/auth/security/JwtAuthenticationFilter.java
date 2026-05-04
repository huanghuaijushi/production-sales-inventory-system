package com.hhjs.psi.auth.security;

import com.hhjs.psi.auth.entity.AdminUser;
import com.hhjs.psi.auth.repository.AdminUserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final AdminUserRepository adminUserRepository;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            TokenBlacklistService tokenBlacklistService,
            AdminUserRepository adminUserRepository
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);
        if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(request, token);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, String token) {
        try {
            JwtTokenClaims claims = jwtTokenProvider.parseAndValidate(token);
            if (tokenBlacklistService.isBlacklisted(claims.jti())) {
                return;
            }

            Optional<AdminUser> optionalAdminUser = adminUserRepository.findByIdWithRoles(claims.adminId());
            if (optionalAdminUser.isEmpty()) {
                return;
            }

            AdminUser adminUser = optionalAdminUser.get();
            if (!isTokenUsable(adminUser, claims)) {
                return;
            }

            AuthenticatedAdmin principal = new AuthenticatedAdmin(
                    adminUser.getId(),
                    adminUser.getUsername(),
                    List.copyOf(adminUser.getRoleCodes()),
                    List.copyOf(adminUser.getPermissionCodes()),
                    adminUser.getTokenVersion()
            );
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    principal.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException | IllegalArgumentException | DataAccessException exception) {
            SecurityContextHolder.clearContext();
            request.setAttribute("auth_error", exception.getMessage());
        }
    }

    private boolean isTokenUsable(AdminUser adminUser, JwtTokenClaims claims) {
        return adminUser.isActive()
                && adminUser.getUsername().equals(claims.username())
                && adminUser.getTokenVersion() != null
                && adminUser.getTokenVersion() == claims.tokenVersion();
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorization.substring(BEARER_PREFIX.length()).trim();
    }
}
