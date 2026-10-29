package com.library.catalog.dto;

import java.time.Instant;

public record ApiResponse<T>(
        int code,
        String message,
        T data,
        Instant timestamp,
        Object errors
) {
    public static <T> ApiResponse<T> success(int code, String message, T data) {
        return new ApiResponse<>(code, message, data, Instant.now(), null);
    }

    public static <T> ApiResponse<T> error(int code, String message, Object errors) {
        return new ApiResponse<>(code, message, null, Instant.now(), errors);
    }
}
