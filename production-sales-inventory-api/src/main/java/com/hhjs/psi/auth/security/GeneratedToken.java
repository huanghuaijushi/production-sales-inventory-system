package com.hhjs.psi.auth.security;

import java.time.Instant;

public record GeneratedToken(
        String value,
        String jti,
        Instant expiresAt,
        long expiresInSeconds
) {
}
