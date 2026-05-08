package com.hhjs.psi.auth.dto;

public record AuthTokenResponse(
        String tokenType,
        String accessToken,
        long expiresIn,
        SysUserProfileResponse sysUser
) {

    public static AuthTokenResponse bearer(String accessToken, long expiresIn, SysUserProfileResponse sysUser) {
        return new AuthTokenResponse("Bearer", accessToken, expiresIn, sysUser);
    }
}
