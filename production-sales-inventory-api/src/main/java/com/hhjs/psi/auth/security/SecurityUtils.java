package com.hhjs.psi.auth.security;

import com.hhjs.psi.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<AuthenticatedSysUser> currentSysUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedSysUser authenticatedSysUser) {
            return Optional.of(authenticatedSysUser);
        }
        return Optional.empty();
    }

    public static AuthenticatedSysUser requireCurrentSysUser() {
        return currentSysUser()
                .orElseThrow(() -> BusinessException.unauthorized("Authentication required"));
    }
}
