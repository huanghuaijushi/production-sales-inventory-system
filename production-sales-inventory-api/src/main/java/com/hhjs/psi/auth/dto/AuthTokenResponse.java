package com.hhjs.psi.auth.dto;

public record AuthTokenResponse(
        String tokenType,
        String accessToken,
        long expiresIn,
        AdminProfileResponse admin
) {

    public static AuthTokenResponse bearer(String accessToken, long expiresIn, AdminProfileResponse admin) {
        return new AuthTokenResponse("Bearer", accessToken, expiresIn, admin);
    }
}
