package com.hhjs.psi.auth.security;

import com.hhjs.psi.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<AuthenticatedAdmin> currentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedAdmin authenticatedAdmin) {
            return Optional.of(authenticatedAdmin);
        }
        return Optional.empty();
    }

    public static AuthenticatedAdmin requireCurrentAdmin() {
        return currentAdmin()
                .orElseThrow(() -> BusinessException.unauthorized("Authentication required"));
    }
}
