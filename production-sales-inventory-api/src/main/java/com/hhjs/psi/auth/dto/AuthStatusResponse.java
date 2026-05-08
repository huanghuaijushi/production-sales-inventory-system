package com.hhjs.psi.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthStatusResponse(
        boolean authenticated,
        SysUserProfileResponse sysUser
) {

    public static AuthStatusResponse anonymous() {
        return new AuthStatusResponse(false, null);
    }

    public static AuthStatusResponse authenticated(SysUserProfileResponse sysUser) {
        return new AuthStatusResponse(true, sysUser);
    }
}
