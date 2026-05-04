package com.hhjs.psi.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hhjs.psi.common.config.RequestTraceFilter;
import org.slf4j.MDC;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int code,
        String message,
        T data,
        OffsetDateTime timestamp,
        String traceId
) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "success", data, OffsetDateTime.now(), MDC.get(RequestTraceFilter.TRACE_ID));
    }

    public static ApiResponse<Void> ok() {
        return ok(null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, OffsetDateTime.now(), MDC.get(RequestTraceFilter.TRACE_ID));
    }

    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return new ApiResponse<>(code, message, data, OffsetDateTime.now(), MDC.get(RequestTraceFilter.TRACE_ID));
    }
}
