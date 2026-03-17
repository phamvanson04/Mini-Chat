package com.minichat.shared.response;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public final class ResponseFactory {
    private ResponseFactory() {
    }

    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(
                true,
                HttpStatus.OK.name(),
                message,
                data,
                LocalDateTime.now()
        );
    }

    public static BaseResponse<Void> success(String message) {
        return success(message, null);
    }

    public static BaseResponse<Void> error(String code, String message) {
        return new BaseResponse<>(
                false,
                code,
                message,
                null,
                LocalDateTime.now()
        );
    }
}

