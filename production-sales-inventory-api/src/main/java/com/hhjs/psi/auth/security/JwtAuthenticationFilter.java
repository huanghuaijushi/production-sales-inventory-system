package com.hhjs.psi.auth.security;

import com.hhjs.psi.auth.entity.SysUser;
import com.hhjs.psi.auth.repository.SysUserRepository;
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
    private final SysUserRepository sysUserRepository;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            TokenBlacklistService tokenBlacklistService,
            SysUserRepository sysUserRepository
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
        this.sysUserRepository = sysUserRepository;
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

            Optional<SysUser> optionalSysUser = sysUserRepository.findByIdWithRoles(claims.adminId());
            if (optionalSysUser.isEmpty()) {
                return;
            }

            SysUser sysUser = optionalSysUser.get();
            if (!isTokenUsable(sysUser, claims)) {
                return;
            }

            AuthenticatedSysUser principal = new AuthenticatedSysUser(
                    sysUser.getId(),
                    sysUser.getUsername(),
                    List.copyOf(sysUser.getRoleCodes()),
                    List.copyOf(sysUser.getPermissionCodes()),
                    sysUser.getTokenVersion()
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

    private boolean isTokenUsable(SysUser sysUser, JwtTokenClaims claims) {
        return sysUser.isActive()
                && sysUser.getUsername().equals(claims.username())
                && sysUser.getTokenVersion() != null
                && sysUser.getTokenVersion() == claims.tokenVersion();
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorization.substring(BEARER_PREFIX.length()).trim();
    }
}
