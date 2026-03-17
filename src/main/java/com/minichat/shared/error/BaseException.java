package com.minichat.shared.error;

import org.springframework.http.HttpStatus;

public abstract class BaseException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus status;

    protected BaseException(String errorCode, String message, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

