package com.hhjs.psi.auth.security;

import com.hhjs.psi.auth.entity.AdminUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public GeneratedToken generateToken(AdminUser adminUser) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(jwtProperties.accessTokenTtl());
        String jti = UUID.randomUUID().toString();

        String token = Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(adminUser.getId().toString())
                .id(jti)
                .claim("username", adminUser.getUsername())
                .claim("roles", List.copyOf(adminUser.getRoleCodes()))
                .claim("permissions", List.copyOf(adminUser.getPermissionCodes()))
                .claim("tokenVersion", adminUser.getTokenVersion())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();

        return new GeneratedToken(token, jti, expiresAt, jwtProperties.accessTokenTtl().toSeconds());
    }

    public JwtTokenClaims parseAndValidate(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(jwtProperties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String subject = claims.getSubject();
        String username = claims.get("username", String.class);
        List<String> roles = getStringListClaim(claims, "roles");
        List<String> permissions = getStringListClaim(claims, "permissions");
        Integer tokenVersion = claims.get("tokenVersion", Integer.class);

        return new JwtTokenClaims(
                Long.valueOf(subject),
                username,
                roles,
                permissions,
                tokenVersion == null ? 0 : tokenVersion,
                claims.getId(),
                claims.getIssuedAt().toInstant(),
                claims.getExpiration().toInstant()
        );
    }

    private List<String> getStringListClaim(Claims claims, String name) {
        Object value = claims.get(name);
        if (!(value instanceof List<?> values)) {
            return List.of();
        }

        return values.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toList();
    }
}
