package com.hhjs.psi.auth.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.security.cors")
public record SecurityCorsProperties(
        List<String> allowedOrigins,
        List<String> allowedOriginPatterns,
        List<String> allowedMethods,
        List<String> allowedHeaders,
        boolean allowCredentials
) {

    public SecurityCorsProperties {
        allowedOrigins = allowedOrigins == null || allowedOrigins.isEmpty()
                ? List.of("http://localhost:5173")
                : List.copyOf(allowedOrigins);
        allowedOriginPatterns = allowedOriginPatterns == null || allowedOriginPatterns.isEmpty()
                ? List.of(
                        "http://localhost:*",
                        "http://127.0.0.1:*",
                        "http://192.168.*:*",
                        "http://10.*:*",
                        "http://172.16.*:*",
                        "http://172.17.*:*",
                        "http://172.18.*:*",
                        "http://172.19.*:*",
                        "http://172.20.*:*",
                        "http://172.21.*:*",
                        "http://172.22.*:*",
                        "http://172.23.*:*",
                        "http://172.24.*:*",
                        "http://172.25.*:*",
                        "http://172.26.*:*",
                        "http://172.27.*:*",
                        "http://172.28.*:*",
                        "http://172.29.*:*",
                        "http://172.30.*:*",
                        "http://172.31.*:*",
                        "http://198.18.*:*"
                )
                : List.copyOf(allowedOriginPatterns);
        allowedMethods = allowedMethods == null || allowedMethods.isEmpty()
                ? List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                : List.copyOf(allowedMethods);
        allowedHeaders = allowedHeaders == null || allowedHeaders.isEmpty()
                ? List.of("Authorization", "Content-Type", "X-Trace-Id")
                : List.copyOf(allowedHeaders);
    }
}
