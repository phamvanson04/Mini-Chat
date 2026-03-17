package com.minichat.shared.error;

import org.springframework.http.HttpStatus;

public class ConflictException extends BaseException {
    public ConflictException(String message) {
        super("CONFLICT", message, HttpStatus.CONFLICT);
    }
}

