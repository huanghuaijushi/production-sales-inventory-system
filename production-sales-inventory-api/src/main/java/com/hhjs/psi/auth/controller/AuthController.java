package com.hhjs.psi.auth.controller;

import com.hhjs.psi.auth.dto.AdminLoginRequest;
import com.hhjs.psi.auth.dto.AdminProfileResponse;
import com.hhjs.psi.auth.dto.AdminRegisterRequest;
import com.hhjs.psi.auth.dto.AuthStatusResponse;
import com.hhjs.psi.auth.dto.AuthTokenResponse;
import com.hhjs.psi.auth.service.AuthService;
import com.hhjs.psi.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<AdminProfileResponse> register(@Valid @RequestBody AdminRegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<AdminProfileResponse> me() {
        return ApiResponse.ok(authService.currentAdmin());
    }

    @GetMapping("/status")
    public ApiResponse<AuthStatusResponse> status() {
        return ApiResponse.ok(authService.status());
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        authService.logout(authorizationHeader);
        return ApiResponse.ok();
    }
}
