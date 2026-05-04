package com.hhjs.psi.auth.security;

import java.time.Instant;
import java.util.List;

public record JwtTokenClaims(
        Long adminId,
        String username,
        List<String> roles,
        List<String> permissions,
        int tokenVersion,
        String jti,
        Instant issuedAt,
        Instant expiresAt
) {
}
