package com.hhjs.psi.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthStatusResponse(
        boolean authenticated,
        AdminProfileResponse admin
) {

    public static AuthStatusResponse anonymous() {
        return new AuthStatusResponse(false, null);
    }

    public static AuthStatusResponse authenticated(AdminProfileResponse admin) {
        return new AuthStatusResponse(true, admin);
    }
}
